package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MathsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val userDao = database.userProgressDao()
    private val materialDao = database.studyMaterialDao()
    private val questionDao = database.quizQuestionDao()
    private val paymentDao = database.paymentRequestDao()
    private val announcementDao = database.announcementDao()
    private val streakDao = database.interactiveStreakDao()
    private val chapterDao = database.chapterDao()

    // --- Theme & Auth Preferences ---
    private val prefs = application.getSharedPreferences("suresh_maths_prefs", android.content.Context.MODE_PRIVATE)

    // --- Active User State ---
    private val _isAppLoading = MutableStateFlow(true)
    val isAppLoading: StateFlow<Boolean> = _isAppLoading.asStateFlow()

    private val _currentUserEmail = MutableStateFlow(prefs.getString("logged_in_email", "") ?: "")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _activeUser = MutableStateFlow<UserProgress?>(null)
    val activeUser: StateFlow<UserProgress?> = _activeUser.asStateFlow()

    // --- Theme State ---
    private val _themeSetting = MutableStateFlow(prefs.getString("theme_setting", "SYSTEM") ?: "SYSTEM")
    val themeSetting: StateFlow<String> = _themeSetting.asStateFlow()

    fun setThemeSetting(setting: String) {
        viewModelScope.launch {
            prefs.edit().putString("theme_setting", setting).apply()
            _themeSetting.value = setting
        }
    }

    // --- Search Query Streams ---
    val chapterSearchQuery = MutableStateFlow("")
    val classSearchQuery = MutableStateFlow("")
    val papersSearchQuery = MutableStateFlow("")
    val adminSearchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            // Data is fetched and synced from the cloud instead of seeding locally.
            
            // Enforce admin password update if they already existed
            val existingAdmin = userDao.getUserByEmail("balabhadrasaisathwik@gmail.com")
            if (existingAdmin != null && (existingAdmin.password != "Admin@2006" || !existingAdmin.isVerified)) {
                val updatedAdmin = existingAdmin.copy(password = "Admin@2006", isVerified = true)
                userDao.insertUser(updatedAdmin)
                FirebasePaymentService.syncUserToCloud(updatedAdmin)
            } else if (existingAdmin != null) {
                FirebasePaymentService.syncUserToCloud(existingAdmin)
            }

            // Provide a seeded checking user
            if (userDao.getUserByEmail("sathwik.edu@example.com") == null) {
                userDao.insertUser(UserProgress(
                    email = "sathwik.edu@example.com",
                    name = "Sathwik",
                    role = "STUDENT",
                    isPremium = true,
                    subscriptionExpiry = "Expires July 2025",
                    password = "password",
                    isVerified = true
                ))
            }


            // Start real-time Firestore sync listeners
            try {
                FirebasePaymentService.startChaptersRealtimeSync(database)
                FirebasePaymentService.startMaterialsRealtimeSync(database)
                FirebasePaymentService.startUsersRealtimeSync(database)
            } catch (e: Exception) {
                Log.e("MathsViewModel", "Failed starting realtime sync: ${e.message}")
            }

            val email = _currentUserEmail.value
            if (email.isNotEmpty()) {
                loadUserInternal(email)
            } else {
                _activeUser.value = null
            }
            _isAppLoading.value = false
        }
    }

    fun loadUser(email: String) {
        viewModelScope.launch {
            loadUserInternal(email)
        }
    }

    private suspend fun loadUserInternal(email: String) {
        var user = userDao.getUserByEmail(email)
        if (user?.status == "BLOCKED") {
            // Force logout
            prefs.edit().putString("logged_in_email", "").apply()
            _currentUserEmail.value = ""
            _activeUser.value = null
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            return
        }

        _currentUserEmail.value = email
        prefs.edit().putString("logged_in_email", email).apply()
        
        if (user == null) {
            // Auto create guest/students if testing other login emails
            val defaultRole = if (email.equals("balabhadrasaisathwik@gmail.com", ignoreCase = true)) "ADMIN" else "STUDENT"
            val isDefaultAdmin = email.equals("balabhadrasaisathwik@gmail.com", ignoreCase = true)
            user = UserProgress(
                email = email,
                name = if (email.equals("balabhadrasaisathwik@gmail.com", ignoreCase = true)) "Sai Sathwik" else email.substringBefore("@").replaceFirstChar { it.uppercase() },
                role = defaultRole,
                isPremium = isDefaultAdmin,
                isVerified = true,
                isAdmin = isDefaultAdmin
            )
            userDao.insertUser(user)
        } else if (email.equals("balabhadrasaisathwik@gmail.com", ignoreCase = true) && (user.isAdmin != true || user.role != "ADMIN")) {
            user = user.copy(isAdmin = true, isPremium = true, role = "ADMIN")
            userDao.insertUser(user)
        }
        _activeUser.value = user

        // Sync user details to Google Firestore 'users' collection immediately on login
        try {
            FirebasePaymentService.syncUserToCloud(user)
        } catch (e: Exception) {
            // Ignore silent sync failures if offline or Firebase not configured
        }

        // Silently check and sync Firebase Premium status if enabled on Cloud
        try {
            val isPremiumInCloud = FirebasePaymentService.checkPremiumStatusFromCloud(email)
            if (isPremiumInCloud && user.isPremium != true) {
                userDao.updateSubscription(email, isPremium = true, expiry = "Expires June 2027")
                _activeUser.value = userDao.getUserByEmail(email)
            }
        } catch (e: Exception) {
            // Ignore silent sync failures if offline or Firebase not configured
        }
    }

    /**
     * Registers a student locally into Room database and Firebase Auth, triggering an email verification.
     */
    fun registerStudent(
        name: String,
        email: String,
        phoneNumber: String,
        targetClass: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            val existing = userDao.getUserByEmail(email)
            if (existing != null) {
                onFailure("This email is already registered locally.")
                return@launch
            }

            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val fbUser = task.result?.user
                            fbUser?.sendEmailVerification()
                                ?.addOnCompleteListener { verificationTask ->
                                    if (verificationTask.isSuccessful) {
                                        Log.d("MathsViewModel", "Email verification sent to $email")
                                    } else {
                                        Log.e("MathsViewModel", "Email verification failed: ${verificationTask.exception?.message}")
                                    }
                                }

                            viewModelScope.launch {
                                val isDefaultAdmin = email.equals("balabhadrasaisathwik@gmail.com", ignoreCase = true)
                                val newUser = UserProgress(
                                    email = email,
                                    name = name,
                                    role = "STUDENT",
                                    isPremium = isDefaultAdmin,
                                    targetClass = targetClass,
                                    password = password,
                                    isVerified = false,
                                    phoneNumber = phoneNumber,
                                    isAdmin = isDefaultAdmin
                                )
                                userDao.insertUser(newUser)
                                FirebasePaymentService.syncUserToCloud(newUser)
                                onSuccess()
                            }
                        } else {
                            val errMsg = task.exception?.message ?: "Firebase registration failed."
                            onFailure(errMsg)
                        }
                    }
            } catch (e: Exception) {
                Log.w("MathsViewModel", "Firebase Auth temporary bypass: ${e.message}")
                val isDefaultAdmin = email.equals("balabhadrasaisathwik@gmail.com", ignoreCase = true)
                val newUser = UserProgress(
                    email = email,
                    name = name,
                    role = "STUDENT",
                    isPremium = isDefaultAdmin,
                    targetClass = targetClass,
                    password = password,
                    isVerified = false,
                    phoneNumber = phoneNumber,
                    isAdmin = isDefaultAdmin
                )
                userDao.insertUser(newUser)
                onSuccess()
            }
        }
    }

    /**
     * Helper to authenticate or register a user logging in via Google/Firebase directly.
     */
    fun loginOrRegisterGoogleUser(
        email: String,
        name: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                var user = userDao.getUserByEmail(email)
                if (user?.status == "BLOCKED") {
                    onFailure("Your account has been blocked by the admin.")
                    return@launch
                }
                if (user == null) {
                    val isDefaultAdmin = email.equals("balabhadrasaisathwik@gmail.com", ignoreCase = true) || 
                                         email.equals("suresh.educator@gmail.com", ignoreCase = true)
                    user = UserProgress(
                        email = email,
                        name = name,
                        role = "STUDENT",
                        isPremium = isDefaultAdmin,
                        targetClass = "Class 7",
                        password = "password",
                        isVerified = true,
                        isAdmin = isDefaultAdmin
                    )
                    userDao.insertUser(user)
                }
                
                _activeUser.value = user
                
                // Sync user details to Google Firestore 'users' collection immediately
                try {
                    FirebasePaymentService.syncUserToCloud(user)
                } catch (e: Exception) {
                    // Ignore silent sync failures if offline
                }

                // Silently check and sync Firebase Premium status if enabled on Cloud
                try {
                    val isPremiumInCloud = FirebasePaymentService.checkPremiumStatusFromCloud(email)
                    if (isPremiumInCloud && !user.isPremium) {
                        val updatedUser = user.copy(isPremium = true)
                        userDao.insertUser(updatedUser)
                        _activeUser.value = updatedUser
                    }
                } catch (e: Exception) {
                    // Ignore background state verify exceptions
                }

                onSuccess()
            } catch (e: Exception) {
                onFailure(e.localizedMessage ?: "Unknown error during Google user registration")
            }
        }
    }

    /**
     * Logs in the user locally. Seeded users (like balabhadrasaisathwik@gmail.com / sathwik.edu@example.com) 
     * use 'password' as their default passwords if not registered with custom passwords.
     */
    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            val isBypass = email.equals("balabhadrasaisathwik@gmail.com", ignoreCase = true)

            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val fbUser = task.result?.user
                            if (fbUser != null && !fbUser.isEmailVerified && !isBypass) {
                                onFailure("Please verify your email. A verification link has been sent to $email. Please verify your email before logging in.")
                                fbUser.sendEmailVerification()
                            } else {
                                viewModelScope.launch {
                                    var user = userDao.getUserByEmail(email)
                                    if (user?.status == "BLOCKED") {
                                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                        onFailure("Your account has been blocked by the admin.")
                                        return@launch
                                    }
                                    if (user == null) {
                                        val isDefaultAdmin = email.equals("balabhadrasaisathwik@gmail.com", ignoreCase = true)
                                        user = UserProgress(
                                            email = email,
                                            name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                                            role = if (isDefaultAdmin) "ADMIN" else "STUDENT",
                                            isPremium = isDefaultAdmin,
                                            password = password,
                                            isVerified = true
                                        )
                                        userDao.insertUser(user)
                                    }
                                    loadUser(email)
                                    onSuccess()
                                }
                            }
                        } else {
                            val errMsg = task.exception?.message ?: "Authentication failed."
                            viewModelScope.launch {
                                var localUser = userDao.getUserByEmail(email)
                                if (localUser == null && email.equals("balabhadrasaisathwik@gmail.com", ignoreCase = true)) {
                                    localUser = UserProgress(
                                        email = email,
                                        name = "Sai Sathwik",
                                        role = "ADMIN",
                                        isPremium = true,
                                        password = "Admin@2006",
                                        isVerified = true,
                                        isAdmin = true
                                    )
                                    userDao.insertUser(localUser)
                                }
                                if (localUser != null && localUser.password == password) {
                                    if (localUser.status == "BLOCKED") {
                                        onFailure("Your account has been blocked by the admin.")
                                        return@launch
                                    }
                                    loadUser(email)
                                    onSuccess()
                                } else {
                                    onFailure(errMsg)
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                // Local SQLite standard login fallback (e.g. offline context)
                viewModelScope.launch {
                    var user = userDao.getUserByEmail(email)
                    
                    // Auto-create seeded admin if doesn't exist
                    if (user == null) {
                        if (email.equals("balabhadrasaisathwik@gmail.com", ignoreCase = true)) {
                            user = UserProgress(
                                email = "balabhadrasaisathwik@gmail.com",
                                name = "Sai Sathwik",
                                role = "ADMIN",
                                isPremium = true,
                                password = "Admin@2006",
                                isVerified = true,
                                isAdmin = true
                            )
                            userDao.insertUser(user)
                        }
                    }

                    if (user == null) {
                        onFailure("No account found with this email.")
                        return@launch
                    }

                    if (user.password != password) {
                        onFailure("Incorrect password.")
                        return@launch
                    }

                    if (user.status == "BLOCKED") {
                        onFailure("Your account has been blocked by the admin.")
                        return@launch
                    }

                    loadUser(email)
                    onSuccess()
                }
            }
        }
    }

    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onSuccess()
                        } else {
                            onFailure(task.exception?.message ?: "Failed to send reset email.")
                        }
                    }
            } catch (e: Exception) {
                onFailure(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun sendMagicLink(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val actionCodeSettings = com.google.firebase.auth.ActionCodeSettings.newBuilder()
                    .setUrl("https://sureshmaths.firebaseapp.com") // Make sure this domain is authorized in Firebase Console -> Auth -> Settings -> Authorized Domains
                    .setHandleCodeInApp(true)
                    .setAndroidPackageName(
                        "com.aistudio.sureshmaths.wyljka",
                        true,
                        "12"
                    )
                    .build()
                
                com.google.firebase.auth.FirebaseAuth.getInstance().sendSignInLinkToEmail(email, actionCodeSettings)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            prefs.edit().putString("magic_link_email", email).apply()
                            onSuccess()
                        } else {
                            onError(task.exception?.message ?: "Failed to send link")
                        }
                    }
            } catch (e: Exception) {
                onError("Failed: ${e.message}")
            }
        }
    }
    
    fun processIntentLink(intentLink: String?) {
        if (intentLink == null) return
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        if (auth.isSignInWithEmailLink(intentLink)) {
            val email = prefs.getString("magic_link_email", "") ?: ""
            if (email.isNotEmpty()) {
                signInWithMagicLink(email, intentLink,
                    onSuccess = {
                        // handled internally
                        prefs.edit().remove("magic_link_email").apply()
                    },
                    onError = {
                        android.util.Log.e("MathsViewModel", "Failed magic link signin: $it")
                    }
                )
            }
        }
    }
    
    fun signInWithMagicLink(email: String, emailLink: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        if (auth.isSignInWithEmailLink(emailLink)) {
            auth.signInWithEmailLink(email, emailLink)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                         val fbUser = task.result?.user
                         if (fbUser != null) {
                             viewModelScope.launch {
                                 val localEmail = fbUser.email ?: ""
                                 val localUser = userDao.getUserByEmail(localEmail)
                                 if (localUser?.status == "BLOCKED") {
                                     auth.signOut()
                                     onError("Your account has been blocked by the admin.")
                                     return@launch
                                 }
                                 _currentUserEmail.value = localEmail
                                 loadUserInternal(localEmail)
                                 onSuccess()
                             }
                         } else {
                             onError("Authentication failed")
                         }
                    } else {
                        onError(task.exception?.message ?: "Failed to sign in")
                    }
                }
        } else {
            onError("Invalid magic link")
        }
    }

    fun signInWithGoogle(idToken: String, onSuccess: (isNewUser: Boolean) -> Unit, onError: (String) -> Unit) {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fbUser = task.result?.user
                    val email = fbUser?.email
                    if (email != null) {
                        viewModelScope.launch {
                            // Ensure the user exists in our local DB as well
                            val existingUser = userDao.getUserByEmail(email)
                            if (existingUser?.status == "BLOCKED") {
                                auth.signOut()
                                onError("Your account has been blocked by the admin.")
                                return@launch
                            }
                            var isNewUser = false
                            if (existingUser == null) {
                                isNewUser = true
                                val newUser = com.example.data.UserProgress(
                                    email = email,
                                    name = fbUser.displayName ?: "Google User",
                                    role = "STUDENT",
                                    isPremium = false,
                                    password = "" // No password for OAuth
                                )
                                userDao.insertUser(newUser)
                            }
                            _currentUserEmail.value = email
                            loadUserInternal(email)
                            onSuccess(isNewUser)
                        }
                    } else {
                        onError("Email not found in Google account.")
                    }
                } else {
                    onError(task.exception?.message ?: "Google Sign-In failed.")
                }
            }
    }

    /**
     * Signs out the user, returning them to the beautiful Login / Create Account screen.
     */
    fun logoutUser() {
        viewModelScope.launch {
            prefs.edit().putString("logged_in_email", "").apply()
            _currentUserEmail.value = ""
            _activeUser.value = null
        }
    }

    /**
     * Toggles a student/user's premium status from the Admin panel.
     */
    fun togglePremiumStatus(email: String, currentStatus: Boolean) {
        viewModelScope.launch {
            val nextStatus = !currentStatus
            val expiry = if (nextStatus) "Premium Managed Plan" else ""
            userDao.updateSubscription(email, nextStatus, expiry)
            if (_currentUserEmail.value == email) {
                loadUser(email)
            }
        }
    }

    /**
     * Toggles a user's role between STUDENT and ADMIN.
     */
    fun toggleAdminRole(email: String, currentRole: String) {
        viewModelScope.launch {
            val user = userDao.getUserByEmail(email)
            if (user != null) {
                val nextRole = if (currentRole == "ADMIN") "STUDENT" else "ADMIN"
                val updated = user.copy(role = nextRole)
                userDao.insertUser(updated)
                if (_currentUserEmail.value == email) {
                    loadUserInternal(email)
                }
            }
        }
    }

    /**
     * Deletes a user account completely from local DB.
     */
    fun deleteUserProgress(email: String) {
        viewModelScope.launch {
            userDao.deleteUser(email)
        }
    }

    /**
     * Toggles a student's verification approval status.
     */
    fun toggleUserVerification(email: String, currentStatus: Boolean) {
        viewModelScope.launch {
            userDao.updateVerification(email, !currentStatus)
            if (_currentUserEmail.value == email) {
                loadUser(email)
            }
        }
    }

    fun updateUserStatus(email: String, status: String) {
        viewModelScope.launch {
            userDao.updateStatus(email, status)
            if (_currentUserEmail.value == email) {
                loadUser(email)
            }
            // Sync the updated user to cloud
            val updatedUser = userDao.getUserByEmail(email)
            if (updatedUser != null) {
                FirebasePaymentService.syncUserToCloud(updatedUser)
            }
        }
    }

    // --- Reactive Database Flows ---
    val students: StateFlow<List<UserProgress>> = userDao.getAllStudentsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<Announcement>> = announcementDao.getAllAnnouncementsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMaterials: StateFlow<List<StudyMaterial>> = materialDao.getAllMaterialsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPublishedMaterials: StateFlow<List<StudyMaterial>> = materialDao.getAllPublishedMaterials()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentRequests: StateFlow<List<PaymentRequest>> = paymentDao.getAllPaymentRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chapters: StateFlow<List<Chapter>> = chapterDao.getAllChaptersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Interactive Quiz Session ---
    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()

    // --- Actions ---

    fun login(email: String, name: String, role: String) {
        viewModelScope.launch {
            val user = UserProgress(
                email = email,
                name = name,
                role = role,
                isPremium = (role == "ADMIN")
            )
            userDao.insertUser(user)
            loadUser(email)
        }
    }

    fun updateUserClassAndPhone(studentClass: String, phoneNumber: String, onComplete: () -> Unit) {
        val currentEmail = _currentUserEmail.value
        if (currentEmail != null && currentEmail.isNotEmpty()) {
            viewModelScope.launch {
                val user = userDao.getUserByEmail(currentEmail)
                if (user != null) {
                    val updatedUser = user.copy(
                        targetClass = studentClass,
                        phoneNumber = phoneNumber
                    )
                    userDao.insertUser(updatedUser)
                    _activeUser.value = updatedUser
                    
                    FirebasePaymentService.syncUserToCloud(updatedUser)
                }
                onComplete()
            }
        } else {
            onComplete()
        }
    }

    fun logout() {
        viewModelScope.launch {
            _currentUserEmail.value = ""
            _activeUser.value = null
            prefs.edit().putString("logged_in_email", "").apply()
        }
    }

    // Student records increment
    fun incrementNotesCount() {
        viewModelScope.launch {
            _activeUser.value?.let { user ->
                userDao.incrementNotesViewed(user.email)
                loadUser(user.email)
            }
        }
    }

    fun incrementWorksheetsCount() {
        viewModelScope.launch {
            _activeUser.value?.let { user ->
                userDao.incrementWorksheetsDownloaded(user.email)
                loadUser(user.email)
            }
        }
    }

    fun incrementQuizzesCount() {
        viewModelScope.launch {
            _activeUser.value?.let { user ->
                userDao.incrementQuizzesCompleted(user.email)
                loadUser(user.email)
            }
        }
    }

    // Load active Quiz question pool
    fun startQuizSession(className: String, chapterName: String, difficulty: String) {
        viewModelScope.launch {
            val cleanChapter = if (chapterName.contains(" - ")) chapterName.substringAfter(" - ").trim() else chapterName.trim()
            var questions = questionDao.getQuestions(className, cleanChapter, difficulty)
            if (questions.isEmpty()) {
                questions = questionDao.getQuestions("Class 7", "Fractions", difficulty) // default fallback
            }
            _quizQuestions.value = questions
        }
    }

    suspend fun generateQuizUsingAI(documentText: String, className: String, chapterName: String, difficulty: String, numQuestions: Int, isPdf: Boolean = false): List<QuizQuestion> {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        val promptText = "Create a $difficulty level Mathematics quiz with $numQuestions multiple-choice questions for $className on the topic of '$chapterName'. Return ONLY a JSON array of objects. Each object should have the exact following keys: \"questionText\", \"optionA\", \"optionB\", \"optionC\", \"optionD\", \"correctAnswer\" (must be exactly \"A\", \"B\", \"C\", or \"D\"), and \"explanation\" (brief explanation of the answer). Do NOT wrap the JSON array in markdown formatting, just return raw JSON."

        val parts = mutableListOf<com.example.data.Part>()
        if (isPdf) {
            parts.add(com.example.data.Part(inlineData = com.example.data.InlineData(mimeType = "application/pdf", data = documentText)))
            parts.add(com.example.data.Part(text = "Based on the attached PDF, $promptText"))
        } else {
            parts.add(com.example.data.Part(text = "$promptText\n\nBased on the following text:\n\n$documentText"))
        }

        val request = com.example.data.GenerateContentRequest(
            contents = listOf(
                com.example.data.Content(
                    parts = parts
                )
            ),
            generationConfig = com.example.data.GenerationConfig(
                temperature = 0.4f
            )
        )

        try {
            val response = com.example.data.RetrofitClient.service.generateContent(apiKey, request)
            var responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            responseText = responseText.trim().removePrefix("```json").removeSuffix("```").trim()

            val jsonArray = try {
                org.json.JSONArray(responseText)
            } catch (e: Exception) { null } ?: return emptyList()

            val generatedQuestions = mutableListOf<QuizQuestion>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                val id = "${className.replace(" ", "")}_${chapterName.replace(" ", "")}_${difficulty}_${System.currentTimeMillis()}_$i"
                generatedQuestions.add(
                    QuizQuestion(
                        id = id,
                        targetClass = className,
                        chapterName = chapterName,
                        difficulty = difficulty,
                        questionText = obj.optString("questionText", ""),
                        optionA = obj.optString("optionA", ""),
                        optionB = obj.optString("optionB", ""),
                        optionC = obj.optString("optionC", ""),
                        optionD = obj.optString("optionD", ""),
                        correctAnswer = obj.optString("correctAnswer", ""),
                        explanation = obj.optString("explanation", "")
                    )
                )
            }
            return generatedQuestions
        } catch (e: Exception) {
            Log.e("MathsViewModel", "Failed to generate AI quiz: ${e.message}")
            return emptyList()
        }
    }

    fun saveQuizQuestions(questions: List<QuizQuestion>) {
        viewModelScope.launch {
            questions.forEach {
                questionDao.insertQuestion(it)
            }
        }
    }

    // Administrative content addition
    fun addNewMaterial(
        title: String,
        targetClass: String,
        chapter: String,
        type: String,
        subCategory: String,
        isPremium: Boolean,
        driveUrl: String,
        description: String
    ) {
        viewModelScope.launch {
            val material = StudyMaterial(
                title = title,
                targetClass = targetClass,
                chapterName = chapter,
                materialType = type,
                subCategory = subCategory,
                isPremium = isPremium,
                driveUrl = driveUrl,
                description = description,
                publishedTime = System.currentTimeMillis()
            )
            materialDao.insertMaterial(material)
            
            try {
                FirebasePaymentService.addMaterialToCloud(material)
            } catch (e: Exception) {
                Log.e("MathsViewModel", "Material upload failure on firestore: ${e.message}")
            }
        }
    }

    fun updateMaterial(material: StudyMaterial) {
        viewModelScope.launch {
            try {
                // Delete old from cloud first so docId matches updated content or we don't have orphan docs
                // Well, to be perfectly safe, we should fetch old one to delete the old docId if title/class changed
                // but for simplicity we will just overwrite local and call addMaterialToCloud.
                val oldMat = materialDao.getMaterialById(material.id)
                if (oldMat != null && (oldMat.title != material.title || oldMat.targetClass != material.targetClass || oldMat.chapterName != material.chapterName)) {
                     FirebasePaymentService.deleteMaterialFromCloud(oldMat.title, oldMat.targetClass, oldMat.chapterName)
                }
                
                materialDao.insertMaterial(material)
                FirebasePaymentService.addMaterialToCloud(material)
            } catch (e: Exception) {
                Log.e("MathsViewModel", "Material update failure: ${e.message}")
            }
        }
    }

    // Administrative content deletion
    fun deleteMaterial(id: Int) {
        viewModelScope.launch {
            try {
                val mat = materialDao.getMaterialById(id)
                if (mat != null) {
                    FirebasePaymentService.deleteMaterialFromCloud(mat.title, mat.targetClass, mat.chapterName)
                }
            } catch (e: Exception) {
                Log.e("MathsViewModel", "Material deletion failure on firestore: ${e.message}")
            }
            materialDao.deleteMaterialById(id)
        }
    }

    // Manual UPI Payment Verification loop
    fun submitUPIPaymentRequest(transactionRef: String) {
        viewModelScope.launch {
            val user = _activeUser.value ?: return@launch
            val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
            val dateStr = sdf.format(Date())

            val request = PaymentRequest(
                studentName = user.name,
                studentEmail = user.email,
                transactionRef = transactionRef,
                dateString = dateStr,
                screenshotAsset = "screenshot_pay_rahul" // mock asset
            )
            paymentDao.insertPaymentRequest(request)
            
            // Upload payment request to Firebase in background
            try {
                FirebasePaymentService.uploadPaymentRequest(user.name, user.email, transactionRef)
            } catch (e: Exception) {
                Log.e("MathsViewModel", "Failed to upload payment to Firebase: ${e.message}")
            }
        }
    }

    // Admin verifies payment and unlocks premium instantly
    fun approvePayment(requestId: Int, studentEmail: String) {
        viewModelScope.launch {
            val request = paymentDao.getPaymentRequestById(requestId)
            paymentDao.updatePaymentRequestStatus(requestId, "APPROVED")
            userDao.updateSubscription(studentEmail, isPremium = true, expiry = "Expires June 2027")
            if (_currentUserEmail.value == studentEmail) {
                loadUser(studentEmail)
            }

            // Sync payment status and premium subscription to Firebase Cloud
            try {
                if (request != null) {
                    FirebasePaymentService.updatePaymentRequestStatusInCloud(request.transactionRef, "APPROVED")
                }
                FirebasePaymentService.syncSubscriptionToCloud(studentEmail, isPremium = true)
            } catch (e: Exception) {
                Log.e("MathsViewModel", "Failed to sync payment approval to Firebase: ${e.message}")
            }
        }
    }

    fun rejectPayment(requestId: Int) {
        viewModelScope.launch {
            val request = paymentDao.getPaymentRequestById(requestId)
            paymentDao.updatePaymentRequestStatus(requestId, "REJECTED")

            // Sync payment status to Firebase Cloud
            try {
                if (request != null) {
                    FirebasePaymentService.updatePaymentRequestStatusInCloud(request.transactionRef, "REJECTED")
                }
            } catch (e: Exception) {
                Log.e("MathsViewModel", "Failed to sync payment rejection to Firebase: ${e.message}")
            }
        }
    }

    // Retrieve active student streak or stats
    private val _userStreak = MutableStateFlow<InteractiveStreak?>(null)
    val userStreak: StateFlow<InteractiveStreak?> = _userStreak.asStateFlow()

    fun loadStreak() {
        viewModelScope.launch {
            _userStreak.value = streakDao.getStreak(_currentUserEmail.value)
        }
    }

    // --- Chapters Actions ---
    fun addNewChapter(name: String, targetClass: String, chapterNumber: Int) {
        viewModelScope.launch {
            val element = Chapter(
                name = name,
                targetClass = targetClass,
                chapterNumber = chapterNumber
            )
            chapterDao.insertChapter(element)
            
            try {
                FirebasePaymentService.addChapterToCloud(name, targetClass, chapterNumber)
            } catch (e: Exception) {
                Log.e("MathsViewModel", "Chapter upload failure on firestore: ${e.message}")
            }
        }
    }

    fun deleteChapter(id: Int) {
        viewModelScope.launch {
            try {
                val ch = chapterDao.getChapterById(id)
                if (ch != null) {
                    FirebasePaymentService.deleteChapterFromCloud(ch.name, ch.targetClass)
                }
            } catch (e: Exception) {
                Log.e("MathsViewModel", "Chapter deletion failure on firestore: ${e.message}")
            }
            chapterDao.deleteChapterById(id)
        }
    }
}

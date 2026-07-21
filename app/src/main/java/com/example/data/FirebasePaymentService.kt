package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Manages Firebase Firestore synchronization purely for premium payment verification,
 * keeping all sensitive user engagement metrics (views, attempts, quizzes) strictly
 * private inside the device's local SQLite database.
 */
object FirebasePaymentService {
    private const val TAG = "FirebasePaymentService"
    
    // Checks if Firebase has been initialized successfully (if google-services.json is present)
    private val isFirebaseAvailable: Boolean
        get() = try {
            FirebaseApp.getInstance()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Firebase is not initialized (likely missing google-services.json). Operating in Local-First database mode.")
            false
        }

    /**
     * Publishes a new UPI payment request to Firebase Firestore so admins can verify it remotely.
     */
    suspend fun uploadPaymentRequest(
        studentName: String,
        studentEmail: String,
        transactionRef: String,
        amount: String = "₹199"
    ) = withContext(Dispatchers.IO) {
        if (!isFirebaseAvailable) return@withContext
        
        try {
            val firestore = FirebaseFirestore.getInstance()
            val paymentData = hashMapOf(
                "studentName" to studentName,
                "studentEmail" to studentEmail,
                "transactionRef" to transactionRef,
                "amount" to amount,
                "status" to "PENDING",
                "timestamp" to System.currentTimeMillis()
            )
            
            firestore.collection("payments")
                .document(transactionRef)
                .set(paymentData)
                .addOnSuccessListener {
                    Log.d(TAG, "Payment request for $transactionRef synced to cloud successfully.")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to sync payment request to cloud", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Cloud sync error: ${e.message}")
        }
    }

    /**
     * Updates payment request state in Firebase Firestore (e.g. APPROVED or REJECTED).
     */
    suspend fun updatePaymentRequestStatusInCloud(
        transactionRef: String,
        status: String
    ) = withContext(Dispatchers.IO) {
        if (!isFirebaseAvailable) return@withContext
        
        try {
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("payments")
                .document(transactionRef)
                .update("status", status)
                .addOnSuccessListener {
                    Log.d(TAG, "Cloud status for $transactionRef updated to $status.")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Cloud update error: ${e.message}")
        }
    }

    /**
     * Syncs subscription state to Firebase Firestore after approval.
     */
    suspend fun syncSubscriptionToCloud(
        studentEmail: String,
        isPremium: Boolean
    ) = withContext(Dispatchers.IO) {
        if (!isFirebaseAvailable) return@withContext
        
        try {
            val firestore = FirebaseFirestore.getInstance()
            val subscriptionData = hashMapOf(
                "email" to studentEmail,
                "isPremium" to isPremium,
                "updatedAt" to System.currentTimeMillis()
            )
            
            firestore.collection("subscriptions")
                .document(studentEmail)
                .set(subscriptionData)
                .addOnSuccessListener {
                    Log.d(TAG, "Subscription for $studentEmail synced to cloud: isPremium=$isPremium")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Cloud subscription sync error: ${e.message}")
        }
    }

    /**
     * Syncs user registration details to Google Firestore 'users' collection on Google Sign-In / login.
     */
    suspend fun syncUserToCloud(user: UserProgress) = withContext(Dispatchers.IO) {
        if (!isFirebaseAvailable) return@withContext
        
        try {
            val firestore = FirebaseFirestore.getInstance()
            val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val formattedDate = isoFormat.format(java.util.Date())
            
            val userData = hashMapOf(
                "uid" to user.email, // using email as uid representation since simulated Google auth uses email
                "displayName" to user.name,
                "email" to user.email,
                "role" to user.role,
                "targetClass" to user.targetClass,
                "isPremium" to user.isPremium,
                "lastLogin" to formattedDate,
                "password" to user.password,
                "phoneNumber" to user.phoneNumber,
                "status" to user.status,
                "isVerified" to user.isVerified
            )
            
            firestore.collection("users")
                .document(user.email)
                .set(userData, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(TAG, "User registration synchronized to Firestore 'users' collection successfully.")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to synchronize user to cloud", e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Cloud user sync error: ${e.message}")
        }
    }

    /**
     * Silent remote lookup to fetch the user's payment / premium status from Firebase Firestore.
     * Crucial for maintaining accurate subscription states if users reset local DB data.
     */
    suspend fun checkPremiumStatusFromCloud(studentEmail: String): Boolean = withContext(Dispatchers.IO) {
        if (!isFirebaseAvailable) return@withContext false
        
        return@withContext try {
            val firestore = FirebaseFirestore.getInstance()
            val doc = firestore.collection("subscriptions")
                .document(studentEmail)
                .get()
                // Wait synchronously in Coroutine
                .let { task ->
                    var isSubPremium = false
                    try {
                        val result = com.google.android.gms.tasks.Tasks.await(task)
                        if (result.exists()) {
                            isSubPremium = result.getBoolean("isPremium") ?: false
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Task execution failed: ${e.message}")
                    }
                    isSubPremium
                }
            doc
        } catch (e: Exception) {
            Log.e(TAG, "Failed checking premium status from cloud: ${e.message}")
            false
        }
    }

    /**
     * Synergizes the local Chapters collection with the cloud Firestore "chapters" collection
     */
    fun startChaptersRealtimeSync(database: AppDatabase) {
        if (!isFirebaseAvailable) return
        
        val firestore = FirebaseFirestore.getInstance()
        val chapterDao = database.chapterDao()
        
        firestore.collection("chapters")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Realtime chapters sync failed: ${error.message}")
                    return@addSnapshotListener
                }
                
                if (snapshot == null) return@addSnapshotListener
                
                // We launch a coroutine to update the local Room database asynchronously
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val documents = snapshot.documents
                        if (documents.isEmpty()) {
                            // If remote collection is completely empty, upload first seeded chapters to Firestore
                            val localChapters = chapterDao.getAllChapters()
                            if (localChapters.isNotEmpty()) {
                                Log.d(TAG, "Firestore 'chapters' collection is empty. Uploading local seeded chapters.")
                                for (ch in localChapters) {
                                    val docId = "${ch.targetClass}_${ch.name}".replace(" ", "_").replace("/", "_")
                                    val chData = hashMapOf(
                                        "name" to ch.name,
                                        "targetClass" to ch.targetClass,
                                        "chapterNumber" to ch.chapterNumber
                                    )
                                    firestore.collection("chapters").document(docId).set(chData)
                                }
                            }
                        } else {
                            // Firestore has chapters, so we synchronize the local Room database with whatever Firestore contains.
                            chapterDao.deleteAllChapters()
                            for (doc in documents) {
                                val name = doc.getString("name") ?: ""
                                val targetClass = doc.getString("targetClass") ?: ""
                                val chapterNumber = doc.getLong("chapterNumber")?.toInt() ?: 1
                                
                                if (name.isNotEmpty() && targetClass.isNotEmpty()) {
                                    chapterDao.insertChapter(
                                        Chapter(
                                            name = name,
                                            targetClass = targetClass,
                                            chapterNumber = chapterNumber
                                        )
                                    )
                                }
                            }
                            Log.d(TAG, "Successfully synced ${documents.size} chapters from Cloud Firestore.")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to apply chapter snapshot to local DB: ${e.message}")
                    }
                }
            }
    }

    /**
     * Synergizes the local Users collection with the cloud Firestore "users" collection
     */
    fun startUsersRealtimeSync(database: AppDatabase) {
        if (!isFirebaseAvailable) return
        
        val firestore = FirebaseFirestore.getInstance()
        val userDao = database.userProgressDao()
        
        firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Realtime users sync failed: ${error.message}")
                    return@addSnapshotListener
                }
                
                if (snapshot == null) return@addSnapshotListener
                
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val documents = snapshot.documents
                        for (doc in documents) {
                            val email = doc.getString("email") ?: continue
                            val displayName = doc.getString("displayName") ?: "Unknown User"
                            val role = doc.getString("role") ?: "STUDENT"
                            val targetClass = doc.getString("targetClass") ?: "Class 7"
                            val isPremium = doc.getBoolean("isPremium") ?: false
                            val password = doc.getString("password") ?: ""
                            val phoneNumber = doc.getString("phoneNumber") ?: ""
                            val status = doc.getString("status") ?: "TEMPORARY"
                            val isVerified = doc.getBoolean("isVerified") ?: false
                            
                            val existingUser = userDao.getUserByEmail(email)
                            if (existingUser != null) {
                                userDao.insertUser(existingUser.copy(
                                    name = displayName,
                                    role = role,
                                    targetClass = targetClass,
                                    isPremium = isPremium,
                                    phoneNumber = phoneNumber,
                                    status = status,
                                    isVerified = isVerified
                                ))
                            } else {
                                userDao.insertUser(UserProgress(
                                    name = displayName,
                                    email = email,
                                    password = password,
                                    isPremium = isPremium,
                                    role = role,
                                    targetClass = targetClass,
                                    isVerified = isVerified,
                                    phoneNumber = phoneNumber,
                                    status = status
                                ))
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to apply users snapshot to local DB: ${e.message}")
                    }
                }
            }
    }

    /**
     * Synergizes the local StudyMaterials collection with the cloud Firestore "materials" collection
     */
    fun startMaterialsRealtimeSync(database: AppDatabase) {
        if (!isFirebaseAvailable) return
        
        val firestore = FirebaseFirestore.getInstance()
        val materialDao = database.studyMaterialDao()
        
        firestore.collection("materials")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Realtime materials sync failed: ${error.message}")
                    return@addSnapshotListener
                }
                
                if (snapshot == null) return@addSnapshotListener
                
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val documents = snapshot.documents
                        if (documents.isEmpty()) {
                            // If empty, upload currently seeded local study materials to Firestore so they are not deleted on other devices
                            val localMaterials = materialDao.getAllMaterials()
                            if (localMaterials.isNotEmpty()) {
                                Log.d(TAG, "Firestore 'materials' collection is empty. Uploading local seeded materials.")
                                for (mat in localMaterials) {
                                    val docId = "${mat.targetClass}_${mat.chapterName}_${mat.title}".replace(" ", "_").replace("/", "_")
                                    val matData = hashMapOf(
                                        "title" to mat.title,
                                        "targetClass" to mat.targetClass,
                                        "chapterName" to mat.chapterName,
                                        "materialType" to mat.materialType,
                                        "subCategory" to mat.subCategory,
                                        "isPremium" to mat.isPremium,
                                        "driveUrl" to mat.driveUrl,
                                        "description" to mat.description,
                                        "publishedTime" to mat.publishedTime,
                                        "isPublished" to mat.isPublished
                                    )
                                    firestore.collection("materials").document(docId).set(matData)
                                }
                            }
                        } else {
                            // Sync current firestore materials to local database
                            materialDao.deleteAllMaterials()
                            for (doc in documents) {
                                val title = doc.getString("title") ?: ""
                                val targetClass = doc.getString("targetClass") ?: ""
                                val chapterName = doc.getString("chapterName") ?: ""
                                val materialType = doc.getString("materialType") ?: ""
                                val subCategory = doc.getString("subCategory") ?: "Concept Notes"
                                val isPremium = doc.getBoolean("isPremium") ?: false
                                val driveUrl = doc.getString("driveUrl") ?: ""
                                val description = doc.getString("description") ?: ""
                                val publishedTime = doc.getLong("publishedTime") ?: System.currentTimeMillis()
                                val isPublished = doc.getBoolean("isPublished") ?: true
                                
                                if (title.isNotEmpty() && targetClass.isNotEmpty()) {
                                    materialDao.insertMaterial(
                                        StudyMaterial(
                                            title = title,
                                            targetClass = targetClass,
                                            chapterName = chapterName,
                                            materialType = materialType,
                                            subCategory = subCategory,
                                            isPremium = isPremium,
                                            driveUrl = driveUrl,
                                            description = description,
                                            publishedTime = publishedTime,
                                            isPublished = isPublished
                                        )
                                    )
                                }
                            }
                            Log.d(TAG, "Successfully synced ${documents.size} study materials from Cloud Firestore.")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to apply study material snapshot to local DB: ${e.message}")
                    }
                }
            }
    }

    /**
     * Publishes a new Chapter to Cloud Firestore.
     */
    suspend fun addChapterToCloud(name: String, targetClass: String, chapterNumber: Int) = withContext(Dispatchers.IO) {
        if (!isFirebaseAvailable) return@withContext
        try {
            val firestore = FirebaseFirestore.getInstance()
            val docId = "${targetClass}_${name}".replace(" ", "_").replace("/", "_")
            val chData = hashMapOf(
                "name" to name,
                "targetClass" to targetClass,
                "chapterNumber" to chapterNumber
            )
            firestore.collection("chapters").document(docId).set(chData).await()
            Log.d(TAG, "Chapter $name successfully synced to Cloud Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload chapter to Firestore: ${e.message}")
        }
    }

    /**
     * Removes a Chapter from Cloud Firestore.
     */
    suspend fun deleteChapterFromCloud(name: String, targetClass: String) = withContext(Dispatchers.IO) {
        if (!isFirebaseAvailable) return@withContext
        try {
            val firestore = FirebaseFirestore.getInstance()
            val docId = "${targetClass}_${name}".replace(" ", "_").replace("/", "_")
            firestore.collection("chapters").document(docId).delete().await()
            Log.d(TAG, "Chapter $name deleted from Cloud Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete chapter from Firestore: ${e.message}")
        }
    }

    /**
     * Publishes Study Material to Cloud Firestore.
     */
    suspend fun addMaterialToCloud(material: StudyMaterial) = withContext(Dispatchers.IO) {
        if (!isFirebaseAvailable) return@withContext
        try {
            val firestore = FirebaseFirestore.getInstance()
            val docId = "${material.targetClass}_${material.chapterName}_${material.title}".replace(" ", "_").replace("/", "_")
            val matData = hashMapOf(
                "title" to material.title,
                "targetClass" to material.targetClass,
                "chapterName" to material.chapterName,
                "materialType" to material.materialType,
                "subCategory" to material.subCategory,
                "isPremium" to material.isPremium,
                "driveUrl" to material.driveUrl,
                "description" to material.description,
                "publishedTime" to material.publishedTime,
                "isPublished" to material.isPublished
            )
            firestore.collection("materials").document(docId).set(matData).await()
            Log.d(TAG, "Study Material ${material.title} successfully synced to Cloud Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload study material to Firestore: ${e.message}")
        }
    }

    /**
     * Removes Study Material from Cloud Firestore.
     */
    suspend fun deleteMaterialFromCloud(title: String, targetClass: String, chapterName: String) = withContext(Dispatchers.IO) {
        if (!isFirebaseAvailable) return@withContext
        try {
            val firestore = FirebaseFirestore.getInstance()
            val docId = "${targetClass}_${chapterName}_${title}".replace(" ", "_").replace("/", "_")
            firestore.collection("materials").document(docId).delete().await()
            Log.d(TAG, "Study Material $title deleted from Cloud Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete study material from Firestore: ${e.message}")
        }
    }
}

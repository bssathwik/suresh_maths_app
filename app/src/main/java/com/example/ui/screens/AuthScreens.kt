package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import android.util.Log
import com.example.viewmodel.MathsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SureshMathsLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(72.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo),
            contentDescription = "App Logo",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: MathsViewModel,
    onNavigateToLogin: () -> Unit,
    onSuccess: () -> Unit,
    onNavigateToOnboarding: () -> Unit = {}
) {
    val context = LocalContext.current
    var fullName by rememberSaveable { mutableStateOf("") }
    var emailAddress by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var targetClass by rememberSaveable { mutableStateOf("Class 7") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var classDropdownExpanded by remember { mutableStateOf(false) }

    val classesList = listOf("Class 6", "Class 7", "Class 8", "Class 9", "Class 10")
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    viewModel.signInWithGoogle(idToken, 
                        onSuccess = { isNewUser ->
                            android.widget.Toast.makeText(context, "Google Sign-In Successful!", android.widget.Toast.LENGTH_SHORT).show()
                            if (isNewUser) {
                                onNavigateToOnboarding()
                            } else {
                                onSuccess()
                            }
                        },
                        onError = { errMsg ->
                            android.widget.Toast.makeText(context, "Google Sign-In Failed: $errMsg", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            } catch (e: ApiException) {
                android.widget.Toast.makeText(context, "Google Sign-In Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Soft visual environment
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // BACK HEADER Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateToLogin) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "back_btn",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(end = 40.dp) // Offset back button to center title
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. App logo block
            SureshMathsLogo()

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Headlines
            Text(
                text = "Join the Excellence",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Start your mathematical journey with Suresh Maths Material",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Form Card Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = false
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Full Name Input
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Full Name",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            placeholder = { Text("Enter your full name", color = Color(0xFF475569)) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = "name", tint = Color(0xFF64748B))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )
                    }

                    // Email Input
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Email Address",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        OutlinedTextField(
                            value = emailAddress,
                            onValueChange = { emailAddress = it },
                            placeholder = { Text("student@example.com", color = Color(0xFF475569)) },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = "email", tint = Color(0xFF64748B))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )
                    }

                    // Phone Number Input
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Phone Number",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            placeholder = { Text("Enter your phone number", color = Color(0xFF475569)) },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = "phone", tint = Color(0xFF64748B))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )
                    }

                    // Target Class selection Dropdown
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Target Class",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = targetClass,
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = "class", tint = Color(0xFF64748B))
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "dropdown",
                                        tint = Color(0xFF334155),
                                        modifier = Modifier.clickable { classDropdownExpanded = !classDropdownExpanded }
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { classDropdownExpanded = !classDropdownExpanded },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF2563EB),
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC)
                                )
                            )
                            // Hidden overlay to click
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { classDropdownExpanded = !classDropdownExpanded }
                            )

                            DropdownMenu(
                                expanded = classDropdownExpanded,
                                onDismissRequest = { classDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f).background(Color.White)
                            ) {
                                classesList.forEach { className ->
                                    DropdownMenuItem(
                                        text = { Text(className, fontWeight = FontWeight.Medium) },
                                        onClick = {
                                            targetClass = className
                                            classDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Password Input
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Password",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("Min. 8 characters", color = Color(0xFF475569)) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "pass", tint = Color(0xFF64748B))
                            },
                            trailingIcon = {
                                Text(
                                    text = if (isPasswordVisible) "HIDE" else "SHOW",
                                    color = Color(0xFF2563EB),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .clickable { isPasswordVisible = !isPasswordVisible }
                                        .padding(end = 8.dp)
                                )
                            },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )
                    }

                    // Confirm Password Input
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Confirm Password",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = { Text("Re-type password", color = Color(0xFF475569)) },
                            leadingIcon = {
                                Icon(Icons.Default.CheckCircle, contentDescription = "confirm_pass", tint = Color(0xFF64748B))
                            },
                            trailingIcon = {
                                Text(
                                    text = if (isConfirmPasswordVisible) "HIDE" else "SHOW",
                                    color = Color(0xFF2563EB),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .clickable { isConfirmPasswordVisible = !isConfirmPasswordVisible }
                                        .padding(end = 8.dp)
                                )
                            },
                            singleLine = true,
                            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )
                    }

                    // Terms checkmark row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { agreedToTerms = !agreedToTerms },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = agreedToTerms,
                            onCheckedChange = { agreedToTerms = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2563EB))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "I agree to the Terms & Conditions",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF475569)
                        )
                    }

                    // Sign up Action button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (fullName.isBlank() || emailAddress.isBlank() || password.isBlank() || phoneNumber.isBlank()) {
                                Toast.makeText(context, "Please fill out all fields completely.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!emailAddress.contains("@")) {
                                Toast.makeText(context, "Please insert a valid Gmail / Email.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (phoneNumber.length < 10) {
                                Toast.makeText(context, "Please enter a valid phone number (minimum 10 digits).", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (password.length < 8) {
                                Toast.makeText(context, "Password must be minimum 8 characters.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (password != confirmPassword) {
                                Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (!agreedToTerms) {
                                Toast.makeText(context, "You must agree to the Terms and Conditions to continue.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            viewModel.registerStudent(
                                name = fullName,
                                email = emailAddress,
                                phoneNumber = phoneNumber,
                                targetClass = targetClass,
                                password = password,
                                onSuccess = {
                                    Toast.makeText(context, "Verification email sent to $emailAddress! Please verify your email before logging in.", Toast.LENGTH_LONG).show()
                                    onNavigateToLogin()
                                },
                                onFailure = { errMsg ->
                                    Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004AC6))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = "arrow", tint = Color.White)
                        }
                    }

                    // Google Sign-Up Button
                    OutlinedButton(
                        onClick = {
                            if (!agreedToTerms) {
                                Toast.makeText(context, "You must agree to the Terms and Conditions to continue.", Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                            if (resId == 0) {
                                Toast.makeText(context, "Google Sign-In is not configured correctly (missing default_web_client_id).", Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            val clientId = context.getString(resId)
                            if (clientId.isEmpty() || clientId == "null" || !clientId.contains(".apps.googleusercontent.com")) {
                                Toast.makeText(context, "Google Sign-In is not configured correctly.", Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(clientId)
                                .requestEmail()
                                .build()
                            val googleSignInClient = GoogleSignIn.getClient(context, gso)
                            googleSignInClient.signOut().addOnCompleteListener {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy()
                    ) {
                        Text("Sign Up with Google", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer link
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Already have an account? ", color = Color(0xFF64748B), style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Login",
                    color = Color(0xFF004AC6),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MathsViewModel,
    onNavigateToRegister: () -> Unit,
    onSuccess: () -> Unit,
    onNavigateToOnboarding: () -> Unit = {}
) {
    val context = LocalContext.current
    var emailAddress by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    viewModel.signInWithGoogle(idToken, 
                        onSuccess = { isNewUser ->
                            android.widget.Toast.makeText(context, "Google Sign-In Successful!", android.widget.Toast.LENGTH_SHORT).show()
                            if (isNewUser) {
                                onNavigateToOnboarding()
                            } else {
                                onSuccess()
                            }
                        },
                        onError = { errMsg ->
                            android.widget.Toast.makeText(context, "Google Sign-In Failed: $errMsg", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            } catch (e: ApiException) {
                android.widget.Toast.makeText(context, "Google Sign-In Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showForgotPasswordDialog) {
        var resetEmail by remember { mutableStateOf(emailAddress) }
        var isSending by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Password Recovery") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter your email address below to receive a password reset link.")
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSending = true
                        viewModel.sendPasswordResetEmail(
                            email = resetEmail.trim(),
                            onSuccess = {
                                isSending = false
                                showForgotPasswordDialog = false
                                android.widget.Toast.makeText(context, "Password reset email sent", android.widget.Toast.LENGTH_LONG).show()
                            },
                            onFailure = { error ->
                                isSending = false
                                android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    enabled = !isSending && resetEmail.isNotBlank()
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Send Link")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }, enabled = !isSending) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // 1. App logo block
            SureshMathsLogo()

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Headlines
            Text(
                text = "Suresh Maths Material",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Master mathematics with clarity",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Form Card Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        clip = false
                    ),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Welcome back",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    // Email Address field
                    OutlinedTextField(
                        value = emailAddress,
                        onValueChange = { emailAddress = it },
                        placeholder = { Text("Email Address", color = Color(0xFF475569)) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = "email", tint = Color(0xFF64748B))
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        )
                    )

                    // Password field
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("Password", color = Color(0xFF475569)) },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "pass", tint = Color(0xFF64748B))
                            },
                            trailingIcon = {
                                Text(
                                    text = if (isPasswordVisible) "HIDE" else "SHOW",
                                    color = Color(0xFF2563EB),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .clickable { isPasswordVisible = !isPasswordVisible }
                                        .padding(end = 8.dp)
                                )
                            },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF0F172A),
                                focusedBorderColor = Color(0xFF2563EB),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )

                        Text(
                            text = "Forgot Password?",
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showForgotPasswordDialog = true }
                                .padding(vertical = 4.dp)
                        )
                    }

                    // Login Action Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (emailAddress.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Please fill out all credentials.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            viewModel.loginUser(
                                email = emailAddress,
                                password = password,
                                onSuccess = {
                                    Toast.makeText(context, "Log in Successful!", Toast.LENGTH_SHORT).show()
                                    onSuccess()
                                },
                                onFailure = { errMsg ->
                                    Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = "login_arrow", tint = Color.White)
                        }
                    }

                    // Google Sign-In Button
                    OutlinedButton(
                        onClick = {
                            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                            if (resId == 0) {
                                Toast.makeText(context, "Google Sign-In is not configured correctly (missing default_web_client_id).", Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            val clientId = context.getString(resId)
                            if (clientId.isEmpty() || clientId == "null" || !clientId.contains(".apps.googleusercontent.com")) {
                                Toast.makeText(context, "Google Sign-In is not configured correctly.", Toast.LENGTH_SHORT).show()
                                return@OutlinedButton
                            }
                            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(clientId)
                                .requestEmail()
                                .build()
                            val googleSignInClient = GoogleSignIn.getClient(context, gso)
                            // Sign out to allow user to pick an account
                            googleSignInClient.signOut().addOnCompleteListener {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy()
                    ) {
                        Text("Sign In with Google", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA4335))
                    }

                    // Secondary Register Button
                    OutlinedButton(
                        onClick = {
                            focusManager.clearFocus()
                            onNavigateToRegister()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy()
                    ) {
                        Text("Register Now", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Alternative link
            Row(
                modifier = Modifier.clickable { onNavigateToRegister() },
                horizontalArrangement = Arrangement.Center
            ) {
                Text("New here? ", color = Color(0xFF64748B), style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Create an account",
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // STUNNING 4. Bottom Decorative Badges row "ALGEBRA", "GEOMETRY", "STATISTICS"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Algebra
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Σ ", color = Color(0xFF2563EB), fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text("ALGEBRA", color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp)
                }

                // Divider dot
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFF94A3B8)))

                // Geometry
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📐 ", fontSize = 12.sp)
                    Text("GEOMETRY", color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp)
                }

                // Divider dot
                Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFF94A3B8)))

                // Statistics
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📈 ", fontSize = 12.sp)
                    Text("STATISTICS", color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.5.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingProfileScreen(
    viewModel: MathsViewModel,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var targetClass by rememberSaveable { mutableStateOf("Class 7") }
    var classDropdownExpanded by remember { mutableStateOf(false) }

    val classesList = listOf("Class 6", "Class 7", "Class 8", "Class 9", "Class 10")
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val isAppLoading by viewModel.isAppLoading.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SureshMathsLogo()
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Complete Your Profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                "Please provide a few details to get started",
                color = Color(0xFF64748B),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Phone number
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number", color = Color(0xFF64748B)) },
                placeholder = { Text("e.g. 9876543210") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF004AC6),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Class selection
            ExposedDropdownMenuBox(
                expanded = classDropdownExpanded,
                onExpandedChange = { classDropdownExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = targetClass,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Class", color = Color(0xFF64748B)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF004AC6),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                ExposedDropdownMenu(
                    expanded = classDropdownExpanded,
                    onDismissRequest = { classDropdownExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    classesList.forEach { cls ->
                        DropdownMenuItem(
                            text = { Text(cls, color = Color(0xFF0F172A)) },
                            onClick = {
                                targetClass = cls
                                classDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (phoneNumber.isBlank()) {
                        android.widget.Toast.makeText(context, "Please enter your phone number", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.updateUserClassAndPhone(targetClass, phoneNumber) {
                        onSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004AC6)),
                enabled = !isAppLoading
            ) {
                if (isAppLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Complete", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

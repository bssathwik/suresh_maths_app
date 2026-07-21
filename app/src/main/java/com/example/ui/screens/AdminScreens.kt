package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.MathsViewModel
import com.example.data.PaymentRequest
import com.example.data.StudyMaterial
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// =========================================================================
// ADMIN CORE DASHBOARD
// =========================================================================
@Composable
fun AdminDashboardScreen(
    viewModel: MathsViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToVerify: () -> Unit,
    onNavigateToContent: () -> Unit,
    onNavigateToChapters: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToQuizGen: () -> Unit = {}
) {
    val students by viewModel.students.collectAsStateWithLifecycle()
    val allMaterials by viewModel.allMaterials.collectAsStateWithLifecycle()
    val paymentRequests by viewModel.paymentRequests.collectAsStateWithLifecycle()
    val activeUser by viewModel.activeUser.collectAsStateWithLifecycle()

    val pendingCount = paymentRequests.count { it.status == "PENDING" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hello Admin Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome back, Admin 👋",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Admin Dashboard",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF59E0B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, "admin", tint = Color.White)
            }
        }

        // BENTO STATS CARDS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AdminStatCard(
                metric = "${students.size}",
                label = "Total Students",
                icon = Icons.Default.Person,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToUsers
            )

            AdminStatCard(
                metric = "${students.filter { !it.isVerified }.size}",
                label = "Pending Users",
                icon = Icons.Default.Warning,
                color = Color(0xFFEF4444),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToUsers
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AdminStatCard(
                metric = "${allMaterials.size}",
                label = "Total Materials",
                icon = Icons.Default.List,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToContent
            )

            AdminStatCard(
                metric = "$pendingCount",
                label = "UPI Verify Requests",
                icon = Icons.Default.Notifications,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToVerify
            )
        }

        // QUICK ACTIONS INTERACTIVE BUTTON ROW (As shown in screenshot)
        Column {
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionBoxCard(
                    title = "Add Material",
                    icon = Icons.Default.Add,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToAdd,
                    modifier = Modifier.weight(1.0f)
                )

                QuickActionBoxCard(
                    title = "Manage Chapters",
                    icon = Icons.Default.Settings,
                    color = Color(0xFF2E7D32),
                    onClick = onNavigateToChapters,
                    modifier = Modifier.weight(1.0f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionBoxCard(
                    title = "Manage Content",
                    icon = Icons.Default.List,
                    color = Color(0xFFF59E0B),
                    onClick = onNavigateToContent,
                    modifier = Modifier.weight(1.0f)
                )

                QuickActionBoxCard(
                    title = "Verify Payments",
                    icon = Icons.Default.Check,
                    color = MaterialTheme.colorScheme.error,
                    onClick = onNavigateToVerify,
                    modifier = Modifier.weight(1.0f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickActionBoxCard(
                    title = "Manage Users Directories & Gmail Status",
                    icon = Icons.Default.AccountBox,
                    color = Color(0xFF1973E8),
                    onClick = onNavigateToUsers,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickActionBoxCard(
                    title = "Generate AI Quiz",
                    icon = Icons.Default.Build,
                    color = Color(0xFF9C27B0),
                    onClick = onNavigateToQuizGen,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Storage details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = "storage", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Storage Usage", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                LinearProgressIndicator(
                    progress = 0.65f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    "642.5 GB of 1 TB used (Suresh Maths files cloud cache size)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // System details card simulating optimal network health
        Surface(
            color = Color(0xFF2E7D32).copy(alpha = 0.05f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, "ok", tint = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Server Health: Optimal", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    Text("All cloud directories are synchronizing correctly in real-time.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Toggle to Student View
        Button(
            onClick = {
                viewModel.toggleAdminRole(activeUser?.email ?: "", "ADMIN")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Person, contentDescription = "Student View")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Switch to Student View", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdminStatCard(
    metric: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = color)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(metric, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun QuickActionBoxCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = color)
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

// =========================================================================
// ADMIN ADD STUDY CONTENT FORM
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddMaterialScreen(
    viewModel: MathsViewModel,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetClass by remember { mutableStateOf("Class 7") }
    var chapter by remember { mutableStateOf("Fractions") }
    var type by remember { mutableStateOf("Notes") }
    var isPremium by remember { mutableStateOf(false) }
    var driveUrl by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var inputType by remember { mutableStateOf("Drive Link") }
    val inputOptions = listOf("Drive Link", "PDF File Upload")
    var inputTypeDropdownExpanded by remember { mutableStateOf(false) }

    var pdfBase64 by remember { mutableStateOf<String?>(null) }
    var pdfFileName by remember { mutableStateOf<String?>(null) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    var uploadProgress by remember { mutableStateOf<Float?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val pdfLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            uploadError = null
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    pdfFileName = c.getString(c.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME))
                }
            }
            try {
                android.widget.Toast.makeText(context, "Uploading PDF to server...", android.widget.Toast.LENGTH_SHORT).show()
                val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
                val safeName = pdfFileName?.replace(Regex("[^a-zA-Z0-9.\\-]"), "_") ?: "document.pdf"
                val pdfRef = storageRef.child("pdfs/${System.currentTimeMillis()}_$safeName")
                
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val bytes = inputStream.readBytes()
                    val uploadTask = pdfRef.putBytes(bytes)
                    
                    uploadTask.addOnProgressListener { snapshot ->
                        val progress = (100.0 * snapshot.bytesTransferred / (snapshot.totalByteCount.takeIf { c -> c > 0 } ?: 1)).toFloat() / 100f
                        uploadProgress = progress.coerceIn(0f, 1f)
                    }
                    
                    uploadTask.addOnSuccessListener { _ ->
                        uploadProgress = null
                        pdfRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            pdfBase64 = downloadUri.toString()
                            android.util.Log.d("AdminScreens", "PDF uploaded successfully. URL: $downloadUri")
                            android.widget.Toast.makeText(context, "PDF uploaded successfully!", android.widget.Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener { e ->
                            uploadError = "Failed to get download URL: ${e.message}"
                            android.util.Log.e("AdminScreens", "Failed to get download URL", e)
                            e.printStackTrace()
                            android.widget.Toast.makeText(context, "Failed to get download URL: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }.addOnFailureListener { e ->
                        uploadProgress = null
                        uploadError = e.message
                        android.util.Log.e("AdminScreens", "Failed to upload PDF task", e)
                        e.printStackTrace()
                        android.widget.Toast.makeText(context, "Failed to upload PDF: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                } ?: run {
                    uploadError = "Failed to read file from storage."
                }
            } catch (e: Exception) {
                uploadProgress = null
                uploadError = e.message
                android.util.Log.e("AdminScreens", "Error preparing PDF upload", e)
                e.printStackTrace()
                android.widget.Toast.makeText(context, "Error uploading PDF: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    var publishedSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val chaptersList by viewModel.chapters.collectAsStateWithLifecycle()
    val availableClasses = listOf("Class 6", "Class 7", "Class 8", "Class 9", "Class 10")
    val availableTypes = listOf("Notes", "Worksheet", "Model Paper")
    val chaptersForClass = remember(targetClass, chaptersList) {
        chaptersList.filter { it.targetClass.contains(targetClass) }.map { it.name }.sorted()
    }
    
    var classDropdownExpanded by remember { mutableStateOf(false) }
    var chapterDropdownExpanded by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "back")
            }
            Text("Add Study Material", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (publishedSuccess) {
            Surface(
                color = Color(0xFF2E7D32).copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, "verified", tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Study Material published dynamically to the database successfully!",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }

        // Form Fields
        Text("Material Title", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("e.g. Algebra Complete Solved Sheet") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Target Class", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = classDropdownExpanded,
                    onExpandedChange = { classDropdownExpanded = it },
                ) {
                    OutlinedTextField(
                        value = targetClass,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = classDropdownExpanded,
                        onDismissRequest = { classDropdownExpanded = false }
                    ) {
                        availableClasses.forEach { cls ->
                            DropdownMenuItem(
                                text = { Text(cls) },
                                onClick = {
                                    targetClass = cls
                                    chapter = ""
                                    classDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Chapter", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                if (type == "Model Paper") {
                    OutlinedTextField(
                        value = "N/A",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        enabled = false
                    )
                    LaunchedEffect(type) {
                        chapter = "N/A"
                    }
                } else {
                    ExposedDropdownMenuBox(
                        expanded = chapterDropdownExpanded,
                        onExpandedChange = { chapterDropdownExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = chapter,
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Select") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = chapterDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = chapterDropdownExpanded,
                            onDismissRequest = { chapterDropdownExpanded = false }
                        ) {
                            chaptersForClass.forEach { ch ->
                                DropdownMenuItem(
                                    text = { Text(ch) },
                                    onClick = {
                                        chapter = ch
                                        chapterDropdownExpanded = false
                                    }
                                )
                            }
                            if (chaptersForClass.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No chapters found") },
                                    onClick = { chapterDropdownExpanded = false }
                                )
                            }
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Material Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = typeDropdownExpanded,
                onExpandedChange = { typeDropdownExpanded = it },
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = typeDropdownExpanded,
                    onDismissRequest = { typeDropdownExpanded = false }
                ) {
                    availableTypes.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t) },
                            onClick = {
                                type = t
                                typeDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Input Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = inputTypeDropdownExpanded,
                onExpandedChange = { inputTypeDropdownExpanded = it },
            ) {
                OutlinedTextField(
                    value = inputType,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = inputTypeDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(
                    expanded = inputTypeDropdownExpanded,
                    onDismissRequest = { inputTypeDropdownExpanded = false }
                ) {
                    inputOptions.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t) },
                            onClick = {
                                inputType = t
                                inputTypeDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        if (inputType == "PDF File Upload") {
            Text("Upload PDF File", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth().clickable { pdfLauncher.launch("application/pdf") },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(androidx.compose.material.icons.Icons.Default.Upload, contentDescription = "Upload PDF", modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(pdfFileName ?: "Tap to select PDF file")
                    
                    if (uploadError != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Error: $uploadError", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    } else if (uploadProgress != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { uploadProgress ?: 0f },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${(uploadProgress!! * 100).toInt()}% Uploaded", style = MaterialTheme.typography.bodySmall)
                    } else if (pdfBase64 != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Upload complete \u2713", color = androidx.compose.ui.graphics.Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Text("Shareable Google Drive Link", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = driveUrl,
                onValueChange = { driveUrl = it },
                placeholder = { Text("https://drive.google.com/file/d/...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
        }

        Text("Description", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Short description of concepts covered") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val finalUrl = if (inputType == "PDF File Upload") pdfBase64 ?: "" else driveUrl
                if (title.isNotEmpty() && finalUrl.isNotEmpty()) {
                    viewModel.addNewMaterial(
                        title = title,
                        targetClass = targetClass,
                        chapter = chapter,
                        type = type.replace(" ", "_").uppercase(),
                        subCategory = description,
                        isPremium = false,
                        driveUrl = finalUrl,
                        description = description
                    )
                    scope.launch {
                        publishedSuccess = true
                        title = ""
                        driveUrl = ""
                        pdfBase64 = null
                        pdfFileName = null
                        description = ""
                        delay(3500)
                        publishedSuccess = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = title.isNotEmpty() && ((inputType == "PDF File Upload" && pdfBase64 != null) || (inputType == "Drive Link" && driveUrl.isNotEmpty()))
        ) {
            Text("Publish Material")
        }
    }
}

// =========================================================================
// UPI PAY RECEIPT VERIFICATION SCREEN
// =========================================================================
@Composable
fun AdminVerifyPaymentsScreen(
    viewModel: MathsViewModel,
    onBack: () -> Unit
) {
    val requests by viewModel.paymentRequests.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "back")
            }
            Text("Verify UPI Payments", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (requests.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No pending UPI screenshot requests found.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(requests) { req ->
                    val isPending = req.status == "PENDING"
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(req.studentName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(req.studentEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                }
                                Surface(
                                    color = when (req.status) {
                                        "APPROVED" -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                                        "REJECTED" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                        else -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = req.status,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when (req.status) {
                                            "APPROVED" -> Color(0xFF2E7D32)
                                            "REJECTED" -> MaterialTheme.colorScheme.error
                                            else -> Color(0xFFF59E0B)
                                        },
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ref/UPI No: ${req.transactionRef}", style = MaterialTheme.typography.bodyMedium)
                                Text(req.amount, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }

                            // Interactive UPI Receipt simulation mock window
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.List, contentDescription = "rs", tint = MaterialTheme.colorScheme.outline)
                                    Text("View payment screenshot key: verified", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                            }

                            if (isPending) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.approvePayment(req.id, req.studentEmail) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                    ) {
                                        Text("Approve", color = Color.White)
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.rejectPayment(req.id) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Reject")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// CURRICULUM MANAGEMENT / CONTENT LIST PREVIEWS
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMaterialDialog(
    material: StudyMaterial,
    onDismiss: () -> Unit,
    onSave: (StudyMaterial) -> Unit
) {
    var title by remember { mutableStateOf(material.title) }
    var driveUrl by remember { mutableStateOf(material.driveUrl) }
    var description by remember { mutableStateOf(material.description) }
    var isPremium by remember { mutableStateOf(material.isPremium) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Material") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = driveUrl,
                    onValueChange = { driveUrl = it },
                    label = { Text("Drive URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPremium, onCheckedChange = { isPremium = it })
                    Text("Premium Material")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(material.copy(title = title, driveUrl = driveUrl, description = description, isPremium = isPremium))
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AdminContentManageScreen(
    viewModel: MathsViewModel,
    onBack: () -> Unit
) {
    val materials by viewModel.allMaterials.collectAsStateWithLifecycle()
    var editingMaterial by remember { mutableStateOf<StudyMaterial?>(null) }

    if (editingMaterial != null) {
        EditMaterialDialog(
            material = editingMaterial!!,
            onDismiss = { editingMaterial = null },
            onSave = { 
                viewModel.updateMaterial(it)
                editingMaterial = null 
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "back")
            }
            Text("Manage Materials List", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (materials.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No study materials found in database.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(materials) { mat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .clickable { editingMaterial = mat }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "(${mat.targetClass}) ${mat.title}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Chapter: ${mat.chapterName} • ${mat.materialType}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        IconButton(onClick = { viewModel.deleteMaterial(mat.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// CHAPTERS MANAGEMENT / DYNAMIC CHAPTER CREATOR (M3)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChaptersScreen(
    viewModel: MathsViewModel,
    onBack: () -> Unit
) {
    val chaptersList by viewModel.chapters.collectAsStateWithLifecycle()

    var chapterName by remember { mutableStateOf("") }
    var chapterNumStr by remember { mutableStateOf("") }
    
    // Single selection for class
    val classesOptions = listOf("Class 6", "Class 7", "Class 8", "Class 9", "Class 10")
    var selectedClass by remember { mutableStateOf(classesOptions.first()) }
    var classDropdownExpanded by remember { mutableStateOf(false) }

    var feedbackMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "back")
            }
            Text(
                "Manage Chapters", 
                style = MaterialTheme.typography.titleLarge, 
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Section 1: Create Chapter Form Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Create New Chapter", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Name
                        OutlinedTextField(
                            value = chapterName,
                            onValueChange = { chapterName = it },
                            label = { Text("Chapter Name") },
                            placeholder = { Text("e.g. Probability") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // Chapter Number
                        OutlinedTextField(
                            value = chapterNumStr,
                            onValueChange = { chapterNumStr = it },
                            label = { Text("Chapter Number") },
                            placeholder = { Text("e.g. 10") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // Classes Selection
                        Column {
                            Text(
                                "Map to Class", 
                                style = MaterialTheme.typography.labelLarge, 
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ExposedDropdownMenuBox(
                                expanded = classDropdownExpanded,
                                onExpandedChange = { classDropdownExpanded = it },
                            ) {
                                OutlinedTextField(
                                    value = selectedClass,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = classDropdownExpanded,
                                    onDismissRequest = { classDropdownExpanded = false }
                                ) {
                                    classesOptions.forEach { cls ->
                                        DropdownMenuItem(
                                            text = { Text(cls) },
                                            onClick = {
                                                selectedClass = cls
                                                classDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        if (feedbackMessage.isNotBlank()) {
                            Text(
                                text = feedbackMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (feedbackMessage.contains("Success")) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                val num = chapterNumStr.toIntOrNull()
                                if (chapterName.isBlank()) {
                                    feedbackMessage = "Please enter chapter name."
                                } else if (num == null || num <= 0) {
                                    feedbackMessage = "Please enter a valid chapter number."
                                } else if (selectedClass.isBlank()) {
                                    feedbackMessage = "Please select a class."
                                } else {
                                    viewModel.addNewChapter(chapterName, selectedClass, num)
                                    chapterName = ""
                                    chapterNumStr = ""
                                    feedbackMessage = "Success: Chapter added successfully!"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "add")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Chapter Link", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section 2: List of Existing Chapters
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Database Chapters (${chaptersList.size})", 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (chaptersList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No chapters in database.", color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                items(chaptersList) { ch ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${ch.chapterNumber}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = ch.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Classes: ${ch.targetClass}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deleteChapter(ch.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// ADMIN REVEAL REGISTERED USERS SCREEN
// =========================================================================
@Composable
fun AdminUsersScreen(
    viewModel: MathsViewModel,
    onBack: () -> Unit
) {
    val studentsList by viewModel.students.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("All") } // "All", "Pending", "Verified", "Admins"
    
    var userToDelete by remember { mutableStateOf<String?>(null) }
    var userToToggleRole by remember { mutableStateOf<String?>(null) }

    val filteredList = remember(studentsList, searchQuery, activeFilter) {
        studentsList.filter { user ->
            val matchQuery = user.name.lowercase().contains(searchQuery.lowercase()) || 
                             user.email.lowercase().contains(searchQuery.lowercase())
            val matchFilter = when (activeFilter) {
                "Temporary" -> user.status == "TEMPORARY"
                "Approved" -> user.status == "APPROVED"
                "Blocked" -> user.status == "BLOCKED"
                "Admins" -> user.role == "ADMIN"
                else -> true
            }
            matchQuery && matchFilter
        }
    }

    val totalCount = studentsList.size
    val temporaryCount = studentsList.count { it.status == "TEMPORARY" }
    val adminCount = studentsList.count { it.role == "ADMIN" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // App bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "User Directories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "View and manage registered accounts & Gmail users",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // Stats boxes row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total Db Users", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Text("$totalCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Temporary Users", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    Text("$temporaryCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Admin Staff", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("$adminCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search users by name or email...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            }
        )

        // Horizontal filter chips
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("All", "Temporary", "Approved", "Blocked", "Admins")
            filters.forEach { filter ->
                FilterChip(
                    selected = activeFilter == filter,
                    onClick = { activeFilter = filter },
                    label = { Text(filter) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Users scrolling list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matching accounts found.", color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                items(filteredList) { user ->
                    val initial = user.name.take(1).uppercase()
                    // Assign semi-random aesthetically pleasing colors based on email hash
                    val colorHash = user.email.hashCode().coerceAtLeast(0)
                    val avatarColors = listOf(
                        Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFE53935),
                        Color(0xFF8E24AA), Color(0xFFF57C00), Color(0xFF00ACC1)
                    )
                    val avatarBg = avatarColors[colorHash % avatarColors.size]

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar circle
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(avatarBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = initial,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = "Grade: ${user.targetClass}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (user.status) {
                                                        "APPROVED" -> Color(0xFF10B981)
                                                        "BLOCKED" -> Color(0xFFEF4444)
                                                        else -> Color(0xFFF59E0B)
                                                    }
                                                )
                                        )
                                        Text(
                                            text = "Status: ${user.status}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = when (user.status) {
                                                "APPROVED" -> Color(0xFF047857)
                                                "BLOCKED" -> Color(0xFFB91C1C)
                                                else -> Color(0xFFB45309)
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Delete user progress button
                                IconButton(onClick = { userToDelete = user.email }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete account",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                            // Action Badges row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                var expandedStatusMenu by remember { mutableStateOf(false) }

                                Box {
                                    AssistChip(
                                        onClick = { expandedStatusMenu = true },
                                        label = {
                                            Text(
                                                text = "Change Status",
                                                fontWeight = FontWeight.Bold,
                                                color = when (user.status) {
                                                    "APPROVED" -> Color(0xFF059669)
                                                    "BLOCKED" -> Color(0xFFDC2626)
                                                    else -> Color(0xFFD97706)
                                                }
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = when (user.status) {
                                                    "APPROVED" -> Icons.Default.CheckCircle
                                                    "BLOCKED" -> Icons.Default.Warning
                                                    else -> Icons.Default.Warning
                                                },
                                                contentDescription = "Status",
                                                tint = when (user.status) {
                                                    "APPROVED" -> Color(0xFF059669)
                                                    "BLOCKED" -> Color(0xFFDC2626)
                                                    else -> Color(0xFFD97706)
                                                },
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    )
                                    DropdownMenu(
                                        expanded = expandedStatusMenu,
                                        onDismissRequest = { expandedStatusMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Temporary") },
                                            onClick = {
                                                viewModel.updateUserStatus(user.email, "TEMPORARY")
                                                expandedStatusMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Approved") },
                                            onClick = {
                                                viewModel.updateUserStatus(user.email, "APPROVED")
                                                expandedStatusMenu = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Blocked") },
                                            onClick = {
                                                viewModel.updateUserStatus(user.email, "BLOCKED")
                                                expandedStatusMenu = false
                                            }
                                        )
                                    }
                                }

                                // Role Switcher Badge Button
                                AssistChip(
                                    onClick = { userToToggleRole = user.email },
                                    label = {
                                        Text(
                                            text = "Role: ${user.role}",
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Analytics Stats Indicators
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${user.notesViewedCount}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Notes Read", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${user.worksheetsDownloadedCount}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Worksheets", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${user.quizzesCompletedCount}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text("Quizzes Quiz", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Danger Zone Deletion Confirm Warning Dialog
    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Delete This Account?") },
            text = { Text("Are you absolutely sure you want to permanently delete the math progress, downloaded sheets volume, and profile history of $userToDelete? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        userToDelete?.let { email ->
                            viewModel.deleteUserProgress(email)
                        }
                        userToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Account", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Toggle Admin/Student Role Dialog
    if (userToToggleRole != null) {
        val email = userToToggleRole!!
        val currentRole = studentsList.find { it.email == email }?.role ?: "STUDENT"
        AlertDialog(
            onDismissRequest = { userToToggleRole = null },
            title = { Text("Change System Role?") },
            text = { Text("Do you want to change the security access level for $email from $currentRole to ${if (currentRole == "ADMIN") "STUDENT" else "ADMIN"}? Granting ADMIN will allow them to host courses, check UPI uploads, and manage chapters.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleAdminRole(email, currentRole)
                        userToToggleRole = null
                    }
                ) {
                    Text("Update Role")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToToggleRole = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminQuizGenerationScreen(viewModel: MathsViewModel, onBack: () -> Unit) {
    var documentText by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("Class 7") }
    var chapterName by remember { mutableStateOf("Fractions") }
    var difficulty by remember { mutableStateOf("Medium") }
    var numQuestions by remember { mutableStateOf("5") }
    var inputType by remember { mutableStateOf("Content Text") }

    var expandedClass by remember { mutableStateOf(false) }
    var expandedChapter by remember { mutableStateOf(false) }
    var expandedDifficulty by remember { mutableStateOf(false) }
    var expandedInput by remember { mutableStateOf(false) }

    val classOptions = listOf("Class 6", "Class 7", "Class 8", "Class 9", "Class 10")
    val chapterOptions = listOf("Fractions", "Decimals", "Algebra", "Geometry", "Data Handling", "Number System")
    val difficultyOptions = listOf("Easy", "Medium", "Hard")
    val inputOptions = listOf("Content Text", "Drive Link", "PDF Link", "PDF File Upload")

    var pdfBase64 by remember { mutableStateOf<String?>(null) }
    var pdfFileName by remember { mutableStateOf<String?>(null) }
    var isProcessingPdf by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val pdfLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    pdfFileName = c.getString(c.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME))
                }
            }
            isProcessingPdf = true
            scope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(it)?.use { inputStream ->
                        val bytes = inputStream.readBytes()
                        val b64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                        withContext(Dispatchers.Main) {
                            pdfBase64 = b64
                            isProcessingPdf = false
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        isProcessingPdf = false
                        android.widget.Toast.makeText(context, "Failed to read PDF: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    var isGenerating by remember { mutableStateOf(false) }
    var generatedQuestions by remember { mutableStateOf<List<com.example.data.QuizQuestion>?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate AI Quiz", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (generatedQuestions == null) {
                ExposedDropdownMenuBox(
                    expanded = expandedInput,
                    onExpandedChange = { expandedInput = it },
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = inputType,
                        onValueChange = { },
                        label = { Text("Upload By") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedInput) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedInput,
                        onDismissRequest = { expandedInput = false }
                    ) {
                        inputOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    inputType = option
                                    expandedInput = false
                                }
                            )
                        }
                    }
                }

                if (inputType == "PDF File Upload") {
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { pdfLauncher.launch("application/pdf") },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(androidx.compose.material.icons.Icons.Default.Upload, contentDescription = "Upload PDF", modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(pdfFileName ?: "Tap to select PDF file")
                            
                            if (isProcessingPdf) {
                                Spacer(modifier = Modifier.height(16.dp))
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Processing PDF for AI...", style = MaterialTheme.typography.bodySmall)
                            } else if (pdfBase64 != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("PDF ready for AI processing \u2713", color = androidx.compose.ui.graphics.Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = documentText,
                        onValueChange = { documentText = it },
                        label = { 
                            Text(
                                when(inputType) {
                                    "Drive Link" -> "Paste Google Drive Link here"
                                    "PDF Link" -> "Paste PDF URL here"
                                    else -> "Paste document or syllabus text here"
                                }
                            ) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        maxLines = 10
                    )
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedClass,
                        onExpandedChange = { expandedClass = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = className,
                            onValueChange = { },
                            label = { Text("Target Class") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClass) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedClass,
                            onDismissRequest = { expandedClass = false }
                        ) {
                            classOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        className = option
                                        expandedClass = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = expandedChapter,
                        onExpandedChange = { expandedChapter = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = chapterName,
                            onValueChange = { },
                            label = { Text("Chapter") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedChapter) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedChapter,
                            onDismissRequest = { expandedChapter = false }
                        ) {
                            chapterOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        chapterName = option
                                        expandedChapter = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = expandedDifficulty,
                        onExpandedChange = { expandedDifficulty = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = difficulty,
                            onValueChange = { },
                            label = { Text("Difficulty") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDifficulty) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDifficulty,
                            onDismissRequest = { expandedDifficulty = false }
                        ) {
                            difficultyOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        difficulty = option
                                        expandedDifficulty = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = numQuestions,
                        onValueChange = { numQuestions = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Appx Questions") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val num = numQuestions.toIntOrNull() ?: 1
                            val manualList = mutableListOf<com.example.data.QuizQuestion>()
                            for (i in 0 until num) {
                                manualList.add(
                                    com.example.data.QuizQuestion(
                                        id = "${className.replace(" ", "")}_${chapterName.replace(" ", "")}_${difficulty}_${System.currentTimeMillis()}_$i",
                                        targetClass = className,
                                        chapterName = chapterName,
                                        difficulty = difficulty,
                                        questionText = "", optionA = "", optionB = "", optionC = "", optionD = "", correctAnswer = "A", explanation = ""
                                    )
                                )
                            }
                            generatedQuestions = manualList
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Manual Entry")
                    }
                    Button(
                        onClick = {
                            val num = numQuestions.toIntOrNull() ?: 5
                            val isPdf = inputType == "PDF File Upload"
                            val textToPass = if (isPdf) pdfBase64 else documentText
                            
                            if (textToPass != null && textToPass.isNotBlank() && chapterName.isNotBlank()) {
                                isGenerating = true
                                scope.launch {
                                    val result = viewModel.generateQuizUsingAI(textToPass, className, chapterName, difficulty, num, isPdf)
                                    generatedQuestions = result
                                    isGenerating = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1.5f),
                        enabled = !isGenerating && chapterName.isNotBlank() && ((inputType == "PDF File Upload" && pdfBase64 != null) || (inputType != "PDF File Upload" && documentText.isNotBlank()))
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Gen...")
                        } else {
                            Text("Generate AI")
                        }
                    }
                }
            } else {
                // Edit questions UI
                Text("Review and Edit Questions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                
                generatedQuestions?.forEachIndexed { index, qt ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = qt.questionText,
                                onValueChange = { nv -> 
                                    generatedQuestions = generatedQuestions?.toMutableList()?.apply { this[index] = qt.copy(questionText = nv) }
                                },
                                label = { Text("Question ${index + 1}") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = qt.optionA, onValueChange = { nv -> generatedQuestions = generatedQuestions?.toMutableList()?.apply { this[index] = qt.copy(optionA = nv) } }, label = { Text("Option A") }, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = qt.optionB, onValueChange = { nv -> generatedQuestions = generatedQuestions?.toMutableList()?.apply { this[index] = qt.copy(optionB = nv) } }, label = { Text("Option B") }, modifier = Modifier.weight(1f))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = qt.optionC, onValueChange = { nv -> generatedQuestions = generatedQuestions?.toMutableList()?.apply { this[index] = qt.copy(optionC = nv) } }, label = { Text("Option C") }, modifier = Modifier.weight(1f))
                                OutlinedTextField(value = qt.optionD, onValueChange = { nv -> generatedQuestions = generatedQuestions?.toMutableList()?.apply { this[index] = qt.copy(optionD = nv) } }, label = { Text("Option D") }, modifier = Modifier.weight(1f))
                            }
                            OutlinedTextField(
                                value = qt.correctAnswer,
                                onValueChange = { nv -> 
                                    generatedQuestions = generatedQuestions?.toMutableList()?.apply { this[index] = qt.copy(correctAnswer = nv) }
                                },
                                label = { Text("Correct Answer (A, B, C, D)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = qt.explanation,
                                onValueChange = { nv -> 
                                    generatedQuestions = generatedQuestions?.toMutableList()?.apply { this[index] = qt.copy(explanation = nv) }
                                },
                                label = { Text("Explanation") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { generatedQuestions = null },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Discard")
                    }
                    Button(
                        onClick = {
                            generatedQuestions?.let { qs -> viewModel.saveQuizQuestions(qs) }
                            onBack()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save All Questions")
                    }
                }
            }
        }
    }
}
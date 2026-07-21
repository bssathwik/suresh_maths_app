package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.screens.*
import com.example.viewmodel.MathsViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MathsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // window.setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE, android.view.WindowManager.LayoutParams.FLAG_SECURE)
        
        try {
            com.google.firebase.FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        viewModel.processIntentLink(intent.data?.toString())

        enableEdgeToEdge()
        setContent {
            val themeSetting by viewModel.themeSetting.collectAsStateWithLifecycle()
            val isDark = when (themeSetting) {
                "DARK" -> true
                "LIGHT" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            MyApplicationTheme(darkTheme = isDark) {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        viewModel.processIntentLink(intent.data?.toString())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: MathsViewModel) {
    val activeUser by viewModel.activeUser.collectAsStateWithLifecycle()

    // State based routing
    var currentScreen by rememberSaveable { mutableStateOf("splash") }
    var selectedClassName by rememberSaveable { mutableStateOf("Class 7") }
    var selectedChapterName by rememberSaveable { mutableStateOf("Chapter 2 - Fractions") }

    val isAdmin = activeUser?.role == "ADMIN"
    val isAppLoading by viewModel.isAppLoading.collectAsStateWithLifecycle()
    
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    
    var isDocumentViewerOpen by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }

    // Set screen paths on user switch
    LaunchedEffect(activeUser, isAppLoading) {
        if (isAppLoading || currentScreen == "onboarding_profile") {
            // Keep current screen if loading or explicitly doing onboarding
            if (isAppLoading) {
                currentScreen = "splash"
            }
        } else if (activeUser == null) {
            currentScreen = "login"
        } else if (activeUser?.role == "ADMIN") {
            if (currentScreen == "splash" || currentScreen == "login" || currentScreen == "register") {
                currentScreen = "admin_dashboard"
            }
        } else {
            if (currentScreen == "splash" || currentScreen == "login" || currentScreen == "register" || currentScreen == "admin_dashboard" || currentScreen.startsWith("admin_")) {
                currentScreen = "home"
            }
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        if (activeUser != null && currentScreen != "notes" && isLandscape && !isDocumentViewerOpen) {
            NavigationRail(
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start)),
                containerColor = MaterialTheme.colorScheme.surface,
                header = {
                    IconButton(onClick = { currentScreen = "profile" }) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(if (isAdmin) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "profile", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            ) {
                if (isAdmin) {
                    NavigationRailItem(
                        selected = currentScreen == "admin_dashboard",
                        onClick = { currentScreen = "admin_dashboard" },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                        label = { Text("Dashboard", style = MaterialTheme.typography.labelSmall) }
                    )
                    NavigationRailItem(
                        selected = currentScreen == "admin_add",
                        onClick = { currentScreen = "admin_add" },
                        icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                        label = { Text("Add Material", style = MaterialTheme.typography.labelSmall) }
                    )
                    NavigationRailItem(
                        selected = currentScreen == "admin_verify",
                        onClick = { currentScreen = "admin_verify" },
                        icon = { Icon(Icons.Default.Check, contentDescription = "Verify") },
                        label = { Text("Verify", style = MaterialTheme.typography.labelSmall) }
                    )
                    NavigationRailItem(
                        selected = currentScreen == "admin_content",
                        onClick = { currentScreen = "admin_content" },
                        icon = { Icon(Icons.Default.Edit, contentDescription = "Edit") },
                        label = { Text("Content", style = MaterialTheme.typography.labelSmall) }
                    )
                } else {
                    NavigationRailItem(
                        selected = currentScreen == "home",
                        onClick = { currentScreen = "home" },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", style = MaterialTheme.typography.labelSmall) }
                    )
                    NavigationRailItem(
                        selected = currentScreen == "classes" || currentScreen == "chapter_list" || currentScreen == "chapter_hub",
                        onClick = { currentScreen = "classes" },
                        icon = { Icon(Icons.Default.List, contentDescription = "Classes") },
                        label = { Text("Classes", style = MaterialTheme.typography.labelSmall) }
                    )
                    NavigationRailItem(
                        selected = currentScreen == "papers",
                        onClick = { currentScreen = "papers" },
                        icon = { Icon(Icons.Default.Info, contentDescription = "Papers") },
                        label = { Text("Papers", style = MaterialTheme.typography.labelSmall) }
                    )
                    NavigationRailItem(
                        selected = currentScreen == "interactive",
                        onClick = { currentScreen = "interactive" },
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Interactive") },
                        label = { Text("Interactive", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        Scaffold(
            modifier = Modifier.weight(1f),
            topBar = {
                if (activeUser != null && currentScreen != "notes" && (!isLandscape) && !isDocumentViewerOpen) { // Only show TopBar in portrait
                    TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (isAdmin) "Suresh Maths Admin" else "Suresh Maths Material",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (!isAdmin) {
                                Text(
                                    text = "Class 6 to 10 Study Hub",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    },
                    actions = {
                        val themeSetting by viewModel.themeSetting.collectAsStateWithLifecycle()
                        IconButton(
                            onClick = {
                                val nextTheme = when (themeSetting) {
                                    "SYSTEM" -> "LIGHT"
                                    "LIGHT" -> "DARK"
                                    else -> "SYSTEM"
                                }
                                viewModel.setThemeSetting(nextTheme)
                            },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                ThemeToggleIcon(
                                    themeSetting = themeSetting,
                                    tintColor = MaterialTheme.colorScheme.primary,
                                    surfaceColor = MaterialTheme.colorScheme.surface
                                )
                            }
                        }

                        IconButton(onClick = { currentScreen = "profile" }) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isAdmin) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                )
                }
            },
            bottomBar = {
                if (activeUser != null && currentScreen != "notes" && (!isLandscape) && !isDocumentViewerOpen) {
                    NavigationBar(
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        if (isAdmin) {
                            NavigationBarItem(
                                selected = currentScreen == "admin_dashboard",
                                onClick = { currentScreen = "admin_dashboard" },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                                label = { Text("Dashboard", style = MaterialTheme.typography.labelSmall) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == "admin_add",
                                onClick = { currentScreen = "admin_add" },
                                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                                label = { Text("Add Material", style = MaterialTheme.typography.labelSmall) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == "admin_verify",
                                onClick = { currentScreen = "admin_verify" },
                                icon = { Icon(Icons.Default.Check, contentDescription = "Verify") },
                                label = { Text("Verify Pay", style = MaterialTheme.typography.labelSmall) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == "admin_content",
                                onClick = { currentScreen = "admin_content" },
                                icon = { Icon(Icons.Default.Edit, contentDescription = "Edit") },
                                label = { Text("Manage Content", style = MaterialTheme.typography.labelSmall) }
                            )
                        } else {
                            NavigationBarItem(
                                selected = currentScreen == "home",
                                onClick = { currentScreen = "home" },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                label = { Text("Home", style = MaterialTheme.typography.labelSmall) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == "classes" || currentScreen == "chapter_list" || currentScreen == "chapter_hub",
                                onClick = { currentScreen = "classes" },
                                icon = { Icon(Icons.Default.List, contentDescription = "Classes") },
                                label = { Text("Classes", style = MaterialTheme.typography.labelSmall) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == "papers",
                                onClick = { currentScreen = "papers" },
                                icon = { Icon(Icons.Default.Info, contentDescription = "Papers") },
                                label = { Text("Papers", style = MaterialTheme.typography.labelSmall) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == "interactive",
                                onClick = { currentScreen = "interactive" },
                                icon = { Icon(Icons.Default.Favorite, contentDescription = "Interactive") },
                                label = { Text("Interactive", style = MaterialTheme.typography.labelSmall) }
                            )
                            NavigationBarItem(
                                selected = currentScreen == "profile",
                                onClick = { currentScreen = "profile" },
                                icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                label = { Text("Profile", style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // ... same AnimatedContent ...
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "screen_transitions"
            ) { target ->
                when (target) {
                    "splash" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    "login" -> LoginScreen(
                        viewModel = viewModel,
                        onNavigateToRegister = { currentScreen = "register" },
                        onSuccess = {
                            if (activeUser?.role == "ADMIN") {
                                currentScreen = "admin_dashboard"
                            } else {
                                currentScreen = "home"
                            }
                        },
                        onNavigateToOnboarding = { currentScreen = "onboarding_profile" }
                    )
                    "register" -> RegisterScreen(
                        viewModel = viewModel,
                        onNavigateToLogin = { currentScreen = "login" },
                        onSuccess = {
                            if (activeUser?.role == "ADMIN") {
                                currentScreen = "admin_dashboard"
                            } else {
                                currentScreen = "home"
                            }
                        },
                        onNavigateToOnboarding = { currentScreen = "onboarding_profile" }
                    )
                    "onboarding_profile" -> OnboardingProfileScreen(
                        viewModel = viewModel,
                        onSuccess = {
                            if (activeUser?.role == "ADMIN") {
                                currentScreen = "admin_dashboard"
                            } else {
                                currentScreen = "home"
                            }
                        }
                    )
                    "home" -> StudentHomeScreen(
                        viewModel = viewModel,
                        onNavigateToClass = {
                            selectedClassName = it
                            currentScreen = "chapter_list"
                        },
                        onNavigateToPapers = { currentScreen = "papers" }
                    )
                    "classes" -> ClassesSelectionScreen(
                        viewModel = viewModel,
                        onClassSelected = {
                            selectedClassName = it
                            currentScreen = "chapter_list"
                        }
                    )
                    "chapter_list" -> ClassChaptersScreen(
                        className = selectedClassName,
                        viewModel = viewModel,
                        onNavigateToChapterHub = {
                            selectedChapterName = it
                            currentScreen = "chapter_hub"
                        }
                    )
                    "chapter_hub" -> ChapterHubScreen(
                        className = selectedClassName,
                        chapterDetails = selectedChapterName,
                        onNavigateToSection = { section ->
                            when (isCleanSectionTitle(section)) {
                                "Notes" -> currentScreen = "notes"
                                "Worksheets" -> currentScreen = "worksheets"
                                else -> currentScreen = "quiz"
                            }
                        }
                    )
                    "notes" -> NotesScreen(
                        className = selectedClassName,
                        chapterDetails = selectedChapterName,
                        viewModel = viewModel,
                        onBack = { currentScreen = "chapter_hub" }
                    )
                    "worksheets" -> WorksheetsScreen(
                        className = selectedClassName,
                        chapterDetails = selectedChapterName,
                        viewModel = viewModel,
                        onBack = { currentScreen = "chapter_hub" },
                        onDocumentOpen = { isOpen -> isDocumentViewerOpen = isOpen }
                    )
                    "quiz" -> QuizScreen(
                        className = selectedClassName,
                        chapterDetails = selectedChapterName,
                        viewModel = viewModel,
                        onBack = { currentScreen = "chapter_hub" }
                    )
                    "papers" -> ModelPapersScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "home" },
                        onDocumentOpen = { isOpen -> isDocumentViewerOpen = isOpen }
                    )
                    "interactive" -> InteractiveLearningScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "home" }
                    )
                    "profile" -> ProfileScreen(
                        viewModel = viewModel,
                        onNavigateToHome = {
                            if (isAdmin) {
                                currentScreen = "admin_dashboard"
                            } else {
                                currentScreen = "home"
                            }
                        }
                    )

                    "admin_dashboard" -> AdminDashboardScreen(
                        viewModel = viewModel,
                        onNavigateToAdd = { currentScreen = "admin_add" },
                        onNavigateToVerify = { currentScreen = "admin_verify" },
                        onNavigateToContent = { currentScreen = "admin_content" },
                        onNavigateToChapters = { currentScreen = "admin_chapters" },
                        onNavigateToUsers = { currentScreen = "admin_users" },
                        onNavigateToQuizGen = { currentScreen = "admin_quiz_gen" }
                    )
                    "admin_add" -> AdminAddMaterialScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "admin_dashboard" }
                    )
                    "admin_verify" -> AdminVerifyPaymentsScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "admin_dashboard" }
                    )
                    "admin_content" -> AdminContentManageScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "admin_dashboard" }
                    )
                    "admin_chapters" -> AdminChaptersScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "admin_dashboard" }
                    )
                    "admin_users" -> AdminUsersScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "admin_dashboard" }
                    )
                    "admin_quiz_gen" -> AdminQuizGenerationScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "admin_dashboard" }
                    )
                }
            }
        }
    }
}
}

fun isCleanSectionTitle(title: String): String {
    return title.trim()
}

@Composable
fun ThemeToggleIcon(themeSetting: String, tintColor: Color, surfaceColor: Color) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(18.dp)) {
        val radius = size.minDimension / 2
        when (themeSetting) {
            "LIGHT" -> {
                // Sun
                drawCircle(color = Color(0xFFF59E0B), radius = radius * 0.48f)
                for (i in 0 until 8) {
                    val angle = i * Math.PI / 4
                    val startX = (center.x + radius * 0.65f * Math.cos(angle)).toFloat()
                    val startY = (center.y + radius * 0.65f * Math.sin(angle)).toFloat()
                    val endX = (center.x + radius * 0.95f * Math.cos(angle)).toFloat()
                    val endY = (center.y + radius * 0.95f * Math.sin(angle)).toFloat()
                    drawLine(
                        color = Color(0xFFF59E0B),
                        start = androidx.compose.ui.geometry.Offset(startX, startY),
                        end = androidx.compose.ui.geometry.Offset(endX, endY),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }
            "DARK" -> {
                // Moon (crescent moon carved out using overlapping background color)
                drawCircle(color = Color(0xFFA4C9FF), radius = radius * 0.8f)
                drawCircle(
                    color = surfaceColor,
                    radius = radius * 0.8f,
                    center = androidx.compose.ui.geometry.Offset(center.x - radius * 0.45f, center.y - radius * 0.15f)
                )
            }
            else -> {
                // System - split circle
                // We'll draw half-filled and half-outlined
                // Outlined outer circle
                drawCircle(
                    color = tintColor,
                    radius = radius,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                )
                // Filled right half
                drawArc(
                    color = tintColor,
                    startAngle = 270f,
                    sweepAngle = 180f,
                    useCenter = true
                )
            }
        }
    }
}

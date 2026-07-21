package com.example.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.MathsViewModel
import com.example.data.UserProgress
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalUriHandler

// helper function for notes content
fun getNoteDetailsContent(title: String): String {
    return when (title) {
        "Concept Notes" -> """
            A fraction represents a part of a whole or, more generally, any number of equal parts. It consists of a numerator (on top) and a denominator (on the bottom).
            
            TYPES OF FRACTIONS:
            1. Proper Fraction: Numerator is less than denominator (e.g., 2/3, 5/8).
            2. Improper Fraction: Numerator is greater than or equal to denominator (e.g., 7/4, 9/5).
            3. Mixed Fraction: Combination of a whole number and a proper fraction (e.g., 2 1/3).
            
            ADDITION & SUBTRACTION:
            To add or subtract fractions, you must find a common denominator first, then combine the numerators.
            Example: 1/3 + 1/2 = 2/6 + 3/6 = 5/6.
        """.trimIndent()
        "Important Formulas" -> """
            REVISION FORMULAS:
            
            1. Product of Fractions:
               (Numerator 1 * Numerator 2) / (Denominator 1 * Denominator 2)
               e.g., a/b * c/d = (ac) / (bd)
            
            2. Division of Fractions:
               Multiply the first fraction by the reciprocal (inverse) of the second fraction.
               a/b ÷ c/d = a/b * d/c = (ad) / (bc)
               
            3. Converting Mixed to Improper:
               Whole Number 1 (a/b) = (Whole Number * b + a) / b
        """.trimIndent()
        else -> """
            QUICK SUMMARY CHEETSHEET:
            
            - The value of a proper fraction is always less than 1.
            - The value of an improper fraction is always greater than or equal to 1.
            - Reciprocal of a fraction is formed by swapping the numerator and the denominator. Note: 0 has no reciprocal.
            - Always simplify fractions to their lowest terms by dividing the numerator and denominator by their Highest Common Factor (HCF).
        """.trimIndent()
    }
}

// =========================================================================
// STUDENT HOME DASHBOARD
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    viewModel: MathsViewModel,
    onNavigateToClass: (String) -> Unit,
    onNavigateToPapers: () -> Unit
) {
    val activeUser by viewModel.activeUser.collectAsStateWithLifecycle()
    val announcements by viewModel.announcements.collectAsStateWithLifecycle()
    val allPublishedMaterials by viewModel.allPublishedMaterials.collectAsStateWithLifecycle()
    var searchInput by remember { mutableStateOf("") }
    var selectedMaterialId by rememberSaveable { mutableStateOf<Int?>(null) }
    var isHeaderVisible by rememberSaveable { mutableStateOf(true) }

    val scrollState = rememberScrollState()

    val selectedMaterial = allPublishedMaterials.find { it.id == selectedMaterialId }

    if (selectedMaterial != null) {
        DocumentViewer(
            driveUrl = selectedMaterial.driveUrl,
            isHeaderVisible = isHeaderVisible,
            onToggleHeader = { isHeaderVisible = !isHeaderVisible },
            onBack = {
                selectedMaterialId = null
                isHeaderVisible = true
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hello Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome back,",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${activeUser?.name ?: "User"} 👋",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

            }

            // Search Notes Bar
            OutlinedTextField(
                value = searchInput,
                onValueChange = { searchInput = it },
                placeholder = { Text("Search notes, worksheets, papers...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            if (searchInput.isNotBlank()) {
                val searchResults = allPublishedMaterials.filter {
                    it.title.contains(searchInput, ignoreCase = true) ||
                    it.chapterName.contains(searchInput, ignoreCase = true) ||
                    it.materialType.contains(searchInput, ignoreCase = true) ||
                    it.targetClass.contains(searchInput, ignoreCase = true)
                }

                if (searchResults.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No materials found.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    Text("Search Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        searchResults.forEach { material ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMaterialId = material.id },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = "Document",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(material.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                        Text("${material.targetClass} • ${material.chapterName} • ${material.materialType}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Welcome Banner
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Welcome to",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Suresh Maths Material",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Learn Maths Easy Way",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Displaying the logo. If you upload the logo to res/drawable as 'logo.png', you can use Image with painterResource
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
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
            }
        }

        // Daily Quote
        val mathQuotes = remember {
            listOf(
                Pair("\"It's not that I'm so smart, it's just that I stay with problems longer.\"", "— Albert Einstein"),
                Pair("\"The only way to learn mathematics is to do mathematics.\"", "— Paul Halmos"),
                Pair("\"Mathematics may not teach us how to add love or minus hate. But it gives us every reason to hope that every problem has a solution.\"", "— Author Unknown"),
                Pair("\"Pure mathematics is, in its way, the poetry of logical ideas.\"", "— Albert Einstein"),
                Pair("\"Do not worry about your difficulties in mathematics. I can assure you mine are still greater.\"", "— Albert Einstein"),
                Pair("\"Mathematics is the most beautiful and most powerful creation of the human spirit.\"", "— Stefan Banach"),
                Pair("\"Nature is written in mathematical language.\"", "— Galileo Galilei"),
                Pair("\"Mathematics is the music of reason.\"", "— James Joseph Sylvester"),
                Pair("\"Without mathematics, there’s nothing you can do. Everything around you is mathematics. Everything around you is numbers.\"", "— Shakuntala Devi"),
                Pair("\"Life is a math equation. In order to gain the most, you have to know how to convert negatives into positives.\"", "— Anonymous"),
                Pair("\"Go down deep enough into anything and you will find mathematics.\"", "— Dean Schlicter"),
                Pair("\"Somehow it's okay for people to chuckle about not being good at math. Yet, if I said 'I never learned to read,' they'd say I was an illiterate dolt.\"", "— Neil deGrasse Tyson"),
                Pair("\"Mathematics allows for no hypocrisy and no vagueness.\"", "— Stendhal"),
                Pair("\"Obvious is the most dangerous word in mathematics.\"", "— Eric Temple Bell")
            )
        }
        val calendar = java.util.Calendar.getInstance()
        val dayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val dailyQuote = remember(dayOfYear) { mathQuotes[dayOfYear % mathQuotes.size] }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = dailyQuote.first,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dailyQuote.second,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }

        // Classes Grid
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Classes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            val classesList = listOf("Class 6", "Class 7", "Class 8", "Class 9", "Class 10")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                for (i in classesList.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (j in 0..1) {
                            val classIndex = i + j
                            if (classIndex < classesList.size) {
                                val className = classesList[classIndex]
                                val cardStyle = getClassCardStyle(className)
                                val count = allPublishedMaterials.count { it.targetClass.equals(className, ignoreCase = true) }
                                val materialText = if (count == 1) "1 Material Available" else "$count Materials Available"
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { onNavigateToClass(className) }
                                        .background(cardStyle.bgBrush)
                                        .border(
                                            1.dp,
                                            cardStyle.borderColor.copy(alpha = 0.4f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .padding(16.dp)
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(cardStyle.iconBgColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.MenuBook,
                                                contentDescription = null,
                                                tint = cardStyle.iconColor
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = className,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = cardStyle.titleColor
                                        )
                                        Text(
                                            text = materialText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = cardStyle.descColor
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
            } // closes else (searchInput)
        } // closes Column (main)
    } // closes else (selectedMaterial)
}

// =========================================================================
// CLASSES SELECTION SCREEN
// =========================================================================
data class ClassCardStyle(
    val bgBrush: Brush,
    val iconBgColor: Color,
    val iconColor: Color,
    val titleColor: Color,
    val descColor: Color,
    val borderColor: Color
)

@Composable
fun getClassCardStyle(className: String): ClassCardStyle {
    val isLight = MaterialTheme.colorScheme.background.luminance() > 0.5f
    
    return if (isLight) {
        when (className) {
            "Class 6" -> ClassCardStyle(
                bgBrush = Brush.horizontalGradient(listOf(Color(0xFFE0F2FE), Color(0xFFF0F9FF))),
                iconBgColor = Color(0xFFBAE6FD),
                iconColor = Color(0xFF0369A1),
                titleColor = Color(0xFF0C4A6E),
                descColor = Color(0xFF0284C7),
                borderColor = Color(0xFF7DD3FC)
            )
            "Class 7" -> ClassCardStyle(
                bgBrush = Brush.horizontalGradient(listOf(Color(0xFFE6F4EA), Color(0xFFF1F8F5))),
                iconBgColor = Color(0xFFCEEAD6),
                iconColor = Color(0xFF137333),
                titleColor = Color(0xFF0D652D),
                descColor = Color(0xFF188038),
                borderColor = Color(0xFF81C995)
            )
            "Class 8" -> ClassCardStyle(
                bgBrush = Brush.horizontalGradient(listOf(Color(0xFFFCE7F3), Color(0xFFFFF1F2))),
                iconBgColor = Color(0xFFFBCFE8),
                iconColor = Color(0xFFBE185D),
                titleColor = Color(0xFF831843),
                descColor = Color(0xFFDB2777),
                borderColor = Color(0xFFF472B6)
            )
            "Class 9" -> ClassCardStyle(
                bgBrush = Brush.horizontalGradient(listOf(Color(0xFFFFEDD5), Color(0xFFFFF8E7))),
                iconBgColor = Color(0xFFFED7AA),
                iconColor = Color(0xFFC2410C),
                titleColor = Color(0xFF7C2D12),
                descColor = Color(0xFFEA580C),
                borderColor = Color(0xFFFDBA74)
            )
            "Class 10" -> ClassCardStyle(
                bgBrush = Brush.horizontalGradient(listOf(Color(0xFFEDE9FE), Color(0xFFF5F3FF))),
                iconBgColor = Color(0xFFDDD6FE),
                iconColor = Color(0xFF6D28D9),
                titleColor = Color(0xFF4C1D95),
                descColor = Color(0xFF7C3AED),
                borderColor = Color(0xFFC4B5FD)
            )
            else -> ClassCardStyle(
                bgBrush = Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface)),
                iconBgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                iconColor = MaterialTheme.colorScheme.primary,
                titleColor = MaterialTheme.colorScheme.primary,
                descColor = MaterialTheme.colorScheme.onSurfaceVariant,
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
            )
        }
    } else {
        val darkSurface = MaterialTheme.colorScheme.surface
        val darkOutline = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        when (className) {
            "Class 6" -> ClassCardStyle(
                bgBrush = Brush.horizontalGradient(listOf(darkSurface, darkSurface)),
                iconBgColor = Color(0xFF0369A1).copy(alpha = 0.2f),
                iconColor = Color(0xFF38BDF8),
                titleColor = Color(0xFFF8FAFC),
                descColor = Color(0xFF94A3B8),
                borderColor = darkOutline
            )
            "Class 7" -> ClassCardStyle(
                bgBrush = Brush.horizontalGradient(listOf(darkSurface, darkSurface)),
                iconBgColor = Color(0xFF137333).copy(alpha = 0.2f),
                iconColor = Color(0xFF34D399),
                titleColor = Color(0xFFF8FAFC),
                descColor = Color(0xFF94A3B8),
                borderColor = darkOutline
            )
            "Class 8" -> ClassCardStyle(
                bgBrush = Brush.horizontalGradient(listOf(darkSurface, darkSurface)),
                iconBgColor = Color(0xFFBE185D).copy(alpha = 0.2f),
                iconColor = Color(0xFFF472B6),
                titleColor = Color(0xFFF8FAFC),
                descColor = Color(0xFF94A3B8),
                borderColor = darkOutline
            )
            "Class 9" -> ClassCardStyle(
                bgBrush = Brush.horizontalGradient(listOf(darkSurface, darkSurface)),
                iconBgColor = Color(0xFFC2410C).copy(alpha = 0.2f),
                iconColor = Color(0xFFFB923C),
                titleColor = Color(0xFFF8FAFC),
                descColor = Color(0xFF94A3B8),
                borderColor = darkOutline
            )
            "Class 10" -> ClassCardStyle(
                bgBrush = Brush.horizontalGradient(listOf(darkSurface, darkSurface)),
                iconBgColor = Color(0xFF6D28D9).copy(alpha = 0.2f),
                iconColor = Color(0xFFA78BFA),
                titleColor = Color(0xFFF8FAFC),
                descColor = Color(0xFF94A3B8),
                borderColor = darkOutline
            )
            else -> ClassCardStyle(
                bgBrush = Brush.horizontalGradient(listOf(darkSurface, darkSurface)),
                iconBgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                iconColor = MaterialTheme.colorScheme.primary,
                titleColor = MaterialTheme.colorScheme.primary,
                descColor = MaterialTheme.colorScheme.onSurfaceVariant,
                borderColor = darkOutline
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesSelectionScreen(
    viewModel: MathsViewModel,
    onClassSelected: (String) -> Unit
) {
    val chaptersList by viewModel.chapters.collectAsStateWithLifecycle()
    var searchInput by remember { mutableStateOf("") }
    val classesList = listOf("Class 6", "Class 7", "Class 8", "Class 9", "Class 10")
    val classes = classesList.map { className ->
        val count = chaptersList.count { it.targetClass == className }
        val suffix = if (count == 1) "Chapter" else "Chapters"
        Pair(className, "$count $suffix")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Classes",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Select your class to continue learning",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val filteredClasses = classes.filter {
                searchInput.isEmpty() || itemMatchesSearch(it.first, searchInput)
            }
            items(filteredClasses) { item ->
                val cardStyle = getClassCardStyle(item.first)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onClassSelected(item.first) }
                        .background(cardStyle.bgBrush)
                        .border(1.dp, cardStyle.borderColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(cardStyle.iconBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.List, contentDescription = "book", tint = cardStyle.iconColor)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = item.first,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = cardStyle.titleColor
                            )
                            Text(
                                text = item.second,
                                style = MaterialTheme.typography.bodyMedium,
                                color = cardStyle.descColor
                            )
                        }
                    }
                    Icon(Icons.Default.ArrowForward, contentDescription = "arrow", tint = cardStyle.iconColor)
                }
            }
        }
    }
}

fun itemMatchesSearch(value: String, query: String): Boolean {
    return value.contains(query, ignoreCase = true)
}

// =========================================================================
// CLASS HOMEPAGE (CHAPTERS LIST)
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassChaptersScreen(
    className: String,
    viewModel: MathsViewModel,
    onNavigateToChapterHub: (String) -> Unit
) {
    var searchInput by remember { mutableStateOf("") }
    val chaptersList by viewModel.chapters.collectAsStateWithLifecycle()

    val displayChapters = remember(chaptersList, className) {
        chaptersList.filter { ch ->
            val classes = ch.targetClass.split(",").map { it.trim().lowercase() }
            classes.contains(className.lowercase())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = className,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Select chapter to study notes & quizzes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchInput,
            onValueChange = { searchInput = it },
            placeholder = { Text("Search chapter...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "search") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val filteredChapters = displayChapters.filter {
                searchInput.isEmpty() || it.name.contains(searchInput, ignoreCase = true)
            }
            if (filteredChapters.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "No Chapters",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No chapters registered yet.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Suresh Sir will add chapters for this class shortly.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredChapters) { ch ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onNavigateToChapterHub("Chapter ${ch.chapterNumber} - ${ch.name}") }
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${ch.chapterNumber}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = ch.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = "arrow", tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

// =========================================================================
// CHAPTER HUB (NOTES, WORKSHEETS, QUIZ SUB-NAVIGATION)
// =========================================================================
@Composable
fun ChapterHubScreen(
    className: String,
    chapterDetails: String,
    onNavigateToSection: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = chapterDetails.substringAfter(" - "),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$className - ${chapterDetails.substringBefore(" - ")}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Hero illustration placeholder card
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Surface(
                        color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Active Chapter",
                            color = Color(0xFF2E7D32),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Build Solid Concepts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // HUB ACTIONS
        HubActionCard(
            title = "Notes",
            subtitle = "Read important concepts and theory",
            icon = Icons.Default.List,
            color = MaterialTheme.colorScheme.primary,
            onClick = { onNavigateToSection("Notes") }
        )

        HubActionCard(
            title = "Worksheets",
            subtitle = "Practice questions and improve your skills",
            icon = Icons.Default.Edit,
            color = Color(0xFF2E7D32),
            onClick = { onNavigateToSection("Worksheets") }
        )

        HubActionCard(
            title = "Quiz",
            subtitle = "Test your knowledge with chapter quizzes",
            icon = Icons.Default.Info,
            color = Color(0xFFF59E0B),
            onClick = { onNavigateToSection("Quiz") }
        )

    }
}

@Composable
fun HubActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        Icon(Icons.Default.ArrowForward, contentDescription = "arrow", tint = MaterialTheme.colorScheme.outline)
    }
}

// =========================================================================
// NOTES SCREEN WITH DRIVE FILE SIMULATED VIEWER
// =========================================================================
@Composable
fun NotesScreen(
    className: String,
    chapterDetails: String,
    viewModel: MathsViewModel,
    onBack: () -> Unit
) {
    var activeTab by rememberSaveable { mutableStateOf("All") }
    var selectedNoteId by rememberSaveable { mutableStateOf<Int?>(null) }
    var isHeaderVisible by rememberSaveable { mutableStateOf(true) }

    val user by viewModel.activeUser.collectAsStateWithLifecycle()
    val allPublishedMaterials by viewModel.allPublishedMaterials.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    val filteredNotes = remember(allPublishedMaterials, className, chapterDetails, activeTab) {
        val cleanChapterName = if (chapterDetails.contains(" - ")) chapterDetails.substringAfter(" - ").trim() else chapterDetails.trim()
        val list = allPublishedMaterials.filter {
            it.materialType == "NOTES" &&
            it.targetClass.lowercase() == className.lowercase() &&
            (it.chapterName.lowercase() == chapterDetails.lowercase() || 
             it.chapterName.lowercase() == cleanChapterName.lowercase() ||
             chapterDetails == "All Chapters")
        }
        if (activeTab == "All") {
            list
        } else {
            list.filter {
                it.subCategory.lowercase().contains(activeTab.lowercase()) ||
                it.title.lowercase().contains(activeTab.lowercase())
            }
        }
    }

    val displayNotes = filteredNotes
    val selectedNote = displayNotes.find { it.id == selectedNoteId }

    BackHandler {
        if (selectedNoteId != null) {
            selectedNoteId = null
            isHeaderVisible = true
        } else {
            onBack()
        }
    }

    if (selectedNote != null) {
        val currentNote = selectedNote
        DocumentViewer(
            driveUrl = currentNote.driveUrl,
            isHeaderVisible = isHeaderVisible,
            onToggleHeader = { isHeaderVisible = !isHeaderVisible },
            onBack = {
                selectedNoteId = null
                isHeaderVisible = true
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "$chapterDetails Notes",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Select study sheet to open",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Horizontal tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Concept", "Formulas", "Summary").forEach { tab ->
                    val selected = activeTab == tab
                    Button(
                        onClick = { activeTab = tab },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.border(
                            1.dp,
                            if (selected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            RoundedCornerShape(20.dp)
                        )
                    ) {
                        Text(tab)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (displayNotes.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "No Notes",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No study notes available for this category yet.",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Suresh Sir will upload official revision sheets and concept notes shortly.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(displayNotes, key = { it.id }) { note ->
                        val isLocked = false
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    if (isLocked) {
                                        // locked
                                    } else {
                                        viewModel.incrementNotesCount()
                                        selectedNoteId = note.id
                                    }
                                }
                                .background(MaterialTheme.colorScheme.surface)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.05f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(
                                                alpha = 0.05f
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.List,
                                        tint = MaterialTheme.colorScheme.primary,
                                        contentDescription = "doc"
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = note.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = if (note.description.isNotBlank() && note.description.length < 100) {
                                            note.description
                                        } else {
                                            note.subCategory
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                            if (isLocked) {
                                Icon(Icons.Default.Lock, contentDescription = "lock", tint = Color(0xFFF59E0B))
                            } else {
                                Icon(Icons.Default.ArrowForward, contentDescription = "arrow", tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// WORKSHEETS LIST SCREEN
// =========================================================================
@Composable
fun WorksheetsScreen(
    className: String,
    chapterDetails: String,
    viewModel: MathsViewModel,
    onBack: () -> Unit,
    onDocumentOpen: (Boolean) -> Unit = {}
) {
    var selectedWorksheetId by rememberSaveable { mutableStateOf<Int?>(null) }
    var isHeaderVisible by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(selectedWorksheetId) {
        onDocumentOpen(selectedWorksheetId != null)
    }

    BackHandler {
        if (selectedWorksheetId != null) {
            selectedWorksheetId = null
            isHeaderVisible = true
        } else {
            onBack()
        }
    }

    val user by viewModel.activeUser.collectAsStateWithLifecycle()
    val allPublishedMaterials by viewModel.allPublishedMaterials.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var checkedDownloadIndex by remember { mutableStateOf<Int?>(null) }
    val uriHandler = LocalUriHandler.current

    val filteredWorksheets = remember(allPublishedMaterials, className, chapterDetails) {
        val cleanChapterName = if (chapterDetails.contains(" - ")) chapterDetails.substringAfter(" - ").trim() else chapterDetails.trim()
        allPublishedMaterials.filter {
            it.materialType == "WORKSHEET" &&
            it.targetClass.lowercase() == className.lowercase() &&
            (it.chapterName.lowercase() == chapterDetails.lowercase() || 
             it.chapterName.lowercase() == cleanChapterName.lowercase() ||
             chapterDetails == "All Chapters")
        }
    }

    val displayWorksheets = filteredWorksheets
    val selectedWorksheet = displayWorksheets.find { it.id == selectedWorksheetId }

    if (selectedWorksheet != null) {
        DocumentViewer(
            driveUrl = selectedWorksheet.driveUrl,
            isHeaderVisible = isHeaderVisible,
            onToggleHeader = { isHeaderVisible = !isHeaderVisible },
            onBack = {
                selectedWorksheetId = null
                isHeaderVisible = true
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "$chapterDetails Worksheets",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Practice questions and improve math speed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (displayWorksheets.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "No Worksheets",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No worksheets available for this chapter yet.",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Suresh Sir will upload official practice questions and sheets shortly.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(displayWorksheets.size) { index ->
                    val ws = displayWorksheets[index]
                    val isLocked = false
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (!isLocked) {
                                    viewModel.incrementWorksheetsCount()
                                    if (ws.driveUrl.isNotBlank()) {
                                        selectedWorksheetId = ws.id
                                    }
                                }
                            }
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.05f),
                                RoundedCornerShape(16.dp)
                              )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.05f
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    tint = MaterialTheme.colorScheme.primary,
                                    contentDescription = "ws"
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = ws.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = if (ws.description.isNotBlank()) ws.description else ws.subCategory,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        if (isLocked) {
                            Icon(Icons.Default.Lock, contentDescription = "locked", tint = Color(0xFFF59E0B))
                        } else {
                            IconButton(
                                onClick = {
                                    if (checkedDownloadIndex != index) {
                                        viewModel.incrementWorksheetsCount()
                                        scope.launch {
                                            checkedDownloadIndex = index
                                            try {
                                                if (ws.driveUrl.isNotBlank()) {
                                                    uriHandler.openUri(ws.driveUrl)
                                                }
                                            } catch (e: Exception) {
                                                // ignore
                                            }
                                            kotlinx.coroutines.delay(2500)
                                            checkedDownloadIndex = null
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (checkedDownloadIndex == index) Icons.Default.CheckCircle else Icons.Default.Download,
                                    tint = if (checkedDownloadIndex == index) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                    contentDescription = "download"
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        // Tip card
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = "bulb", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tip: Solve at least 2 worksheets per chapter to improve your mental math speed!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    }
}

// =========================================================================
// QUIZ ATTEMPT & SCREEN MODES
// =========================================================================
@Composable
fun QuizScreen(
    className: String,
    chapterDetails: String,
    viewModel: MathsViewModel,
    onBack: () -> Unit
) {
    if (true) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Coming Soon",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Quizzes Coming Soon",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "We are working hard to bring you interactive quizzes for $chapterDetails.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onBack) {
                Text("Go Back")
            }
        }
        return
    }

    val quizQuestions by viewModel.quizQuestions.collectAsStateWithLifecycle()
    var quizStarted by remember { mutableStateOf(false) }
    val user by viewModel.activeUser.collectAsStateWithLifecycle()

    var activeDifficulty by remember { mutableStateOf("Easy") }

    // Quiz Playing Session Fields
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var scoreValue by remember { mutableStateOf(0) }
    var answerChecked by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }
    var quizCompleted by remember { mutableStateOf(false) }

    BackHandler {
        if (quizStarted) {
            quizStarted = false
            quizCompleted = false
            currentQuestionIndex = 0
        } else {
            onBack()
        }
    }

    val difficultiesList = listOf(
        Triple("Easy Quiz", "10 Questions • 5 mins", false),
        Triple("Medium Quiz", "15 Questions • 10 mins", false),
        Triple("Hard Quiz", "20 Questions • 15 mins", false),
        Triple("Chapter Test", "25 Questions • 20 mins", false),
        Triple("Olympiad Prep", "Advanced competitive prep", true)
    )

    if (quizCompleted) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "victory",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Quiz Completed!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$chapterDetails - $activeDifficulty Difficulty",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$scoreValue / ${quizQuestions.size}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Total Correct Answers")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    viewModel.incrementQuizzesCount()
                    quizStarted = false
                    quizCompleted = false
                    currentQuestionIndex = 0
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Return to Quiz List")
            }
        }
    } else if (quizStarted && quizQuestions.isNotEmpty()) {
        val currentQuestion = quizQuestions[currentQuestionIndex]

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        quizStarted = false
                        quizCompleted = false
                        currentQuestionIndex = 0
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "back to quiz selector")
                    }
                    Text(
                        text = "Question ${currentQuestionIndex + 1} of ${quizQuestions.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text("Score: $scoreValue", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            // progress indicator
            LinearProgressIndicator(
                progress = (currentQuestionIndex + 1).toFloat() / quizQuestions.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )

            // Question Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = currentQuestion.questionText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Options List
            val options = listOf(
                Pair("A", currentQuestion.optionA),
                Pair("B", currentQuestion.optionB),
                Pair("C", currentQuestion.optionC),
                Pair("D", currentQuestion.optionD)
            )

            options.forEach { opt ->
                val isSelected = selectedAnswer == opt.first
                val isCorrect = opt.first == currentQuestion.correctAnswer
                val isWrongAndSelected = isSelected && !isCorrect

                val cardColor = when {
                    answerChecked && isCorrect -> Color(0xFF2E7D32).copy(alpha = 0.15f)
                    answerChecked && isWrongAndSelected -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surface
                }

                val borderColor = when {
                    answerChecked && isCorrect -> Color(0xFF2E7D32)
                    answerChecked && isWrongAndSelected -> MaterialTheme.colorScheme.error
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { if (!answerChecked) selectedAnswer = opt.first }
                        .background(cardColor)
                        .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${opt.first}. ${opt.second}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (answerChecked && isCorrect) {
                        Icon(Icons.Default.Check, "correct", tint = Color(0xFF2E7D32))
                    } else if (answerChecked && isWrongAndSelected) {
                        Icon(Icons.Default.Close, "wrong", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (showExplanation) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Explanation:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(currentQuestion.explanation, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Bottom Flow controller
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!answerChecked) {
                    Button(
                        onClick = {
                            if (selectedAnswer != null) {
                                answerChecked = true
                                showExplanation = true
                                if (selectedAnswer == currentQuestion.correctAnswer) {
                                    scoreValue++
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedAnswer != null
                    ) {
                        Text("Check Answer")
                    }
                } else {
                    Button(
                        onClick = {
                            if (currentQuestionIndex + 1 < quizQuestions.size) {
                                currentQuestionIndex++
                                selectedAnswer = null
                                answerChecked = false
                                showExplanation = false
                            } else {
                                quizCompleted = true
                             }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (currentQuestionIndex + 1 == quizQuestions.size) "Show Score" else "Next Question")
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "$chapterDetails Quizzes",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Choose the difficulty tier to test math speed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(difficultiesList) { item ->
                    val isLocked = false
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (!isLocked) {
                                    val dif = item.first.substringBefore(" Quiz")
                                    activeDifficulty = dif
                                    viewModel.startQuizSession(className, chapterDetails, dif)
                                    quizStarted = true
                                }
                            }
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.05f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.05f
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    tint = MaterialTheme.colorScheme.primary,
                                    contentDescription = "level"
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.first,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = item.second,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        if (isLocked) {
                            Icon(Icons.Default.Lock, contentDescription = "locked", tint = Color(0xFFF59E0B))
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = "start", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// INTERACTIVE LEARNING BENTO & STREAK
// =========================================================================
@Composable
fun InteractiveLearningScreen(
    viewModel: MathsViewModel,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Coming Soon",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Interactive Learning",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Interactive games and challenges are coming soon!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBack) {
            Text("Go Back")
        }
    }
}

// =========================================================================
// MODEL PAPERS SCREEN
// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelPapersScreen(
    viewModel: MathsViewModel,
    onBack: () -> Unit,
    onDocumentOpen: (Boolean) -> Unit = {}
) {
    var selectedClass by remember { mutableStateOf<String?>(null) }
    var searchInput by remember { mutableStateOf("") }
    val user by viewModel.activeUser.collectAsStateWithLifecycle()
    val allPublishedMaterials by viewModel.allPublishedMaterials.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    var selectedPaperId by rememberSaveable { mutableStateOf<Int?>(null) }
    var isHeaderVisible by rememberSaveable { mutableStateOf(true) }
    var checkedDownloadId by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedPaperId) {
        onDocumentOpen(selectedPaperId != null)
    }

    val modelPapers = allPublishedMaterials.filter { it.materialType == "MODEL_PAPER" }
    val selectedPaper = modelPapers.find { it.id == selectedPaperId }

    // Intercept back actions while a class is selected
    BackHandler(enabled = selectedClass != null || selectedPaperId != null) {
        if (selectedPaperId != null) {
            selectedPaperId = null
            isHeaderVisible = true
        } else {
            selectedClass = null
        }
    }

    if (selectedPaper != null) {
        DocumentViewer(
            driveUrl = selectedPaper.driveUrl,
            isHeaderVisible = isHeaderVisible,
            onToggleHeader = { isHeaderVisible = !isHeaderVisible },
            onBack = {
                selectedPaperId = null
                isHeaderVisible = true
            }
        )
    } else {
        Crossfade(targetState = selectedClass, label = "model_papers_navigation") { currentSelectedClass ->
        if (currentSelectedClass == null) {
            // STATE A: CLASS SELECTION
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Model Papers",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Select a class to practice with model papers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                val classesList = listOf("Class 6", "Class 7", "Class 8", "Class 9", "Class 10")

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(classesList) { className ->
                        val count = modelPapers.count { it.targetClass.equals(className, ignoreCase = true) }
                        val paperText = if (count == 1) "1 Model Paper Available" else "$count Model Papers Available"
                        val cardStyle = getClassCardStyle(className)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedClass = className }
                                .background(cardStyle.bgBrush)
                                .border(
                                    1.dp,
                                    cardStyle.borderColor.copy(alpha = 0.4f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(cardStyle.iconBgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.List,
                                        contentDescription = "papers_list",
                                        tint = cardStyle.iconColor
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = className,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = cardStyle.titleColor
                                    )
                                    Text(
                                        text = paperText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = cardStyle.descColor
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = "Open class papers",
                                tint = cardStyle.iconColor
                            )
                        }
                    }
                }
            }
        } else {
            // STATE B: PAPERS UNDER SELECTED CLASS
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { selectedClass = null },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back to classes",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text(
                            text = "$currentSelectedClass Papers",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Practice with model question papers and keys",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Search field for papers within the selected class
                OutlinedTextField(
                    value = searchInput,
                    onValueChange = { searchInput = it },
                    placeholder = { Text("Search papers in this class...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "search") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                val filteredPapers = modelPapers.filter {
                    it.targetClass.equals(currentSelectedClass, ignoreCase = true) &&
                            (searchInput.isEmpty() || it.title.contains(searchInput, ignoreCase = true))
                }

                if (filteredPapers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Empty",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No papers available",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "Model papers specifically designed for $currentSelectedClass are being compiled by Suresh Maths.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredPapers) { paper ->
                            val isLocked = false
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        if (!isLocked) {
                                            viewModel.incrementWorksheetsCount()
                                            if (paper.driveUrl.isNotBlank()) {
                                                selectedPaperId = paper.id
                                            }
                                        }
                                    }
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.05f),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.List,
                                            tint = MaterialTheme.colorScheme.primary,
                                            contentDescription = "doc"
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = paper.subCategory ?: "Model Paper",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                        Text(
                                            text = paper.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                if (isLocked) {
                                    Icon(Icons.Default.Lock, contentDescription = "locked", tint = Color(0xFFF59E0B))
                                } else {
                                    IconButton(onClick = {
                                        if (checkedDownloadId != paper.id) {
                                            viewModel.incrementWorksheetsCount()
                                            scope.launch {
                                                checkedDownloadId = paper.id
                                                try {
                                                    if (paper.driveUrl.isNotBlank()) {
                                                        uriHandler.openUri(paper.driveUrl)
                                                    }
                                                } catch (e: Exception) {
                                                    // ignore
                                                }
                                                kotlinx.coroutines.delay(2500)
                                                checkedDownloadId = null
                                            }
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if (checkedDownloadId == paper.id) Icons.Default.CheckCircle else Icons.Default.Download,
                                            tint = if (checkedDownloadId == paper.id) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                                            contentDescription = "download"
                                        )
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
}

// =========================================================================
// USER PROFILE SCREEN (STATISTICS & UPI VERIFICATION PORTAL)
@Composable
fun ProfileScreen(
    viewModel: MathsViewModel,
    onNavigateToHome: () -> Unit
) {
    val activeUser by viewModel.activeUser.collectAsStateWithLifecycle()
    var upiRefInput by remember { mutableStateOf("") }
    var notificationMsg by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Sathwik header
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = "profile", tint = Color.White, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                activeUser?.name ?: "User",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                activeUser?.email ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "badge",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = activeUser?.role ?: "STUDENT",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Views Count Card grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatsDisplayCard(
                metric = "${activeUser?.notesViewedCount ?: 24}",
                label = "Notes Viewed",
                icon = Icons.Default.List,
                modifier = Modifier.weight(1f)
            )
            StatsDisplayCard(
                metric = "${activeUser?.worksheetsDownloadedCount ?: 12}",
                label = "Worksheets",
                icon = Icons.Default.List,
                modifier = Modifier.weight(1f)
            )
            StatsDisplayCard(
                metric = "${activeUser?.quizzesCompletedCount ?: 8}",
                label = "Quizzes Finished",
                icon = Icons.Default.Star,
                modifier = Modifier.weight(1f)
            )
        }



        // Notification center message
        if (notificationMsg != null) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Info, contentDescription = "info", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(notificationMsg!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        // Support Contact Section
        Text("Support & Contact", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        var supportMessage by remember { mutableStateOf("") }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            val context = androidx.compose.ui.platform.LocalContext.current
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = supportMessage,
                    onValueChange = { supportMessage = it },
                    placeholder = { Text("Type your message here...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            try {
                                val url = "https://api.whatsapp.com/send?phone=916301205264&text=${java.net.URLEncoder.encode(supportMessage, "UTF-8")}"
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    data = android.net.Uri.parse(url)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {}
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("WhatsApp", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                    data = android.net.Uri.parse("mailto:sureshmathsmaterial@gmail.com")
                                    putExtra(android.content.Intent.EXTRA_SUBJECT, "Support Request: Suresh Maths Material App")
                                    putExtra(android.content.Intent.EXTRA_TEXT, supportMessage)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {}
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Email", maxLines = 1)
                    }
                }
            }
        }

        // Switch to Admin Dashboard
        if (activeUser?.isAdmin == true) {
            val isCurrentlyAdmin = activeUser?.role == "ADMIN"
            val buttonText = if (isCurrentlyAdmin) "Switch to Student View" else "Switch to Admin Dashboard"
            Button(
                onClick = {
                    viewModel.toggleAdminRole(activeUser?.email ?: "", activeUser?.role ?: "STUDENT")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(if (isCurrentlyAdmin) Icons.Default.Person else Icons.Default.Settings, contentDescription = "Toggle View")
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonText, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Log out button
        Button(
            onClick = {
                viewModel.logoutUser()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = "logout")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out from Suresh Maths", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatsDisplayCard(
    metric: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(10.dp))
            Text(metric, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

fun borderStrokeVal(color: Color) = androidx.compose.foundation.BorderStroke(2.dp, color)

@Composable
fun DocumentViewer(
    driveUrl: String,
    isHeaderVisible: Boolean,
    onToggleHeader: () -> Unit,
    onBack: () -> Unit
) {
    val previewUrl = if (driveUrl.contains("drive.google.com")) {
        val url = driveUrl
        if (url.contains("/view")) {
            url.substringBeforeLast("/view") + "/preview?rm=minimal"
        } else if (url.contains("/edit")) {
            url.substringBeforeLast("/edit") + "/preview?rm=minimal"
        } else {
            url
        }
    } else if (driveUrl.contains("firebasestorage.googleapis.com")) {
        "https://docs.google.com/gview?embedded=true&url=${java.net.URLEncoder.encode(driveUrl, "UTF-8")}"
    } else {
        driveUrl
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (driveUrl.isNotBlank()) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (driveUrl.startsWith("data:application/pdf;base64,") || driveUrl.startsWith("local_pdf:") || driveUrl.contains("firebasestorage")) {
                    LocalPdfViewer(pdfData = driveUrl, modifier = Modifier.fillMaxSize())
                } else {
                    AndroidView(
                        factory = { context ->
                            android.webkit.WebView(context).apply {
                                settings.javaScriptEnabled = true
                            settings.builtInZoomControls = true
                            settings.displayZoomControls = false
                            settings.setSupportZoom(true)
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            
                            addJavascriptInterface(object {
                                @android.webkit.JavascriptInterface
                                fun toggleHeader() {
                                    onToggleHeader()
                                }
                            }, "Android")

                            webViewClient = object : android.webkit.WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: android.webkit.WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                                    val requestUrl = request?.url?.toString() ?: ""
                                    if (requestUrl != previewUrl && !requestUrl.contains("googleusercontent.com") && !requestUrl.contains("docs.google.com")) {
                                        return true // Prevent navigation
                                    }
                                    return super.shouldOverrideUrlLoading(view, request)
                                }
                                
                                override fun onPageFinished(view: android.webkit.WebView, url: String) {
                                    super.onPageFinished(view, url)
                                    val js = """
                                        javascript:(function() {
                                            var style = document.createElement('style');
                                            style.innerHTML = '.ndfHFb-c4YZDc-Wrql6b, .ndfHFb-c4YZDc-GSGtqb, [title="Pop-out"], .ndfHFb-c8obeo-CJAk1b, .ndfHFb-c4YZDc-mNS1-H9tDt, .drive-viewer-header, .drive-viewer-footer { display: none !important; }';
                                            document.head.appendChild(style);
                                            
                                            var meta = document.createElement('meta');
                                            meta.name = 'viewport';
                                            meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes';
                                            document.getElementsByTagName('head')[0].appendChild(meta);
                                            
                                            document.body.addEventListener('click', function() {
                                                Android.toggleHeader();
                                            });
                                            
                                            /* Also try to hook into iframe clicks */
                                            var iframes = document.getElementsByTagName('iframe');
                                            for (var i = 0; i < iframes.length; i++) {
                                                iframes[i].onload = function() {
                                                    this.contentWindow.document.body.addEventListener('click', function() {
                                                        Android.toggleHeader();
                                                    });
                                                };
                                            }
                                        })();
                                    """.trimIndent()
                                    view.evaluateJavascript(js, null)
                                }
                            }
                            loadUrl(previewUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No document link available")
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isHeaderVisible,
                enter = androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.fadeOut()
            ) {
                SmallFloatingActionButton(
                    onClick = onBack,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "back")
                }
            }
        }
    }
}

package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserProgress(
    @PrimaryKey val email: String,
    val name: String,
    val role: String = "STUDENT", // "STUDENT" or "ADMIN"
    val isPremium: Boolean = false,
    val subscriptionExpiry: String = "",
    val notesViewedCount: Int = 0,
    val worksheetsDownloadedCount: Int = 0,
    val quizzesCompletedCount: Int = 0,
    val savedDownloads: String = "", // Comma-separated file names
    val password: String = "password", // Clear text password for simple local login demo
    val targetClass: String = "Class 7", // e.g. "Class 6", "Class 7", "Class 8", "Class 9", "Class 10"
    val isVerified: Boolean = false, // Admin manual verification status
    val phoneNumber: String = "", // Phone number for custom user registration
    val isAdmin: Boolean? = null, // only this need to have both acces to student and admin
    val status: String = "TEMPORARY" // "TEMPORARY", "APPROVED", "BLOCKED"
)

@Entity(tableName = "materials")
data class StudyMaterial(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetClass: String, // "Class 6", "Class 7", "Class 8", "Class 9", "Class 10"
    val chapterName: String, // e.g. "Integers", "Fractions"
    val materialType: String, // "NOTES", "WORKSHEET", "QUIZ", "MODEL_PAPER"
    val subCategory: String = "Concept Notes", // "Concept Notes", "Formula Sheet", "Summary", "Quarterly", "Annual", etc.
    val isPremium: Boolean = false,
    val driveUrl: String = "",
    val description: String = "",
    val publishedTime: Long = System.currentTimeMillis(),
    val isPublished: Boolean = true
)

@Entity(tableName = "quizzes")
data class QuizConfig(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val targetClass: String,
    val chapterName: String,
    val difficulty: String, // "Easy", "Medium", "Hard", "Chapter Test", "Olympiad Prep"
    val questionsCount: Int,
    val durationMinutes: Int
)

@Entity(tableName = "quiz_questions")
data class QuizQuestion(
    @PrimaryKey(autoGenerate = false) val id: String, // compound key e.g. "7_fractions_easy_1"
    val targetClass: String,
    val chapterName: String,
    val difficulty: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String, // "A", "B", "C", "D"
    val explanation: String
)

@Entity(tableName = "payment_requests")
data class PaymentRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentName: String,
    val studentEmail: String,
    val planName: String = "Premium Subscription",
    val amount: String = "₹199",
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val transactionRef: String = "", // UPI Ref No
    val dateString: String = "",
    val screenshotAsset: String = "" // Placeholder name
)

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val priority: String = "NORMAL", // "NORMAL", "IMPORTANT", "URGENT"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "interactive_streaks")
data class InteractiveStreak(
    @PrimaryKey val userEmail: String,
    val streakDays: Int = 12,
    val mentalMathHighScore: Int = 0,
    val puzzlesSolved: Int = 0,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chapters")
data class Chapter(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetClass: String, // Comma-separated or single, e.g. "Class 6", "Class 7", or "Class 6, Class 7"
    val chapterNumber: Int = 1
)

package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// --- USER ACCESS ---
@Dao
interface UserProgressDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserProgress?

    @Query("SELECT * FROM users ORDER BY email DESC")
    fun getAllStudentsFlow(): Flow<List<UserProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProgress)

    @Query("UPDATE users SET isPremium = :isPremium, subscriptionExpiry = :expiry WHERE email = :email")
    suspend fun updateSubscription(email: String, isPremium: Boolean, expiry: String)

    @Query("UPDATE users SET notesViewedCount = notesViewedCount + 1 WHERE email = :email")
    suspend fun incrementNotesViewed(email: String)

    @Query("UPDATE users SET worksheetsDownloadedCount = worksheetsDownloadedCount + 1 WHERE email = :email")
    suspend fun incrementWorksheetsDownloaded(email: String)

    @Query("UPDATE users SET quizzesCompletedCount = quizzesCompletedCount + 1 WHERE email = :email")
    suspend fun incrementQuizzesCompleted(email: String)

    @Query("DELETE FROM users WHERE email = :email")
    suspend fun deleteUser(email: String)

    @Query("UPDATE users SET isVerified = :isVerified WHERE email = :email")
    suspend fun updateVerification(email: String, isVerified: Boolean)

    @Query("UPDATE users SET status = :status WHERE email = :email")
    suspend fun updateStatus(email: String, status: String)
}

// --- MATERIALS ACCESS ---
@Dao
interface StudyMaterialDao {
    @Query("SELECT * FROM materials WHERE isPublished = 1 ORDER BY publishedTime DESC")
    fun getAllPublishedMaterials(): Flow<List<StudyMaterial>>

    @Query("SELECT * FROM materials ORDER BY publishedTime DESC")
    fun getAllMaterialsFlow(): Flow<List<StudyMaterial>>

    @Query("SELECT * FROM materials ORDER BY publishedTime DESC")
    suspend fun getAllMaterials(): List<StudyMaterial>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: StudyMaterial)

    @Query("SELECT * FROM materials WHERE id = :id LIMIT 1")
    suspend fun getMaterialById(id: Int): StudyMaterial?

    @Query("DELETE FROM materials WHERE id = :id")
    suspend fun deleteMaterialById(id: Int)

    @Query("DELETE FROM materials")
    suspend fun deleteAllMaterials()
}

// --- QUIZ QUESTIONS ACCESS ---
@Dao
interface QuizQuestionDao {
    @Query("SELECT * FROM quiz_questions WHERE targetClass = :className AND chapterName = :chapterName AND difficulty = :difficulty")
    suspend fun getQuestions(className: String, chapterName: String, difficulty: String): List<QuizQuestion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuizQuestion)
}

// --- UPI PAYMENT VERIFICATION ACCESS ---
@Dao
interface PaymentRequestDao {
    @Query("SELECT * FROM payment_requests ORDER BY id DESC")
    fun getAllPaymentRequests(): Flow<List<PaymentRequest>>

    @Query("SELECT * FROM payment_requests WHERE id = :id LIMIT 1")
    suspend fun getPaymentRequestById(id: Int): PaymentRequest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentRequest(request: PaymentRequest)

    @Query("UPDATE payment_requests SET status = :status WHERE id = :id")
    suspend fun updatePaymentRequestStatus(id: Int, status: String)
}

// --- ANNOUNCEMENTS ACCESS ---
@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY timestamp DESC")
    fun getAllAnnouncementsFlow(): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement)
}

// --- STREAK ACCESS ---
@Dao
interface InteractiveStreakDao {
    @Query("SELECT * FROM interactive_streaks WHERE userEmail = :email LIMIT 1")
    suspend fun getStreak(email: String): InteractiveStreak?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: InteractiveStreak)
}

// --- CHAPTERS ACCESS ---
@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters ORDER BY chapterNumber ASC, id ASC")
    fun getAllChaptersFlow(): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters ORDER BY chapterNumber ASC, id ASC")
    suspend fun getAllChapters(): List<Chapter>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: Chapter)

    @Query("SELECT * FROM chapters WHERE id = :id LIMIT 1")
    suspend fun getChapterById(id: Int): Chapter?

    @Query("DELETE FROM chapters WHERE id = :id")
    suspend fun deleteChapterById(id: Int)

    @Query("DELETE FROM chapters")
    suspend fun deleteAllChapters()
}

// --- MAIN DATABASE ---
@Database(
    entities = [
        UserProgress::class,
        StudyMaterial::class,
        QuizConfig::class,
        QuizQuestion::class,
        PaymentRequest::class,
        Announcement::class,
        InteractiveStreak::class,
        Chapter::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProgressDao(): UserProgressDao
    abstract fun studyMaterialDao(): StudyMaterialDao
    abstract fun quizQuestionDao(): QuizQuestionDao
    abstract fun paymentRequestDao(): PaymentRequestDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun interactiveStreakDao(): InteractiveStreakDao
    abstract fun chapterDao(): ChapterDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "suresh_maths_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AchievementsFragment : Fragment() {

    // Firestore and FirebaseAuth instance
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    // UI elements for progress bars and TextViews
    private lateinit var xpProgressBar: ProgressBar
    private lateinit var levelNumberTextView: TextView
    private lateinit var levelProgressText: TextView

    // Achievement UI elements
    private lateinit var bookNookerProgressBar: ProgressBar
    private lateinit var firstChapterProgressBar: ProgressBar
    private lateinit var readingRookieProgressBar: ProgressBar
    private lateinit var storySeekerProgressBar: ProgressBar
    private lateinit var novelNavigatorProgressBar: ProgressBar
    private lateinit var bookEnthusiastProgressBar: ProgressBar
    private lateinit var legendaryLibrarianProgressBar: ProgressBar
    private lateinit var bookGodProgressBar: ProgressBar
    private lateinit var fantasyExplorerProgressBar: ProgressBar
    private lateinit var sciFiVoyagerProgressBar: ProgressBar
    private lateinit var mysterySolverProgressBar: ProgressBar
    private lateinit var romanceEnthusiastProgressBar: ProgressBar

    private lateinit var bookNookerProgressText: TextView
    private lateinit var firstChapterProgressText: TextView
    private lateinit var readingRookieProgressText: TextView
    private lateinit var storySeekerProgressText: TextView
    private lateinit var novelNavigatorProgressText: TextView
    private lateinit var bookEnthusiastProgressText: TextView
    private lateinit var legendaryLibrarianProgressText: TextView
    private lateinit var bookGodProgressText: TextView
    private lateinit var fantasyExplorerProgressText: TextView
    private lateinit var sciFiVoyagerProgressText: TextView
    private lateinit var mysterySolverProgressText: TextView
    private lateinit var romanceEnthusiastProgressText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_achievements, container, false)

        // Initialize UI components
        xpProgressBar = view.findViewById(R.id.xpProgressBar)
        levelNumberTextView = view.findViewById(R.id.level_number)
        levelProgressText = view.findViewById(R.id.level_progress_text)

        // Initialize progress bars and TextViews for achievements
        bookNookerProgressBar = view.findViewById(R.id.book_nooker_progress_bar)
        firstChapterProgressBar = view.findViewById(R.id.first_chapter_progress_bar)
        readingRookieProgressBar = view.findViewById(R.id.reading_rookie_progress_bar)
        storySeekerProgressBar = view.findViewById(R.id.story_seeker_progress_bar)
        novelNavigatorProgressBar = view.findViewById(R.id.Novel_navagator_progress_bar)
        bookEnthusiastProgressBar = view.findViewById(R.id.book_Enthusiast_progress_bar)
        legendaryLibrarianProgressBar = view.findViewById(R.id.legendary_librarian_progress_bar)
        bookGodProgressBar = view.findViewById(R.id.book_destroyer_progress_bar)
        fantasyExplorerProgressBar = view.findViewById(R.id.fantasy_explorer_progress_bar)
        sciFiVoyagerProgressBar = view.findViewById(R.id.sci_fi_voyager_progress_bar)
        mysterySolverProgressBar = view.findViewById(R.id.mystery_solver_progress_bar)
        romanceEnthusiastProgressBar = view.findViewById(R.id.romance_enthusiast_progress_bar)

        // Initialize TextViews for progress descriptions
        bookNookerProgressText = view.findViewById(R.id.book_nooker_progress)
        firstChapterProgressText = view.findViewById(R.id.first_chapter_progress)
        readingRookieProgressText = view.findViewById(R.id.reading_rookie_progress)
        storySeekerProgressText = view.findViewById(R.id.story_seeker_progress)
        novelNavigatorProgressText = view.findViewById(R.id.Novel_navagator_progress)
        bookEnthusiastProgressText = view.findViewById(R.id.book_Enthusiast_progress)
        legendaryLibrarianProgressText = view.findViewById(R.id.legendary_librarian_progress)
        bookGodProgressText = view.findViewById(R.id.book_destroyer_progress)
        fantasyExplorerProgressText = view.findViewById(R.id.fantasy_explorer_progress)
        sciFiVoyagerProgressText = view.findViewById(R.id.sci_fi_voyager_progress)
        mysterySolverProgressText = view.findViewById(R.id.mystery_solver_progress)
        romanceEnthusiastProgressText = view.findViewById(R.id.romance_enthusiast_progress)

        // Load achievements and XP data from Firestore
        userId?.let {
            loadAchievementsData(it)
        }

        return view
    }

    // Load achievements data from Firestore
    private fun loadAchievementsData(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Fetch XP, level, and number of books read from Firestore
                val currentXp = document.getLong("xp")?.toInt() ?: 0
                val currentLevel = document.getLong("level")?.toInt() ?: 1
                val booksRead = document.getLong("booksRead")?.toInt() ?: 0

                // Fetch genre-specific achievements (example: sci-fi, fantasy, etc.)
                val fantasyBooksRead = document.getLong("fantasyBooksRead")?.toInt() ?: 0
                val sciFiBooksRead = document.getLong("sciFiBooksRead")?.toInt() ?: 0
                val mysteryBooksRead = document.getLong("mysteryBooksRead")?.toInt() ?: 0
                val romanceBooksRead = document.getLong("romanceBooksRead")?.toInt() ?: 0

                // Update UI for overall progress
                updateProgressUI(currentXp, currentLevel, booksRead)

                // Update individual achievements progress
                updateAchievementProgress(bookNookerProgressBar, bookNookerProgressText, 1, booksRead, "Welcome to Book Nook!")
                updateAchievementProgress(firstChapterProgressBar, firstChapterProgressText, 1, booksRead, "Finish 1 book")
                updateAchievementProgress(readingRookieProgressBar, readingRookieProgressText, 5, booksRead, "Log 5 books as finished")
                updateAchievementProgress(storySeekerProgressBar, storySeekerProgressText, 10, booksRead, "Log 10 books as finished")
                updateAchievementProgress(novelNavigatorProgressBar, novelNavigatorProgressText, 50, booksRead, "Log 50 books as finished")
                updateAchievementProgress(bookEnthusiastProgressBar, bookEnthusiastProgressText, 100, booksRead, "Log 100 books as finished")
                updateAchievementProgress(legendaryLibrarianProgressBar, legendaryLibrarianProgressText, 500, booksRead, "Read 500 books")
                updateAchievementProgress(bookGodProgressBar, bookGodProgressText, 1000, booksRead, "Read 1000 books")

                // Genre-specific achievements
                updateAchievementProgress(fantasyExplorerProgressBar, fantasyExplorerProgressText, 10, fantasyBooksRead, "Read 10 fantasy books")
                updateAchievementProgress(sciFiVoyagerProgressBar, sciFiVoyagerProgressText, 10, sciFiBooksRead, "Read 10 sci-fi books")
                updateAchievementProgress(mysterySolverProgressBar, mysterySolverProgressText, 10, mysteryBooksRead, "Solve 10 mysteries")
                updateAchievementProgress(romanceEnthusiastProgressBar, romanceEnthusiastProgressText, 10, romanceBooksRead, "Read 10 romance books")
            }
        }.addOnFailureListener { exception ->
            Log.e("Firestore", "Error loading achievements: ", exception)
        }
    }

    // Function to update the overall XP and level progress
    private fun updateProgressUI(currentXp: Int, currentLevel: Int, booksRead: Int) {
        val xpForNextLevel = calculateXpForNextLevel(currentLevel)

        // Update level text and XP progress bar
        levelNumberTextView.text = "Level $currentLevel"
        levelProgressText.text = "$booksRead more books to level $currentLevel"

        // Update XP progress bar percentage
        val xpProgressPercentage = (currentXp.toFloat() / xpForNextLevel * 100).toInt()
        xpProgressBar.progress = xpProgressPercentage
    }

    // Function to calculate the XP needed for the next level
    private fun calculateXpForNextLevel(currentLevel: Int): Int {
        // Example: simple XP calculation based on the current level
        return currentLevel * 100 // This can be adjusted based on your leveling system
    }

    // Function to update the progress of each achievement
    private fun updateAchievementProgress(
        progressBar: ProgressBar,
        progressTextView: TextView,
        maxValue: Int,
        currentValue: Int,
        description: String
    ) {
        progressBar.max = maxValue
        progressBar.progress = currentValue.coerceAtMost(maxValue)
        progressTextView.text = description
    }
}

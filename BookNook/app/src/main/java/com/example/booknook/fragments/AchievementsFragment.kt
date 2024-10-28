package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.ceil

class AchievementsFragment : Fragment() {

    // Firestore and FirebaseAuth instance
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    // UI elements for progress bars and TextViews
    private lateinit var xpProgressBar: ProgressBar
    private lateinit var currentXpTextView: TextView
    private lateinit var xpNeededTextView: TextView
    private lateinit var levelNumberTextView: TextView
    private lateinit var levelProgressText: TextView

    // Achievement UI elements
    private lateinit var bookNookerCard: CardView
    private lateinit var firstChapterCard: CardView
    private lateinit var readingRookieCard: CardView
    private lateinit var storySeekerCard: CardView
    private lateinit var novelNavigatorCard: CardView
    private lateinit var bookEnthusiastCard: CardView
    private lateinit var legendaryLibrarianCard: CardView
    private lateinit var bookGodCard: CardView
    private lateinit var fantasyExplorerCard: CardView
    private lateinit var sciFiVoyagerCard: CardView
    private lateinit var mysterySolverCard: CardView
    private lateinit var romanceEnthusiastCard: CardView

    // Progress bars for achievements
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

    // Progress TextViews for achievements
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
        currentXpTextView = view.findViewById(R.id.currentXpTextView)
        xpNeededTextView = view.findViewById(R.id.xpNeededTextView)

        // Initialize CardViews for achievements
        bookNookerCard = view.findViewById(R.id.book_nooker_card)
        firstChapterCard = view.findViewById(R.id.first_chapter_card)
        readingRookieCard = view.findViewById(R.id.reading_rookie_card)
        storySeekerCard = view.findViewById(R.id.story_seeker_card)
        novelNavigatorCard = view.findViewById(R.id.novel_navigator_card)
        bookEnthusiastCard = view.findViewById(R.id.book_enthusiast_card)
        legendaryLibrarianCard = view.findViewById(R.id.legendary_librarian_card)
        bookGodCard = view.findViewById(R.id.book_god_card)
        fantasyExplorerCard = view.findViewById(R.id.fantasy_explorer_card)
        sciFiVoyagerCard = view.findViewById(R.id.sci_fi_voyager_card)
        mysterySolverCard = view.findViewById(R.id.mystery_solver_card)
        romanceEnthusiastCard = view.findViewById(R.id.romance_enthusiast_card)

        // Initialize progress bars for achievements
        bookNookerProgressBar = view.findViewById(R.id.book_nooker_progress_bar)
        firstChapterProgressBar = view.findViewById(R.id.first_chapter_progress_bar)
        readingRookieProgressBar = view.findViewById(R.id.reading_rookie_progress_bar)
        storySeekerProgressBar = view.findViewById(R.id.story_seeker_progress_bar)
        novelNavigatorProgressBar = view.findViewById(R.id.novel_navigator_progress_bar)
        bookEnthusiastProgressBar = view.findViewById(R.id.book_enthusiast_progress_bar)
        legendaryLibrarianProgressBar = view.findViewById(R.id.legendary_librarian_progress_bar)
        bookGodProgressBar = view.findViewById(R.id.book_god_progress_bar)
        fantasyExplorerProgressBar = view.findViewById(R.id.fantasy_explorer_progress_bar)
        sciFiVoyagerProgressBar = view.findViewById(R.id.sci_fi_voyager_progress_bar)
        mysterySolverProgressBar = view.findViewById(R.id.mystery_solver_progress_bar)
        romanceEnthusiastProgressBar = view.findViewById(R.id.romance_enthusiast_progress_bar)

        // Initialize progress TextViews for achievements
        bookNookerProgressText = view.findViewById(R.id.book_nooker_progress_text)
        firstChapterProgressText = view.findViewById(R.id.first_chapter_progress_text)
        readingRookieProgressText = view.findViewById(R.id.reading_rookie_progress_text)
        storySeekerProgressText = view.findViewById(R.id.story_seeker_progress_text)
        novelNavigatorProgressText = view.findViewById(R.id.novel_navigator_progress_text)
        bookEnthusiastProgressText = view.findViewById(R.id.book_enthusiast_progress_text)
        legendaryLibrarianProgressText = view.findViewById(R.id.legendary_librarian_progress_text)
        bookGodProgressText = view.findViewById(R.id.book_god_progress_text)
        fantasyExplorerProgressText = view.findViewById(R.id.fantasy_explorer_progress_text)
        sciFiVoyagerProgressText = view.findViewById(R.id.sci_fi_voyager_progress_text)
        mysterySolverProgressText = view.findViewById(R.id.mystery_solver_progress_text)
        romanceEnthusiastProgressText = view.findViewById(R.id.romance_enthusiast_progress_text)

        // Load achievements and XP data from Firestore
        userId?.let {
            loadAchievementsData(it)
        }

        return view
    }

    // Load achievements data from Firestore
    private fun loadAchievementsData(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)

        // Retrieve the user's document
        userDocRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // Get the number of books marked as finished
                val booksFinished = (document.get("standardCollections.Finished") as? List<*>)?.size ?: 0

                // Calculate total XP
                val totalXp = booksFinished * 20

                // Calculate current level and XP within the current level
                val currentLevel = totalXp / 100
                val currentXpInLevel = totalXp % 100

                // Calculate books needed for next level
                val xpNeededForNextLevel = 100
                val xpRemaining = xpNeededForNextLevel - currentXpInLevel
                val booksNeeded = if (xpRemaining > 0) ceil(xpRemaining / 20.0).toInt() else 0

                // Update the Level and XP UI
                updateLevelAndXpUI(
                    totalXp = totalXp,
                    currentLevel = currentLevel,
                    currentXpInLevel = currentXpInLevel,
                    booksNeeded = booksNeeded
                )

                // Update achievements based on books read
                // Assuming 'booksRead' includes 'booksFinished'
                val booksRead = document.getLong("booksRead")?.toInt() ?: 0

                // Update individual achievements
                updateAchievementProgress(
                    progressBar = firstChapterProgressBar,
                    progressTextView = firstChapterProgressText,
                    cardView = firstChapterCard,
                    iconView = requireView().findViewById(R.id.first_chapter_icon),
                    titleTextView = requireView().findViewById(R.id.first_chapter_title),
                    requirementTextView = requireView().findViewById(R.id.first_chapter_requirement),
                    maxValue = 1,
                    currentValue = booksFinished.coerceAtMost(1),
                    description = "Finish 1 book",
                    isAchieved = booksFinished >= 1
                )

                // Unlock the First Chapter Achievement when the first book is finished
                val isFirstChapterAchieved = document.getBoolean("firstChapterAchieved") ?: false
                if (!isFirstChapterAchieved && booksFinished >= 1) {
                    unlockFirstChapterAchievement(userDocRef)
                }

                // Continue updating other achievements based on 'booksRead'
                updateAchievementProgress(
                    progressBar = readingRookieProgressBar,
                    progressTextView = readingRookieProgressText,
                    cardView = readingRookieCard,
                    iconView = requireView().findViewById(R.id.reading_rookie_icon),
                    titleTextView = requireView().findViewById(R.id.reading_rookie_title),
                    requirementTextView = requireView().findViewById(R.id.reading_rookie_requirement),
                    maxValue = 5,
                    currentValue = booksRead.coerceAtMost(5),
                    description = "Log 5 books as finished",
                    isAchieved = booksRead >= 5
                )

                updateAchievementProgress(
                    progressBar = storySeekerProgressBar,
                    progressTextView = storySeekerProgressText,
                    cardView = storySeekerCard,
                    iconView = requireView().findViewById(R.id.story_seeker_icon),
                    titleTextView = requireView().findViewById(R.id.story_seeker_title),
                    requirementTextView = requireView().findViewById(R.id.story_seeker_requirement),
                    maxValue = 10,
                    currentValue = booksRead.coerceAtMost(10),
                    description = "Log 10 books as finished",
                    isAchieved = booksRead >= 10
                )

                updateAchievementProgress(
                    progressBar = novelNavigatorProgressBar,
                    progressTextView = novelNavigatorProgressText,
                    cardView = novelNavigatorCard,
                    iconView = requireView().findViewById(R.id.novel_navigator_icon),
                    titleTextView = requireView().findViewById(R.id.novel_navigator_title),
                    requirementTextView = requireView().findViewById(R.id.novel_navigator_requirement),
                    maxValue = 25,
                    currentValue = booksRead.coerceAtMost(25),
                    description = "Log 25 books as finished",
                    isAchieved = booksRead >= 25
                )

                updateAchievementProgress(
                    progressBar = bookEnthusiastProgressBar,
                    progressTextView = bookEnthusiastProgressText,
                    cardView = bookEnthusiastCard,
                    iconView = requireView().findViewById(R.id.book_enthusiast_icon),
                    titleTextView = requireView().findViewById(R.id.book_enthusiast_title),
                    requirementTextView = requireView().findViewById(R.id.book_enthusiast_requirement),
                    maxValue = 50,
                    currentValue = booksRead.coerceAtMost(50),
                    description = "Log 50 books as finished",
                    isAchieved = booksRead >= 50
                )

                updateAchievementProgress(
                    progressBar = legendaryLibrarianProgressBar,
                    progressTextView = legendaryLibrarianProgressText,
                    cardView = legendaryLibrarianCard,
                    iconView = requireView().findViewById(R.id.legendary_librarian_icon),
                    titleTextView = requireView().findViewById(R.id.legendary_librarian_title),
                    requirementTextView = requireView().findViewById(R.id.legendary_librarian_requirement),
                    maxValue = 75,
                    currentValue = booksRead.coerceAtMost(75),
                    description = "Read 75 books",
                    isAchieved = booksRead >= 75
                )

                updateAchievementProgress(
                    progressBar = bookGodProgressBar,
                    progressTextView = bookGodProgressText,
                    cardView = bookGodCard,
                    iconView = requireView().findViewById(R.id.book_god_icon),
                    titleTextView = requireView().findViewById(R.id.book_god_title),
                    requirementTextView = requireView().findViewById(R.id.book_god_requirement),
                    maxValue = 500,
                    currentValue = booksRead.coerceAtMost(500),
                    description = "Read 500 books",
                    isAchieved = booksRead >= 500
                )

                // Genre achievements
                val fantasyBooksRead = document.getLong("fantasyBooksRead")?.toInt() ?: 0
                val sciFiBooksRead = document.getLong("sciFiBooksRead")?.toInt() ?: 0
                val mysteryBooksRead = document.getLong("mysteryBooksRead")?.toInt() ?: 0
                val romanceBooksRead = document.getLong("romanceBooksRead")?.toInt() ?: 0

                updateAchievementProgress(
                    progressBar = fantasyExplorerProgressBar,
                    progressTextView = fantasyExplorerProgressText,
                    cardView = fantasyExplorerCard,
                    iconView = requireView().findViewById(R.id.fantasy_explorer_icon),
                    titleTextView = requireView().findViewById(R.id.fantasy_explorer_title),
                    requirementTextView = requireView().findViewById(R.id.fantasy_explorer_requirement),
                    maxValue = 10,
                    currentValue = fantasyBooksRead.coerceAtMost(10),
                    description = "Read 10 fantasy books",
                    isAchieved = fantasyBooksRead >= 10
                )

                updateAchievementProgress(
                    progressBar = sciFiVoyagerProgressBar,
                    progressTextView = sciFiVoyagerProgressText,
                    cardView = sciFiVoyagerCard,
                    iconView = requireView().findViewById(R.id.sci_fi_voyager_icon),
                    titleTextView = requireView().findViewById(R.id.sci_fi_voyager_title),
                    requirementTextView = requireView().findViewById(R.id.sci_fi_voyager_requirement),
                    maxValue = 10,
                    currentValue = sciFiBooksRead.coerceAtMost(10),
                    description = "Read 10 sci-fi books",
                    isAchieved = sciFiBooksRead >= 10
                )

                updateAchievementProgress(
                    progressBar = mysterySolverProgressBar,
                    progressTextView = mysterySolverProgressText,
                    cardView = mysterySolverCard,
                    iconView = requireView().findViewById(R.id.mystery_solver_icon),
                    titleTextView = requireView().findViewById(R.id.mystery_solver_title),
                    requirementTextView = requireView().findViewById(R.id.mystery_solver_requirement),
                    maxValue = 10,
                    currentValue = mysteryBooksRead.coerceAtMost(10),
                    description = "Solve 10 mysteries",
                    isAchieved = mysteryBooksRead >= 10
                )

                updateAchievementProgress(
                    progressBar = romanceEnthusiastProgressBar,
                    progressTextView = romanceEnthusiastProgressText,
                    cardView = romanceEnthusiastCard,
                    iconView = requireView().findViewById(R.id.romance_enthusiast_icon),
                    titleTextView = requireView().findViewById(R.id.romance_enthusiast_title),
                    requirementTextView = requireView().findViewById(R.id.romance_enthusiast_requirement),
                    maxValue = 10,
                    currentValue = romanceBooksRead.coerceAtMost(10),
                    description = "Read 10 romance books",
                    isAchieved = romanceBooksRead >= 10
                )
            }
        }.addOnFailureListener { exception ->
            Log.e("Firestore", "Error loading achievements: ", exception)
        }
    }

    // Function to update the Level and XP UI
    private fun updateLevelAndXpUI(totalXp: Int, currentLevel: Int, currentXpInLevel: Int, booksNeeded: Int) {
        // Update level number
        levelNumberTextView.text = "Level $currentLevel"

        // Update XP progress bar
        val progressPercentage = ((currentXpInLevel.toFloat() / 100) * 100).toInt()
        xpProgressBar.progress = progressPercentage

        // Update current XP and XP needed TextViews
        currentXpTextView.text = "$currentXpInLevel"
        xpNeededTextView.text = "100"

        // Update level progress text
        levelProgressText.text = "$booksNeeded more books to level ${currentLevel + 1}"
    }

    // Unlock the First Chapter Achievement and show a Toast notification
    private fun unlockFirstChapterAchievement(userDocRef: DocumentReference) {
        userDocRef.update("firstChapterAchieved", true)
            .addOnSuccessListener {
                // Show a toast when the achievement is unlocked
                Toast.makeText(context, "Achievement Unlocked: First Chapter!", Toast.LENGTH_SHORT).show()

                // Update the UI to reflect the unlocked achievement
                updateAchievementProgress(
                    progressBar = firstChapterProgressBar,
                    progressTextView = firstChapterProgressText,
                    cardView = firstChapterCard,
                    iconView = requireView().findViewById(R.id.first_chapter_icon),
                    titleTextView = requireView().findViewById(R.id.first_chapter_title),
                    requirementTextView = requireView().findViewById(R.id.first_chapter_requirement),
                    maxValue = 1,
                    currentValue = 1,
                    description = "Finish 1 book",
                    isAchieved = true
                )
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error unlocking achievement: ", exception)
            }
    }

    // Function to update individual achievement progress
    private fun updateAchievementProgress(
        progressBar: ProgressBar,
        progressTextView: TextView,
        cardView: CardView,
        iconView: ImageView, // The ImageView for the achievement icon
        titleTextView: TextView, // The title TextView for the achievement
        requirementTextView: TextView, // The requirement description TextView for the achievement
        maxValue: Int, // Maximum value required for the achievement
        currentValue: Int, // Current progress towards the achievement
        description: String, // Description to show when not achieved
        isAchieved: Boolean = false // Whether the achievement is completed
    ) {
        progressBar.max = maxValue
        progressBar.progress = currentValue.coerceAtMost(maxValue)

        if (isAchieved) {
            // Achievement is unlocked
            progressBar.progress = maxValue
            progressTextView.text = "Completed!"
            progressTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

            // Set title and requirement text to black when achievement is unlocked
            titleTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            requirementTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

            // Change card background and progress bar to unlocked color
            cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.achievement_card_unlocked))
            progressBar.progressTintList = ContextCompat.getColorStateList(requireContext(), R.color.achievement_unlocked_color)

            // Restore the icon color by removing the gray tint
            iconView.setColorFilter(null)
            iconView.imageTintList = null
        } else {
            // Achievement is still locked
            progressTextView.text = description
            progressTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))

            // Set title and requirement text to gray when achievement is locked
            titleTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
            requirementTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))

            // Keep progress bar gray for locked achievements
            progressBar.progressTintList = ContextCompat.getColorStateList(requireContext(), R.color.grey)

            // Apply gray tint to the icon to indicate the locked state
            iconView.setColorFilter(ContextCompat.getColor(requireContext(), R.color.grey))
        }
    }
}

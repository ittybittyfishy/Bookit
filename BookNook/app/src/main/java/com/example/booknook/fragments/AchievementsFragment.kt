// AchievementsFragment.kt
package com.example.booknook.fragments

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.ceil

class AchievementsFragment : Fragment() {

    // Firestore and FirebaseAuth instances
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    // UI elements for XP and Level
    private lateinit var xpProgressBar: ProgressBar
    private lateinit var currentXpTextView: TextView
    private lateinit var xpNeededTextView: TextView
    private lateinit var levelNumberTextView: TextView
    private lateinit var levelProgressText: TextView
    private lateinit var xpRangeTextView: TextView // New TextView for XP Range

    // Achievement UI elements (CardViews)
    private lateinit var firstChapterCard: CardView
    private lateinit var readingRookieCard: CardView
    // Add other CardViews as needed...

    // Progress bars for achievements
    private lateinit var firstChapterProgressBar: ProgressBar
    private lateinit var readingRookieProgressBar: ProgressBar
    // Add other ProgressBars as needed...

    // Progress TextViews for achievements
    private lateinit var firstChapterProgressText: TextView
    private lateinit var readingRookieProgressText: TextView
    // Add other Progress TextViews as needed...

    // Achievement Icons, Titles, and Requirements (ImageView and TextView)
    private lateinit var firstChapterIcon: ImageView
    private lateinit var firstChapterTitle: TextView
    private lateinit var firstChapterRequirement: TextView

    private lateinit var readingRookieIcon: ImageView
    private lateinit var readingRookieTitle: TextView
    private lateinit var readingRookieRequirement: TextView
    // Add other Icons, Titles, and Requirements as needed...

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.fragment_achievements, container, false)

        // Initialize UI components for XP and Level
        xpProgressBar = view.findViewById(R.id.xpProgressBar)
        levelNumberTextView = view.findViewById(R.id.level_number)
        levelProgressText = view.findViewById(R.id.level_progress_text)
        currentXpTextView = view.findViewById(R.id.currentXpTextView)
        xpNeededTextView = view.findViewById(R.id.xpNeededTextView)
        xpRangeTextView = view.findViewById(R.id.xp_range_text_view) // Initialize new TextView

        // Initialize CardViews for achievements
        firstChapterCard = view.findViewById(R.id.first_chapter_card)
        readingRookieCard = view.findViewById(R.id.reading_rookie_card)
        // Initialize other CardViews as needed...

        // Initialize ProgressBars for achievements
        firstChapterProgressBar = view.findViewById(R.id.first_chapter_progress_bar)
        readingRookieProgressBar = view.findViewById(R.id.reading_rookie_progress_bar)
        // Initialize other ProgressBars as needed...

        // Initialize Progress TextViews for achievements
        firstChapterProgressText = view.findViewById(R.id.first_chapter_progress_text)
        readingRookieProgressText = view.findViewById(R.id.reading_rookie_progress_text)
        // Initialize other Progress TextViews as needed...

        // Initialize Achievement Icons, Titles, and Requirements
        firstChapterIcon = view.findViewById(R.id.first_chapter_icon)
        firstChapterTitle = view.findViewById(R.id.first_chapter_title)
        firstChapterRequirement = view.findViewById(R.id.first_chapter_requirement)

        readingRookieIcon = view.findViewById(R.id.reading_rookie_icon)
        readingRookieTitle = view.findViewById(R.id.reading_rookie_title)
        readingRookieRequirement = view.findViewById(R.id.reading_rookie_requirement)
        // Initialize other Icons, Titles, and Requirements as needed...

        // Load achievements and XP data from Firestore
        userId?.let {
            loadAchievementsData(it)
            setupRealtimeUpdates(it) // Optional: For real-time updates
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

                // Calculate current level and XP within the current level (Cumulative XP)
                val currentLevel = if (totalXp == 0) 1 else ceil(totalXp / 100.0).toInt()
                val currentXpInLevel = if (totalXp == 0) 0 else totalXp - ((currentLevel - 1) * 100)

                // Calculate books needed for next level
                val xpNeededForNextLevel = 100
                val xpRemaining = xpNeededForNextLevel - currentXpInLevel
                val booksNeeded = if (xpRemaining > 0) ceil(xpRemaining / 20.0).toInt() else 0

                Log.d("AchievementsFragment", "Total XP: $totalXp, Level: $currentLevel, XP in Level: $currentXpInLevel, Books Needed: $booksNeeded")

                // Update the Level and XP UI with absolute values
                updateLevelAndXpUI(
                    totalXp = totalXp,
                    currentLevel = currentLevel,
                    currentXpInLevel = currentXpInLevel,
                    booksNeeded = booksNeeded
                )

                // Update achievements based on books read
                // Assuming 'booksRead' includes 'booksFinished'
                val booksRead = document.getLong("booksRead")?.toInt() ?: 0

                Log.d("AchievementsFragment", "Books Read: $booksRead")

                // Update individual achievements
                updateAchievementProgress(
                    progressBar = firstChapterProgressBar,
                    progressTextView = firstChapterProgressText,
                    cardView = firstChapterCard,
                    iconView = firstChapterIcon,
                    titleTextView = firstChapterTitle,
                    requirementTextView = firstChapterRequirement,
                    maxValue = 1,
                    currentValue = booksFinished.coerceAtMost(1),
                    description = "Finish 1 book",
                    isAchieved = booksFinished >= 1
                )

                // Unlock the First Chapter Achievement when the first book is finished
                val isFirstChapterAchieved = document.getBoolean("firstChapterAchieved") ?: false
                if (!isFirstChapterAchieved && booksFinished >= 1) {
                    unlockFirstChapterAchievement(userDocRef)
                } else {
                    // Ensure UI is updated based on current Firestore data
                    updateAchievementProgress(
                        progressBar = firstChapterProgressBar,
                        progressTextView = firstChapterProgressText,
                        cardView = firstChapterCard,
                        iconView = firstChapterIcon,
                        titleTextView = firstChapterTitle,
                        requirementTextView = firstChapterRequirement,
                        maxValue = 1,
                        currentValue = booksFinished.coerceAtMost(1),
                        description = "Finish 1 book",
                        isAchieved = isFirstChapterAchieved || booksFinished >= 1
                    )
                }

                // Continue updating other achievements based on 'booksRead'
                updateAchievementProgress(
                    progressBar = readingRookieProgressBar,
                    progressTextView = readingRookieProgressText,
                    cardView = readingRookieCard,
                    iconView = readingRookieIcon,
                    titleTextView = readingRookieTitle,
                    requirementTextView = readingRookieRequirement,
                    maxValue = 5,
                    currentValue = booksRead.coerceAtMost(5),
                    description = "Log 5 books as finished",
                    isAchieved = booksRead >= 5
                )

                // Add similar blocks for other achievements...

            }
        }.addOnFailureListener { exception ->
            Log.e("Firestore", "Error loading achievements: ", exception)
        }
    }

    // Function to update the Level and XP UI
    private fun updateLevelAndXpUI(
        totalXp: Int,
        currentLevel: Int,
        currentXpInLevel: Int,
        booksNeeded: Int
    ) {
        if (!isAdded) return // Prevent UI updates if fragment is not attached

        // Update level number
        levelNumberTextView.text = "Level $currentLevel"

        // Update XP progress bar
        xpProgressBar.max = 100 // Each level requires 100 XP
        xpProgressBar.progress = currentXpInLevel

        // Calculate XP range
        val lowerBound = (currentLevel - 1) * 100 + 1
        val upperBound = currentLevel * 100

        // Update current XP and XP needed TextViews with absolute values
        currentXpTextView.text = "$totalXp XP"
        xpNeededTextView.text = "$upperBound XP"

        // Update level progress text with XP range
        xpRangeTextView.text = "$lowerBound to $upperBound XP"

        // Update level progress description
        levelProgressText.text = if (booksNeeded > 0) {
            "$booksNeeded more book${if (booksNeeded > 1) "s" else ""} to level ${currentLevel + 1}"
        } else {
            "You are at the highest level!"
        }
    }

    // Unlock the First Chapter Achievement and show a Toast notification
    private fun unlockFirstChapterAchievement(userDocRef: DocumentReference) {
        userDocRef.update("firstChapterAchieved", true)
            .addOnSuccessListener {
                if (isAdded && context != null) { // Ensure fragment is attached
                    requireActivity().runOnUiThread {
                        // Show a toast when the achievement is unlocked
                        Toast.makeText(context, "Achievement Unlocked: First Chapter!", Toast.LENGTH_SHORT).show()

                        // Animate the card background color change
                        animateCardBackgroundColor(
                            cardView = firstChapterCard,
                            fromColor = ContextCompat.getColor(requireContext(), R.color.achievement_card),
                            toColor = ContextCompat.getColor(requireContext(), R.color.achievement_card_unlocked)
                        )

                        // Update the UI to reflect the unlocked achievement
                        updateAchievementProgress(
                            progressBar = firstChapterProgressBar,
                            progressTextView = firstChapterProgressText,
                            cardView = firstChapterCard,
                            iconView = firstChapterIcon,
                            titleTextView = firstChapterTitle,
                            requirementTextView = firstChapterRequirement,
                            maxValue = 1,
                            currentValue = 1,
                            description = "Finish 1 book",
                            isAchieved = true
                        )
                    }
                }
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
        iconView: ImageView,
        titleTextView: TextView,
        requirementTextView: TextView,
        maxValue: Int,
        currentValue: Int,
        description: String,
        isAchieved: Boolean = false
    ) {
        Log.d("AchievementsFragment", "Updating achievement: $description, isAchieved: $isAchieved")
        if (!isAdded || context == null) return // Prevent UI updates if fragment is not attached

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

            // Animate card background color change
            val fromColor = ContextCompat.getColor(requireContext(), R.color.achievement_card)
            val toColor = ContextCompat.getColor(requireContext(), R.color.achievement_card_unlocked)
            Log.d("AchievementsFragment", "Animating from $fromColor to $toColor")
            animateCardBackgroundColor(
                cardView = cardView,
                fromColor = fromColor,
                toColor = toColor
            )

            // Update progress bar tint
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

    // Function to animate the background color of a CardView
    private fun animateCardBackgroundColor(cardView: CardView, fromColor: Int, toColor: Int) {
        Log.d("AchievementsFragment", "Animating card background color from $fromColor to $toColor")
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor)
        colorAnimation.duration = 500 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            val animatedColor = animator.animatedValue as Int
            cardView.setCardBackgroundColor(animatedColor)
            Log.d("AchievementsFragment", "Animated color: $animatedColor")
        }
        colorAnimation.start()
    }

    // Optional: Setup Firestore snapshot listener for real-time updates
    private fun setupRealtimeUpdates(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)
        userDocRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("Firestore", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                if (isAdded && context != null) { // Ensure fragment is attached
                    // Reload achievements data to reflect real-time changes
                    loadAchievementsData(userId)
                }
            } else {
                Log.d("Firestore", "Current data: null")
            }
        }
    }
}

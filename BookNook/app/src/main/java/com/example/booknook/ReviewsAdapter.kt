package com.example.booknook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//Yunjong Noh
// This is the adapter class for the RecyclerView, responsible for managing and displaying
// Either with-template or no-template is using appropriate ViewHolders
class ReviewsAdapter(private val reviews: List<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Determines the type of view to display based on whether the review uses a template
    override fun getItemViewType(position: Int): Int {
        return if (reviews[position] is TemplateReview) {
            R.layout.item_template_review // Returns layout for with-template reviews
        } else {
            R.layout.item_review // Returns layout for no-temp reviews
        }
    }

    // Creates and returns the appropriate ViewHolder based on the type of review
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return if (viewType == R.layout.item_template_review) {
            TemplateReviewViewHolder(view) // Inflate item_template_Review.xml layout
        } else {
            ReviewViewHolder(view) // Inflate item_review.xml layout
        }
    }

    // Binds the data to the corresponding ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = reviews[position]
        if (holder is TemplateReviewViewHolder) {
            holder.bind(item as TemplateReview) // Bind with-template review data
        } else if (holder is ReviewViewHolder) {
            holder.bind(item as Review) // Bind no-template review data
        }
    }

    override fun getItemCount() = reviews.size // Returns total number of reviews

    // ViewHolder for no-template reviews
    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Defines all UI elements (overall data(username, review, etc.), timestamp)
        private val username: TextView = itemView.findViewById(R.id.Username)
        private val reviewText: TextView = itemView.findViewById(R.id.ReviewText)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val timestamp: TextView = itemView.findViewById(R.id.Timestamp)
        private val overallReviewHeading: TextView = itemView.findViewById(R.id.OverallReviewHeading)

        // binds the data from no-template Review object to the UI elements in the ViewHolder.
        fun bind(review: Review) {
            username.text = review.username // Set username
            reviewText.text = review.reviewText // Set review text
            ratingBar.rating = review.rating.toFloat() // Set rating
            timestamp.text = review.timestamp.toString() // Set timestamp
            overallReviewHeading.visibility = View.VISIBLE // Show "Overall" heading
        }
    }

    // ViewHolder for with-template reviews
    class TemplateReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Defines all UI elements
        // Overall data, Characters, Writing, Plot, Themes, Strengths, Weaknesses, timestamp
        private val username: TextView = itemView.findViewById(R.id.Username)
        private val overallReviewText: TextView = itemView.findViewById(R.id.OverallReviewText)
        private val overallRatingBar: RatingBar = itemView.findViewById(R.id.overallRatingBar)
        private val overallReviewHeading: TextView = itemView.findViewById(R.id.OverallReviewHeading)

        private val charactersReview: TextView = itemView.findViewById(R.id.CharactersReview)
        private val charactersRatingBar: RatingBar = itemView.findViewById(R.id.charactersRatingBar)
        private val charactersHeading: TextView = itemView.findViewById(R.id.CharactersHeading)

        private val writingReview: TextView = itemView.findViewById(R.id.WritingReview)
        private val writingRatingBar: RatingBar = itemView.findViewById(R.id.writingRatingBar)
        private val writingHeading: TextView = itemView.findViewById(R.id.WritingHeading)

        private val plotReview: TextView = itemView.findViewById(R.id.PlotReview)
        private val plotRatingBar: RatingBar = itemView.findViewById(R.id.plotRatingBar)
        private val plotHeading: TextView = itemView.findViewById(R.id.PlotHeading)

        private val themesReview: TextView = itemView.findViewById(R.id.ThemesReview)
        private val themesRatingBar: RatingBar = itemView.findViewById(R.id.themesRatingBar)
        private val themesHeading: TextView = itemView.findViewById(R.id.ThemesHeading)

        private val strengthsReview: TextView = itemView.findViewById(R.id.StrengthsReview)
        private val strengthsRatingBar: RatingBar = itemView.findViewById(R.id.strengthsRatingBar)
        private val strengthsHeading: TextView = itemView.findViewById(R.id.StrengthsHeading)

        private val weaknessesReview: TextView = itemView.findViewById(R.id.WeaknessesReview)
        private val weaknessesRatingBar: RatingBar = itemView.findViewById(R.id.weaknessesRatingBar)
        private val weaknessesHeading: TextView = itemView.findViewById(R.id.WeaknessesHeading)

        private val timestamp: TextView = itemView.findViewById(R.id.Timestamp)

        // binds the data from with-template Review object to the UI elements in the ViewHolder.
        fun bind(templateReview: TemplateReview) {
            username.text = templateReview.username // Set username
            overallReviewText.text = templateReview.reviewText // Set overall review text
            overallRatingBar.rating = templateReview.rating.toFloat() // Set overall rating
            overallReviewHeading.visibility = View.VISIBLE // Show "Overall" heading

            // Characters section
            if (!templateReview.charactersReview.isNullOrEmpty()) {
                charactersHeading.visibility = View.VISIBLE  // Show the header
                charactersRatingBar.visibility = View.VISIBLE  // Show the rating bar
                charactersReview.visibility = View.VISIBLE  // Show the review text

                charactersReview.text = templateReview.charactersReview // Set characters review text
                charactersRatingBar.rating = templateReview.charactersRating.toFloat() // Set characters rating
            } else {
                // Hide the section if no data exists
                charactersHeading.visibility = View.GONE
                charactersRatingBar.visibility = View.GONE
                charactersReview.visibility = View.GONE
            }

            // Writing section
            if (!templateReview.writingReview.isNullOrEmpty()) {
                writingHeading.visibility = View.VISIBLE
                writingRatingBar.visibility = View.VISIBLE
                writingReview.visibility = View.VISIBLE

                writingReview.text = templateReview.writingReview
                writingRatingBar.rating = templateReview.writingRating.toFloat()
            } else {
                writingHeading.visibility = View.GONE
                writingRatingBar.visibility = View.GONE
                writingReview.visibility = View.GONE
            }

            // Plot section
            if (!templateReview.plotReview.isNullOrEmpty()) {
                plotHeading.visibility = View.VISIBLE
                plotRatingBar.visibility = View.VISIBLE
                plotReview.visibility = View.VISIBLE

                plotReview.text = templateReview.plotReview
                plotRatingBar.rating = templateReview.plotRating.toFloat()
            } else {
                plotHeading.visibility = View.GONE
                plotRatingBar.visibility = View.GONE
                plotReview.visibility = View.GONE
            }

            // Themes section
            if (!templateReview.themesReview.isNullOrEmpty()) {
                themesHeading.visibility = View.VISIBLE
                themesRatingBar.visibility = View.VISIBLE
                themesReview.visibility = View.VISIBLE

                themesReview.text = templateReview.themesReview
                themesRatingBar.rating = templateReview.themesRating.toFloat()
            } else {
                themesHeading.visibility = View.GONE
                themesRatingBar.visibility = View.GONE
                themesReview.visibility = View.GONE
            }

            // Strengths section
            if (!templateReview.strengthsReview.isNullOrEmpty()) {
                strengthsHeading.visibility = View.VISIBLE
                strengthsRatingBar.visibility = View.VISIBLE
                strengthsReview.visibility = View.VISIBLE

                strengthsReview.text = templateReview.strengthsReview
                strengthsRatingBar.rating = templateReview.strengthsRating.toFloat()
            } else {
                strengthsHeading.visibility = View.GONE
                strengthsRatingBar.visibility = View.GONE
                strengthsReview.visibility = View.GONE
            }

            // Weaknesses section
            if (!templateReview.weaknessesReview.isNullOrEmpty()) {
                weaknessesHeading.visibility = View.VISIBLE
                weaknessesRatingBar.visibility = View.VISIBLE
                weaknessesReview.visibility = View.VISIBLE

                weaknessesReview.text = templateReview.weaknessesReview
                weaknessesRatingBar.rating = templateReview.weaknessesRating.toFloat()
            } else {
                weaknessesHeading.visibility = View.GONE
                weaknessesRatingBar.visibility = View.GONE
                weaknessesReview.visibility = View.GONE
            }

            // Set timestamp
            timestamp.text = templateReview.timestamp.toString()
        }
    }

}

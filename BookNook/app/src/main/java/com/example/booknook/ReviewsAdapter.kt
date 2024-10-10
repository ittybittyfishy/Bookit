package com.example.booknook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReviewsAdapter(private val reviews: List<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return if (reviews[position] is TemplateReview) {
            R.layout.item_template_review
        } else {
            R.layout.item_review
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return if (viewType == R.layout.item_template_review) {
            TemplateReviewViewHolder(view)
        } else {
            ReviewViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = reviews[position]
        if (holder is TemplateReviewViewHolder) {
            holder.bind(item as TemplateReview)
        } else if (holder is ReviewViewHolder) {
            holder.bind(item as Review)
        }
    }

    override fun getItemCount() = reviews.size

    // ViewHolder for regular reviews
    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val username: TextView = itemView.findViewById(R.id.Username)
        private val reviewText: TextView = itemView.findViewById(R.id.ReviewText)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val timestamp: TextView = itemView.findViewById(R.id.Timestamp)

        fun bind(review: Review) {
            username.text = review.username
            reviewText.text = review.reviewText
            ratingBar.rating = review.rating.toFloat()
            timestamp.text =
                review.timestamp.toString() // Assuming you have a timestamp field in the Review class
        }
    }

    class TemplateReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val username: TextView = itemView.findViewById(R.id.Username)
        private val overallReviewText: TextView = itemView.findViewById(R.id.OverallReviewText)
        private val overallRatingBar: RatingBar = itemView.findViewById(R.id.overallRatingBar)
        private val charactersReview: TextView = itemView.findViewById(R.id.CharactersReview)
        private val charactersRatingBar: RatingBar = itemView.findViewById(R.id.charactersRatingBar)
        private val writingReview: TextView = itemView.findViewById(R.id.WritingReview)
        private val writingRatingBar: RatingBar = itemView.findViewById(R.id.writingRatingBar)
        private val plotReview: TextView = itemView.findViewById(R.id.PlotReview)
        private val plotRatingBar: RatingBar = itemView.findViewById(R.id.plotRatingBar)
        private val themesReview: TextView = itemView.findViewById(R.id.ThemesReview)
        private val themesRatingBar: RatingBar = itemView.findViewById(R.id.themesRatingBar)
        private val strengthsReview: TextView = itemView.findViewById(R.id.StrengthsReview)
        private val strengthsRatingBar: RatingBar = itemView.findViewById(R.id.strengthsRatingBar)
        private val weaknessesReview: TextView = itemView.findViewById(R.id.WeaknessesReview)
        private val weaknessesRatingBar: RatingBar = itemView.findViewById(R.id.weaknessesRatingBar)
        private val timestamp: TextView = itemView.findViewById(R.id.Timestamp)

        fun bind(templateReview: TemplateReview) {
            username.text = templateReview.username
            overallReviewText.text = templateReview.reviewText
            overallRatingBar.rating = templateReview.rating.toFloat()

            // Characters section
            if (templateReview.charactersReview?.isNotEmpty() == true) {
                charactersReview.text = templateReview.charactersReview
                charactersRatingBar.rating = templateReview.charactersRating.toFloat()
                charactersReview.visibility = View.VISIBLE
            } else {
                charactersReview.visibility = View.GONE
            }

            // Writing section
            if (templateReview.writingReview?.isNotEmpty() == true) {
                writingReview.text = templateReview.writingReview
                writingRatingBar.rating = templateReview.writingRating.toFloat()
                writingReview.visibility = View.VISIBLE
            } else {
                writingReview.visibility = View.GONE
            }

            // Plot section
            if (templateReview.plotReview?.isNotEmpty() == true) {
                plotReview.text = templateReview.plotReview
                plotRatingBar.rating = templateReview.plotRating.toFloat()
                plotReview.visibility = View.VISIBLE
            } else {
                plotReview.visibility = View.GONE
            }

            // Themes section
            if (templateReview.themesReview?.isNotEmpty() == true) {
                themesReview.text = templateReview.themesReview
                themesRatingBar.rating = templateReview.themesRating.toFloat()
                themesReview.visibility = View.VISIBLE
            } else {
                themesReview.visibility = View.GONE
            }

            // Strengths section
            if (templateReview.themesReview?.isNotEmpty() == true) {
                strengthsReview.text = templateReview.strengthsReview
                strengthsRatingBar.rating = templateReview.strengthsRating.toFloat()
                strengthsReview.visibility = View.VISIBLE
            } else {
                strengthsReview.visibility = View.GONE
            }

            // Weaknesses section
            if (templateReview.themesReview?.isNotEmpty() == true) {
                weaknessesReview.text = templateReview.weaknessesReview
                weaknessesRatingBar.rating = templateReview.weaknessesRating.toFloat()
                weaknessesReview.visibility = View.VISIBLE
            } else {
                weaknessesReview.visibility = View.GONE
            }

            // Set timestamp
            timestamp.text = templateReview.timestamp.toString()
        }
    }

}

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
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvReviewText: TextView = itemView.findViewById(R.id.tvReviewText)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)

        fun bind(review: Review) {
            tvUsername.text = review.username
            tvReviewText.text = review.reviewText
            ratingBar.rating = review.rating.toFloat()
            tvTimestamp.text =
                review.timestamp.toString() // Assuming you have a timestamp field in the Review class
        }
    }

    class TemplateReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvOverallReviewText: TextView = itemView.findViewById(R.id.tvOverallReviewText)
        private val overallRatingBar: RatingBar = itemView.findViewById(R.id.overallRatingBar)
        private val tvCharactersReview: TextView = itemView.findViewById(R.id.tvCharactersReview)
        private val charactersRatingBar: RatingBar = itemView.findViewById(R.id.charactersRatingBar)
        private val tvWritingReview: TextView = itemView.findViewById(R.id.tvWritingReview)
        private val writingRatingBar: RatingBar = itemView.findViewById(R.id.writingRatingBar)
        private val tvPlotReview: TextView = itemView.findViewById(R.id.tvPlotReview)
        private val plotRatingBar: RatingBar = itemView.findViewById(R.id.plotRatingBar)
        private val tvThemesReview: TextView = itemView.findViewById(R.id.tvThemesReview)
        private val themesRatingBar: RatingBar = itemView.findViewById(R.id.themesRatingBar)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)

        fun bind(templateReview: TemplateReview) {
            tvUsername.text = templateReview.username
            tvOverallReviewText.text = templateReview.reviewText
            overallRatingBar.rating = templateReview.rating.toFloat()

            // Characters section
            if (templateReview.charactersReview?.isNotEmpty() == true) {
                tvCharactersReview.text = templateReview.charactersReview
                charactersRatingBar.rating = templateReview.charactersRating.toFloat()
                tvCharactersReview.visibility = View.VISIBLE
            } else {
                tvCharactersReview.visibility = View.GONE
            }

            // Writing section
            if (templateReview.writingReview?.isNotEmpty() == true) {
                tvWritingReview.text = templateReview.writingReview
                writingRatingBar.rating = templateReview.writingRating.toFloat()
                tvWritingReview.visibility = View.VISIBLE
            } else {
                tvWritingReview.visibility = View.GONE
            }

            // Plot section
            if (templateReview.plotReview?.isNotEmpty() == true) {
                tvPlotReview.text = templateReview.plotReview
                plotRatingBar.rating = templateReview.plotRating.toFloat()
                tvPlotReview.visibility = View.VISIBLE
            } else {
                tvPlotReview.visibility = View.GONE
            }

            // Themes section
            if (templateReview.themesReview?.isNotEmpty() == true) {
                tvThemesReview.text = templateReview.themesReview
                themesRatingBar.rating = templateReview.themesRating.toFloat()
                tvThemesReview.visibility = View.VISIBLE
            } else {
                tvThemesReview.visibility = View.GONE
            }

            // Set timestamp
            tvTimestamp.text = templateReview.timestamp.toString()
        }
    }

}

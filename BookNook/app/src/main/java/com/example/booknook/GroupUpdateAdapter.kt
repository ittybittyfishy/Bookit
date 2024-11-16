import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booknook.GroupMemberUpdate
import com.example.booknook.R

class GroupUpdateAdapter(
    private val memberUpdates: List<GroupMemberUpdate>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Types of group updates
    companion object {
        const val TYPE_START_BOOK = 1
        const val TYPE_FINISH_BOOK = 2
        const val TYPE_RECOMMEND_BOOK = 3
        const val TYPE_REVIEW_BOOK_NO_TEMPLATE = 4
        const val TYPE_REVIEW_BOOK_TEMPLATE = 5
    }

    // Gets the type of the update
    override fun getItemViewType(position: Int): Int {
        val type = memberUpdates[position].type  // Gets the type of the update
        Log.d("GroupUpdateAdapter", "Item type: $type")  // Log the type value
        // Returns the corresponding type in the companion object
        return when (memberUpdates[position].type) {
            "startBook" -> TYPE_START_BOOK
            "finishBook" -> TYPE_FINISH_BOOK
            "recommendation" -> TYPE_RECOMMEND_BOOK
            "reviewBookNoTemplate" -> TYPE_REVIEW_BOOK_NO_TEMPLATE
            "reviewBookTemplate" -> TYPE_REVIEW_BOOK_TEMPLATE
            else -> throw IllegalArgumentException("Invalid update type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        // Inflates different views based on the update type
        return when (viewType) {
            TYPE_START_BOOK -> {
                val view = inflater.inflate(R.layout.item_start_book, parent, false)
                StartBookViewHolder(view)
            }
            TYPE_FINISH_BOOK -> {
                val view = inflater.inflate(R.layout.item_finish_book, parent, false)
                FinishBookViewHolder(view)
            }
            TYPE_RECOMMEND_BOOK -> {
                val view = inflater.inflate(R.layout.item_recommend_book, parent, false)
                RecommendBookViewHolder(view)
            }
            TYPE_REVIEW_BOOK_NO_TEMPLATE -> {
                val view = inflater.inflate(R.layout.item_review_book_no_template, parent, false)
                ReviewBookNoTemplateViewHolder(view)
            }
            TYPE_REVIEW_BOOK_TEMPLATE -> {
                val view = inflater.inflate(R.layout.item_review_book_template, parent, false)
                ReviewBookTemplateViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val update = memberUpdates[position]
        // Sets up view holders for each type
        when (holder) {
            is StartBookViewHolder -> holder.bind(update)
            is FinishBookViewHolder -> holder.bind(update)
            is RecommendBookViewHolder -> holder.bind(update)
            is ReviewBookNoTemplateViewHolder -> holder.bind(update)
            is ReviewBookTemplateViewHolder -> holder.bind(update)
        }
    }

    override fun getItemCount(): Int = memberUpdates.size

    // Sets up view for start book update
    inner class StartBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        fun bind(update: GroupMemberUpdate) {
            messageTextView.text = "${update.username} started a book: ${update.bookTitle}"
        }
    }

    // Sets up view for finish book update
    inner class FinishBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        fun bind(update: GroupMemberUpdate) {
            messageTextView.text = "${update.username} finished a book: ${update.bookTitle}"
        }
    }

    // Sets up view for recommending a book update
    inner class RecommendBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageText)
        private val bookImageView: ImageView = itemView.findViewById(R.id.bookImage)
        private val titleTextView: TextView = itemView.findViewById(R.id.bookTitle)
        private val authorsTextView: TextView = itemView.findViewById(R.id.bookAuthors)
        private val bookRatingBar: RatingBar = itemView.findViewById(R.id.bookRatingBar)
        private val ratingTextView: TextView = itemView.findViewById(R.id.ratingNumber)

        fun bind(update: GroupMemberUpdate) {
            messageTextView.text = "${update.username} recommended book: ${update.bookTitle}"
            titleTextView.text = update.bookTitle
            authorsTextView.text = update.bookAuthors
            bookRatingBar.rating = update.bookRating!!
            ratingTextView.text = update.bookRating.toString()

            // Load book image
            Glide.with(itemView.context)
                .load(update.bookImage)
                .placeholder(R.drawable.placeholder_image)
                .into(bookImageView)
        }
    }

    // Sets up view for writing a review without a template update
    inner class ReviewBookNoTemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val reviewTextView: TextView = itemView.findViewById(R.id.messageText)
        private val reviewTitle: TextView = itemView.findViewById(R.id.reviewTitle)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val ratingNumber: TextView = itemView.findViewById(R.id.ratingNumber)
        private val reviewText: TextView = itemView.findViewById(R.id.reviewText)
        private val spoilerText: TextView = itemView.findViewById(R.id.spoilerText)
        private val sensitiveTopicsText: TextView = itemView.findViewById(R.id.sensitiveTopicsText)

        fun bind(update: GroupMemberUpdate) {
            reviewTextView.text = "${update.username} left a review for: ${update.bookTitle}"
            ratingBar.rating = update.rating!!
            ratingNumber.text = update.rating.toString()
            reviewText.text = update.reviewText

            // Show reviewText only if it's not empty
            if (!update.reviewText.isNullOrEmpty()) {
                reviewText.text = update.reviewText
                reviewTitle.visibility = View.VISIBLE
                reviewText.visibility = View.VISIBLE
            } else {
                reviewTitle.visibility = View.GONE
                reviewText.visibility = View.GONE
            }

            // Shows/hides the spoilers and sensitive topics text based on boolean value
            spoilerText.visibility = if (update.hasSpoilers == true) View.VISIBLE else View.GONE
            sensitiveTopicsText.visibility = if (update.hasSensitiveTopics == true) View.VISIBLE else View.GONE
        }
    }

    // Sets up view for writing a review with a template update
    inner class ReviewBookTemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val reviewTextView: TextView = itemView.findViewById(R.id.messageText)
        private val reviewTitle: TextView = itemView.findViewById(R.id.reviewTitle)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val ratingNumber: TextView = itemView.findViewById(R.id.ratingNumber)
        private val reviewText: TextView = itemView.findViewById(R.id.reviewText)

        private val charactersTitle: TextView = itemView.findViewById(R.id.charactersTitle)
        private val charactersData: LinearLayout = itemView.findViewById(R.id.charactersData)
        private val charactersRatingBar: RatingBar = itemView.findViewById(R.id.charactersRatingBar)
        private val charactersRating: TextView = itemView.findViewById(R.id.charactersRating)
        private val charactersReview: TextView = itemView.findViewById(R.id.charactersText)

        private val writingTitle: TextView = itemView.findViewById(R.id.writingTitle)
        private val writingData: LinearLayout = itemView.findViewById(R.id.writingData)
        private val writingRatingBar: RatingBar = itemView.findViewById(R.id.writingRatingBar)
        private val writingRating: TextView = itemView.findViewById(R.id.writingRating)
        private val writingReview: TextView = itemView.findViewById(R.id.writingText)

        private val plotTitle: TextView = itemView.findViewById(R.id.plotTitle)
        private val plotData: LinearLayout = itemView.findViewById(R.id.plotData)
        private val plotRatingBar: RatingBar = itemView.findViewById(R.id.plotRatingBar)
        private val plotRating: TextView = itemView.findViewById(R.id.plotRating)
        private val plotReview: TextView = itemView.findViewById(R.id.plotText)

        private val themesTitle: TextView = itemView.findViewById(R.id.themesTitle)
        private val themesData: LinearLayout = itemView.findViewById(R.id.themesData)
        private val themesRatingBar: RatingBar = itemView.findViewById(R.id.themesRatingBar)
        private val themesRating: TextView = itemView.findViewById(R.id.themesRating)
        private val themesReview: TextView = itemView.findViewById(R.id.themesText)

        private val strengthsTitle: TextView = itemView.findViewById(R.id.strengthsTitle)
        private val strengthsData: LinearLayout = itemView.findViewById(R.id.strengthsData)
        private val strengthsRatingBar: RatingBar = itemView.findViewById(R.id.strengthsRatingBar)
        private val strengthsRating: TextView = itemView.findViewById(R.id.strengthsRating)
        private val strengthsReview: TextView = itemView.findViewById(R.id.strengthsText)

        private val weaknessesTitle: TextView = itemView.findViewById(R.id.weaknessesTitle)
        private val weaknessesData: LinearLayout = itemView.findViewById(R.id.weaknessesData)
        private val weaknessesRatingBar: RatingBar = itemView.findViewById(R.id.weaknessesRatingBar)
        private val weaknessesRating: TextView = itemView.findViewById(R.id.weaknessesRating)
        private val weaknessesReview: TextView = itemView.findViewById(R.id.weaknessesText)


        fun bind(update: GroupMemberUpdate) {
            // Configure main review text
            reviewTextView.text = "${update.username} left a review for: ${update.bookTitle}"
            ratingBar.rating = update.rating ?: 0f
            ratingNumber.text = update.rating?.toString() ?: "No Rating"

            // Show review text if present
            if (!update.reviewText.isNullOrEmpty()) {
                reviewText.text = update.reviewText
                reviewTitle.visibility = View.VISIBLE
                reviewText.visibility = View.VISIBLE
            } else {
                reviewTitle.visibility = View.GONE
                reviewText.visibility = View.GONE
            }

            // Helper function to manage title, rating, and review visibility
            fun configureCategory(
                titleView: TextView,
                dataView: View,
                reviewView: TextView,
                ratingBar: RatingBar,
                ratingText: TextView,
                rating: Float?,
                review: String?
            ) {
                var hasContent = false

                // Show rating if available and hides it if not
                if (rating != null) {
                    dataView.visibility = View.VISIBLE
                    ratingBar.rating = rating
                    ratingText.text = rating.toString()
                    hasContent = true
                } else {
                    dataView.visibility = View.GONE
                }

                // Show review if available and hides it if not
                if (!review.isNullOrEmpty()) {
                    reviewView.visibility = View.VISIBLE
                    reviewView.text = review
                    hasContent = true
                } else {
                    reviewView.visibility = View.GONE
                }

                // Show title if either rating or review exists
                titleView.visibility = if (hasContent) View.VISIBLE else View.GONE
            }

            // Configure each category
            configureCategory(
                charactersTitle,
                charactersData,
                charactersReview,
                charactersRatingBar,
                charactersRating,
                update.charactersRating,
                update.charactersReview
            )
            configureCategory(
                writingTitle,
                writingData,
                writingReview,
                writingRatingBar,
                writingRating,
                update.writingRating,
                update.writingReview
            )
            configureCategory(
                plotTitle,
                plotData,
                plotReview,
                plotRatingBar,
                plotRating,
                update.plotRating,
                update.plotReview
            )
            configureCategory(
                themesTitle,
                themesData,
                themesReview,
                themesRatingBar,
                themesRating,
                update.themesRating,
                update.themesReview
            )
            configureCategory(
                strengthsTitle,
                strengthsData,
                strengthsReview,
                strengthsRatingBar,
                strengthsRating,
                update.strengthsRating,
                update.strengthsReview
            )
            configureCategory(
                weaknessesTitle,
                weaknessesData,
                weaknessesReview,
                weaknessesRatingBar,
                weaknessesRating,
                update.weaknessesRating,
                update.weaknessesReview
            )
        }
    }
}

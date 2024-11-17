import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booknook.GroupMemberUpdate
import com.example.booknook.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class GroupUpdateAdapter(
    private val memberUpdates: List<GroupMemberUpdate>,
    private val groupId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_START_BOOK = 1
        const val TYPE_FINISH_BOOK = 2
        const val TYPE_RECOMMEND_BOOK = 3
        const val TYPE_REVIEW_BOOK_NO_TEMPLATE = 4
        const val TYPE_REVIEW_BOOK_TEMPLATE = 5
    }

    override fun getItemViewType(position: Int): Int {
        val type = memberUpdates[position].type
        return when (type) {
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
        when (holder) {
            is StartBookViewHolder -> holder.bind(update)
            is FinishBookViewHolder -> holder.bind(update)
            is RecommendBookViewHolder -> holder.bind(update)
            is ReviewBookNoTemplateViewHolder -> holder.bind(update)
            is ReviewBookTemplateViewHolder -> holder.bind(update)
        }
    }

    override fun getItemCount(): Int = memberUpdates.size

    // BaseViewHolder with comment handling
    open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val commentInput: EditText = itemView.findViewById(R.id.commentInput)
        private val postCommentButton: Button = itemView.findViewById(R.id.postCommentButton)

        fun setOnClickListener(update: GroupMemberUpdate, groupId: String) {
            postCommentButton.setOnClickListener {
                val commentText = commentInput.text.toString().trim()
                if (commentText.isNotEmpty()) {
                    saveCommentToDatabase(groupId, update.updateId, commentText, update.username, update.userId)
                    commentInput.text.clear()
                } else {
                    Toast.makeText(itemView.context, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Utility function for saving comments to the database
        private fun saveCommentToDatabase(groupId: String, updateId: String, commentText: String, username: String, userId: String) {
            val db = FirebaseFirestore.getInstance()
            val commentId = db.collection("groups")
                .document(groupId)
                .collection("memberUpdates")
                .document(updateId)
                .collection("comments")
                .document()
                .id

            val comment = mapOf(
                "commentText" to commentText,
                "username" to username,
                "userId" to userId,
                "timestamp" to FieldValue.serverTimestamp()
            )

            db.collection("groups")
                .document(groupId)
                .collection("memberUpdates")
                .document(updateId)
                .collection("comments")
                .document(commentId)
                .set(comment)
                .addOnSuccessListener {
                    Toast.makeText(itemView.context, "Comment posted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(itemView.context, "Failed to post comment: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    // Handles view for starting a book
    inner class StartBookViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)

        fun bind(update: GroupMemberUpdate) {
            messageTextView.text = "${update.username} started a book: ${update.bookTitle}"
            setOnClickListener(update, groupId)
        }
    }

    // Handles view for finishing a book
    inner class FinishBookViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)

        fun bind(update: GroupMemberUpdate) {
            messageTextView.text = "${update.username} finished a book: ${update.bookTitle}"
            setOnClickListener(update, groupId)
        }
    }

    // Handles view for recommending a book
    inner class RecommendBookViewHolder(itemView: View) : BaseViewHolder(itemView) {
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

            Glide.with(itemView.context)
                .load(update.bookImage)
                .placeholder(R.drawable.placeholder_image)
                .into(bookImageView)

            setOnClickListener(update, groupId)
        }
    }

    // Handles view for writing a review without a template
    inner class ReviewBookNoTemplateViewHolder(itemView: View) : BaseViewHolder(itemView) {
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

            if (!update.reviewText.isNullOrEmpty()) {
                reviewText.text = update.reviewText
                reviewTitle.visibility = View.VISIBLE
                reviewText.visibility = View.VISIBLE
            } else {
                reviewTitle.visibility = View.GONE
                reviewText.visibility = View.GONE
            }

            spoilerText.visibility = if (update.hasSpoilers == true) View.VISIBLE else View.GONE
            sensitiveTopicsText.visibility = if (update.hasSensitiveTopics == true) View.VISIBLE else View.GONE

            setOnClickListener(update, groupId)
        }
    }

    // Sets up view for writing a review with a template update
    inner class ReviewBookTemplateViewHolder(itemView: View) : BaseViewHolder(itemView) {
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
            setOnClickListener(update, groupId)
        }
    }
}

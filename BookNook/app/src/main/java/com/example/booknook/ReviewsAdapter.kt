package com.example.booknook

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

//Yunjong Noh
// Adapter class to manage no-template and with-template reviews
class ReviewsAdapter(private val reviews: List<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Determines the type of view to display based on whether the review uses a template
    override fun getItemViewType(position: Int): Int {
        return if (reviews[position] is TemplateReview) {
            R.layout.item_template_review // Layout for with-template reviews
        } else {
            R.layout.item_review // Layout for no-template reviews
        }
    }

    // Creates the appropriate ViewHolder based on the type of review
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

    // ViewHolder for no-template reviews
    class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val username: TextView = itemView.findViewById(R.id.Username)
        private val reviewText: TextView = itemView.findViewById(R.id.ReviewText)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val timestamp: TextView = itemView.findViewById(R.id.Timestamp)
        private val overallReviewHeading: TextView = itemView.findViewById(R.id.OverallReviewHeading)

        // Comment-related views
        private val commentsRecyclerView: RecyclerView = itemView.findViewById(R.id.commentsRecyclerView)
        private val commentInput: EditText = itemView.findViewById(R.id.commentInput)
        private val postCommentButton: Button = itemView.findViewById(R.id.postCommentButton)

        // Binds data from the review object to the UI elements
        fun bind(review: Review) {
            username.text = review.username
            reviewText.text = review.reviewText
            ratingBar.rating = review.rating.toFloat()
            timestamp.text = review.timestamp.toString()
            overallReviewHeading.visibility = View.VISIBLE

            // Load existing comments
            loadComments(review)

            // Post a new comment
            postCommentButton.setOnClickListener {
                val commentText = commentInput.text.toString()
                if (commentText.isNotBlank()) {
                    postComment(review, commentText)
                }
            }
        }

        private fun loadComments(review: Review) {
            val commentsAdapter = CommentsAdapter(listOf())
            commentsRecyclerView.adapter = commentsAdapter
            commentsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)

            val isbn = review.isbn
            val reviewId = review.reviewId

            FirebaseFirestore.getInstance()
                .collection("books")
                .document(isbn)
                .collection("reviews")
                .document(reviewId)
                .collection("comments")
                .get()
                .addOnSuccessListener { documents ->
                    val comments = documents.map { it.toObject(Comment::class.java) }
                    commentsAdapter.updateComments(comments)
                }
                .addOnFailureListener { exception ->
                    Log.e("ReviewViewHolder", "Error loading comments", exception)
                }
        }

        private fun postComment(review: Review, commentText: String) {
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid ?: ""

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val username = document.getString("username") ?: "Anonymous"

                    val comment = Comment(
                        userId = userId,
                        username = username,
                        text = commentText,
                        timestamp = Date()
                    )

                    val isbn = review.isbn
                    val reviewId = review.reviewId

                    if (isbn.isEmpty() || reviewId.isEmpty()) {
                        Log.e("ReviewViewHolder", "Invalid bookId or reviewId")
                        return@addOnSuccessListener
                    }

                    FirebaseFirestore.getInstance()
                        .collection("books")
                        .document(isbn)
                        .collection("reviews")
                        .document(reviewId)
                        .collection("comments")
                        .add(comment)
                        .addOnSuccessListener {
                            commentInput.text.clear()
                            loadComments(review)
                        }
                        .addOnFailureListener { exception ->
                            Log.e("ReviewViewHolder", "Error posting comment", exception)
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e("postComment", "Error fetching user info", exception)
                }
        }
    }

    // ViewHolder for with-template reviews
    class TemplateReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

        private val commentsRecyclerView: RecyclerView = itemView.findViewById(R.id.commentsRecyclerView)
        private val commentInput: EditText = itemView.findViewById(R.id.commentInput)
        private val postCommentButton: Button = itemView.findViewById(R.id.postCommentButton)

        fun bind(templateReview: TemplateReview) {
            username.text = templateReview.username
            overallReviewText.text = templateReview.reviewText
            overallRatingBar.rating = templateReview.rating.toFloat()
            overallReviewHeading.visibility = View.VISIBLE

            if (!templateReview.charactersReview.isNullOrEmpty()) {
                charactersHeading.visibility = View.VISIBLE
                charactersRatingBar.visibility = View.VISIBLE
                charactersReview.visibility = View.VISIBLE

                charactersReview.text = templateReview.charactersReview
                charactersRatingBar.rating = templateReview.charactersRating.toFloat()
            } else {
                charactersHeading.visibility = View.GONE
                charactersRatingBar.visibility = View.GONE
                charactersReview.visibility = View.GONE
            }

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

            timestamp.text = templateReview.timestamp.toString()

            loadComments(templateReview)

            postCommentButton.setOnClickListener {
                val commentText = commentInput.text.toString()
                if (commentText.isNotBlank()) {
                    postComment(templateReview, commentText)
                }
            }
        }

        private fun loadComments(templateReview: TemplateReview) {
            val commentsAdapter = CommentsAdapter(listOf())
            commentsRecyclerView.adapter = commentsAdapter
            commentsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)

            val isbn = templateReview.isbn
            val reviewId = templateReview.reviewId

            FirebaseFirestore.getInstance()
                .collection("books")
                .document(isbn)
                .collection("reviews")
                .document(reviewId)
                .collection("comments")
                .get()
                .addOnSuccessListener { documents ->
                    val comments = documents.map { it.toObject(Comment::class.java) }
                    commentsAdapter.updateComments(comments)
                }
                .addOnFailureListener { exception ->
                    Log.e("TemplateReviewViewHolder", "Error loading comments", exception)
                }
        }

        private fun postComment(templateReview: TemplateReview, commentText: String) {
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid ?: ""

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val username = document.getString("username") ?: "Anonymous"

                    val comment = Comment(
                        userId = userId,
                        username = username,
                        text = commentText,
                        timestamp = Date()
                    )

                    val isbn = templateReview.isbn
                    val reviewId = templateReview.reviewId

                    if (isbn.isEmpty() || reviewId.isEmpty()) {
                        Log.e("TemplateReviewViewHolder", "Invalid bookId or reviewId")
                        return@addOnSuccessListener
                    }

                    FirebaseFirestore.getInstance()
                        .collection("books")
                        .document(isbn)
                        .collection("reviews")
                        .document(reviewId)
                        .collection("comments")
                        .add(comment)
                        .addOnSuccessListener {
                            commentInput.text.clear()
                            loadComments(templateReview)
                        }
                        .addOnFailureListener { exception ->
                            Log.e("TemplateReviewViewHolder", "Error posting comment", exception)
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e("postComment", "Error fetching user info", exception)
                }
        }
    }
}
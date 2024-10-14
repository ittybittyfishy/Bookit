package com.example.booknook

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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

        // Likes and Dislikes views
        private val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
        private val dislikeButton: ImageButton = itemView.findViewById(R.id.dislikeButton)
        private val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        private val dislikeCount: TextView = itemView.findViewById(R.id.dislikeCount)

        private fun checkUserReaction(review: Review, userId: String?) {
            if (userId != null) {
                val db = FirebaseFirestore.getInstance()

                // Check if the user has already liked the review
                db.collection("books")
                    .document(review.isbn)
                    .collection("reviews")
                    .document(review.reviewId)
                    .collection("likes")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            likeButton.isEnabled = false // Disable like button if already liked
                        } else {
                            likeButton.isEnabled = true // Enable like button if not liked
                        }
                    }

                // Check if the user has already disliked the review
                db.collection("books")
                    .document(review.isbn)
                    .collection("reviews")
                    .document(review.reviewId)
                    .collection("dislikes")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            dislikeButton.isEnabled = false // Disable dislike button if already disliked
                        } else {
                            dislikeButton.isEnabled = true // Enable dislike button if not disliked
                        }
                    }
            }
        }


        // Binds data from the review object to the UI elements
        fun bind(review: Review) {
            username.text = review.username
            reviewText.text = review.reviewText
            ratingBar.rating = review.rating.toFloat()
            timestamp.text = review.timestamp.toString()
            overallReviewHeading.visibility = View.VISIBLE

            // Update like and dislike counts
            likeCount.text = review.likes.toString()
            dislikeCount.text = review.dislikes.toString()

            // Check user interaction status for likes and dislikes
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            checkUserReaction(review, userId)

            // Handle like and dislike button actions
            likeButton.setOnClickListener { handleLike(review, userId) }
            dislikeButton.setOnClickListener { handleDislike(review, userId) }


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

        private fun handleLike(review: Review, userId: String?) {
            if (userId != null) {
                val db = FirebaseFirestore.getInstance()

                db.collection("books")
                    .document(review.isbn)
                    .collection("reviews")
                    .document(review.reviewId)
                    .collection("likes")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // User already liked, remove like
                            db.collection("books")
                                .document(review.isbn)
                                .collection("reviews")
                                .document(review.reviewId)
                                .collection("likes")
                                .document(userId)
                                .delete() // Remove like
                                .addOnSuccessListener {
                                    review.likes--
                                    updateReviewInFirestore(review)
                                    likeCount.text = review.likes.toString()
                                }
                        } else {
                            // User hasn't liked yet, add like
                            db.collection("books")
                                .document(review.isbn)
                                .collection("reviews")
                                .document(review.reviewId)
                                .collection("likes")
                                .document(userId)
                                .set(mapOf("timestamp" to Date())) // Add the like
                                .addOnSuccessListener {
                                    review.likes++
                                    updateReviewInFirestore(review)
                                    likeCount.text = review.likes.toString()
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("ReviewViewHolder", "Error checking like status", exception)
                    }
            }
        }

        private fun handleDislike(review: Review, userId: String?) {
            if (userId != null) {
                val db = FirebaseFirestore.getInstance()

                db.collection("books")
                    .document(review.isbn)
                    .collection("reviews")
                    .document(review.reviewId)
                    .collection("dislikes")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // User already disliked, remove dislike
                            db.collection("books")
                                .document(review.isbn)
                                .collection("reviews")
                                .document(review.reviewId)
                                .collection("dislikes")
                                .document(userId)
                                .delete() // Remove dislike
                                .addOnSuccessListener {
                                    review.dislikes--
                                    updateReviewInFirestore(review)
                                    dislikeCount.text = review.dislikes.toString()
                                }
                        } else {
                            // User hasn't disliked yet, add dislike
                            db.collection("books")
                                .document(review.isbn)
                                .collection("reviews")
                                .document(review.reviewId)
                                .collection("dislikes")
                                .document(userId)
                                .set(mapOf("timestamp" to Date())) // Add the dislike
                                .addOnSuccessListener {
                                    review.dislikes++
                                    updateReviewInFirestore(review)
                                    dislikeCount.text = review.dislikes.toString()
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("ReviewViewHolder", "Error checking dislike status", exception)
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
                    val comments = documents.map { doc ->
                        Comment(
                            userId = doc.getString("userId") ?: "",
                            username = doc.getString("username") ?: "Anonymous",
                            text = doc.getString("text") ?: "",
                            timestamp = doc.getDate("timestamp") ?: Date(),
                            isbn = isbn,                 // ISBN을 전달
                            reviewId = reviewId,         // 리뷰 ID를 전달
                            commentId = doc.id           // Firestore에서 생성된 문서 ID를 사용
                        )
                    }
                    commentsAdapter.updateComments(comments)
                }
                .addOnFailureListener { exception ->
                    Log.e("ReviewViewHolder", "댓글 로드 중 오류 발생", exception)
                }
        }

        private fun postComment(review: Review, commentText: String) {
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid ?: ""

            // 사용자 정보를 가져와서 댓글 생성
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
                        timestamp = Date(),
                        isbn = review.isbn,
                        reviewId = review.reviewId
                    )

                    val isbn = review.isbn
                    val reviewId = review.reviewId

                    // Firestore에 댓글 추가
                    FirebaseFirestore.getInstance()
                        .collection("books")
                        .document(isbn)
                        .collection("reviews")
                        .document(reviewId)
                        .collection("comments")
                        .add(comment) // Firestore가 자동으로 commentId를 생성합니다.
                        .addOnSuccessListener {
                            commentInput.text.clear()
                            // 댓글 추가 후 UI 업데이트
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

        private fun updateReviewInFirestore(review: Review) {
            // Update likes and dislikes in Firestore
            val db = FirebaseFirestore.getInstance()
            db.collection("books")
                .document(review.isbn)
                .collection("reviews")
                .document(review.reviewId)
                .update("likes", review.likes, "dislikes", review.dislikes)
                .addOnSuccessListener {
                    Log.d("ReviewViewHolder", "Review updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("ReviewViewHolder", "Error updating review", e)
                }
        }


    }
    //Yunjong Noh
    // ViewHolder for with-template reviews
    class TemplateReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Views for displaying user information
        private val username: TextView = itemView.findViewById(R.id.Username)
        private val overallReviewText: TextView = itemView.findViewById(R.id.OverallReviewText)
        private val overallRatingBar: RatingBar = itemView.findViewById(R.id.overallRatingBar)
        private val overallReviewHeading: TextView = itemView.findViewById(R.id.OverallReviewHeading)

        // Views for displaying character reviews
        private val charactersReview: TextView = itemView.findViewById(R.id.CharactersReview)
        private val charactersRatingBar: RatingBar = itemView.findViewById(R.id.charactersRatingBar)
        private val charactersHeading: TextView = itemView.findViewById(R.id.CharactersHeading)

        // Views for displaying writing reviews
        private val writingReview: TextView = itemView.findViewById(R.id.WritingReview)
        private val writingRatingBar: RatingBar = itemView.findViewById(R.id.writingRatingBar)
        private val writingHeading: TextView = itemView.findViewById(R.id.WritingHeading)

        // Views for displaying plot reviews
        private val plotReview: TextView = itemView.findViewById(R.id.PlotReview)
        private val plotRatingBar: RatingBar = itemView.findViewById(R.id.plotRatingBar)
        private val plotHeading: TextView = itemView.findViewById(R.id.PlotHeading)

        // Views for displaying themes reviews
        private val themesReview: TextView = itemView.findViewById(R.id.ThemesReview)
        private val themesRatingBar: RatingBar = itemView.findViewById(R.id.themesRatingBar)
        private val themesHeading: TextView = itemView.findViewById(R.id.ThemesHeading)

        // Views for displaying strengths reviews
        private val strengthsReview: TextView = itemView.findViewById(R.id.StrengthsReview)
        private val strengthsRatingBar: RatingBar = itemView.findViewById(R.id.strengthsRatingBar)
        private val strengthsHeading: TextView = itemView.findViewById(R.id.StrengthsHeading)

        // Views for displaying weaknesses reviews
        private val weaknessesReview: TextView = itemView.findViewById(R.id.WeaknessesReview)
        private val weaknessesRatingBar: RatingBar = itemView.findViewById(R.id.weaknessesRatingBar)
        private val weaknessesHeading: TextView = itemView.findViewById(R.id.WeaknessesHeading)

        // View for displaying the timestamp of the review
        private val timestamp: TextView = itemView.findViewById(R.id.Timestamp)

        // Comment-related views
        private val commentsRecyclerView: RecyclerView = itemView.findViewById(R.id.commentsRecyclerView)
        private val commentInput: EditText = itemView.findViewById(R.id.commentInput)
        private val postCommentButton: Button = itemView.findViewById(R.id.postCommentButton)

        // Likes and dislikes views
        private val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
        private val dislikeButton: ImageButton = itemView.findViewById(R.id.dislikeButton)
        private val likeCount: TextView = itemView.findViewById(R.id.likeCount)
        private val dislikeCount: TextView = itemView.findViewById(R.id.dislikeCount)

        // Bind the templateReview data to the views
        fun bind(templateReview: TemplateReview) {
            // Set username and overall review details
            username.text = templateReview.username
            overallReviewText.text = templateReview.reviewText
            overallRatingBar.rating = templateReview.rating.toFloat()
            overallReviewHeading.visibility = View.VISIBLE
            likeCount.text = templateReview.likes.toString()
            dislikeCount.text = templateReview.dislikes.toString()

            // Update like and dislike counts
            likeCount.text = templateReview.likes.toString()
            dislikeCount.text = templateReview.dislikes.toString()

            // Check user interaction status for likes and dislikes
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            checkUserReaction(templateReview, userId)

            // Handle like and dislike button actions
            likeButton.setOnClickListener { handleLike(templateReview, userId) }
            dislikeButton.setOnClickListener { handleDislike(templateReview, userId) }

            // Set visibility and text for characters review if available
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

            // Set visibility and text for writing review if available
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

            // Set visibility and text for plot review if available
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

            // Set visibility and text for themes review if available
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

            // Set visibility and text for strengths review if available
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

            // Set visibility and text for weaknesses review if available
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

            // Set the timestamp of the review
            timestamp.text = templateReview.timestamp.toString()

            // Load comments for this review
            loadComments(templateReview)

            // Post a new comment when the button is clicked
            postCommentButton.setOnClickListener {
                val commentText = commentInput.text.toString()
                if (commentText.isNotBlank()) {
                    postComment(templateReview, commentText)
                }
            }
        }

        // Check if the user has reacted to the review
        private fun checkUserReaction(templateReview: TemplateReview, userId: String?) {
            if (userId != null) {
                val db = FirebaseFirestore.getInstance()

                // Check if the user has liked the review
                db.collection("books")
                    .document(templateReview.isbn)
                    .collection("reviews")
                    .document(templateReview.reviewId)
                    .collection("likes")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        likeButton.isEnabled = !document.exists() // Disable if already liked
                    }
                    .addOnFailureListener { exception ->
                        Log.e("TemplateReviewViewHolder", "Error checking like status", exception)
                    }

                // Check if the user has disliked the review
                db.collection("books")
                    .document(templateReview.isbn)
                    .collection("reviews")
                    .document(templateReview.reviewId)
                    .collection("dislikes")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        dislikeButton.isEnabled = !document.exists() // Disable if already disliked
                    }
                    .addOnFailureListener { exception ->
                        Log.e("TemplateReviewViewHolder", "Error checking dislike status", exception)
                    }
            }
        }

        // Handle like button action
        private fun handleLike(templateReview: TemplateReview, userId: String?) {
            if (userId != null) {
                val db = FirebaseFirestore.getInstance()

                // Check if the user has already liked the review
                db.collection("books")
                    .document(templateReview.isbn)
                    .collection("reviews")
                    .document(templateReview.reviewId)
                    .collection("likes")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // User already liked, remove like
                            db.collection("books")
                                .document(templateReview.isbn)
                                .collection("reviews")
                                .document(templateReview.reviewId)
                                .collection("likes")
                                .document(userId)
                                .delete() // Remove like
                                .addOnSuccessListener {
                                    templateReview.likes--
                                    updateReviewInFirestore(templateReview)
                                    likeCount.text = templateReview.likes.toString()
                                }
                        } else {
                            // User hasn't liked yet, add like
                            db.collection("books")
                                .document(templateReview.isbn)
                                .collection("reviews")
                                .document(templateReview.reviewId)
                                .collection("likes")
                                .document(userId)
                                .set(mapOf("timestamp" to Date())) // Add the like
                                .addOnSuccessListener {
                                    templateReview.likes++
                                    updateReviewInFirestore(templateReview)
                                    likeCount.text = templateReview.likes.toString()
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("TemplateReviewViewHolder", "Error checking like status", exception)
                    }
            }
        }

        // Handle dislike button action
        private fun handleDislike(templateReview: TemplateReview, userId: String?) {
            if (userId != null) {
                val db = FirebaseFirestore.getInstance()

                // Check if the user has already disliked the review
                db.collection("books")
                    .document(templateReview.isbn)
                    .collection("reviews")
                    .document(templateReview.reviewId)
                    .collection("dislikes")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // User already disliked, remove dislike
                            db.collection("books")
                                .document(templateReview.isbn)
                                .collection("reviews")
                                .document(templateReview.reviewId)
                                .collection("dislikes")
                                .document(userId)
                                .delete() // Remove dislike
                                .addOnSuccessListener {
                                    templateReview.dislikes--
                                    updateReviewInFirestore(templateReview)
                                    dislikeCount.text = templateReview.dislikes.toString()
                                }
                        } else {
                            // User hasn't disliked yet, add dislike
                            db.collection("books")
                                .document(templateReview.isbn)
                                .collection("reviews")
                                .document(templateReview.reviewId)
                                .collection("dislikes")
                                .document(userId)
                                .set(mapOf("timestamp" to Date())) // Add the dislike
                                .addOnSuccessListener {
                                    templateReview.dislikes++
                                    updateReviewInFirestore(templateReview)
                                    dislikeCount.text = templateReview.dislikes.toString()
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("TemplateReviewViewHolder", "Error checking dislike status", exception)
                    }
            }
        }

        // Load comments for the review
        private fun loadComments(templateReview: TemplateReview) {
            val commentsAdapter = CommentsAdapter(listOf())
            commentsRecyclerView.adapter = commentsAdapter
            commentsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)

            val isbn = templateReview.isbn
            val reviewId = templateReview.reviewId

            // Fetch comments from Firestore
            FirebaseFirestore.getInstance()
                .collection("books")
                .document(isbn)
                .collection("reviews")
                .document(reviewId)
                .collection("comments")
                .get()
                .addOnSuccessListener { documents ->
                    val comments = documents.map { doc ->
                        Comment(
                            userId = doc.getString("userId") ?: "",
                            username = doc.getString("username") ?: "Anonymous",
                            text = doc.getString("text") ?: "",
                            timestamp = doc.getDate("timestamp") ?: Date(),
                            isbn = isbn,
                            reviewId = reviewId,
                            commentId = doc.id
                        )
                    }
                    commentsAdapter.updateComments(comments) // Update the adapter with new comments
                }
                .addOnFailureListener { exception ->
                    Log.e("TemplateReviewViewHolder", "Error loading comments", exception)
                }
        }

        // Post a new comment
        private fun postComment(templateReview: TemplateReview, commentText: String) {
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid ?: ""

            // Fetch user info to get username
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val username = document.getString("username") ?: "Anonymous"

                    // Create a new Comment object
                    val comment = Comment(
                        userId = userId,
                        username = username,
                        text = commentText,
                        timestamp = Date()
                    )

                    val isbn = templateReview.isbn
                    val reviewId = templateReview.reviewId

                    // Ensure valid ISBN and reviewId
                    if (isbn.isEmpty() || reviewId.isEmpty()) {
                        Log.e("TemplateReviewViewHolder", "Invalid bookId or reviewId")
                        return@addOnSuccessListener
                    }

                    // Add the comment to Firestore
                    FirebaseFirestore.getInstance()
                        .collection("books")
                        .document(isbn)
                        .collection("reviews")
                        .document(reviewId)
                        .collection("comments")
                        .add(comment)
                        .addOnSuccessListener {
                            commentInput.text.clear() // Clear the input field
                            loadComments(templateReview) // Reload comments after posting
                        }
                        .addOnFailureListener { exception ->
                            Log.e("TemplateReviewViewHolder", "Error posting comment", exception)
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e("postComment", "Error fetching user info", exception)
                }
        }

        // Update the review document in Firestore with new like/dislike counts
        private fun updateReviewInFirestore(templateReview: TemplateReview) {
            val db = FirebaseFirestore.getInstance()
            db.collection("books")
                .document(templateReview.isbn) // Use templateReview ISBN
                .collection("reviews")
                .document(templateReview.reviewId) // Use templateReview reviewId
                .update("likes", templateReview.likes, "dislikes", templateReview.dislikes) // Update counts
                .addOnSuccessListener {
                    Log.d("TemplateReviewViewHolder", "Review updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("TemplateReviewViewHolder", "Error updating review", e)
                }
        }
    }
}
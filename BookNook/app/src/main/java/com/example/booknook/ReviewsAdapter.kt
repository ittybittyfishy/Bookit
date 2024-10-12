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

        // Comment-related views
        private val commentsRecyclerView: RecyclerView = itemView.findViewById(R.id.commentsRecyclerView)
        private val commentInput: EditText = itemView.findViewById(R.id.commentInput)
        private val postCommentButton: Button = itemView.findViewById(R.id.postCommentButton)

        // binds the data from no-template Review object to the UI elements in the ViewHolder.
        fun bind(review: Review) {
            username.text = review.username // Set username
            reviewText.text = review.reviewText // Set review text
            ratingBar.rating = review.rating.toFloat() // Set rating
            timestamp.text = review.timestamp.toString() // Set timestamp
            overallReviewHeading.visibility = View.VISIBLE // Show "Overall" heading

            // Load existing comments from Firestore
            loadComments(review)

            postCommentButton.setOnClickListener {
                val commentText = commentInput.text.toString()
                if (commentText.isNotBlank()) {
                    postComment(review, commentText)
                }
            }
        }

        private fun loadComments(review: Review) {
            val commentsAdapter = CommentsAdapter(listOf())  // Initialize empty adapter
            commentsRecyclerView.adapter = commentsAdapter
            commentsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)

            val isbn = review.isbn.ifEmpty { return }  // Check for valid bookId
            val reviewId = review.reviewId.ifEmpty { return }  // Check for valid reviewId

            // Fetch comments from Firestore
            FirebaseFirestore.getInstance()
                .collection("books")
                .document(isbn)
                .collection("reviews")
                .document(reviewId)
                .collection("comments")
                .get()
                .addOnSuccessListener { documents ->
                    val comments = documents.map { it.toObject(Comment::class.java) }
                    commentsAdapter.updateComments(comments)  // Update adapter with new comments
                }
                .addOnFailureListener { exception ->
                    Log.e("ReviewViewHolder", "Error loading comments", exception)
                }
        }

        private fun postComment(review: Review, commentText: String) {
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid ?: ""

            // Firestore에서 사용자 이름을 가져오기
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    // Firestore에서 username 필드를 가져오고, 없으면 "Anonymous"로 설정
                    val username = document.getString("username") ?: "Anonymous"

                    // 댓글 객체 생성
                    val comment = Comment(
                        userId = userId,
                        username = username,  // Firestore에서 가져온 사용자 이름
                        text = commentText,
                        timestamp = Date()
                    )

                    val isbn = review.isbn
                    val reviewId = review.reviewId

                    // isbn과 reviewId가 유효하지 않은 경우 처리
                    if (isbn.isEmpty()) {
                        Log.e("ReviewViewHolder", "Invalid bookId")
                        return@addOnSuccessListener
                    }

                    if (reviewId.isEmpty()) {
                        Log.e("ReviewViewHolder", "Invalid reviewId")
                        return@addOnSuccessListener
                    }

                    // Firestore에 댓글 추가
                    FirebaseFirestore.getInstance()
                        .collection("books")
                        .document(isbn)
                        .collection("reviews")
                        .document(reviewId)
                        .collection("comments")
                        .add(comment)
                        .addOnSuccessListener {
                            // 댓글 입력 필드 초기화
                            commentInput.text.clear()
                            // 댓글 다시 로드
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

        private val commentsRecyclerView: RecyclerView = itemView.findViewById(R.id.commentsRecyclerView)
        private val commentInput: EditText = itemView.findViewById(R.id.commentInput)
        private val postCommentButton: Button = itemView.findViewById(R.id.postCommentButton)

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

            // Load existing comments
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

            // Check if isbn and reviewId are valid
            val isbn = templateReview.isbn.ifEmpty { return } // isbn가 비어있으면 함수 종료
            val reviewId = templateReview.reviewId.ifEmpty { return } // reviewId가 비어있으면 함수 종료


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

            // Firestore에서 사용자 이름 가져오기
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    // Firestore에서 username 필드를 가져옴, 없으면 "Anonymous" 사용
                    val username = document.getString("username") ?: "Anonymous"

                    // 댓글 객체 생성
                    val comment = Comment(
                        userId = userId,
                        username = username,  // Firestore에서 가져온 사용자 이름
                        text = commentText,
                        timestamp = Date()
                    )

                    // Get the bookId and reviewId from the templateReview object
                    val isbn = templateReview.isbn
                    val reviewId = templateReview.reviewId

                    // Validate the bookId and reviewId
                    if (isbn.isNullOrEmpty()) {
                        Log.e("TemplateReviewViewHolder", "Invalid bookId: $isbn")
                        return@addOnSuccessListener  // Invalid bookId
                    }

                    if (reviewId.isNullOrEmpty()) {
                        Log.e("TemplateReviewViewHolder", "Invalid reviewId: $reviewId")
                        return@addOnSuccessListener  // Invalid reviewId
                    }

                    // Create document references for the book and review
                    val firestore = FirebaseFirestore.getInstance()
                    val bookDocumentReference = firestore.collection("books").document(isbn)
                    val reviewDocumentReference = bookDocumentReference.collection("reviews").document(reviewId)

                    // Add the comment to the Firestore database under the specific review
                    reviewDocumentReference.collection("comments")
                        .add(comment)
                        .addOnSuccessListener {
                            // Clear the comment input field after posting
                            commentInput.text.clear()

                            // Reload comments after posting
                            loadComments(templateReview)

                            // Log success message
                            Log.d("TemplateReviewViewHolder", "Comment posted successfully")
                        }
                        .addOnFailureListener { exception ->
                            // Log error message in case of failure
                            Log.e("TemplateReviewViewHolder", "Error posting comment", exception)
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e("postComment", "Error fetching user info", exception)
                }
        }
    }
}
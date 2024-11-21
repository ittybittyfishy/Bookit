import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booknook.GroupComment
import com.example.booknook.GroupCommentsAdapter
import com.example.booknook.GroupMemberUpdate
import com.example.booknook.R
import com.example.booknook.fragments.NotificationItem
import com.example.booknook.fragments.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Date

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
    // yunjong Noh (edit)
    // bind and shows notification for updates
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val update = memberUpdates[position]

        when (holder) {
            is StartBookViewHolder -> {
                holder.bind(update)
                sendGroupUpdateNotification(groupId) // Notify on StartBook update
            }
            is FinishBookViewHolder -> {
                holder.bind(update)
                sendGroupUpdateNotification(groupId) // Notify on FinishBook update
            }
            is RecommendBookViewHolder -> {
                holder.bind(update)
                sendGroupUpdateNotification(groupId) // Notify on RecommendBook update
            }
            is ReviewBookNoTemplateViewHolder -> {
                holder.bind(update)
                sendGroupUpdateNotification(groupId) // Notify on Review without Template update
            }
            is ReviewBookTemplateViewHolder -> {
                holder.bind(update)
                sendGroupUpdateNotification(groupId) // Notify on Review with Template update
            }
        }

    // Call the notification function when all updates are bound
        if (position == memberUpdates.size - 1) {
            sendGroupUpdateNotification(groupId)
        }
    }

    override fun getItemCount(): Int = memberUpdates.size

    // Utility function to fetch comments from Firestore
    private fun fetchComments(updateId: String, groupId: String, onSuccess: (List<GroupComment>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("groups")
            .document(groupId)
            .collection("memberUpdates")
            .document(updateId)
            .collection("comments")
            .get()
            .addOnSuccessListener { documents ->
                val comments = documents.map { doc ->
                    GroupComment(
                        userId = doc.getString("userId") ?: "",
                        username = doc.getString("username") ?: "Anonymous",
                        commentText = doc.getString("commentText") ?: "",
                        timestamp = doc.getDate("timestamp"),
                        commentId = doc.id
                    )
                }
                onSuccess(comments)
            }
            .addOnFailureListener { exception ->
                Log.e("GroupUpdateAdapter", "Error loading comments", exception)
            }
    }

    // Handles view for starting a book
    inner class StartBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.profileImage)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val commentsRecyclerView: RecyclerView = itemView.findViewById(R.id.commentsRecyclerView)
        private lateinit var groupCommentsAdapter: GroupCommentsAdapter
        private lateinit var commentsList: MutableList<GroupComment>
        private val commentInput: EditText = itemView.findViewById(R.id.commentInput)
        private val postCommentButton: Button = itemView.findViewById(R.id.postCommentButton)
        private val dismissButton: ImageButton = itemView.findViewById(R.id.dismiss_button)

        fun bind(update: GroupMemberUpdate) {
            commentsList = mutableListOf()
            groupCommentsAdapter = GroupCommentsAdapter(mutableListOf(), groupId, update.updateId)
            commentsRecyclerView.adapter = groupCommentsAdapter
            commentsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)

            fetchComments(update.updateId, groupId) { comments ->
                commentsList.clear()
                commentsList.addAll(comments)
                groupCommentsAdapter.loadComments(groupId, update.updateId)
                Log.d("CommentsList", "Size after fetching: ${commentsList.size}") // Log the size here
            }

            val profileImageUrl = update.profileImageUrl
            if (!profileImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(profileImageUrl) // Load the image from the URL
                    .circleCrop() // Optionally crop it to a circle
                    .into(profileImage) // Set the image into the CircleImageView
            }

            postCommentButton.setOnClickListener {
                val commentText = commentInput.text.toString().trim()
                if (commentText.isNotEmpty()) {
                    // Get current user's username from FirebaseAuth or Firestore
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val db = FirebaseFirestore.getInstance()
                    val userDocRef = db.collection("users").document(currentUser?.uid ?: "") // Retrieve the current user's document

                    userDocRef.get().addOnSuccessListener { documentSnapshot ->
                        val currentUsername = documentSnapshot.getString("username") // Assuming username field is stored in Firestore

                        if (currentUsername != null) {
                            val newComment = GroupComment(
                                commentText = commentText,
                                username = currentUsername, // Use the current user's username
                                userId = currentUser?.uid ?: "", // Use current user's UID
                                timestamp = Date(),
                                commentId = ""
                            )

                            saveCommentToDatabase(groupId, update.updateId, newComment) { success, commentId ->
                                if (success) {
                                    // Once the comment is successfully saved, assign the commentId
                                    newComment.commentId = commentId
                                    commentsList.add(newComment)
                                    // Add the new comment to the adapter and update RecyclerView
                                    groupCommentsAdapter.addComment(newComment)
                                    commentsRecyclerView.smoothScrollToPosition(commentsList.size - 1)
                                }
                            }
                            commentInput.text.clear()
                        } else {
                            Toast.makeText(itemView.context, "Failed to get current username", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { e ->
                        Toast.makeText(itemView.context, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(itemView.context, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }

            dismissButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) { // Ensure the position is valid
                    // Remove the update from the list for this user
                    (memberUpdates as MutableList).removeAt(position)

                    // Notify the adapter about the item removal
                    notifyItemRemoved(position)

                    Toast.makeText(itemView.context, "Update dismissed", Toast.LENGTH_SHORT).show()
                }
            }
            messageTextView.text = "${update.username} started a book: ${update.bookTitle}"

        }

        // Utility function for saving comments to the database
        private fun saveCommentToDatabase(
            groupId: String,
            updateId: String,
            newComment: GroupComment,
            callback: (Boolean, String) -> Unit
        ) {
            val db = FirebaseFirestore.getInstance()
            val commentId = db.collection("groups")
                .document(groupId)
                .collection("memberUpdates")
                .document(updateId)
                .collection("comments")
                .document()
                .id

            val comment = mapOf(
                "commentId" to commentId,
                "commentText" to newComment.commentText,
                "username" to newComment.username,
                "userId" to newComment.userId,
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
                    callback(true, commentId)
                    Toast.makeText(itemView.context, "Comment posted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    callback(false, commentId)
                    Toast.makeText(itemView.context, "Failed to post comment: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Handles view for finishing a book
    inner class FinishBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.profileImage)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        private val commentsRecyclerView: RecyclerView = itemView.findViewById(R.id.commentsRecyclerView)
        private lateinit var groupCommentsAdapter: GroupCommentsAdapter
        private lateinit var commentsList: MutableList<GroupComment>
        private val commentInput: EditText = itemView.findViewById(R.id.commentInput)
        private val postCommentButton: Button = itemView.findViewById(R.id.postCommentButton)
        private val dismissButton: ImageButton = itemView.findViewById(R.id.dismiss_button)

        fun bind(update: GroupMemberUpdate) {
            commentsList = mutableListOf()
            groupCommentsAdapter = GroupCommentsAdapter(mutableListOf(), groupId, update.updateId)
            commentsRecyclerView.adapter = groupCommentsAdapter
            commentsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)

            fetchComments(update.updateId, groupId) { comments ->
                commentsList.clear()
                commentsList.addAll(comments)
                groupCommentsAdapter.loadComments(groupId, update.updateId)
                Log.d("CommentsList", "Size after fetching: ${commentsList.size}") // Log the size here
            }

            val profileImageUrl = update.profileImageUrl
            if (!profileImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(profileImageUrl) // Load the image from the URL
                    .circleCrop() // Optionally crop it to a circle
                    .into(profileImage) // Set the image into the CircleImageView
            }

            postCommentButton.setOnClickListener {
                val commentText = commentInput.text.toString().trim()
                if (commentText.isNotEmpty()) {
                    // Get current user's username from FirebaseAuth or Firestore
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val db = FirebaseFirestore.getInstance()
                    val userDocRef = db.collection("users").document(currentUser?.uid ?: "") // Retrieve the current user's document

                    userDocRef.get().addOnSuccessListener { documentSnapshot ->
                        val currentUsername = documentSnapshot.getString("username") // Assuming username field is stored in Firestore

                        if (currentUsername != null) {
                            val newComment = GroupComment(
                                commentText = commentText,
                                username = currentUsername, // Use the current user's username
                                userId = currentUser?.uid ?: "", // Use current user's UID
                                timestamp = Date(),
                                commentId = ""
                            )

                            saveCommentToDatabase(groupId, update.updateId, newComment) { success, commentId ->
                                if (success) {
                                    // Once the comment is successfully saved, assign the commentId
                                    newComment.commentId = commentId
                                    commentsList.add(newComment)
                                    // Add the new comment to the adapter and update RecyclerView
                                    groupCommentsAdapter.addComment(newComment)
                                    commentsRecyclerView.smoothScrollToPosition(commentsList.size - 1)
                                }
                            }
                            commentInput.text.clear()
                        } else {
                            Toast.makeText(itemView.context, "Failed to get current username", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { e ->
                        Toast.makeText(itemView.context, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(itemView.context, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }

            dismissButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) { // Ensure the position is valid
                    // Remove the update from the list for this user
                    (memberUpdates as MutableList).removeAt(position)

                    // Notify the adapter about the item removal
                    notifyItemRemoved(position)

                    Toast.makeText(itemView.context, "Update dismissed", Toast.LENGTH_SHORT).show()
                }
            }
            messageTextView.text = "${update.username} finished a book: ${update.bookTitle}"
        }

        // Utility function for saving comments to the database
        private fun saveCommentToDatabase(
            groupId: String,
            updateId: String,
            newComment: GroupComment,
            callback: (Boolean, String) -> Unit
        ) {
            val db = FirebaseFirestore.getInstance()
            val commentId = db.collection("groups")
                .document(groupId)
                .collection("memberUpdates")
                .document(updateId)
                .collection("comments")
                .document()
                .id

            val comment = mapOf(
                "commentId" to commentId,
                "commentText" to newComment.commentText,
                "username" to newComment.username,
                "userId" to newComment.userId,
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
                    callback(true, commentId)
                    Toast.makeText(itemView.context, "Comment posted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    callback(false, commentId)
                    Toast.makeText(itemView.context, "Failed to post comment: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Handles view for recommending a book
    inner class RecommendBookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.profileImage)
        private val messageTextView: TextView = itemView.findViewById(R.id.messageText)
        private val bookImageView: ImageView = itemView.findViewById(R.id.bookImage)
        private val titleTextView: TextView = itemView.findViewById(R.id.bookTitle)
        private val authorsTextView: TextView = itemView.findViewById(R.id.bookAuthors)
        private val bookRatingBar: RatingBar = itemView.findViewById(R.id.bookRatingBar)
        private val ratingTextView: TextView = itemView.findViewById(R.id.ratingNumber)
        private val commentsRecyclerView: RecyclerView = itemView.findViewById(R.id.commentsRecyclerView)
        private lateinit var groupCommentsAdapter: GroupCommentsAdapter
        private lateinit var commentsList: MutableList<GroupComment>
        private val commentInput: EditText = itemView.findViewById(R.id.commentInput)
        private val postCommentButton: Button = itemView.findViewById(R.id.postCommentButton)
        private val dismissButton: ImageButton = itemView.findViewById(R.id.dismiss_button)

        fun bind(update: GroupMemberUpdate) {
            commentsList = mutableListOf()
            groupCommentsAdapter = GroupCommentsAdapter(mutableListOf(), groupId, update.updateId)
            commentsRecyclerView.adapter = groupCommentsAdapter
            commentsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)

            fetchComments(update.updateId, groupId) { comments ->
                commentsList.clear()
                commentsList.addAll(comments)
                groupCommentsAdapter.loadComments(groupId, update.updateId)
                Log.d("CommentsList", "Size after fetching: ${commentsList.size}") // Log the size here
            }

            val profileImageUrl = update.profileImageUrl
            if (!profileImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(profileImageUrl) // Load the image from the URL
                    .circleCrop() // Optionally crop it to a circle
                    .into(profileImage) // Set the image into the CircleImageView
            }

            postCommentButton.setOnClickListener {
                val commentText = commentInput.text.toString().trim()
                if (commentText.isNotEmpty()) {
                    // Get current user's username from FirebaseAuth or Firestore
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val db = FirebaseFirestore.getInstance()
                    val userDocRef = db.collection("users").document(currentUser?.uid ?: "") // Retrieve the current user's document

                    userDocRef.get().addOnSuccessListener { documentSnapshot ->
                        val currentUsername = documentSnapshot.getString("username") // Assuming username field is stored in Firestore

                        if (currentUsername != null) {
                            val newComment = GroupComment(
                                commentText = commentText,
                                username = currentUsername, // Use the current user's username
                                userId = currentUser?.uid ?: "", // Use current user's UID
                                timestamp = Date(),
                                commentId = ""
                            )

                            saveCommentToDatabase(groupId, update.updateId, newComment) { success, commentId ->
                                if (success) {
                                    // Once the comment is successfully saved, assign the commentId
                                    newComment.commentId = commentId
                                    commentsList.add(newComment)
                                    // Add the new comment to the adapter and update RecyclerView
                                    groupCommentsAdapter.addComment(newComment)
                                    commentsRecyclerView.smoothScrollToPosition(commentsList.size - 1)
                                }
                            }
                            commentInput.text.clear()
                        } else {
                            Toast.makeText(itemView.context, "Failed to get current username", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { e ->
                        Toast.makeText(itemView.context, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(itemView.context, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }

            dismissButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) { // Ensure the position is valid
                    // Remove the update from the list for this user
                    (memberUpdates as MutableList).removeAt(position)

                    // Notify the adapter about the item removal
                    notifyItemRemoved(position)

                    Toast.makeText(itemView.context, "Update dismissed", Toast.LENGTH_SHORT).show()
                }
            }

            messageTextView.text = "${update.username} recommended book: ${update.bookTitle}"
            titleTextView.text = update.bookTitle
            authorsTextView.text = update.bookAuthors
            bookRatingBar.rating = update.bookRating!!
            ratingTextView.text = update.bookRating.toString()

            Glide.with(itemView.context)
                .load(update.bookImage)
                .placeholder(R.drawable.placeholder_image)
                .into(bookImageView)
        }

        // Utility function for saving comments to the database
        private fun saveCommentToDatabase(
            groupId: String,
            updateId: String,
            newComment: GroupComment,
            callback: (Boolean, String) -> Unit
        ) {
            val db = FirebaseFirestore.getInstance()
            val commentId = db.collection("groups")
                .document(groupId)
                .collection("memberUpdates")
                .document(updateId)
                .collection("comments")
                .document()
                .id

            val comment = mapOf(
                "commentId" to commentId,
                "commentText" to newComment.commentText,
                "username" to newComment.username,
                "userId" to newComment.userId,
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
                    callback(true, commentId)
                    Toast.makeText(itemView.context, "Comment posted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    callback(false, commentId)
                    Toast.makeText(itemView.context, "Failed to post comment: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Handles view for writing a review without a template
    inner class ReviewBookNoTemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.profileImage)
        private val reviewTextView: TextView = itemView.findViewById(R.id.messageText)
        private val reviewTitle: TextView = itemView.findViewById(R.id.reviewTitle)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        private val ratingNumber: TextView = itemView.findViewById(R.id.ratingNumber)
        private val reviewText: TextView = itemView.findViewById(R.id.reviewText)
        private val spoilerText: TextView = itemView.findViewById(R.id.spoilerText)
        private val sensitiveTopicsText: TextView = itemView.findViewById(R.id.sensitiveTopicsText)
        private val commentsRecyclerView: RecyclerView = itemView.findViewById(R.id.commentsRecyclerView)
        private lateinit var groupCommentsAdapter: GroupCommentsAdapter
        private lateinit var commentsList: MutableList<GroupComment>
        private val commentInput: EditText = itemView.findViewById(R.id.commentInput)
        private val postCommentButton: Button = itemView.findViewById(R.id.postCommentButton)
        private val dismissButton: ImageButton = itemView.findViewById(R.id.dismiss_button)

        fun bind(update: GroupMemberUpdate) {
            commentsList = mutableListOf()
            groupCommentsAdapter = GroupCommentsAdapter(mutableListOf(), groupId, update.updateId)
            commentsRecyclerView.adapter = groupCommentsAdapter
            commentsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)

            fetchComments(update.updateId, groupId) { comments ->
                commentsList.clear()
                commentsList.addAll(comments)
                groupCommentsAdapter.loadComments(groupId, update.updateId)
                Log.d("CommentsList", "Size after fetching: ${commentsList.size}") // Log the size here
            }

            val profileImageUrl = update.profileImageUrl
            if (!profileImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(profileImageUrl) // Load the image from the URL
                    .circleCrop() // Optionally crop it to a circle
                    .into(profileImage) // Set the image into the CircleImageView
            }

            postCommentButton.setOnClickListener {
                val commentText = commentInput.text.toString().trim()
                if (commentText.isNotEmpty()) {
                    // Get current user's username from FirebaseAuth or Firestore
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val db = FirebaseFirestore.getInstance()
                    val userDocRef = db.collection("users").document(currentUser?.uid ?: "") // Retrieve the current user's document

                    userDocRef.get().addOnSuccessListener { documentSnapshot ->
                        val currentUsername = documentSnapshot.getString("username") // Assuming username field is stored in Firestore

                        if (currentUsername != null) {
                            val newComment = GroupComment(
                                commentText = commentText,
                                username = currentUsername, // Use the current user's username
                                userId = currentUser?.uid ?: "", // Use current user's UID
                                timestamp = Date(),
                                commentId = ""
                            )

                            saveCommentToDatabase(groupId, update.updateId, newComment) { success, commentId ->
                                if (success) {
                                    // Once the comment is successfully saved, assign the commentId
                                    newComment.commentId = commentId
                                    commentsList.add(newComment)
                                    // Add the new comment to the adapter and update RecyclerView
                                    groupCommentsAdapter.addComment(newComment)
                                    commentsRecyclerView.smoothScrollToPosition(commentsList.size - 1)
                                }
                            }
                            commentInput.text.clear()
                        } else {
                            Toast.makeText(itemView.context, "Failed to get current username", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { e ->
                        Toast.makeText(itemView.context, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(itemView.context, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }

            dismissButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) { // Ensure the position is valid
                    // Remove the update from the list for this user
                    (memberUpdates as MutableList).removeAt(position)

                    // Notify the adapter about the item removal
                    notifyItemRemoved(position)

                    Toast.makeText(itemView.context, "Update dismissed", Toast.LENGTH_SHORT).show()
                }
            }

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
        }

        // Utility function for saving comments to the database
        private fun saveCommentToDatabase(
            groupId: String,
            updateId: String,
            newComment: GroupComment,
            callback: (Boolean, String) -> Unit
        ) {
            val db = FirebaseFirestore.getInstance()
            val commentId = db.collection("groups")
                .document(groupId)
                .collection("memberUpdates")
                .document(updateId)
                .collection("comments")
                .document()
                .id

            val comment = mapOf(
                "commentId" to commentId,
                "commentText" to newComment.commentText,
                "username" to newComment.username,
                "userId" to newComment.userId,
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
                    callback(true, commentId)
                    Toast.makeText(itemView.context, "Comment posted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    callback(false, commentId)
                    Toast.makeText(itemView.context, "Failed to post comment: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Sets up view for writing a review with a template update
    inner class ReviewBookTemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.profileImage)
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

        private val commentsRecyclerView: RecyclerView = itemView.findViewById(R.id.commentsRecyclerView)
        private lateinit var groupCommentsAdapter: GroupCommentsAdapter
        private lateinit var commentsList: MutableList<GroupComment>
        private val commentInput: EditText = itemView.findViewById(R.id.commentInput)
        private val postCommentButton: Button = itemView.findViewById(R.id.postCommentButton)
        private val dismissButton: ImageButton = itemView.findViewById(R.id.dismiss_button)


        fun bind(update: GroupMemberUpdate) {
            commentsList = mutableListOf()
            groupCommentsAdapter = GroupCommentsAdapter(mutableListOf(), groupId, update.updateId)
            commentsRecyclerView.adapter = groupCommentsAdapter
            commentsRecyclerView.layoutManager = LinearLayoutManager(itemView.context)

            fetchComments(update.updateId, groupId) { comments ->
                commentsList.clear()
                commentsList.addAll(comments)
                groupCommentsAdapter.loadComments(groupId, update.updateId)
                Log.d("CommentsList", "Size after fetching: ${commentsList.size}") // Log the size here
            }

            val profileImageUrl = update.profileImageUrl
            if (!profileImageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(profileImageUrl) // Load the image from the URL
                    .circleCrop() // Optionally crop it to a circle
                    .into(profileImage) // Set the image into the CircleImageView
            }

            postCommentButton.setOnClickListener {
                val commentText = commentInput.text.toString().trim()
                if (commentText.isNotEmpty()) {
                    // Get current user's username from FirebaseAuth or Firestore
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val db = FirebaseFirestore.getInstance()
                    val userDocRef = db.collection("users").document(currentUser?.uid ?: "") // Retrieve the current user's document

                    userDocRef.get().addOnSuccessListener { documentSnapshot ->
                        val currentUsername = documentSnapshot.getString("username") // Assuming username field is stored in Firestore

                        if (currentUsername != null) {
                            val newComment = GroupComment(
                                commentText = commentText,
                                username = currentUsername, // Use the current user's username
                                userId = currentUser?.uid ?: "", // Use current user's UID
                                timestamp = Date(),
                                commentId = ""
                            )

                            saveCommentToDatabase(groupId, update.updateId, newComment) { success, commentId ->
                                if (success) {
                                    // Once the comment is successfully saved, assign the commentId
                                    newComment.commentId = commentId
                                    commentsList.add(newComment)
                                    // Add the new comment to the adapter and update RecyclerView
                                    groupCommentsAdapter.addComment(newComment)
                                    commentsRecyclerView.smoothScrollToPosition(commentsList.size - 1)
                                }
                            }
                            commentInput.text.clear()
                        } else {
                            Toast.makeText(itemView.context, "Failed to get current username", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { e ->
                        Toast.makeText(itemView.context, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(itemView.context, "Comment cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }

            dismissButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) { // Ensure the position is valid
                    // Remove the update from the list for this user
                    (memberUpdates as MutableList).removeAt(position)

                    // Notify the adapter about the item removal
                    notifyItemRemoved(position)

                    Toast.makeText(itemView.context, "Update dismissed", Toast.LENGTH_SHORT).show()
                }
            }

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

        // Utility function for saving comments to the database
        private fun saveCommentToDatabase(
            groupId: String,
            updateId: String,
            newComment: GroupComment,
            callback: (Boolean, String) -> Unit
        ) {
            val db = FirebaseFirestore.getInstance()
            val commentId = db.collection("groups")
                .document(groupId)
                .collection("memberUpdates")
                .document(updateId)
                .collection("comments")
                .document()
                .id

            val comment = mapOf(
                "commentId" to commentId,
                "commentText" to newComment.commentText,
                "username" to newComment.username,
                "userId" to newComment.userId,
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
                    callback(true, commentId)
                    Toast.makeText(itemView.context, "Comment posted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    callback(false, commentId)
                    Toast.makeText(itemView.context, "Failed to post comment: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    // Yunjong Noh
    // Function to send a simple notification for any group update
    private fun sendGroupUpdateNotification(groupId: String) {
        val db = FirebaseFirestore.getInstance()
        val currentTime = System.currentTimeMillis()
        val expirationTime = currentTime + 10 * 24 * 60 * 60 * 1000 // 10 days expiration time

        // Simple notification message
        val notificationMessage = "There is a new update in your group."

        // Get the current user ID (sender)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Fetch current user's profile details
        val currentUserDocRef = db.collection("users").document(currentUserId)
        currentUserDocRef.get().addOnSuccessListener { currentUserDoc ->
            if (currentUserDoc.exists()) {
                val senderProfileImageUrl = currentUserDoc.getString("profileImageUrl") ?: ""
                val senderUsername = currentUserDoc.getString("username") ?: "Unknown User"

                // Send notification to all group members (excluding sender)
                sendNotificationToGroupMembers(
                    groupId,
                    notificationMessage,
                    NotificationType.GROUP_MESSAGES,
                    expirationTime,
                    currentUserId,
                    senderProfileImageUrl,
                    senderUsername
                )
            }
        }.addOnFailureListener {
            Log.e("GroupUpdateNotification", "Failed to retrieve current user data for notification.")
        }
    }


    // Yunjong Noh
    // Function to send notification (with sender's details like profile image and username)
    private fun sendNotification(userId: String, message: String, notificationType: NotificationType, expirationTime: Long, senderId: String, receiverId: String, senderProfileImageUrl: String, senderUsername: String) {
        val db = FirebaseFirestore.getInstance()

        // Skip sending notification if the current user is the sender (userId is the same as currentUserId)
        if (userId == FirebaseAuth.getInstance().currentUser?.uid) {
            Log.d("ReviewNotification", "Notification not sent to the sender (userId = currentUserId).")
            return
        }

        val notification = NotificationItem(
            userId = userId,  // Receiver's ID
            senderId = senderId,  // Sender's ID
            receiverId = receiverId,  // Receiver's ID
            message = message,
            timestamp = System.currentTimeMillis(),
            type = notificationType,
            dismissed = false,
            expirationTime = expirationTime,
            profileImageUrl = senderProfileImageUrl, // Use sender's profile image
            username = senderUsername // Use sender's username
        )

        // Add the notification to the "notifications" collection in Firestore
        db.collection("notifications").add(notification)
            .addOnSuccessListener { documentReference ->
                val notificationId = documentReference.id
                db.collection("notifications").document(notificationId)
                    .update("notificationId", notificationId)
                    .addOnSuccessListener {
                        Log.d("ReviewNotification", "Notification added with ID: $notificationId") // Log success
                    }
                    .addOnFailureListener { e ->
                        Log.e("ReviewNotification", "Error updating notificationId: ${e.message}", e) // Log any errors
                    }
            }
            .addOnFailureListener { e ->
                Log.e("ReviewNotification", "Error adding notification: ${e.message}", e) // Log any errors adding the notification
            }
    }
    // Yunjong Noh
    // Existing function modified to handle sending notifications to group members
    private fun sendNotificationToGroupMembers(
        groupId: String,
        message: String,
        notificationType: NotificationType,
        expirationTime: Long,
        senderId: String,
        senderProfileImageUrl: String,
        senderUsername: String
    ) {
        val db = FirebaseFirestore.getInstance() // Get an instance of the Firestore database

        // Fetch the group document from Firestore using the groupId
        db.collection("groups").document(groupId).get()
            .addOnSuccessListener { groupDoc ->
                if (groupDoc.exists()) { // Check if the group document exists
                    // Get the list of group member IDs from the document
                    val groupMembers = groupDoc.get("members") as? List<String> ?: emptyList()

                    // Iterate over each member in the group
                    groupMembers.forEach { memberId ->
                        // Only send the notification if the member is not the sender
                        if (memberId != FirebaseAuth.getInstance().currentUser?.uid) {
                            // Call the sendNotification function to send the notification to the member
                            sendNotification(
                                memberId,
                                message,
                                notificationType,
                                expirationTime,
                                senderId,
                                memberId,
                                senderProfileImageUrl,
                                senderUsername
                            )
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                // Log an error if there is a problem fetching the group document
                Log.e("GroupUpdateNotification", "Error fetching group members: ${e.message}", e)
            }
    }

}
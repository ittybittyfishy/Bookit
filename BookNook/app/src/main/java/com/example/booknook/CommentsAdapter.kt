package com.example.booknook

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.fragments.NotificationItem
import com.example.booknook.fragments.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Yunjong Noh
// Adapter class for managing a list of comments in a RecyclerView
class CommentsAdapter(private var comments: List<Comment>) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    // ViewHolder class to hold the views for each comment item
    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val username: TextView = itemView.findViewById(R.id.commentUsername) // Username of the commenter
        private val commentText: TextView = itemView.findViewById(R.id.commentText) // Text of the comment
        private val timestamp: TextView = itemView.findViewById(R.id.commentTimestamp) // Timestamp of the comment
        private val repliesRecyclerView: RecyclerView = itemView.findViewById(R.id.repliesRecyclerView) // RecyclerView for replies
        private val replyInput: EditText = itemView.findViewById(R.id.replyInput) // Input field for entering a reply
        private val postReplyButton: Button = itemView.findViewById(R.id.postReplyButton) // Button to post the reply

        // Yunjong Noh
        // Prevent multiple click events
        private var isPostingReply = false

        // Bind the comment data to the views
        fun bind(comment: Comment) {
            // Set the comment's username, text, and timestamp
            username.text = comment.username
            commentText.text = comment.text
            timestamp.text = comment.timestamp.toString()

            // Initialize RepliesAdapter to manage replies to this comment
            val repliesAdapter = RepliesAdapter(listOf(), comment) // Pass the comment to the RepliesAdapter
            repliesRecyclerView.adapter = repliesAdapter // Set the adapter for the replies RecyclerView
            repliesRecyclerView.layoutManager = LinearLayoutManager(itemView.context) // Use LinearLayoutManager for the RecyclerView

            // Load replies from Firestore when the comment is bound
            repliesAdapter.loadReplies()
            // Yunjong Noh
            // Set up the post reply button's click listener
            postReplyButton.setOnClickListener {
                if (isPostingReply) return@setOnClickListener // Prevent multiple clicks

                val replyText = replyInput.text.toString() // Get the text from the reply input
                if (replyText.isNotBlank()) { // Check if the reply text is not empty
                    isPostingReply = true // Prevent further clicks until the process is done
                    repliesAdapter.postReply(replyText, replyInput) // Post the reply

                    // Add notification functionality: Only send notification to the comment author
                    val senderId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
                    sendNotificationToReviewerOnly(comment.userId, senderId, replyText)

                    // Re-enable posting after a short delay to prevent accidental multiple clicks
                    isPostingReply = false
                }
            }
        }
        // Yunjong Noh
        // Function to send a notification to the review author only
        private fun sendNotificationToReviewerOnly(receiverId: String, senderId: String, replyText: String) {
            if (receiverId == senderId) return // Do not send a notification if the sender and receiver are the same

            val db = FirebaseFirestore.getInstance()
            val currentTime = System.currentTimeMillis()
            val expirationTime = currentTime + 10 * 24 * 60 * 60 * 1000 // Notification expires in 10 days

            // Fetch the current user's information
            val currentUserDocRef = db.collection("users").document(senderId)
            currentUserDocRef.get().addOnSuccessListener { currentUserDoc ->
                if (currentUserDoc.exists()) {
                    // Retrieve sender's profile picture and its username
                    val senderProfileImageUrl = currentUserDoc.getString("profileImageUrl") ?: ""
                    val senderUsername = currentUserDoc.getString("username") ?: "Unknown User"

                    // Create the notification message
                    val notificationMessage = "You have a new reply: $replyText"

                    // Send the notification to the review author
                    sendNotification(receiverId, notificationMessage, expirationTime, senderId, senderProfileImageUrl, senderUsername)
                }
            }
        }
        // Yunjong Noh
        // Function to send an individual notification
        private fun sendNotification(receiverId: String, message: String, expirationTime: Long, senderId: String, senderProfileImageUrl: String, senderUsername: String) {
            val db = FirebaseFirestore.getInstance()

            val notification = NotificationItem(
                userId = receiverId, // Receiver's ID
                senderId = senderId, // Sender's ID
                receiverId = receiverId, // Receiver's ID
                message = message,
                timestamp = System.currentTimeMillis(),
                type = NotificationType.REVIEW_REPLY,
                dismissed = false,
                expirationTime = expirationTime,
                profileImageUrl = senderProfileImageUrl,
                username = senderUsername
            )
            // iterates notification collection
            db.collection("notifications").add(notification)
                .addOnSuccessListener { documentReference ->
                    val notificationId = documentReference.id
                    db.collection("notifications").document(notificationId)
                        // updates notification ID
                        .update("notificationId", notificationId)
                        .addOnSuccessListener {
                            Log.d("CommentsAdapter", "Notification added with ID: $notificationId")
                        }
                        .addOnFailureListener { e ->
                            Log.e("CommentsAdapter", "Error updating notificationId: ${e.message}", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("CommentsAdapter", "Error adding notification: ${e.message}", e)
                }
        }
    }

    // Create a new ViewHolder for comment items
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(view) // Yunjong Noh
    }

    // Bind the comment data to the ViewHolder
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position]) // Yunjong Noh
    }

    // Return the total number of comments
    override fun getItemCount(): Int = comments.size

    // Update the list of comments and notify the RecyclerView to refresh
    // Yunjong Noh
    fun updateComments(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged()
    }
    // Yunjong Noh
    // Function to load comments from Firestore
    fun loadComments(isbn: String, reviewId: String) {
        // Access the Firestore database instance
        FirebaseFirestore.getInstance()
            .collection("books")
            .document(isbn)
            .collection("reviews")
            .document(reviewId)
            .collection("comments")
            .get()
            .addOnSuccessListener { documents ->
                val commentsList = mutableListOf<Comment>() // List to store comments
                for (document in documents) {
                    val comment = document.toObject(Comment::class.java) // Convert document to Comment object
                    val commentId = document.id
                    Log.d("CommentsAdapter", "Loaded comment with ID: $commentId")
                    commentsList.add(comment)
                }
                updateComments(commentsList) // Update app with loaded comments
            }
            .addOnFailureListener { exception ->
                Log.e("CommentsAdapter", "Error loading comments", exception)
            }
    }
}
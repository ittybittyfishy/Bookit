package com.example.booknook

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.fragments.NotificationItem
import com.example.booknook.fragments.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

// Yunjong Noh
// RepliesAdapter class to manage replies to comments in the app
class RepliesAdapter(private var replies: List<Reply>, private val comment: Comment) : RecyclerView.Adapter<RepliesAdapter.ReplyViewHolder>() {

    // ViewHolder for each reply item
    class ReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val username: TextView = itemView.findViewById(R.id.replyUsername) // Username of the replier
        private val replyText: TextView = itemView.findViewById(R.id.replyText) // Text of the reply
        private val timestamp: TextView = itemView.findViewById(R.id.replyTimestamp) // Timestamp of the reply

        // Bind reply data to the views
        fun bind(reply: Reply) {
            username.text = reply.username // Set the username
            replyText.text = reply.text // Set the reply text
            timestamp.text = reply.timestamp.toString() // Set the timestamp
        }
    }

    // Create a new ViewHolder for replies
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.reply_item, parent, false) // Inflate reply layout
        return ReplyViewHolder(view) // Return the ViewHolder
    }

    // Bind the reply data to the ViewHolder
    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        holder.bind(replies[position]) // Bind data for the reply at the given position
    }

    // Return the total number of replies
    override fun getItemCount(): Int = replies.size

    // Update the list of replies and refresh the RecyclerView
    fun updateReplies(newReplies: List<Reply>) {
        replies = newReplies // Update replies with new data
        notifyDataSetChanged() // Notify the RecyclerView to refresh
    }

    // Function to post a reply to Firestore
    fun postReply(replyText: String, replyInput: EditText) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: ""

        // Fetch user information to get the username
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val username = document.getString("username") ?: "Anonymous"

                // Create a Reply object
                val reply = Reply(
                    userId = userId,
                    username = username,
                    text = replyText,
                    timestamp = Date()
                )

                // Retrieve necessary IDs for Firestore
                val isbn = comment.isbn
                val reviewId = comment.reviewId
                val commentId = comment.commentId

                // Ensure valid IDs before adding reply to Firestore
                if (isbn.isNotEmpty() && reviewId.isNotEmpty() && commentId.isNotEmpty()) {
                    val replyRef = FirebaseFirestore.getInstance()
                        .collection("books")
                        .document(isbn)
                        .collection("reviews")
                        .document(reviewId)
                        .collection("comments")
                        .document(commentId)
                        .collection("replies")
                        .document() // Auto-generate the reply ID

                    replyRef.set(reply) // Add reply to Firestore
                        .addOnSuccessListener {
                            Log.d("RepliesAdapter", "Reply added successfully")
                            replyInput.text.clear() // Clear the input field
                            loadReplies() // Reload replies to update the display
                            // Yunjong Noh
                            // Send notification to the original commenter
                            sendNotification(userId, comment.userId, replyText)
                        }
                        .addOnFailureListener { exception ->
                            Log.e("RepliesAdapter", "Error adding reply", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("RepliesAdapter", "Error fetching user info", exception)
            }
    }
    // Yunjong Noh
    // Function to send a notification to Firestore
    private fun sendNotification(senderId: String, receiverId: String, replyText: String) {
        if (receiverId.isEmpty()) {
            Log.e("RepliesAdapter", "Receiver ID is empty, notification not sent")
            return
        }

        // Create the notification item
        val notification = NotificationItem(
            notificationId = FirebaseFirestore.getInstance().collection("notifications").document().id,
            userId = receiverId, // The user who will receive the notification
            senderId = senderId, // The user who sent the reply
            message = "You have a new reply: $replyText",
            timestamp = System.currentTimeMillis(),
            type = NotificationType.REVIEW_REPLY, // Type of the notification
            dismissed = false
        )

        // Save the notification to Firestore
        FirebaseFirestore.getInstance()
            .collection("notifications")
            .document(notification.notificationId)
            .set(notification)
            .addOnSuccessListener {
                Log.d("RepliesAdapter", "Notification sent successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("RepliesAdapter", "Error sending notification", exception)
            }
    }

    // Load replies from Firestore for the specific comment
    fun loadReplies() {
        val isbn = comment.isbn // Get the ISBN from the comment
        val reviewId = comment.reviewId // Get the review ID from the comment
        val commentId = comment.commentId // Get the comment ID from the comment

        // Ensure valid IDs before querying Firestore
        if (isbn.isNotEmpty() && reviewId.isNotEmpty() && commentId.isNotEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("books")
                .document(isbn)
                .collection("reviews")
                .document(reviewId)
                .collection("comments")
                .document(commentId)
                .collection("replies")
                .get() // Get replies from Firestore
                .addOnSuccessListener { documents ->
                    val repliesList = mutableListOf<Reply>() // Initialize list for replies
                    for (document in documents) {
                        // Convert each document to a Reply object
                        val reply = document.toObject(Reply::class.java)
                        repliesList.add(reply) // Add to replies list
                    }
                    updateReplies(repliesList) // Update adapter with new replies
                }
                .addOnFailureListener { exception ->
                    Log.e("RepliesAdapter", "Error loading replies", exception) // Log any errors
                }
        }
    }
}

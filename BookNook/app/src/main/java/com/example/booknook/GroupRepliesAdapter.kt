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
class GroupRepliesAdapter(
    private var replies: List<GroupReply>,
    private val groupComment: GroupComment,
    private val groupId: String,
    private val updateId: String
) : RecyclerView.Adapter<GroupRepliesAdapter.ReplyViewHolder>() {

    // ViewHolder for each reply item
    class ReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val username: TextView = itemView.findViewById(R.id.replyUsername) // Username of the replier
        private val replyText: TextView = itemView.findViewById(R.id.replyText) // Text of the reply
        private val timestamp: TextView = itemView.findViewById(R.id.replyTimestamp) // Timestamp of the reply

        // Bind reply data to the views
        fun bind(reply: GroupReply) {
            username.text = reply.username // Set the username
            replyText.text = reply.replyText // Set the reply text
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
    fun updateReplies(newReplies: List<GroupReply>) {
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

                // Create a GroupReply object
                val reply = GroupReply(
                    userId = userId,
                    username = username,
                    replyText = replyText,
                    timestamp = Date()
                )

                val commentId = groupComment.commentId // Get the comment ID from the comment

                // Ensure valid IDs before adding reply to Firestore
                if (groupId.isNotEmpty() && updateId.isNotEmpty() && commentId.isNotEmpty()) {
                    val replyRef = FirebaseFirestore.getInstance()
                        .collection("groups")
                        .document(groupId)
                        .collection("memberUpdates")
                        .document(updateId)
                        .collection("comments")
                        .document(commentId)
                        .collection("replies")
                        .document() // Auto-generate the reply ID

                    replyRef.set(reply) // Add reply to Firestore
                        .addOnSuccessListener {
                            Log.d("RepliesAdapter", "Reply added successfully")
                            replyInput.text.clear() // Clear the input field
                            loadReplies() // Reload replies to update the display
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

    // Load replies from Firestore for the specific comment
    fun loadReplies() {
        val commentId = groupComment.commentId // Get the comment ID from the comment

        // Ensure valid IDs before querying Firestore
        if (groupId.isNotEmpty() && updateId.isNotEmpty() && commentId.isNotEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("groups")
                .document(groupId)
                .collection("memberUpdates")
                .document(updateId)
                .collection("comments")
                .document(commentId)
                .collection("replies")
                .get() // Get replies from Firestore
                .addOnSuccessListener { documents ->
                    val repliesList = mutableListOf<GroupReply>() // Initialize list for replies
                    for (document in documents) {
                        // Convert each document to a GroupReply object
                        val reply = document.toObject(GroupReply::class.java)
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

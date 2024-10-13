package com.example.booknook

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

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

            // Set up the post reply button's click listener
            postReplyButton.setOnClickListener {
                val replyText = replyInput.text.toString() // Get the text from the reply input
                if (replyText.isNotBlank()) { // Check if the reply text is not empty
                    repliesAdapter.postReply(replyText, replyInput) // Post the reply
                }
            }
        }
    }

    // Create a new ViewHolder for comment items
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        // Inflate the layout for a comment item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(view) // Return the created ViewHolder
    }

    // Bind the comment data to the ViewHolder
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position]) // Bind the comment at the given position
    }

    // Return the total number of comments
    override fun getItemCount(): Int = comments.size

    // Update the list of comments and notify the RecyclerView to refresh
    fun updateComments(newComments: List<Comment>) {
        comments = newComments // Update the comments list with new data
        notifyDataSetChanged() // Notify the RecyclerView to refresh its data
    }
}
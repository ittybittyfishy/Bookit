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
import de.hdodenhof.circleimageview.CircleImageView

class GroupCommentsAdapter(private var comments: List<GroupComment>) : RecyclerView.Adapter<GroupCommentsAdapter.GroupCommentViewHolder>() {
    // ViewHolder class to hold the views for each comment item
    class GroupCommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        fun bind(comment: GroupComment) {
            // Set the comment's username, text, and timestamp
            username.text = comment.username
            commentText.text = comment.commentText
            Log.d("Comment", "Comment Text: ${comment.commentText}")
            timestamp.text = comment.timestamp.toString()

            // Initialize RepliesAdapter to manage replies to this comment
            val groupRepliesAdapter = GroupRepliesAdapter(listOf(), comment) // Pass the comment to the RepliesAdapter
            repliesRecyclerView.adapter = groupRepliesAdapter // Set the adapter for the replies RecyclerView
            repliesRecyclerView.layoutManager = LinearLayoutManager(itemView.context) // Use LinearLayoutManager for the RecyclerView

            // Load replies from Firestore when the comment is bound
//            groupRepliesAdapter.loadReplies()
            // Yunjong Noh
            // Set up the post reply button's click listener
//            postReplyButton.setOnClickListener {
//                if (isPostingReply) return@setOnClickListener // Prevent multiple clicks
//
//                val replyText = replyInput.text.toString() // Get the text from the reply input
//                if (replyText.isNotBlank()) { // Check if the reply text is not empty
//                    isPostingReply = true // Prevent further clicks until the process is done
//                    groupRepliesAdapter.postReply(replyText, replyInput) // Post the reply
//
//                    // Add notification functionality: Only send notification to the comment author
//                    val senderId = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener
//
//                    // Re-enable posting after a short delay to prevent accidental multiple clicks
//                    isPostingReply = false
//                }
//            }
        }
    }

    // Create a new ViewHolder for comment items
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupCommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return GroupCommentViewHolder(view)
    }

    // Bind the comment data to the ViewHolder
    override fun onBindViewHolder(holder: GroupCommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    // Return the total number of comments
    override fun getItemCount(): Int = comments.size

    // Update the list of comments and notify the RecyclerView to refresh
    fun updateComments(newComments: List<GroupComment>) {
        comments = newComments
        notifyDataSetChanged()
    }

    // Function to load comments from Firestore
    fun loadComments(groupId: String, updateId: String) {
        FirebaseFirestore.getInstance()
            .collection("groups")
            .document(groupId)
            .collection("memberUpdates")
            .document(updateId)
            .collection("comments")
            .get()
            .addOnSuccessListener { documents ->
                val commentsList = mutableListOf<GroupComment>()
                for (document in documents) {
                    val comment = document.toObject(GroupComment::class.java)
                    val commentId = document.id
                    Log.d("CommentsAdapter", "Loaded comment with ID: $commentId")
                    commentsList.add(comment)
                }
                updateComments(commentsList)
            }
            .addOnFailureListener { exception ->
                Log.e("CommentsAdapter", "Error loading comments", exception)
            }
    }
}

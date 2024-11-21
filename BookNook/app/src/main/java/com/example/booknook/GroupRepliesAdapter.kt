package com.example.booknook

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.fragments.NotificationItem
import com.example.booknook.fragments.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import java.util.UUID

// Yunjong Noh
// RepliesAdapter class to manage replies to comments in the app
class GroupRepliesAdapter(
    private var replies: MutableList<GroupReply>,
    private val groupComment: GroupComment,
    private val groupId: String,
    private val updateId: String,
    private val commentId: String
) : RecyclerView.Adapter<GroupRepliesAdapter.GroupReplyViewHolder>() {

    // ViewHolder for each reply item
    class GroupReplyViewHolder(
        itemView: View,
        private val replies: MutableList<GroupReply>,
        private val adapter: GroupRepliesAdapter
    ) : RecyclerView.ViewHolder(itemView) {
        private val username: TextView = itemView.findViewById(R.id.replyUsername) // Username of the replier
        private val replyText: TextView = itemView.findViewById(R.id.replyText) // Text of the reply
        private val timestamp: TextView = itemView.findViewById(R.id.replyTimestamp) // Timestamp of the reply
        private val likeButton: ImageButton = itemView.findViewById(R.id.like_button)
        private val dislikeButton: ImageButton = itemView.findViewById(R.id.dislike_button)
        private val numLikes: TextView = itemView.findViewById(R.id.num_likes)
        private val numDislikes: TextView = itemView.findViewById(R.id.num_dislikes)

        // Bind reply data to the views
        fun bind(reply: GroupReply, groupId: String, updateId: String, commentId: String) {
            username.text = reply.username // Set the username
            replyText.text = reply.replyText // Set the reply text
            timestamp.text = reply.timestamp.toString() // Set the timestamp

            // Initialize the like/dislike counts
            numLikes.text = "${reply.numLikes}"
            numDislikes.text = "${reply.numDislikes}"

            val db = FirebaseFirestore.getInstance()
            val replyRef = db.collection("groups")
                .document(groupId)
                .collection("memberUpdates")
                .document(updateId)
                .collection("comments")
                .document(commentId)
                .collection("replies")
                .document(reply.replyId)
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

            // Track user's like/dislike status
            var userAction: String? = null // Can be "like", "dislike", or null

            // Check user's like/dislike status
            replyRef.collection("likes").document(userId).get()
                .addOnSuccessListener { doc ->
                    userAction = doc.getString("type")
                    updateLikeDislikeUI(userAction)
                }

            likeButton.setOnClickListener {
                handleLikeDislike(
                    replyRef, userId, "like",
                    currentAction = userAction,
                    onComplete = { updatedAction ->
                        userAction = updatedAction
                        updateLikeDislikeUI(userAction)
                    }
                )
            }

            dislikeButton.setOnClickListener {
                handleLikeDislike(
                    replyRef, userId, "dislike",
                    currentAction = userAction,
                    onComplete = { updatedAction ->
                        userAction = updatedAction
                        updateLikeDislikeUI(userAction)
                    }
                )
            }
        }

        // Update the button designs based on the current user action
        private fun updateLikeDislikeUI(userAction: String?) {
            when (userAction) {
                "like" -> {
                    likeButton.setImageResource(R.drawable.selected_thumbs_up)
                    dislikeButton.setImageResource(R.drawable.thumbs_down)
                }

                "dislike" -> {
                    likeButton.setImageResource(R.drawable.thumbs_up)
                    dislikeButton.setImageResource(R.drawable.selected_thumbs_down)
                }

                else -> {
                    likeButton.setImageResource(R.drawable.thumbs_up)
                    dislikeButton.setImageResource(R.drawable.thumbs_down)
                }
            }
        }

        // Handle like/dislike logic
        private fun handleLikeDislike(
            commentRef: DocumentReference,
            userId: String,
            action: String,
            currentAction: String?,
            onComplete: (String?) -> Unit
        ) {
            val isSameAction = currentAction == action
            val newAction = if (isSameAction) null else action
            val increment = if (newAction == null) -1 else 1

            val updates = mutableMapOf<String, Any>(
                "num${action.replaceFirstChar { it.uppercaseChar() }}s" to FieldValue.increment(
                    increment.toLong()
                )
            )

            if (currentAction != null && !isSameAction) {
                updates["num${currentAction.replaceFirstChar { it.uppercaseChar() }}s"] =
                    FieldValue.increment(-1)
            }

            // Update Firestore within a transaction
            FirebaseFirestore.getInstance().runTransaction { transaction ->
                transaction.update(commentRef, updates)

                // Adds "likes" subcollection to track the type of like and the users that liked/disliked
                val userDoc = commentRef.collection("likes").document(userId)
                if (newAction == null) {
                    transaction.delete(userDoc)
                } else {
                    transaction.set(userDoc, mapOf("type" to newAction))
                }
            }.addOnSuccessListener {
                Log.d("CommentsAdapter", "Successfully updated $action")

                // After transaction completes, fetch the updated values and update the UI
                commentRef.get()
                    .addOnSuccessListener { document ->
                        val updatedReply = document.toObject(GroupReply::class.java)
                        val position = bindingAdapterPosition
                        if (position != RecyclerView.NO_POSITION && updatedReply != null) {
                            // Update the local comment object with new values from Firestore
                            replies[position] = updatedReply.copy(
                                numLikes = updatedReply.numLikes,
                                numDislikes = updatedReply.numDislikes
                            )

                            // Directly update the UI elements with the values in the database
                            numLikes.text = "${updatedReply.numLikes}"
                            numDislikes.text = "${updatedReply.numDislikes}"
                        }

                        onComplete(newAction)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("CommentsAdapter", "Error fetching updated comment data", exception)
                    }
            }.addOnFailureListener { exception ->
                Log.e("CommentsAdapter", "Error updating like/dislike", exception)
            }
        }
    }

    // Create a new ViewHolder for replies
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupReplyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_reply, parent, false) // Inflate reply layout
        return GroupReplyViewHolder(view, replies, this) // Return the ViewHolder
    }

    // Bind the reply data to the ViewHolder
    override fun onBindViewHolder(holder: GroupReplyViewHolder, position: Int) {
        holder.bind(replies[position], groupId, updateId, commentId) // Bind data for the reply at the given position
    }

    // Return the total number of replies
    override fun getItemCount(): Int = replies.size

    // Update the list of replies and refresh the RecyclerView
    fun updateReplies(newReplies: MutableList<GroupReply>) {
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
                        .document() // Create a new document with an auto-generated ID

                    val replyId = replyRef.id // Get the auto-generated ID

                    // Create a GroupReply object
                    val reply = GroupReply(
                        userId = userId,
                        username = username,
                        replyText = replyText,
                        timestamp = Date(),
                        numLikes = 0,
                        numDislikes = 0,
                        replyId = replyId // Set the auto-generated ID here
                    )

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

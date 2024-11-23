package com.example.booknook

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.fragments.NotificationItem
import com.example.booknook.fragments.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Date

class GroupCommentsAdapter(
    private var comments: MutableList<GroupComment>,
    private val groupId: String,
    private val updateId: String
    ) : RecyclerView.Adapter<GroupCommentsAdapter.GroupCommentViewHolder>() {

    fun addComment(newComment: GroupComment) {
        comments.add(newComment) // Add the new comment to the list
        notifyItemInserted(comments.size - 1) // Notify adapter about the new item
    }

    // ViewHolder class to hold the views for each comment item
    class GroupCommentViewHolder(itemView: View,
                                 private val comments: MutableList<GroupComment>,
                                 private val adapter: GroupCommentsAdapter
    ) : RecyclerView.ViewHolder(itemView) {
        private val username: TextView = itemView.findViewById(R.id.commentUsername) // Username of the commenter
        private val commentText: TextView = itemView.findViewById(R.id.commentText) // Text of the comment
        private val timestamp: TextView = itemView.findViewById(R.id.commentTimestamp) // Timestamp of the comment
        private val repliesRecyclerView: RecyclerView = itemView.findViewById(R.id.repliesRecyclerView) // RecyclerView for replies
        private val replyInput: EditText = itemView.findViewById(R.id.replyInput) // Input field for entering a reply
        private val postReplyButton: Button = itemView.findViewById(R.id.postReplyButton) // Button to post the reply
        private val likeButton: ImageButton = itemView.findViewById(R.id.like_button)
        private val dislikeButton: ImageButton = itemView.findViewById(R.id.dislike_button)
        private val numLikes: TextView = itemView.findViewById(R.id.num_likes)
        private val numDislikes: TextView = itemView.findViewById(R.id.num_dislikes)

        // Yunjong Noh
        // Prevent multiple click events
        private var isPostingReply = false

        // Bind the comment data to the views
        fun bind(comment: GroupComment, groupId: String, updateId: String) {
            // Set the comment's username, text, and timestamp
            username.text = comment.username
            commentText.text = comment.commentText
            timestamp.text = comment.timestamp.toString()

            // Initialize the like/dislike counts
            numLikes.text = "${comment.numLikes}"
            numDislikes.text = "${comment.numDislikes}"

            val db = FirebaseFirestore.getInstance()
            val commentRef = db.collection("groups")
                .document(groupId)
                .collection("memberUpdates")
                .document(updateId)
                .collection("comments")
                .document(comment.commentId)
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

            // Fetch group membership
            val currentUser = FirebaseAuth.getInstance().currentUser
            val groupDocRef = db.collection("groups").document(groupId)

            groupDocRef.get().addOnSuccessListener { documentSnapshot ->
                val members = documentSnapshot.get("members") as? List<String> ?: emptyList()

                val isMember = currentUser?.uid in members

                // Hide reply input and button if user is not a member
                if (!isMember) {
                    replyInput.visibility = View.GONE
                    postReplyButton.visibility = View.GONE
                } else {
                    replyInput.visibility = View.VISIBLE
                    postReplyButton.visibility = View.VISIBLE
                }

                // Track user's like/dislike status
                var userAction: String? = null // Can be "like", "dislike", or null

                if (isMember) {
                    // Check user's like/dislike status
                    commentRef.collection("likes").document(userId).get()
                        .addOnSuccessListener { doc ->
                            userAction = doc.getString("type")
                            updateLikeDislikeUI(userAction)
                        }

                    likeButton.setOnClickListener {
                        handleLikeDislike(
                            commentRef, userId, "like",
                            currentAction = userAction,
                            onComplete = { updatedAction ->
                                userAction = updatedAction
                                updateLikeDislikeUI(userAction)
                            }
                        )
                    }

                    dislikeButton.setOnClickListener {
                        handleLikeDislike(
                            commentRef, userId, "dislike",
                            currentAction = userAction,
                            onComplete = { updatedAction ->
                                userAction = updatedAction
                                updateLikeDislikeUI(userAction)
                            }
                        )
                    }
                } else {
                    // Show toast if non-member clicks like/dislike
                    likeButton.setOnClickListener {
                        Toast.makeText(itemView.context, "Join the group to like or dislike a comment", Toast.LENGTH_SHORT).show()
                    }
                    dislikeButton.setOnClickListener {
                        Toast.makeText(itemView.context, "Join the group to like or dislike a comment", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Initialize RepliesAdapter to manage replies to this comment
            val groupRepliesAdapter = GroupRepliesAdapter(mutableListOf(), comment, groupId, updateId, comment.commentId) // Pass the comment to the RepliesAdapter
            repliesRecyclerView.adapter = groupRepliesAdapter // Set the adapter for the replies RecyclerView
            repliesRecyclerView.layoutManager = LinearLayoutManager(itemView.context) // Use LinearLayoutManager for the RecyclerView

            // Load replies from Firestore when the comment is bound
            groupRepliesAdapter.loadReplies()
            // Yunjong Noh
            // Set up the post reply button's click listener
            postReplyButton.setOnClickListener {
                if (isPostingReply) {
                    return@setOnClickListener
                }
                val replyText = replyInput.text.toString()

                if (replyText.isNotBlank()) {
                    isPostingReply = true
                    groupRepliesAdapter.postReply(replyText, replyInput)
                } else {
                    Log.d("RepliesAdapter", "Reply text is blank, not posting")
                }
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
                "num${action.replaceFirstChar { it.uppercaseChar() }}s" to FieldValue.increment(increment.toLong())
            )

            if (currentAction != null && !isSameAction) {
                updates["num${currentAction.replaceFirstChar { it.uppercaseChar() }}s"] = FieldValue.increment(-1)
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
                        val updatedComment = document.toObject(GroupComment::class.java)
                        val position = bindingAdapterPosition
                        if (position != RecyclerView.NO_POSITION && updatedComment != null) {
                            // Update the local comment object with new values from Firestore
                            comments[position] = updatedComment.copy(
                                numLikes = updatedComment.numLikes,
                                numDislikes = updatedComment.numDislikes
                            )

                            // Directly update the UI elements with the values in the database
                            numLikes.text = "${updatedComment.numLikes}"
                            numDislikes.text = "${updatedComment.numDislikes}"
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

    // Create a new ViewHolder for comment items
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupCommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_comment, parent, false)
        return GroupCommentViewHolder(view, comments, this)
    }

    // Bind the comment data to the ViewHolder
    override fun onBindViewHolder(holder: GroupCommentViewHolder, position: Int) {
        holder.bind(comments[position], groupId, updateId)
    }

    // Return the total number of comments
    override fun getItemCount(): Int = comments.size

    // Update the list of comments and notify the RecyclerView to refresh
    fun updateComments(newComments: MutableList<GroupComment>) {
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
                    val comment = document.toObject(GroupComment::class.java).copy(commentId = document.id)
                    commentsList.add(comment)
                }
                updateComments(commentsList)
            }
            .addOnFailureListener { exception ->
                Log.e("CommentsAdapter", "Error loading comments", exception)
            }
    }
}

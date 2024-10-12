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

class CommentsAdapter(private var comments: List<Comment>) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val username: TextView = itemView.findViewById(R.id.commentUsername)
        private val commentText: TextView = itemView.findViewById(R.id.commentText)
        private val timestamp: TextView = itemView.findViewById(R.id.commentTimestamp)

        private val repliesRecyclerView: RecyclerView = itemView.findViewById(R.id.repliesRecyclerView)
        private val replyInput: EditText = itemView.findViewById(R.id.replyInput)
        private val postReplyButton: Button = itemView.findViewById(R.id.postReplyButton)

        fun bind(comment: Comment) {
            username.text = comment.username
            commentText.text = comment.text
            timestamp.text = comment.timestamp.toString()

            // Load replies
            loadReply(comment)

            postReplyButton.setOnClickListener {
                val replyText = replyInput.text.toString()
                if (replyText.isNotBlank()) {
                    postReply(comment, replyText)
                }
            }
        }

        private fun postReply(comment: Comment, replyText: String) {
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid ?: ""

            // Firestore에서 사용자 정보 가져오기
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val username = document.getString("username") ?: "Anonymous"

                    // Reply 객체 생성
                    val reply = Reply(
                        userId = userId,
                        username = username,
                        text = replyText,
                        timestamp = Date()
                    )

                    // Firestore에 Reply 추가
                    val isbn = comment.isbn
                    val reviewId = comment.reviewId
                    val commentId = comment.commentId

                    // 경로 값이 유효한지 확인
                    if (isbn.isNotEmpty() && reviewId.isNotEmpty() && commentId.isNotEmpty()) {
                        FirebaseFirestore.getInstance()
                            .collection("books")
                            .document(isbn)
                            .collection("reviews")
                            .document(reviewId)
                            .collection("comments")
                            .document(commentId)
                            .update("replies", FieldValue.arrayUnion(reply))
                            .addOnSuccessListener {
                                Log.d("postReply", "Reply added successfully")
                                replyInput.text.clear()
                                loadReply(comment) // 이 부분에서 답글을 다시 로드
                            }
                            .addOnFailureListener { exception ->
                                Log.e("postReply", "Error adding reply", exception)
                            }
                    } else {
                        Log.e("postReply", "Invalid paths: ISBN, reviewId, or commentId is empty")
                        // Provide user feedback if necessary
                        Toast.makeText(itemView.context, "Unable to post reply. Please check the details.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("postReply", "Error fetching user info", exception)
                }
        }

        private fun loadReply(comment: Comment) {
            val repliesAdapter = RepliesAdapter(listOf())
            repliesRecyclerView.adapter = repliesAdapter
            repliesRecyclerView.layoutManager = LinearLayoutManager(itemView.context)

            val isbn = comment.isbn
            val reviewId = comment.reviewId
            val commentId = comment.commentId

            if (isbn.isNotEmpty() && reviewId.isNotEmpty() && commentId.isNotEmpty()) {
                FirebaseFirestore.getInstance()
                    .collection("books")
                    .document(isbn)
                    .collection("reviews")
                    .document(reviewId)
                    .collection("comments")
                    .document(commentId)
                    .collection("replies")
                    .get()
                    .addOnSuccessListener { documents ->
                        val replies = documents.map { it.toObject(Reply::class.java) }
                        repliesAdapter.updateReplies(replies)

                    }
                    .addOnFailureListener { exception ->
                        Log.e("loadReply", "Error loading replies", exception)
                    }
            } else {
                Log.e("loadReply", "Invalid paths: ISBN, reviewId, or commentId is empty")
            }
        }
    }

    // RepliesAdapter
    class RepliesAdapter(private var replies: List<Reply>) : RecyclerView.Adapter<RepliesAdapter.ReplyViewHolder>() {

        class ReplyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val username: TextView = itemView.findViewById(R.id.replyUsername)
            private val replyText: TextView = itemView.findViewById(R.id.replyText)
            private val timestamp: TextView = itemView.findViewById(R.id.replyTimestamp)

            fun bind(reply: Reply) {
                username.text = reply.username
                replyText.text = reply.text
                timestamp.text = reply.timestamp.toString()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.reply_item, parent, false)
            return ReplyViewHolder(view)
        }

        override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
            holder.bind(replies[position])
        }

        override fun getItemCount(): Int = replies.size

        fun updateReplies(newReplies: List<Reply>) {
            replies = newReplies
            notifyDataSetChanged()  // 데이터가 변경되었음을 RecyclerView에 알립니다.
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    override fun getItemCount(): Int = comments.size

    fun updateComments(newComments: List<Comment>) {
        comments = newComments
        notifyDataSetChanged()
    }
}
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
            // 댓글 정보 설정
            username.text = comment.username
            commentText.text = comment.text
            timestamp.text = comment.timestamp.toString()

            // RepliesAdapter 설정 (답글 표시)
            val repliesAdapter = RepliesAdapter(listOf(), comment) // comment 전달
            repliesRecyclerView.adapter = repliesAdapter
            repliesRecyclerView.layoutManager = LinearLayoutManager(itemView.context)

            // Firestore에서 답글 로드
            repliesAdapter.loadReplies()

            // Post Reply 버튼 설정
            postReplyButton.setOnClickListener {
                val replyText = replyInput.text.toString()
                if (replyText.isNotBlank()) {
                    repliesAdapter.postReply(replyText, replyInput)
                }
            }
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
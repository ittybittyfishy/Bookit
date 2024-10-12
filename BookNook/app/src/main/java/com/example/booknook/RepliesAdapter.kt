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

class RepliesAdapter(private var replies: List<Reply>, private val comment: Comment) : RecyclerView.Adapter<RepliesAdapter.ReplyViewHolder>() {

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
        notifyDataSetChanged()
    }

    // 답글 Firestore에 추가
    fun postReply(replyText: String, replyInput: EditText) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: ""

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val username = document.getString("username") ?: "Anonymous"

                // Firestore에 답글 추가
                val reply = Reply(
                    userId = userId,
                    username = username,
                    text = replyText,
                    timestamp = Date()
                )

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
                        .add(reply)
                        .addOnSuccessListener {
                            Log.d("RepliesAdapter", "Reply added successfully")
                            replyInput.text.clear()
                            loadReplies() // 답글 다시 로드하여 갱신
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

    // Firestore에서 답글 로드
    fun loadReplies() {
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
                    val repliesList = mutableListOf<Reply>()
                    for (document in documents) {
                        val reply = document.toObject(Reply::class.java)
                        repliesList.add(reply)
                    }
                    updateReplies(repliesList) // 새로 받은 답글로 업데이트
                }
                .addOnFailureListener { exception ->
                    Log.e("RepliesAdapter", "Error loading replies", exception)
                }
        }
    }
}



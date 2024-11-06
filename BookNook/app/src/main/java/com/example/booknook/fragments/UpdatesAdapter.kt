package com.example.booknook.fragments

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booknook.R
import de.hdodenhof.circleimageview.CircleImageView

// Yunjong Noh
class UpdatesAdapter(
    private val notifications: MutableList<NotificationItem>,
    private val onDismiss: (String) -> Unit // onDismiss 콜백 파라미터 추가
) : RecyclerView.Adapter<UpdatesAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)

        Log.d("UpdatesAdapter", "Binding notification: ${notification.message}, Type: ${notification.type}")
    }

    override fun getItemCount(): Int = notifications.size

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: CircleImageView = itemView.findViewById(R.id.profileImage)
        private val messageType: TextView = itemView.findViewById(R.id.messageType)
        private val messageBody: TextView = itemView.findViewById(R.id.messageBody)
        private val dismissButton: ImageButton = itemView.findViewById(R.id.dismiss_button)

        fun bind(notification: NotificationItem) {
            messageBody.text = notification.message
            messageType.text = when (notification.type) {
                NotificationType.FRIEND_REQUEST -> "Friend Request"
                NotificationType.GROUP_JOIN_REQUEST -> "Group Join Request"
                NotificationType.GROUP_MESSAGES -> "Group Message"
                NotificationType.FRIEND_STARTED_BOOK -> "Friend Started a Book"
                NotificationType.FRIEND_FINISHED_BOOK -> "Friend Finished a Book"
                NotificationType.REVIEW_REPLY -> "Review Reply"
            }

            Glide.with(itemView.context)
                .load(R.drawable.profile_picture_placeholder)
                .error(R.drawable.profile_picture_placeholder) // 로드 실패 시 기본 이미지 표시
                .into(profileImage)

            dismissButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val dismissedNotification = notifications[position]
                    notifications.removeAt(position)
                    notifyItemRemoved(position)
                    onDismiss(dismissedNotification.notificationId) // Pass the correct notificationId here
                    Log.d("UpdatesAdapter", "Notification dismissed: ${dismissedNotification.message}")
                }
            }
        }
    }
}
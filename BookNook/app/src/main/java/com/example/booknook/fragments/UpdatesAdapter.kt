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
// Adapter class for displaying notifications in a RecyclerView
class UpdatesAdapter(
    private val notifications: MutableList<NotificationItem>, // List of notifications
    private val onDismiss: (String) -> Unit // Callback for dismissing a notification
) : RecyclerView.Adapter<UpdatesAdapter.NotificationViewHolder>() {

    // Create and inflate the view holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    // Bind notification data to the view holder
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification)
        Log.d("UpdatesAdapter", "Binding notification: ${notification.message}, Type: ${notification.type}")
    }

    // Return the total number of notifications
    override fun getItemCount(): Int = notifications.size

    // ViewHolder class for handling the layout of each notification item
    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageType: TextView = itemView.findViewById(R.id.messageType)
        private val messageBody: TextView = itemView.findViewById(R.id.messageBody)
        private val dismissButton: ImageButton = itemView.findViewById(R.id.dismiss_button)
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val profileImage: CircleImageView = itemView.findViewById(R.id.profileImage)

        // Bind data to the view elements
        fun bind(notification: NotificationItem) {
            userName.text = notification.username // Set the user's name
            messageBody.text = notification.message // Set the notification message
            messageType.text = when (notification.type) { // Set the notification type
                NotificationType.FRIEND_REQUEST -> "Friend Request"
                NotificationType.GROUP_JOIN_REQUEST -> "Group Join Request"
                NotificationType.GROUP_MESSAGES -> "Group Message"
                NotificationType.FRIEND_STARTED_BOOK -> "Friend Started a Book"
                NotificationType.FRIEND_FINISHED_BOOK -> "Friend Finished a Book"
                NotificationType.REVIEW_REPLY -> "Review Reply"
                NotificationType.REVIEW_EDIT -> "Review Edit"
                NotificationType.REVIEW_ADDED -> "Review Added"
            }

            // Load the profile image using Glide
            Glide.with(itemView.context)
                .load(notification.profileImageUrl)
                .placeholder(R.drawable.profile_picture_placeholder)
                .error(R.drawable.profile_picture_placeholder)
                .into(profileImage)

            // Handle the dismiss button click
            dismissButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val dismissedNotification = notifications[position]
                    notifications.removeAt(position) // Remove the notification from the list
                    notifyItemRemoved(position) // Notify the adapter about item removal
                    onDismiss(dismissedNotification.notificationId) // Trigger the onDismiss callback
                    Log.d("UpdatesAdapter", "Notification dismissed: ${dismissedNotification.message}")
                }
            }
        }
    }
}
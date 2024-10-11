package com.example.booknook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GroupAdapter(
    private val groupList: List<GroupItem>,
    private val clickListener: (GroupItem) -> Unit
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupName: TextView = itemView.findViewById(R.id.groupNameText)
        val groupInfo: TextView = itemView.findViewById(R.id.groupInfoText)
        val tagsChipGroup: ChipGroup = itemView.findViewById(R.id.tagsChipGroup)
        val bannerImageView: ImageView = itemView.findViewById(R.id.groupBannerImage)


        fun bind(groupItem: GroupItem, clickListener: (GroupItem) -> Unit) {
            groupName.text = groupItem.groupName

            // Fetch and display group creator's username
            getUsernameFromFirestore(groupItem.createdBy) { username ->
                groupInfo.text = "${username ?: "Unknown"} • ${groupItem.members.size} members • ${if (groupItem.private) "Private" else "Public"}"
            }


            // Populate ChipGroup with tags
            tagsChipGroup.removeAllViews()
            for (tag in groupItem.tags) {
                val chip = Chip(itemView.context)
                chip.text = tag
                chip.isCheckable = false
                tagsChipGroup.addView(chip)
            }

            // Load the banner image
            if (!groupItem.bannerImg.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(groupItem.bannerImg)
                    .into(bannerImageView)
            } else {
                bannerImageView.setImageResource(R.drawable.mr_blobby)  // Fallback image
            }

            // Handle item clicks
            itemView.setOnClickListener {
                clickListener(groupItem)
            }

        }
        // Fetch the username from Firestore using the userId
        private fun getUsernameFromFirestore(userId: String, callback: (String?) -> Unit) {
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val username = document.get("username") as? String
                    callback(username)
                }
                .addOnFailureListener {
                    callback(null)  // Return null in case of error
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val groupItem = groupList[position]
        holder.bind(groupItem, clickListener)
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

}
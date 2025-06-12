package com.example.myeclinic.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myeclinic.R
import com.example.myeclinic.presenter.UserData

class UsersAdapter(
    val onUserClicked: (UserData, clickedRole: Boolean) -> Unit
) : ListAdapter<UserData, UsersAdapter.UserViewHolder>(DiffCallback()) {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.userName)
        val role = itemView.findViewById<TextView>(R.id.userRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.name.text = user.name
        holder.role.text = user.role

        holder.name.setOnClickListener { onUserClicked(user, false) }
        holder.role.setOnClickListener { onUserClicked(user, true) }
    }

    class DiffCallback : DiffUtil.ItemCallback<UserData>() {
        override fun areItemsTheSame(oldItem: UserData, newItem: UserData) = oldItem.userId == newItem.userId
        override fun areContentsTheSame(oldItem: UserData, newItem: UserData) = oldItem == newItem
    }
}

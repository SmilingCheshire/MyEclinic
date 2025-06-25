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
/**
 * RecyclerView adapter for displaying a list of users (patients, doctors, or admins).
 *
 * Provides two click listeners:
 * - Clicking the name invokes the callback with `clickedRole = false`
 * - Clicking the role invokes the callback with `clickedRole = true`
 *
 * @property onUserClicked Callback invoked when either the user's name or role is clicked.
 *                         The boolean indicates whether the role was clicked (`true`) or the name (`false`).
 */
class UsersAdapter(
    val onUserClicked: (UserData, clickedRole: Boolean) -> Unit
) : ListAdapter<UserData, UsersAdapter.UserViewHolder>(DiffCallback()) {
    /**
     * ViewHolder for displaying a user's name and role.
     *
     * @property name The TextView displaying the user's name.
     * @property role The TextView displaying the user's role.
     */
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.userName)
        val role = itemView.findViewById<TextView>(R.id.userRole)
    }
    /**
     * Inflates the layout for a single user item view.
     *
     * @param parent The parent view group.
     * @param viewType The view type (not used here).
     * @return A new instance of [UserViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }
    /**
     * Binds a [UserData] item to the view holder.
     *
     * Sets up click listeners for both the user's name and role.
     *
     * @param holder The view holder to bind.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.name.text = user.name
        holder.role.text = user.role

        holder.name.setOnClickListener { onUserClicked(user, false) }
        holder.role.setOnClickListener { onUserClicked(user, true) }
    }
    /**
     * Utility class for efficiently comparing [UserData] items.
     */
    class DiffCallback : DiffUtil.ItemCallback<UserData>() {
        override fun areItemsTheSame(oldItem: UserData, newItem: UserData) = oldItem.userId == newItem.userId
        override fun areContentsTheSame(oldItem: UserData, newItem: UserData) = oldItem == newItem
    }
}

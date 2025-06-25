package com.example.myeclinic.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myeclinic.R
import com.example.myeclinic.presenter.CommunicationOversightPresenter.ChatInfo
/**
 * RecyclerView adapter for displaying a list of chat summaries in a communication oversight view.
 *
 * Uses [ListAdapter] for efficient list diffing and supports click interaction on chat items.
 *
 * @property onChatClick Callback triggered when a chat item is clicked.
 */
class ChatListAdapter(
    private val onChatClick: (ChatInfo) -> Unit
) : ListAdapter<ChatInfo, ChatListAdapter.ChatViewHolder>(DiffCallback) {
    /**
     * Inflates the layout for an individual chat summary item.
     *
     * @param parent The parent view group.
     * @param viewType The type of view (not used here as there's only one type).
     * @return A new instance of [ChatViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_summary, parent, false)
        return ChatViewHolder(view)
    }
    /**
     * Binds a [ChatInfo] item to a [ChatViewHolder].
     *
     * @param holder The view holder to bind the data to.
     * @param position The position of the item in the adapter.
     */
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    /**
     * ViewHolder for displaying chat participant names.
     *
     * Shows a formatted string indicating the participants in the chat.
     */
    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val namesText: TextView = itemView.findViewById(R.id.chatNames)
        /**
         * Binds the given [ChatInfo] to the view.
         *
         * Sets the names of the chat participants and configures the click listener.
         *
         * @param chat The chat item to display.
         */
        fun bind(chat: ChatInfo) {
            namesText.text = "${chat.user1Name} ↔ ${chat.user2Name}"
            itemView.setOnClickListener { onChatClick(chat) }
        }
    }
    /**
     * A [DiffUtil.ItemCallback] implementation for efficiently updating the chat list.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<ChatInfo>() {
        override fun areItemsTheSame(oldItem: ChatInfo, newItem: ChatInfo): Boolean =
            oldItem.chatId == newItem.chatId

        override fun areContentsTheSame(oldItem: ChatInfo, newItem: ChatInfo): Boolean =
            oldItem == newItem
    }
}

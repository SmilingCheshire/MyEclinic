package com.example.myeclinic.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myeclinic.R
import com.example.myeclinic.presenter.ChatListPresenter.ChatPreview
/**
 * RecyclerView adapter for displaying a list of chat previews.
 *
 * Each item shows the name(s) of the participants, the last message,
 * and a formatted timestamp. The view adjusts based on whether the current user is an admin.
 *
 * @property onChatClicked Callback triggered when a chat preview item is clicked.
 *                         Provides the chat ID and the ID of the other participant.
 */
class ChatPreviewAdapter(
    private val onChatClicked: (chatId: String, otherUserId: String) -> Unit
) : RecyclerView.Adapter<ChatPreviewAdapter.ViewHolder>() {

    private val chats = mutableListOf<ChatPreview>()
    /**
     * Updates the adapter's data with a new list of [ChatPreview] items and refreshes the UI.
     *
     * @param newList A new list of chat preview data to display.
     */
    fun submitList(newList: List<ChatPreview>) {
        chats.clear()
        chats.addAll(newList)
        notifyDataSetChanged()
    }
    /**
     * Inflates the layout for a single chat preview item.
     *
     * @param parent The parent view group.
     * @param viewType The type of view (not used here).
     * @return A new [ViewHolder] instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_preview, parent, false)
        return ViewHolder(view)
    }
    /**
     * Returns the total number of chat preview items in the list.
     *
     * @return The number of items.
     */
    override fun getItemCount(): Int = chats.size
    /**
     * Binds the data from the specified [ChatPreview] item to the view holder.
     *
     * @param holder The view holder to bind data to.
     * @param position The position of the item in the adapter.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(chats[position])
    }
    /**
     * ViewHolder for a single chat preview item.
     *
     * Displays the name(s) of the chat participants, the last message, and the timestamp.
     * Formats the timestamp as either time ("hh:mm a") if from today or as date ("MMM dd") otherwise.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val username: TextView = itemView.findViewById(R.id.usernameTextView)
        private val lastMessage: TextView = itemView.findViewById(R.id.lastMessageTextView)
        private val timestampView: TextView = itemView.findViewById(R.id.timestampTextView)
        /**
         * Binds a [ChatPreview] object to the UI elements of the view.
         *
         * Adjusts participant name formatting based on user role (admin vs non-admin),
         * formats and displays the last message timestamp,
         * and handles click events.
         *
         * @param chat The [ChatPreview] item to display.
         */
        fun bind(chat: ChatPreview) {
            val currentUser = com.example.myeclinic.util.UserSession.currentUser

            username.text = if (currentUser?.role == "Admin") {
                // Show both names
                "${chat.user1Name} ↔ ${chat.user2Name}"
            } else {
                // Show the other user's name only
                if (chat.user1Id == currentUser?.userId) chat.user2Name else chat.user1Name
            }

            lastMessage.text = chat.lastMessage

            chat.timestamp?.let { date ->
                val now = java.util.Calendar.getInstance()
                val msgCal = java.util.Calendar.getInstance().apply { time = date }

                val formatted = if (
                    now.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR) &&
                    now.get(java.util.Calendar.DAY_OF_YEAR) == msgCal.get(java.util.Calendar.DAY_OF_YEAR)
                ) {
                    android.text.format.DateFormat.format("hh:mm a", date).toString()
                } else {
                    android.text.format.DateFormat.format("MMM dd", date).toString()
                }

                timestampView.text = formatted
            } ?: run {
                timestampView.text = ""
            }

            itemView.setOnClickListener {
                val otherUserId = if (chat.user1Id == currentUser?.userId) chat.user2Id else chat.user1Id
                onChatClicked(chat.chatId, otherUserId)
            }
    }
    }

}

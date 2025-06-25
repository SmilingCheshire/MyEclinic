package com.example.myeclinic.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myeclinic.R
import com.example.myeclinic.presenter.ChatPresenter
import com.example.myeclinic.util.UserSession
import com.example.myeclinic.presenter.ChatPresenter.Message
/**
 * RecyclerView adapter for displaying a list of chat messages.
 *
 * Distinguishes between sent and received messages using different layouts.
 * Admin users see sender names; other users do not.
 */
class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private val messages = mutableListOf<ChatPresenter.Message>()
    private val currentUserId = UserSession.currentUser?.userId
    /**
     * Updates the adapter's message list and refreshes the UI.
     *
     * @param newMessages The new list of messages to display.
     */
    fun submitList(newMessages: List<Message>) {
        messages.clear()
        messages.addAll(newMessages)
        notifyDataSetChanged()
    }
    /**
     * Determines the view type for each message based on whether it's sent or received.
     *
     * @param position The index of the message.
     * @return `1` for messages sent by the current user, `0` for received messages.
     */
    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) 1 else 0
    }
    /**
     * Inflates the appropriate message layout based on message direction (sent or received).
     *
     * @param parent The parent view group.
     * @param viewType The view type as returned by [getItemViewType].
     * @return A new instance of [MessageViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == 1)
            R.layout.item_message_sent
        else
            R.layout.item_message_received

        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }
    /**
     * Binds message data to the view holder.
     *
     * @param holder The view holder to bind.
     * @param position The position of the message in the list.
     */
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }
    /**
     * Returns the number of messages in the list.
     *
     * @return The size of the message list.
     */
    override fun getItemCount() = messages.size
    /**
     * ViewHolder for displaying an individual chat message.
     *
     * Uses different text visibility depending on user role (e.g., admins see sender names).
     */
    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val messageText: TextView = view.findViewById(R.id.messageText)
        private val senderNameText: TextView = view.findViewById(R.id.senderNameText)

        /**
         * Binds a [Message] to the UI elements of the message item view.
         *
         * @param msg The message to bind.
         */
        fun bind(msg: Message) {
            messageText.text = msg.text

            if (UserSession.currentUser?.role == "Admin") {
                senderNameText.visibility = View.VISIBLE
                // Display sender ID or name (ideally use name if available)
                senderNameText.text = "From: ${msg.senderName}"
            } else {
                senderNameText.visibility = View.GONE
            }
        }
    }
}

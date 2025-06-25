package com.example.myeclinic.presenter

import com.google.firebase.firestore.FirebaseFirestore
import com.example.myeclinic.util.UserSession
import java.util.Date

class ChatListPresenter(
    private val onChatsLoaded: (List<ChatPreview>) -> Unit,
    private val onError: (String) -> Unit
) {
    private val db = FirebaseFirestore.getInstance()
    private val userId = UserSession.currentUser?.userId ?: ""

    /**
     * Loads the list of chat previews for the currently logged-in user.
     *
     * Depending on the user's role (Doctor, Patient, or Admin), this function accesses the correct Firestore
     * path, retrieves the list of chat IDs, and then initiates loading of chat details.
     *
     * Triggers [onChatsLoaded] callback with an empty list if no chats are found or an error occurs.
     */
    fun loadChatPreviews() {
        val role = UserSession.currentUser?.role ?: return
        val path = when (role) {
            "Doctor" -> "doctors"
            "Patient" -> "patients"
            "Admin" -> "admins"
            else -> return
        }

        db.collection(path).document(userId).get()
            .addOnSuccessListener { doc ->
                val chatList = (doc["chats"] as? List<*>)
                    ?.mapNotNull { (it as? Map<*, *>)?.get("chatId") as? String }
                    ?: emptyList()
                println("Fetched chat list: $chatList")

                if (chatList.isEmpty()) {
                    onChatsLoaded(emptyList())
                    return@addOnSuccessListener
                }

                fetchChatDetails(chatList)
            }
            .addOnFailureListener {
                onError("Failed to load chats: ${it.message}")
            }
    }

    /**
     * Fetches detailed chat metadata for each provided chat ID.
     *
     * For each chat:
     * - Retrieves participants and their names
     * - Loads the last message and timestamp
     * - Calculates unread message count for the current user
     * - Identifies the other participant in the conversation
     *
     * The collected chat previews are sorted by most recent timestamp and returned via [onChatsLoaded].
     *
     * @param chatIds A list of chat document IDs to be queried from the Firestore "chats" collection.
     */
    private fun fetchChatDetails(chatIds: List<String>) {
        val previews = mutableListOf<ChatPreview>()
        val collection = db.collection("chats")
        var fetched = 0

        for (id in chatIds) {
            collection.document(id).get().addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    fetched++
                    if (fetched == chatIds.size) {
                        onChatsLoaded(previews.sortedByDescending { it.timestamp })
                    }
                    return@addOnSuccessListener
                }
                val participants = id.split("_")

                val Id1 = participants.getOrNull(0) ?: ""
                val Id2 = participants.getOrNull(1) ?: ""

                val currentUserId = UserSession.currentUser?.userId ?: ""
                val user1Id = doc.getString("user1Id") ?: Id1
                val user2Id = doc.getString("user2Id") ?: Id2
                val user1Name = doc.getString("user1Name") ?: "Unknown"
                val user2Name = doc.getString("user2Name") ?: "Unknown"

                val lastMessage = doc.getString("lastMessage") ?: "No messages yet"
                val lastTimestamp = doc.getTimestamp("lastTimestamp")
                val unreadCount = (doc.get("unreadCounts.$userId") as? Long)?.toInt() ?: 0

                val otherUserId: String
                val otherUserName: String

                if (currentUserId == user1Id) {
                    otherUserId = user2Id
                    otherUserName = user2Name
                } else {
                    otherUserId = user1Id
                    otherUserName = user1Name
                }

                previews.add(
                    ChatPreview(
                        chatId = id,
                        user1Id = user1Id,
                        user1Name = user1Name,
                        user2Id = user2Id,
                        user2Name = user2Name,
                        lastMessage = lastMessage,
                        timestamp = lastTimestamp?.toDate(),
                        unreadCount = unreadCount
                    ).copy(user2Id = otherUserId, user2Name = otherUserName)
                )

                fetched++
                if (fetched == chatIds.size) {
                    onChatsLoaded(previews.sortedByDescending { it.timestamp })
                }
            }.addOnFailureListener {
                fetched++
                if (fetched == chatIds.size) {
                    onChatsLoaded(previews.sortedByDescending { it.timestamp })
                }
            }
        }
    }

    /**
     * Data class representing a chat preview item.
     *
     * Used for displaying chat summaries in a UI list. Includes metadata about participants,
     * the last message, timestamp, and unread message count.
     *
     * @property chatId The unique identifier of the chat.
     * @property user1Id The ID of the first participant.
     * @property user1Name The name of the first participant.
     * @property user2Id The ID of the second participant or the "other" user for the current user.
     * @property user2Name The name of the second participant or the "other" user for the current user.
     * @property lastMessage The last sent message in the chat.
     * @property timestamp The timestamp of the last message, if available.
     * @property unreadCount The number of unread messages for the current user.
     */
    data class ChatPreview(
        val chatId: String,
        val user1Id: String,
        val user1Name: String,
        val user2Id: String,
        val user2Name: String,
        val lastMessage: String,
        val timestamp: Date?,
        val unreadCount: Int
    )

}

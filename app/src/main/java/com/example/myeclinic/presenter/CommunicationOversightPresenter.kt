package com.example.myeclinic.presenter

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class CommunicationOversightPresenter(
    private val onChatsLoaded: (List<ChatInfo>) -> Unit,
    private val onError: (String) -> Unit
) {
    private val db = FirebaseFirestore.getInstance()
    /**
     * Loads all chat metadata stored in the Firestore "chats" collection.
     *
     * For each chat document, this function extracts:
     * - Chat ID
     * - Participant names (user1 and user2)
     * - User2's ID (used for additional identification or navigation)
     *
     * The resulting list of [ChatInfo] is passed to [onChatsLoaded].
     * If the query fails, the [onError] callback is triggered with an error message.
     */
    fun loadAllChats() {
        db.collection("chats")
            .get()
            .addOnSuccessListener { snapshot ->
                val chats = snapshot.documents.mapNotNull { doc ->
                    val chatId = doc.id
                    val user1Name = doc.getString("user1Name") ?: "Unknown"
                    val user2Name = doc.getString("user2Name") ?: "Unknown"
                    val user2Id = doc.get("user2Id") as? String ?: ""
                    ChatInfo(chatId, user1Name, user2Name, user2Id)
                }
                onChatsLoaded(chats)
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to load chats")
            }
    }
    /**
     * Represents basic information about a chat used for administrative or monitoring purposes.
     *
     * @property chatId The unique identifier of the chat.
     * @property user1Name The name of the first user in the chat.
     * @property user2Name The name of the second user in the chat.
     * @property user2Id The unique identifier of the second user (useful for navigation or filtering).
     */
    data class ChatInfo(
        val chatId: String,
        val user1Name: String,
        val user2Name: String,
        val user2Id: String
    )
}
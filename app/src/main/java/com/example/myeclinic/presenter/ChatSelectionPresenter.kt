package com.example.myeclinic.presenter

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.myeclinic.util.UserSession
import com.example.myeclinic.model.User

class ChatSelectionPresenter(
    private val onChatsLoaded: (List<ChatItem>) -> Unit,
    private val onError: (String) -> Unit
) {

    private val db = FirebaseFirestore.getInstance()

    fun loadChats() {
        val userId = UserSession.currentUser?.userId ?: return

        db.collection("chats")
            .whereArrayContains("participantIds", userId)
            .orderBy("lastTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    onError(error.message ?: "Unknown error")
                    return@addSnapshotListener
                }

                val chats = snapshots?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val chatId = doc.id
                    ChatItem(
                        chatId = chatId,
                        lastMessage = data["lastMessage"] as? String ?: "",
                        lastTimestamp = data["lastTimestamp"],
                        otherParticipantId = (data["participantIds"] as List<*>)
                            .firstOrNull { it != userId } as? String ?: ""
                    )
                } ?: emptyList()

                onChatsLoaded(chats)
            }
    }

    data class ChatItem(
        val chatId: String,
        val lastMessage: String,
        val lastTimestamp: Any?,
        val otherParticipantId: String
    )
}
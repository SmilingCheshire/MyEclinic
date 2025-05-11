package com.example.myeclinic.presenter

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myeclinic.util.UserSession

class ChatPresenter(
    private val chatId: String,
    private val onMessagesLoaded: (List<Message>) -> Unit,
    private val onError: (String) -> Unit
) {
    private val db = FirebaseFirestore.getInstance()

    fun loadMessages() {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    onError(error.message ?: "Error loading messages")
                    return@addSnapshotListener
                }

                val messages = snapshots?.documents?.mapNotNull {
                    val data = it.data ?: return@mapNotNull null
                    Message(
                        text = data["text"] as? String ?: "",
                        senderId = data["senderId"] as? String ?: "",
                        timestamp = data["timestamp"] as? Timestamp
                    )
                } ?: emptyList()

                onMessagesLoaded(messages)
            }
    }

    fun sendMessage(text: String) {
        val senderId = UserSession.currentUser?.userId ?: return
        val timestamp = Timestamp.now()

        val message = mapOf(
            "text" to text,
            "senderId" to senderId,
            "timestamp" to timestamp,
            "seen" to false
        )

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                // update chat metadata
                db.collection("chats")
                    .document(chatId)
                    .update(
                        mapOf(
                            "lastMessage" to text,
                            "lastTimestamp" to timestamp,
                            "unreadCounts.$senderId" to 0
                        )
                    )
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to send")
            }
    }

    data class Message(
        val text: String,
        val senderId: String,
        val timestamp: Timestamp?
    )
}
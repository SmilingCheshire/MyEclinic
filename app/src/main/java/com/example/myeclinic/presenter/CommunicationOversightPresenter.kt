package com.example.myeclinic.presenter

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class CommunicationOversightPresenter(
    private val onChatsLoaded: (List<ChatInfo>) -> Unit,
    private val onError: (String) -> Unit
) {
    private val db = FirebaseFirestore.getInstance()

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

    data class ChatInfo(
        val chatId: String,
        val user1Name: String,
        val user2Name: String,
        val user2Id: String
    )
}
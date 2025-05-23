package com.example.myeclinic.presenter

import com.google.firebase.firestore.FirebaseFirestore
import com.example.myeclinic.util.UserSession

class ChatListPresenter(
    private val onChatsLoaded: (List<ChatPreview>) -> Unit,
    private val onError: (String) -> Unit
) {
    private val db = FirebaseFirestore.getInstance()
    private val userId = UserSession.currentUser?.userId ?: ""

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
                val otherUserId = if (Id1 == UserSession.currentUser?.userId) Id2 else Id1

                val lastMessage = doc.getString("lastMessage") ?: "No messages yet"
                val lastTimestamp = doc.getTimestamp("lastTimestamp")
                val unreadCount = (doc.get("unreadCounts.$userId") as? Long)?.toInt() ?: 0

                val user1Name = doc.getString("user1Name") ?: "Unknown"
                val user2Name = doc.getString("user2Name") ?: "Unknown"

                val otherUserName = if (user1Name == UserSession.currentUser?.name) user2Name else user1Name


                previews.add(ChatPreview(id, otherUserId, otherUserName, lastMessage, lastTimestamp?.toDate(), unreadCount))

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

    data class ChatPreview(
        val chatId: String,
        val otherUserId: String,
        val otherUserName: String,
        val lastMessage: String,
        val timestamp: java.util.Date?,
        val unreadCount: Int
    )

}

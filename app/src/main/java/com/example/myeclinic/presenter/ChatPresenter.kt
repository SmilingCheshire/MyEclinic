package com.example.myeclinic.presenter

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myeclinic.util.UserSession
import com.google.firebase.firestore.SetOptions

class ChatPresenter(
    private val chatId: String,
    private val otherUserId: String,
    private val onMessagesLoaded: (List<Message>) -> Unit,
    private val onError: (String) -> Unit
) {
    private val db = FirebaseFirestore.getInstance()
    private val senderId = UserSession.currentUser?.userId ?: ""

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

                markMessagesAsSeen() // Optional: update seen status after loading
                onMessagesLoaded(messages)
            }
    }

    fun sendMessage(text: String) {
        if (senderId.isBlank()) {
            onError("Invalid sender")
            return
        }

        val timestamp = Timestamp.now()
        val message = mapOf(
            "text" to text,
            "senderId" to senderId,
            "receiverId" to otherUserId,
            "timestamp" to timestamp,
            "seen" to false
        )

        val chatRef = db.collection("chats").document(chatId)

        chatRef.collection("messages")
            .add(message)
            .addOnSuccessListener {
                val senderRole = UserSession.currentUser?.role ?: return@addOnSuccessListener
                val senderName = UserSession.currentUser?.name ?: "User"

                val receiverPaths = listOf("patients", "doctors", "admins")
                var receiverNameFound = false

                for (path in receiverPaths) {
                    db.collection(path).document(otherUserId).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists() && !receiverNameFound) {
                                receiverNameFound = true
                                val receiverName = doc.getString("name") ?: "User"

                                val updates = mapOf(
                                    "lastMessage" to text,
                                    "lastTimestamp" to timestamp,
                                    "unreadCounts.$senderId" to 0,
                                    "unreadCounts.$otherUserId" to FieldValue.increment(1),
                                    "user1Name" to senderName,
                                    "user2Name" to receiverName
                                )

                                chatRef.set(mapOf("created" to Timestamp.now()), SetOptions.merge())
                                    .addOnSuccessListener {
                                        chatRef.update(updates)
                                        registerChatReference()
                                    }
                                    .addOnFailureListener {
                                        onError(it.message ?: "Failed to initialize chat document")
                                    }
                            }
                        }
                }
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to send message")
            }
    }

    private fun markMessagesAsSeen() {
        val messagesRef = db.collection("chats")
            .document(chatId)
            .collection("messages")

        messagesRef
            .whereEqualTo("receiverId", senderId)
            .whereEqualTo("seen", false)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                for (doc in documents) {
                    batch.update(doc.reference, "seen", true)
                }
                batch.commit()

                db.collection("chats")
                    .document(chatId)
                    .update("unreadCounts.$senderId", 0)
            }
    }
    private fun registerChatReference() {
        val senderRole = UserSession.currentUser?.role ?: return

        val senderPath = when (senderRole) {
            "Doctor" -> "doctors"
            "Patient" -> "patients"
            "Admin" -> "admins"
            else -> return
        }

        // You might want to infer receiver role, but here we default to trying both
        val possibleReceiverPaths = listOf("patients", "doctors", "admins")

        val senderDocRef = db.collection(senderPath).document(senderId)
        val chatRefObject = mapOf("chatId" to chatId)

        // Add chat reference to sender
        senderDocRef.update("chats", FieldValue.arrayUnion(chatRefObject))

        // Try adding chat reference to receiver (we don't know their role exactly)
        for (path in possibleReceiverPaths) {
            val receiverDocRef = db.collection(path).document(otherUserId)
            receiverDocRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    receiverDocRef.update("chats", FieldValue.arrayUnion(chatRefObject))
                }
            }
        }
    }

    data class Message(
        val text: String,
        val senderId: String,
        val timestamp: Timestamp?
    )
}
package com.example.myeclinic.presenter

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myeclinic.util.UserSession
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import okhttp3.Callback
import okhttp3.Call
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

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
                        senderName = data["senderName"] as? String ?: "Unknown",
                        timestamp = data["timestamp"] as? Timestamp
                    )
                } ?: emptyList()

                markMessagesAsSeen()
                onMessagesLoaded(messages)
            }
    }

    fun sendMessage(text: String) {
        if (senderId.isBlank()) {
            onError("Invalid sender")
            return
        }
        val senderName = UserSession.currentUser?.name ?: "User"

        val timestamp = Timestamp.now()
        val message = mapOf(
            "text" to text,
            "senderId" to senderId,
            "senderName" to senderName,
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
                                        findAndSendPush(otherUserId, text, senderName, chatId, senderId)
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

    fun findAndSendPush(otherUserId: String, text: String, senderName: String, chatId: String, senderId: String) {
        val possiblePaths = listOf("patients", "doctors", "admins")
        val db = FirebaseFirestore.getInstance()

        fun tryNext(index: Int) {
            if (index >= possiblePaths.size) {
                Log.e("FCM", "No FCM token found in any path")
                return
            }

            val path = possiblePaths[index]
            db.collection(path).document(otherUserId).get()
                .addOnSuccessListener { doc ->
                    val token = doc.getString("deviceToken")
                    if (!token.isNullOrBlank()) {

                        val payload = hashMapOf<String, Any>(
                            "token" to token,
                            "title" to "New message from $senderName",
                            "body" to text,
                            "chatId" to chatId,
                            "senderId" to senderId
                        )

                        Log.d("FCM", "Sending push with: $payload")

                        Firebase.functions
                            .getHttpsCallable("sendPushNotification")
                            .call(payload)
                            .addOnSuccessListener {
                                Log.d("FCM", "Push sent successfully")
                            }
                            .addOnFailureListener {
                                Log.e("FCM", "Push failed: ${it.message}")
                            }
                    } else {
                        tryNext(index + 1)
                    }
                }
                .addOnFailureListener {
                    Log.e("FCM", "Error fetching from $path: ${it.message}")
                    tryNext(index + 1)
                }
        }

        tryNext(0)
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

        val possibleReceiverPaths = listOf("patients", "doctors", "admins")

        val senderDocRef = db.collection(senderPath).document(senderId)
        val chatRefObject = mapOf("chatId" to chatId)

        senderDocRef.update("chats", FieldValue.arrayUnion(chatRefObject))

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
        val senderName: String,
        val timestamp: Timestamp?
    )
}
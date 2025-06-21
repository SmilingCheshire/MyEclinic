const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.sendPushNotification = functions.https.onCall(async (requestData, context) => {
  try {

    const data = requestData.data || requestData;
    console.log("RAW data received:", data);

    const required = ["senderId", "title", "body", "chatId", "token"];
    const missing = required.filter(k => !data[k]);

    if (missing.length) {
      console.error("Missing fields:", missing);
      throw new functions.https.HttpsError("invalid-argument", `Missing fields: ${missing.join(", ")}`);
    }

    const { senderId, title, body, chatId, token } = data;

    const message = {
      token: token,
      data: {
        chatId,
        senderId
      },
      notification: {
        title,
        body
      }
    };

    const response = await admin.messaging().send(message);
    console.log("Push sent successfully:", response);
    return { success: true, response };
  } catch (error) {
    console.error("Push failed:", error.message);
    throw new functions.https.HttpsError("internal", error.message);
  }
});

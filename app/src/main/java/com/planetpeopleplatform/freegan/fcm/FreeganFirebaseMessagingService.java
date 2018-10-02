package com.planetpeopleplatform.freegan.fcm;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.planetpeopleplatform.freegan.utils.PushNotifications;

import java.util.Map;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kINSTANCEID;
import static com.planetpeopleplatform.freegan.utils.Constants.kMESSAGE;
import static com.planetpeopleplatform.freegan.utils.Constants.kMESSAGEDATE;
import static com.planetpeopleplatform.freegan.utils.Constants.kMESSAGEID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTID;
import static com.planetpeopleplatform.freegan.utils.Constants.kRECEIVERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kSENDERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERNAME;

public class FreeganFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = FreeganFirebaseMessagingService.class.getSimpleName();

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);

    }

    private void sendRegistrationToServer(String token) {
        // This is where you'd send the token to the server.

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            firebase.child(kUSER)
                    .child(firebaseUser.getUid())
                    .child(kINSTANCEID)
                    .setValue(token);
        }
    }


    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with FCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages.


        // Check if message contains a data payload.

        Map<String, String> data = remoteMessage.getData();

        if (data.size() > 0) {

            String userName = data.get(kUSERNAME);
            String message = data.get(kMESSAGE);
            String chatRoomId = data.get(kCHATROOMID);
            String postId = data.get(kPOSTID);
            String chatMateId = data.get(kSENDERID);
            String currentUserUid = data.get(kRECEIVERID);
            String messageDate = data.get(kMESSAGEDATE);

            //load chat post
            PushNotifications.loadPost(this, messageDate,
                    postId, chatMateId, message, userName, chatRoomId, currentUserUid);
        }
    }

}
package com.planetpeopleplatform.freegan.utils;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.activity.MessageActivity;
import com.planetpeopleplatform.freegan.data.FreeganContract;
import com.planetpeopleplatform.freegan.free.FreeganAppWidget;
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import tgio.rncryptor.RNCryptorNative;

import static com.planetpeopleplatform.freegan.free.FreeganAppWidget.updateFreeganWidgets;
import static com.planetpeopleplatform.freegan.utils.Constants.JSONARRAYKEY;
import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kCOUNTER;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kMESSAGEID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kRECENT;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERID;

public class PushNotifications {

    private static DatabaseReference mRef = firebase.child(kRECENT);
    private static boolean mShouldSendPushNotification = false;

    private static final int NOTIFICATION_MAX_CHARACTERS = 30;
    private static final int CHAT_MESSAGE_NOTIFICATION_ID = 0;
    private static final String MESSAGE_RECEIVED_NOTIFICATION_CHANNEL_ID = "message_received_notification_channel";


    /**
     * This pending intent id is used to uniquely reference the pending intent
     */
    private static final int RECEIVED_MESSAGE_PENDING_INTENT_ID = 3417;

    //Helper method for fetching post
    public static void loadPost(final Context context, final String messageId, String postId, final String chatMateId,
                                final String message, final String userName, final String chatRoomId, final String currentUserUid){

        firebase.child(kPOST).child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    HashMap<String, Object> map = (HashMap<String, Object>) dataSnapshot.getValue();
                    Post post = (new Post(map));
                    loadUser(context, messageId, chatMateId, message, userName, post, chatRoomId, currentUserUid);
                } catch (Exception e) {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private static void loadUser(final Context context, final String messageId, final String chatMateId,
                                 final String message, final String userName,
                                 final Post post, final String chatRoomId, final String currentUserUid){
        firebase.child(kUSER).child(chatMateId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    User chatMate = new User((java.util.HashMap<String, Object>) dataSnapshot.getValue());
                    numberOfUnreadMessagesOfUser(context, messageId, post, chatMate,
                            message, userName, chatRoomId, currentUserUid);
                }catch (Exception ex){}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    /**
     * Create and show a simple notification containing the received data message
     */
    private static void sendNotification(Context context, String messageId, Post post, User chatMate,
                                         String message, String userName, String chatRoomId, String currentUserUid,
                                          int counter) {

        RNCryptorNative rncryptor = new RNCryptorNative();
        String decrypted = rncryptor.decrypt(message, chatRoomId);

        insertData(context, messageId, chatMate,
                decrypted, userName, chatRoomId, currentUserUid);

        // If the message is longer than the max number of characters we want in our
        // notification, truncate it and add the unicode character for ellipsis
        if (decrypted.length() > NOTIFICATION_MAX_CHARACTERS) {
            decrypted = decrypted.substring(0, NOTIFICATION_MAX_CHARACTERS) + "\u2026";
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, FreeganAppWidget.class));
//        //Trigger data update to handle the GridView widgets and force a data refresh
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);
        //Now update all widgets
        updateFreeganWidgets(context, appWidgetManager,
        appWidgetIds, userName, decrypted,
                currentUserUid, chatRoomId,
                post, chatMate);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    MESSAGE_RECEIVED_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.main_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, MESSAGE_RECEIVED_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_freegan_foreground)
                .setContentTitle(String.format(context.getString(R.string.notification_message), userName))
                .setContentText(decrypted)
                .setSound(defaultSoundUri)
                .setContentIntent(contentIntent(context, messageId, currentUserUid, chatRoomId,
                        post, chatMate))
                .setNumber(counter)
                //.addAction(ignoreReminderAction(context))
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }

        if (!isAppOnForeground(context)) {
            notificationManager.notify(CHAT_MESSAGE_NOTIFICATION_ID, notificationBuilder.build());
        }

    }

    public static void clearNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(CHAT_MESSAGE_NOTIFICATION_ID);
    }

    private static boolean isAppOnForeground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private static PendingIntent contentIntent(Context context, String messageId,
                                               String currentUserUid, String chatRoomId,
                                               Post post, User chatMate) {

        Intent startActivityIntent = new Intent(context, MessageActivity.class);
        startActivityIntent.putExtra(kCURRENTUSERID, currentUserUid);
        startActivityIntent.putExtra(kCHATROOMID, chatRoomId);
        startActivityIntent.putExtra(kPOST, post);
        startActivityIntent.putExtra(kUSER, chatMate);
        startActivityIntent.putExtra(kMESSAGEID, messageId);

        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addNextIntentWithParentStack(startActivityIntent);

        return taskStackBuilder.getPendingIntent(RECEIVED_MESSAGE_PENDING_INTENT_ID, PendingIntent.FLAG_ONE_SHOT);
    }


    private static void numberOfUnreadMessagesOfUser(final Context context, final String messageId,
                                                     final Post post, final User chatMate,
                                                     final String message, final String userName,
                                                     final String chatRoomId, final String currentUserUid) {


        final int[] counter = {0};
        final int[] resultCounter = {0};

        mRef.orderByChild(kUSERID).equalTo(currentUserUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    Collection<Object> recents =
                            ((HashMap<String, Object>) dataSnapshot.getValue()).values();

                    for (Object recent : recents) {

                        final HashMap<String, Object> currentRecent = (HashMap<String, Object>) recent;
                        long tempCount = ((long) currentRecent.get(kCOUNTER));

                        resultCounter[0] += 1;
                        counter[0] += tempCount;

                        if (resultCounter[0] == recents.size()) {
                            // Send a notification that you got a new message
                            sendNotification(context, messageId, post, chatMate,
                                    message, userName, chatRoomId, currentUserUid, counter[0]);
                        }


                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    // insert data into database
    private static void insertData(Context context, String messageId, User chatMate,
                                   String message, String userName, String chatRoomId, String currentUserUid){
        ContentValues postValues = new ContentValues();

        postValues.put(FreeganContract.MessagesEntry.COLUMN_MESSAGE, message);
        postValues.put(FreeganContract.MessagesEntry.COLUMN_MESSAGE_ID, messageId);
        postValues.put(FreeganContract.MessagesEntry.COLUMN_CURRENT_USER_ID, currentUserUid);
        postValues.put(FreeganContract.MessagesEntry.COLUMN_CHAT_ROOM_ID, chatRoomId);
        postValues.put(FreeganContract.MessagesEntry.COLUMN_USER_NAME, userName);
        postValues.put(FreeganContract.MessagesEntry.COLUMN_CHAT_MATE_ID, chatMate.getObjectId());

        context.getContentResolver().insert(FreeganContract.FreegansEntry.CONTENT_URI,
                postValues);
    }

}

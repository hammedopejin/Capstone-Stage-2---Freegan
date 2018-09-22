package com.planetpeopleplatform.freegan.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kCOUNTER;
import static com.planetpeopleplatform.freegan.utils.Constants.kMEMBERS;
import static com.planetpeopleplatform.freegan.utils.Constants.kRECENT;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERID;

public class PushNotifications {

    private static DatabaseReference mRef = firebase.child(kRECENT);
    private static boolean mShouldSendPushNotification = false;

    public  static void sendPushNotification1(String chatRoomID, final String message, final String currentUserId) {

        mRef.orderByChild(kCHATROOMID).equalTo(chatRoomID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Collection<Object> recents =
                            ((HashMap<String, Object>) dataSnapshot.getValue()).values();
                    for (Object recent : recents) {

                        final HashMap<String, Object> currentRecent = (HashMap<String, Object>) recent;
                        sendPushNotification2((String[]) currentRecent.get(kMEMBERS), message, currentUserId);
                        //String[] list = (String[]) currentRecent.get(kMEMBERS);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private static void sendPushNotification2(String[] members, String message, String currentUserId) {

        String[] newMembers = removeCurrentUserFromMembersArray(members, currentUserId);

        User[] usersArray = getMembersToPush(newMembers);

            for (User user : usersArray) {
                mShouldSendPushNotification = true;

                sendPushNotification(user, message);
            }

    }

    private static void sendPushNotification(User user, String message) {
        int counter = numberOfUnreadMessagesOfUser(user.getObjectId());

            //String deviceId = (String) user.getProperty(kDEVICEID);

            //DeliveryOptions deliveryOptions = new DeliveryOptions();

            //deliveryOptions.pushSinglecast = deviceId[];


            //PublishOptions publishOptions = PublishOptions();

           // publishOptions.assignHeaders(["\(firebase.userService.currentUser.name!) \n \(message)",
            //     "\(counter)",  "default"]);

//            if (firebase.messaging.publish("default", message,  publishOptions, deliveryOptions)){
//
//                mShouldSendPushNotification = false;
//
//            }else{
//
//                Log.d("TAG", "sendPushNotification: ");
//            }
    }

    private static int numberOfUnreadMessagesOfUser(String userId) {


        final int[] counter = {0};
        final int[] resultCounter = {0};

        mRef.orderByChild(kUSERID).equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    Collection<Object> recents =
                            ((HashMap<String, Object>) dataSnapshot.getValue()).values();

                    for (Object recent : recents) {

                        final HashMap<String, Object> currentRecent = (HashMap<String, Object>) recent;
                        int tempCount = ((int) currentRecent.get(kCOUNTER));

                        resultCounter[0] += 1;
                        counter[0] += tempCount;

//                        if (mShouldSendPushNotification) {
//
//                            if (resultCounter[0] == recents.size()) {
//
//                                return counter[0];
//                            }
//                        }

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return 0;

    }

    private static User[] getMembersToPush(String[] newMembers) {
        return null;
    }

    private static String[] removeCurrentUserFromMembersArray(String[] members, String currentUserId) {

        String[] updatedMembers = new String[1];

        for (String member : members) {

            if (!member.equals(currentUserId)) {

                updatedMembers[0] = (member);
            }
        }

        return updatedMembers;
    }


}

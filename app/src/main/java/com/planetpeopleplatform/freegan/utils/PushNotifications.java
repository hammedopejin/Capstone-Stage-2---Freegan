package com.planetpeopleplatform.freegan.utils;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Collection;
import java.util.HashMap;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kMEMBERS;
import static com.planetpeopleplatform.freegan.utils.Constants.kRECENT;

public class PushNotifications {
    private static DatabaseReference mRef = firebase.child(kRECENT);
    private static boolean mShouldSendPushNotification = false;

    public  static void sendPushNotification1(String chatRoomID, final String message) {

        mRef.orderByChild(kCHATROOMID).equalTo(chatRoomID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    Collection<Object> recents =
                            ((HashMap<String, Object>) dataSnapshot.getValue()).values();
                    for (Object recent : recents) {

                        final HashMap<String, Object> currentRecent = (HashMap<String, Object>) recent;
                        sendPushNotification2((String) currentRecent.get(kMEMBERS), message);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private static void sendPushNotification2(String s, String message) {

    }


}

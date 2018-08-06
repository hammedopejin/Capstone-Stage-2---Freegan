package com.planetpeopleplatform.freegan.utils;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.model.Message;
import com.planetpeopleplatform.freegan.model.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static butterknife.internal.Utils.listOf;
import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kCOUNTER;
import static com.planetpeopleplatform.freegan.utils.Constants.kDATE;
import static com.planetpeopleplatform.freegan.utils.Constants.kLASTMESSAGE;
import static com.planetpeopleplatform.freegan.utils.Constants.kMEMBERS;
import static com.planetpeopleplatform.freegan.utils.Constants.kMESSAGE;
import static com.planetpeopleplatform.freegan.utils.Constants.kMESSAGEID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPRIVATE;
import static com.planetpeopleplatform.freegan.utils.Constants.kRECENT;
import static com.planetpeopleplatform.freegan.utils.Constants.kRECENTID;
import static com.planetpeopleplatform.freegan.utils.Constants.kSTATUS;
import static com.planetpeopleplatform.freegan.utils.Constants.kTYPE;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kWITHUSERUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kWITHUSERUSERNAME;

public class Utils {

    public static String startChat(User user1, User user2) {

        String userId1 = user1.getObjectId();
        String userId2 = user2.getObjectId();

        String chatRoomId = "";

        int value = userId1.compareTo(userId2);

        if (value < 0) {
            chatRoomId = userId1 + userId2;
        } else {
            chatRoomId = userId2 + userId1;
        }

        List<String> members = listOf(userId1, userId2);

        createRecent( userId1,  chatRoomId,  members,  userId2,  user2.getUserName(),  kPRIVATE);
        createRecent( userId2,  chatRoomId,  members,  userId1,  user1.getUserName(),  kPRIVATE);


        return chatRoomId;
    }

    public static void updateChatStatus(HashMap<String, Object> chat, String chatRoomId) {

        firebase.child(kMESSAGE).child(chatRoomId).child(((String)chat.get(kMESSAGEID))).child(kSTATUS).setValue(Message.STATUS_READ);

    }

    public static void createRecent(final String userId, final String chatRoomId, final List<String> members,
                                    final String withUserUserId, final String withUserUsername, final String type) {

        firebase.child(kRECENT).orderByChild(kCHATROOMID).equalTo(chatRoomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Boolean create = true;

                if (dataSnapshot.exists()) {
                    for (Object recent : ( (HashMap<String, Object>) dataSnapshot.getValue()).values() ){
                        HashMap<String, Object> currentRecent = (HashMap<String, Object>) recent;
                        if (currentRecent.get(kUSERID).equals(userId)){
                            create = false;
                        }

                        firebase.child(kRECENT).orderByChild(kCHATROOMID).equalTo((String)currentRecent.get(kCHATROOMID))
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }
                }
                if (create && !(userId.equals(withUserUserId))) {

                    createRecentItem(userId, chatRoomId, members, withUserUserId, withUserUsername, type);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static void createRecentItem(String userId, String chatRoomId, List<String> members, String withUserUserId,
                                String withUserUsername, String type) {

        DatabaseReference reference = firebase.child(kRECENT).push();

        String recentId = reference.getKey();
        SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        String date = sfd.format(new Date());

        HashMap<String, Object> recent = new HashMap<String, Object>();
        recent.put(kRECENTID, recentId);
        recent.put(kUSERID, userId);
        recent.put(kCHATROOMID, chatRoomId);
        recent.put(kMEMBERS, members);
        recent.put(kWITHUSERUSERNAME, withUserUsername);
        recent.put(kWITHUSERUSERID, withUserUserId);
        recent.put(kLASTMESSAGE, "");
        recent.put(kCOUNTER, 0);
        recent.put(kDATE, date);
        recent.put(kTYPE, type);

        reference.setValue(recent);
    }

    public static class DateHelper{
        final static String DF_SIMPLE_STRING = "yyyy-MM-dd hh:mm:ss a";
        public static ThreadLocal<DateFormat> DF_SIMPLE_FORMAT = new ThreadLocal<DateFormat>() {
            @Override
            protected DateFormat initialValue()  {
                return new SimpleDateFormat(DF_SIMPLE_STRING, Locale.US);
            }
        };
    }


    public static void updateRecents(String chatRoomId, final String lastMessage){

        firebase.child(kRECENT).orderByChild(kCHATROOMID).equalTo(chatRoomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    for (Object recent : (((HashMap<String, Object>)dataSnapshot.getValue()).values())) {

                        updateRecentItem((HashMap<String, Object>)recent, lastMessage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private static void updateRecentItem(HashMap<String, Object> recent, String lastMessage) {

        SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        String date = sfd.format(new Date());

        Long counter = (Long) recent.get(kCOUNTER);

        if (!(recent.get(kUSERID).equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))) {
            counter += 1;
        }

        firebase.child(kRECENT).child(((String) recent.get(kRECENTID))).child(kLASTMESSAGE).setValue(lastMessage);
        firebase.child(kRECENT).child(((String) recent.get(kRECENTID))).child(kCOUNTER).setValue(counter);
        firebase.child(kRECENT).child(((String) recent.get(kRECENTID))).child(kDATE).setValue(date);


    }



    public static void clearRecentCounter(String chatRoomID) {

        firebase.child(kRECENT).orderByChild(kCHATROOMID).equalTo(chatRoomID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (Object recent : (((HashMap<String, Object>) dataSnapshot.getValue()).values())) {
                        HashMap<String, Object> currentRecent = (HashMap<String, Object>) recent;
                        if (currentRecent.get(kUSERID).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                            clearRecentCounterItem(currentRecent);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static void clearRecentCounterItem(HashMap<String, Object> recent) {

        firebase.child(kRECENT).child(((String) recent.get(kRECENTID))).child(kCOUNTER).setValue(0);

    }

}

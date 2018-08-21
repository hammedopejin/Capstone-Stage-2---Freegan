package com.planetpeopleplatform.freegan.activity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.model.Message;
import com.planetpeopleplatform.freegan.model.User;
import com.planetpeopleplatform.freegan.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import tgio.rncryptor.RNCryptorNative;

import static com.planetpeopleplatform.freegan.model.Message.STATUS_READ;
import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kAUDIO;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kDATE;
import static com.planetpeopleplatform.freegan.utils.Constants.kLOCATION;
import static com.planetpeopleplatform.freegan.utils.Constants.kMESSAGE;
import static com.planetpeopleplatform.freegan.utils.Constants.kMESSAGEID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPICTURE;
import static com.planetpeopleplatform.freegan.utils.Constants.kSENDERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kSENDERNAME;
import static com.planetpeopleplatform.freegan.utils.Constants.kSTATUS;
import static com.planetpeopleplatform.freegan.utils.Constants.kTEXT;
import static com.planetpeopleplatform.freegan.utils.Constants.kTYPE;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kVIDEO;

public class ChatActivity extends CustomActivity {

    private ChildEventListener mChildEventListener;
    private DatabaseReference mMessagesDatabaseReference;
    private DatabaseReference chatRef = firebase.child(kMESSAGE);
    private String chatRoomId = null;
    Boolean initialLoadComplete = false;


    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    // The Editext to compose the message.
    @BindView(R.id.chat_message_edit_text)
    EditText mChatMessageEdittext;

    @BindView(R.id.chat_list_view)
    ListView mListView;

    /** The Conversation list.  */
    private ArrayList convList = new ArrayList<Message>();

        /** The chat adapter.  */
        private ChatAdapter mChatAdapter = null;

        /** The date of last message in conversation.  */
        private Date lastMsgDate = null;

        /** Flag to hold if the activity is running or not.  */
        private Boolean isRunning = false;


        /** The current user object. */
        /**
         * Allow access to the current user info
         */
        User mCurrentUser = null;

        String mCurrentUserUID = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_chat);
            ButterKnife.bind(this);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.chats_title);

            mLoadingIndicator.setVisibility(View.VISIBLE);

            mCurrentUserUID = getIntent().getStringExtra(kCURRENTUSERID);
            chatRoomId = getIntent().getStringExtra(kCHATROOMID);
           // getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#51b79f")));
            //getSupportActionBar().setTitle(chatMate);

            mMessagesDatabaseReference = firebase.child(kMESSAGE).child(chatRoomId);

            firebase.child(kUSER).child(mCurrentUserUID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        mCurrentUser = new User((HashMap<String,Object>) dataSnapshot.getValue());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            mChatMessageEdittext = findViewById(R.id.chat_message_edit_text);
            mChatMessageEdittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

            setTouchNClick(R.id.btnSend);


            mChatAdapter = new ChatAdapter();
            mListView.setAdapter(mChatAdapter);
            mListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            mListView.setStackFromBottom(true);

            //listView.setOnScrollListener(AbsListView.OnScrollListener{ })
        }


        @Override
        public void onResume() {
            super.onResume();
            attachDatabaseReadListener();
            Utils.clearRecentCounter(chatRoomId);
            isRunning = true;
        }


        @Override
        public void onPause() {
            super.onPause();
            isRunning = false;
            convList.clear();
            detachDatabaseReadListener();
        }

        @Override
        public void onDestroy() {
            Utils.clearRecentCounter(chatRoomId);
            super.onDestroy();
        }

        @Override
        public void onClick(View v) {
            super.onClick(v);
            if (v.getId() == R.id.btnSend) {
                sendMessage();
            }
        }

        /**
         * Call this method to Send message to opponent. It does nothing if the text
         * is empty.
         */
        private void sendMessage() {
            if (mChatMessageEdittext.length() == 0) {
                return;
            }
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(mChatMessageEdittext.getWindowToken(), 0);

            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
            RNCryptorNative rncryptor =  new RNCryptorNative();

            String encrypted = new String((rncryptor.encrypt(mChatMessageEdittext.getText().toString(), chatRoomId)));
            mChatMessageEdittext.setText(null);
            DatabaseReference reference = chatRef.child(chatRoomId).push();
            String messageId = reference.getKey();
            Message cryptMessage = new Message( encrypted,  sfd.format(new Date()), messageId, mCurrentUserUID,
                    mCurrentUser.getUserName(),  Message.STATUS_DELIVERED, kTEXT);

            reference.setValue(cryptMessage);

            Utils.updateRecents(chatRoomId, encrypted);

        }

    private void attachDatabaseReadListener() {
        String[] array = {kAUDIO, kVIDEO, kTEXT, kLOCATION, kPICTURE};
        final List<String> legitTypes = new ArrayList<>(Arrays.asList(array));
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    if (dataSnapshot.exists()){


                        HashMap<String,Object> item = (HashMap<String,Object>) dataSnapshot.getValue();

                        if (item.get(kTYPE) != null) {

                            if (legitTypes.contains(item.get(kTYPE))) {

                                RNCryptorNative rncryptor  =  new RNCryptorNative();
                                String decrypted = rncryptor.decrypt((String) (item.get(kMESSAGE)), chatRoomId);
                                Message message = new Message(decrypted, (String) item.get(kDATE),
                                        (String) item.get(kMESSAGEID), (String) item.get(kSENDERID),
                                        (String) item.get(kSENDERNAME), (String) item.get(kSTATUS),
                                        (String) item.get(kTYPE));
                                convList.add(message);

                                // if (lastMsgDate == null || lastMsgDate.before(c.date)) {
                                //    lastMsgDate = message.getDate()
                                //}

                                if (!((item.get(kSENDERID)).equals(mCurrentUserUID))) {
                                    Utils.updateChatStatus(item, chatRoomId);
                                }
                            }

                        }
                        mChatAdapter.notifyDataSetChanged();
                        mLoadingIndicator.setVisibility(View.INVISIBLE);
                    }
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                public void onCancelled(DatabaseError databaseError) {}
            };
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }


    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

        /**
         * The Class ChatAdapter is the adapter class for Chat ListView. This
         * adapter shows the Sent or Received Chat message in each list item.
         *
         */
        class ChatAdapter extends BaseAdapter {


            @Override
            public int getCount() {
                return convList.size();
            }

            @Override
            public Message getItem(int i) {
                return (Message) convList.get(i);
            }

            @Override
            public long getItemId(int arg0) {
                return (long) arg0;
            }

            @Override
            public View getView(int pos, View v, ViewGroup arg2){
                View myView;
                Message message = getItem(pos);
                if (mCurrentUser.getUserName().equals(message.getSenderName())) {
                    myView = getLayoutInflater().inflate(R.layout.chat_item_sent, null);
                }else{
                    myView = getLayoutInflater().inflate(R.layout.chat_item_receive, null);
                }
                TextView lbl;
                try {
                        lbl = myView.findViewById(R.id.lbl1);
                        lbl.setText(DateUtils.getRelativeDateTimeString(getApplicationContext(),
                                Utils.DateHelper.DF_SIMPLE_FORMAT.get().parse(message.getDate()).getTime(), DateUtils.SECOND_IN_MILLIS,
                        DateUtils.SECOND_IN_MILLIS, 0));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    lbl = myView.findViewById(R.id.lbl2);
                    lbl.setText(message.getMessage());

                    lbl = myView.findViewById(R.id.lbl3);

                    if (mCurrentUser.getUserName().equals(message.getSenderName()) && (pos == convList.size() - 1)) {
                    lbl.setText(message.getStatus());
                    } else{
                        lbl.setText("");
                             }
                return myView;
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                Utils.clearRecentCounter(chatRoomId);
                finish();
                }
                return super.onOptionsItemSelected(item);
        }

        void updateMessage(HashMap<String, Object> item) {

            for (int index = 0; index < item.size(); index++) {

                HashMap<String, Object> temp = (HashMap<String, Object>) item.values();

                if (item.get(kMESSAGEID).equals(temp.get(kMESSAGEID))) {

                    Message c = new Message((String) item.get(kMESSAGE), (String) item.get(kDATE),
                            (String) item.get(kMESSAGEID), (String) item.get(kSENDERID),
                            (String) item.get(kSENDERNAME),  STATUS_READ, kTEXT);
                    convList.add(c);

                    mChatAdapter.notifyDataSetChanged();
                    }
            }
        }

}
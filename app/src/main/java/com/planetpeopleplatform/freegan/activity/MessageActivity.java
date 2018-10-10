package com.planetpeopleplatform.freegan.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.adapter.MessageAdapter;
import com.planetpeopleplatform.freegan.model.Message;
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;
import com.planetpeopleplatform.freegan.utils.EmptyStateRecyclerView;
import com.planetpeopleplatform.freegan.utils.EndlessRecyclerViewScrollListener;
import com.planetpeopleplatform.freegan.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import tgio.rncryptor.RNCryptorNative;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kAUDIO;
import static com.planetpeopleplatform.freegan.utils.Constants.kBLOCKEDUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kBUNDLE;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kDATE;
import static com.planetpeopleplatform.freegan.utils.Constants.kLOCATION;
import static com.planetpeopleplatform.freegan.utils.Constants.kMESSAGE;
import static com.planetpeopleplatform.freegan.utils.Constants.kMESSAGEID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPICTURE;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTER;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTID;
import static com.planetpeopleplatform.freegan.utils.Constants.kRECEIVERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kSENDERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kSENDERNAME;
import static com.planetpeopleplatform.freegan.utils.Constants.kSTATUS;
import static com.planetpeopleplatform.freegan.utils.Constants.kTEXT;
import static com.planetpeopleplatform.freegan.utils.Constants.kTYPE;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kVIDEO;

public class MessageActivity extends CustomActivity {

    private static final int REPORT_USER_REQUEST_CODE = 234;

    private FirebaseAuth mAuth;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mMessagesDatabaseReference;
    private DatabaseReference chatRef = firebase.child(kMESSAGE);
    private String mChatRoomId = null;
    private Post mPost = null;
    private User mChatMate = null;
    private Parcelable mRecyclerViewState;

    /** The Conversation list.  */
    private ArrayList mMessageList = new ArrayList<Message>();

    /** The message adapter.  */
    private MessageAdapter mMessageAdapter = null;


    /** Flag to hold if the activity is running or not.  */
    private Boolean isRunning = false;

    // Store a member variable for the listener
    private EndlessRecyclerViewScrollListener mScrollListener;

    /** The current user object.
     * Allow access to the current user info
     */
    private User mCurrentUser = null;
    private String mCurrentUserUid = null;
    private LinearLayoutManager mLinearLayoutManager = null;

    private Query mDataBaseQuery;
    private ArrayList mLastSeenKey = new ArrayList<String>();

    @BindView(R.id.chat_content)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    @BindView(R.id.button_chat_box_send)
    android.support.design.widget.FloatingActionButton mSendButton;

    @BindView(R.id.post_Img)
    ImageView mPostImage;

    @BindView(R.id.chat_options)
    ImageView mChatOptions;

    @BindView(R.id.layout_chat_user)
    LinearLayout mChatLayout;

    @BindView(R.id.layout_blocked_user)
    LinearLayout mBlockedChatLayout;

    // The Editext to compose the message.
    @BindView(R.id.chat_message_edit_text)
    android.support.design.widget.TextInputEditText mChatMessageEdittext;

    @BindView(R.id.reyclerview_message_list)
    RecyclerView mMessageRecycler;

    @BindView(R.id.empty_chat_message_text)
    TextView mEmptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLoadingIndicator.setVisibility(View.VISIBLE);

        mCurrentUserUid = getIntent().getStringExtra(kCURRENTUSERID);
        mChatRoomId = getIntent().getStringExtra(kCHATROOMID);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserUid = mAuth.getCurrentUser().getUid();

        mPost = getIntent().getParcelableExtra(kPOST);
        mChatMate = getIntent().getParcelableExtra(kUSER);

        getSupportActionBar().setTitle(mPost.getDescription());
        Glide.with(this).load(mPost.getImageUrl().get(0)).into(mPostImage);

        mMessagesDatabaseReference = chatRef.child(mChatRoomId);
        mDataBaseQuery = mMessagesDatabaseReference.limitToLast(30);
        mDataBaseQuery.keepSynced(true);

        mLinearLayoutManager = new LinearLayoutManager(MessageActivity.this);

        firebase.child(kUSER).child(mCurrentUserUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mCurrentUser = new User((HashMap<String,Object>) dataSnapshot.getValue());
                    mMessageAdapter = new MessageAdapter(MessageActivity.this, mMessageList, mChatMate);

                    mLinearLayoutManager.setSmoothScrollbarEnabled(true);
                    mLinearLayoutManager.setReverseLayout(true);
                    mMessageRecycler.setLayoutManager(mLinearLayoutManager);
                    mMessageRecycler.setAdapter(mMessageAdapter);


                        if (mCurrentUser.getBlockedUsersList().contains(mChatMate.getObjectId())
                            || mChatMate.getBlockedUsersList().contains(mCurrentUser.getObjectId())) {
                            mChatLayout.setVisibility(View.GONE);
                            mBlockedChatLayout.setVisibility(View.VISIBLE);
                        } else {
                            mChatLayout.setVisibility(View.VISIBLE);
                            mBlockedChatLayout.setVisibility(View.GONE);
                        }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Retain an instance so that you can call `resetState()` for fresh searches
        mScrollListener = new EndlessRecyclerViewScrollListener(mLinearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (totalItemsCount > 29) {
                    loadNextDataFromApi(totalItemsCount);
                }
            }
        };
        // Adds the scroll listener to RecyclerView
        mMessageRecycler.addOnScrollListener(mScrollListener);

        mChatMessageEdittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        // Enable Send button when there's text to send
        mChatMessageEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setBackground(getResources().getDrawable(R.drawable.ic_send));
                    mSendButton.setVisibility(View.VISIBLE);
                } else {
                    mSendButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        setTouchNClick(R.id.button_chat_box_send);

        mChatOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUserSettingsPopup(view);
            }
        });

        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startProfileView(view);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        attachDatabaseReadListener();
        Utils.clearRecentCounter(mChatRoomId);
        isRunning = true;
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            releaseInstance ();
        }
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        Utils.clearRecentCounter(mChatRoomId);
        isRunning = false;
        mMessageList.clear();
        mCurrentUser = null;
        detachDatabaseReadListener();
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.button_chat_box_send) {
            sendMessage();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Utils.clearRecentCounter(mChatRoomId);
            onBackPressed();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadNextDataFromApi(int offset) {

        String[] array = {kAUDIO, kVIDEO, kTEXT, kLOCATION, kPICTURE};
        final List<String> legitTypes = new ArrayList<>(Arrays.asList(array));

        mRecyclerViewState = mMessageRecycler.getLayoutManager().onSaveInstanceState();
        mMessageRecycler.setLayoutFrozen(true);
        mMessageList.clear();

        mDataBaseQuery = mMessagesDatabaseReference.orderByKey().limitToLast(60 + offset).endAt((String) mLastSeenKey.get(mLastSeenKey.size() - 1));
        mDataBaseQuery.keepSynced(true);
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mDataBaseQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.exists()){

                    mLastSeenKey.add(dataSnapshot.getKey());
                    HashMap<String,Object> item = (HashMap<String,Object>) dataSnapshot.getValue();

                    if (item.get(kTYPE) != null) {

                        if (legitTypes.contains(item.get(kTYPE))) {

                            RNCryptorNative rncryptor  =  new RNCryptorNative();
                            String decrypted = rncryptor.decrypt((String) (item.get(kMESSAGE)), mChatRoomId);
                            Message message = new Message(decrypted, (String) item.get(kDATE),
                                    (String) item.get(kMESSAGEID), (String) item.get(kSENDERID),
                                    (String) item.get(kRECEIVERID), (String) item.get(kCHATROOMID),
                                    (String) item.get(kSENDERNAME), (String) item.get(kSTATUS),
                                    (String) item.get(kTYPE), (String) item.get(kPOSTID));
                            mMessageList.add(0, message);

                        }

                    }
                    mMessageRecycler.setLayoutFrozen(false);
                    mMessageAdapter.notifyDataSetChanged();
                    mMessageRecycler.getLayoutManager().onRestoreInstanceState(mRecyclerViewState);
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                }
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
            public void onChildRemoved(DataSnapshot dataSnapshot) {}
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
            public void onCancelled(DatabaseError databaseError) {}
        });
        mMessageAdapter.notifyDataSetChanged();
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

        String encrypted = new String((rncryptor.encrypt(mChatMessageEdittext.getText().toString(), mChatRoomId)));
        mChatMessageEdittext.setText(null);
        DatabaseReference reference = chatRef.child(mChatRoomId).push();



        String messageId = reference.getKey();
        final Message cryptMessage = new Message( encrypted,  sfd.format(new Date()), messageId, mCurrentUserUid, mChatMate.getObjectId(),
                mChatRoomId, mCurrentUser.getUserName(),  Message.STATUS_DELIVERED, kTEXT, mPost.getPostId());

        reference.setValue(cryptMessage);

        Utils.updateRecents(mChatRoomId, encrypted);

    }

    private void attachDatabaseReadListener() {
        String[] array = {kAUDIO, kVIDEO, kTEXT, kLOCATION, kPICTURE};
        final List<String> legitTypes = new ArrayList<>(Arrays.asList(array));
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    if (dataSnapshot.exists()){

                        mLastSeenKey.add(dataSnapshot.getKey());

                        HashMap<String,Object> item = (HashMap<String,Object>) dataSnapshot.getValue();

                        if (item.get(kTYPE) != null) {

                            if (legitTypes.contains(item.get(kTYPE))) {

                                RNCryptorNative rncryptor  =  new RNCryptorNative();
                                String decrypted = rncryptor.decrypt((String) (item.get(kMESSAGE)), mChatRoomId);
                                Message message = new Message(decrypted, (String) item.get(kDATE),
                                        (String) item.get(kMESSAGEID), (String) item.get(kSENDERID),
                                        (String) item.get(kRECEIVERID), (String) item.get(kCHATROOMID),
                                        (String) item.get(kSENDERNAME), (String) item.get(kSTATUS),
                                        (String) item.get(kTYPE), (String) item.get(kPOSTID));
                                mMessageList.add(0, message);


                                if (!((item.get(kSENDERID)).equals(mCurrentUserUid))) {
                                    Utils.updateChatStatus(item, mChatRoomId);
                                }
                            }

                        }
                        mMessageAdapter.notifyDataSetChanged();
                        mMessageRecycler.smoothScrollToPosition(0);
                        mLoadingIndicator.setVisibility(View.INVISIBLE);
                    }
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    if (dataSnapshot.exists()){

                        mLastSeenKey.add(dataSnapshot.getKey());

                        HashMap<String,Object> item = (HashMap<String,Object>) dataSnapshot.getValue();

                        if (item.get(kTYPE) != null) {

                            if (legitTypes.contains(item.get(kTYPE))) {

                                RNCryptorNative rncryptor  =  new RNCryptorNative();
                                String decrypted = rncryptor.decrypt((String) (item.get(kMESSAGE)), mChatRoomId);
                                Message message = new Message(decrypted, (String) item.get(kDATE),
                                        (String) item.get(kMESSAGEID), (String) item.get(kSENDERID),
                                        (String) item.get(kRECEIVERID), (String) item.get(kCHATROOMID),
                                        (String) item.get(kSENDERNAME), (String) item.get(kSTATUS),
                                        (String) item.get(kTYPE), (String) item.get(kPOSTID));
                                mMessageList.remove(0);
                                mMessageList.add(0, message);


                                if (!((item.get(kSENDERID)).equals(mCurrentUserUid))) {
                                    Utils.updateChatStatus(item, mChatRoomId);
                                }
                            }

                        }
                        mMessageAdapter.notifyDataSetChanged();
                        mMessageRecycler.smoothScrollToPosition(0);
                    }

                }
                public void onChildRemoved(DataSnapshot dataSnapshot) {}
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mDataBaseQuery.addChildEventListener(mChildEventListener);
            mDataBaseQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    if(!dataSnapshot.exists()){
                        mEmptyTextView.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void showUserSettingsPopup(View view) {

        PopupMenu popup = new PopupMenu(this, view);

            if (mChatMate.getBlockedUsersList().contains(mCurrentUserUid)) {
                popup.inflate(R.menu.popup_chat_visitor_settings_unblock_option);
            } else {
                popup.inflate(R.menu.popup_chat_visitor_settings);
            }


        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {


                    case R.id.action_report_user:
                        Intent reportUserIntent = new Intent(getApplicationContext(), ReportUserActivity.class);
                        Bundle data = new Bundle();
                        data.putParcelable(kCURRENTUSER, mCurrentUser);
                        data.putParcelable(kPOSTER, mChatMate);
                        reportUserIntent.putExtra(kBUNDLE, data);
                        startActivityForResult(reportUserIntent, REPORT_USER_REQUEST_CODE);
                        return true;

                    case R.id.action_block_user:

                        ArrayList<String> blockedList = mChatMate.getBlockedUsersList();
                        blockedList.add(mCurrentUserUid);
                        mChatMate.addBlockedUser(mCurrentUserUid);
                        HashMap<String, Object> newBlockedUser = new HashMap<String, Object>();
                        newBlockedUser.put(kBLOCKEDUSER, blockedList);
                        firebase.child(kUSER).child(mChatMate.getObjectId()).updateChildren(newBlockedUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Snackbar.make(mCoordinatorLayout,
                                            R.string.alert_user_blocked_successfully_string, Snackbar.LENGTH_SHORT).show();
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                        recreate();
                                    } else {
                                        Intent intent = getIntent();
                                        finish();
                                        startActivity(intent);
                                    }
                                } else {
                                    Snackbar.make(mCoordinatorLayout,
                                            R.string.err_user_block_failed_string, Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });

                        return true;

                    case R.id.action_unblock_user:
                        ArrayList<String> blockedLists = mChatMate.getBlockedUsersList();
                        int blockedPosition = blockedLists.indexOf(mCurrentUserUid);
                        mChatMate.removeBlockedUser(mCurrentUserUid);
                        firebase.child(kUSER).child(mChatMate.getObjectId()).child(kBLOCKEDUSER).child(String.valueOf(blockedPosition)).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Snackbar.make(mCoordinatorLayout,
                                                    R.string.alert_user_unblocked_successfully_string, Snackbar.LENGTH_SHORT).show();
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                                recreate();
                                            } else {
                                                Intent intent = getIntent();
                                                finish();
                                                startActivity(intent);
                                            }
                                        } else {
                                            Snackbar.make(mCoordinatorLayout,
                                                    R.string.err_user_unblock_failed_string, Snackbar.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                        return true;

                    default:

                        return false;
                }
            }
        });
        popup.show();

    }

    public void startProfileView(View view){
        Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
        profileIntent.putExtra(kPOSTERID, mPost.getPostUserObjectId());
        startActivity(profileIntent);
    }

    public void startChatMateProfileView(View view){
        Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
        profileIntent.putExtra(kPOSTERID, mChatMate.getObjectId());
        startActivity(profileIntent);
    }
}
package com.planetpeopleplatform.freegan.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.fragment.DeleteDialogFragment;
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;
import com.planetpeopleplatform.freegan.utils.ItemTouchHelperAdapter;
import com.planetpeopleplatform.freegan.utils.MyItemTouchHelperCallback;
import com.planetpeopleplatform.freegan.utils.Utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import tgio.rncryptor.RNCryptorNative;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kCOUNTER;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kDATE;
import static com.planetpeopleplatform.freegan.utils.Constants.kIMAGEURL;
import static com.planetpeopleplatform.freegan.utils.Constants.kLASTMESSAGE;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPRIVATE;
import static com.planetpeopleplatform.freegan.utils.Constants.kRECENT;
import static com.planetpeopleplatform.freegan.utils.Constants.kRECENTID;
import static com.planetpeopleplatform.freegan.utils.Constants.kTYPE;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERIMAGEURL;
import static com.planetpeopleplatform.freegan.utils.Constants.kWITHUSERUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kWITHUSERUSERNAME;

public class RecentChatActivity extends CustomActivity  implements DeleteDialogFragment.OnCompleteListener {

    private static final String TAG = RegisterActivity.class.getSimpleName();

    private ValueEventListener mValueEventListener;
    private DatabaseReference mRecentsDatabaseReference;

    private Post mPost = null;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    @BindView(R.id.recent_recyclerview)
    RecyclerView mRecentRecyclerView;

    private final int MESSAGE_ACTIVITY = 777;

    private String mCurrentUserUid;
    private String mWithUserId;
    private String mPostId;
    private ArrayList mChatRoomIDs = new ArrayList<String>();
    private ArrayList mRecents = new ArrayList<HashMap<String, Object>>();

    private ArrayList mPostList = new ArrayList<Post>();

    //Chat order participants
    private ArrayList mUserList = new ArrayList<User>();


    PostAdapater mAdapater;

    /** The user. */
    private User mCurrentUser = null;

    /** Flag to hold if the activity is running or not.  */
    private boolean isRunning = false;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_chat);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.chats_title);

        mLoadingIndicator.setVisibility(View.VISIBLE);

        mCurrentUserUid = getIntent().getStringExtra(kCURRENTUSERID);

        firebase.child(kUSER).child(mCurrentUserUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    mCurrentUser = new User((java.util.HashMap<String, Object>) dataSnapshot.getValue());
                        updateUserStatus(true);
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRecentsDatabaseReference = firebase.child(kRECENT);
    }


    @Override
    public void onResume(){
        super.onResume();
        attachDatabaseReadListener();
        isRunning = true;
    }


    @Override
    public void onPause(){
        super.onPause();
        isRunning = false;
        mRecents.clear();
        mUserList.clear();
        mChatRoomIDs.clear();
        mPostList.clear();

        detachDatabaseReadListener();
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        updateUserStatus(false);
    }

    private void attachDatabaseReadListener() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    try{
                        mRecents.clear();
                        mUserList.clear();
                        mChatRoomIDs.clear();
                        mPostList.clear();

                        Collection<Object> sortedArray =
                                ((HashMap<String, Object>) dataSnapshot.getValue()).values();



                        for (Object recent : sortedArray){

                            final HashMap<String, Object> currentRecent = (HashMap<String, Object>) recent;

                            if(( (currentRecent.get(kTYPE)).equals(kPRIVATE))) {

                                mWithUserId = (String) currentRecent.get(kWITHUSERUSERID);
                                mPostId = (String) currentRecent.get(kPOSTID);
                                mRecents.add(currentRecent);

                                mChatRoomIDs.add(currentRecent.get(kCHATROOMID));

                                firebase.child(kUSER).child(mWithUserId)
                                        .addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists()){

                                                    User chatMate = new User((HashMap<String, Object>) dataSnapshot.getValue());

                                                    mUserList.add(chatMate);

                                                    firebase.child(kPOST).child(mPostId)
                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                                    if(dataSnapshot.exists()) {

                                                                        mPost = new Post ((HashMap<String, Object>) dataSnapshot.getValue());
                                                                        mPostList.add(mPost);

                                                                        mAdapater = new PostAdapater(getApplicationContext(), mUserList, mPostList);
                                                                        MyItemTouchHelperCallback callback = new MyItemTouchHelperCallback(mAdapater);
                                                                        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                                                                        touchHelper.attachToRecyclerView(mRecentRecyclerView);
                                                                        mRecentRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                                                        mRecentRecyclerView.hasFixedSize();
                                                                        mRecentRecyclerView.setAdapter(mAdapater);

                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });

                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                            }
                        }
                    }catch(Exception e){
                        Log.d(TAG, "onDataChange: " + e.getLocalizedMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mRecentsDatabaseReference.orderByChild(kUSERID).equalTo(mCurrentUserUid)
                    .addValueEventListener(mValueEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mValueEventListener != null) {
            mRecentsDatabaseReference.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }


    private void updateUserStatus(boolean online){
        mCurrentUser.put("online", online);
    }

    class Item extends RecyclerView.ViewHolder{

                    public Item(View itemView) {
                        super(itemView);
                    }

                    private void bindData(User user, Post post){


                        TextView lbl = itemView.findViewById(R.id.tv_recent_name);
                        lbl.setMaxLines(1);
                        lbl.setText(user.getUserName());


                        Glide.with(getApplicationContext())
                                .load(user.getUserImgUrl())
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable>
                                            target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable>
                                            target, DataSource dataSource, boolean isFirstResource) {
                                        return false;
                                    }
                                })
                                .into((de.hdodenhof.circleimageview.CircleImageView) itemView.findViewById(R.id.img_recent_user));

                        Glide.with(getApplicationContext())
                                .load(post.getImageUrl())
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable>
                                            target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable>
                                            target, DataSource dataSource, boolean isFirstResource) {
                                        return false;
                                    }
                                })
                                .into((ImageView) itemView.findViewById(R.id.img_recent_post_item));
                    }
    }


    class PostAdapater extends RecyclerView.Adapter<RecyclerView.ViewHolder>
            implements ItemTouchHelperAdapter{

        View myView = null;
        Context mContext;
        ArrayList<User> uList;
        ArrayList<Post> uPosts;

        public PostAdapater(Context context, ArrayList userList, ArrayList posts) {
            mContext = context;
            uList = userList;
            uPosts = posts;

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            myView = LayoutInflater.from(mContext).inflate(R.layout.recent_item, parent, false);
            return new Item(myView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position){

            ((Item) holder).bindData((User) uList.get(position), (Post) uPosts.get(position));
            holder.itemView.setTag(position);

            TextView lbl = holder.itemView.findViewById(R.id.tv_recent_message_date);

            try {
                lbl.setText(DateUtils.getRelativeDateTimeString(getApplicationContext(), Utils.DateHelper.DF_SIMPLE_FORMAT
                                .get().parse((String) ((HashMap<String, Object>) mRecents.get(position)).get(kDATE)).getTime(),DateUtils.SECOND_IN_MILLIS,
                        DateUtils.SECOND_IN_MILLIS,0));
                } catch (ParseException e) {
                e.printStackTrace();
                }
                lbl = holder.itemView.findViewById(R.id.tv_recent_message);
            lbl.setMaxLines(1);
            RNCryptorNative rncryptor = new RNCryptorNative();

            String decrypted = rncryptor.decrypt((String) ((HashMap<String, Object>) mRecents.get(position)).get(kLASTMESSAGE),
                    (String) ((HashMap<String, Object>) mRecents.get(position)).get(kCHATROOMID));

            if(decrypted.equals(getString(R.string.error_decrypting_string))){
                decrypted = "";
                }
                lbl.setText(decrypted);

            lbl = holder.itemView.findViewById(R.id.tv_recent_counter);
            lbl.setText("");

            if( (long) ((HashMap<String, Object>) mRecents.get(position)).get(kCOUNTER)   > 0){

                lbl.setText( ((HashMap<String, Object>) mRecents.get(position)).get(kCOUNTER).toString() + " New  ");
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) { Intent intent = new Intent(getApplicationContext(), MessageActivity.class);

                        intent.putExtra(kCURRENTUSERID, mCurrentUserUid);
                        intent.putExtra(kCHATROOMID, (String) mChatRoomIDs.get(position));
                        intent.putExtra(kPOST, (Post) mPostList.get(position));
                        intent.putExtra(kUSER, (User) mUserList.get(position));

                        startActivityForResult(intent, MESSAGE_ACTIVITY);
                        }
            });
        }


        @Override
        public long getItemId(int p0) {
            return (long) p0;
        }

        @Override
        public int getItemCount(){
            return uPosts.size();
        }

        @Override
        public void onItemDismiss(int position){
            deleteWarning(position);
            mAdapater.notifyDataSetChanged();
        }
    }


    private void deleteWarning(int position){

        DeleteDialogFragment deleteDialog = DeleteDialogFragment.newInstance(getString(R.string.attention_alert_title),
                ((String) ((HashMap<String, Object>) mRecents.get(position)).get(kRECENTID)), kRECENT, position);
        deleteDialog.show(getSupportFragmentManager(),getString(R.string.delete_fragment_alert_tag));
    }

    @Override
    public void onComplete(int position) {
        mRecentRecyclerView.removeViewAt(position);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MESSAGE_ACTIVITY){
            mAdapater.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

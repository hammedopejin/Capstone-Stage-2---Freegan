package com.planetpeopleplatform.freegan.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateUtils;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.fragment.DeleteDialogFragment;
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;
import com.planetpeopleplatform.freegan.utils.ItemTouchHelperAdapter;
import com.planetpeopleplatform.freegan.utils.FreeganItemTouchHelperCallback;
import com.planetpeopleplatform.freegan.utils.Utils;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import tgio.rncryptor.RNCryptorNative;

import static com.bumptech.glide.request.RequestOptions.centerInsideTransform;
import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kCOUNTER;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kDATE;
import static com.planetpeopleplatform.freegan.utils.Constants.kLASTMESSAGE;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPRIVATE;
import static com.planetpeopleplatform.freegan.utils.Constants.kRECENT;
import static com.planetpeopleplatform.freegan.utils.Constants.kRECENTID;
import static com.planetpeopleplatform.freegan.utils.Constants.kTYPE;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kWITHUSERUSERID;
import static com.planetpeopleplatform.freegan.utils.Utils.DF_SIMPLE_STRING_WITH_DETAILS;

public class RecentChatActivity extends CustomActivity  implements DeleteDialogFragment.OnCompleteListener {

    private ValueEventListener mValueEventListener;
    private DatabaseReference mRecentsDatabaseReference;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    @BindView(R.id.recent_recyclerview)
    RecyclerView mRecentRecyclerView;

    @BindView(R.id.empty_recent_chat_text)
    TextView mEmptyTextView;

    @BindView(R.id.fragment_container)
    CoordinatorLayout mCoordinatorLayout;

    private final int MESSAGE_ACTIVITY = 777;

    private String mCurrentUserUid;
    private ArrayList mWithUserIds = new ArrayList<String>();
    private ArrayList mPostIds = new ArrayList<String>();
    private ArrayList mChatRoomIDs = new ArrayList<String>();
    private ArrayList mRecents = new ArrayList<HashMap<String, Object>>();

    //Chat order participants
    private ArrayList mUserList = new ArrayList<User>();

    RNCryptorNative mRncryptor;
    PostAdapter mAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_chat);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.chats_title);


        mCurrentUserUid = getIntent().getStringExtra(kCURRENTUSERID);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        mCurrentUserUid = auth.getCurrentUser().getUid();

        mRncryptor = new RNCryptorNative();

        mRecentsDatabaseReference = firebase.child(kRECENT);
        mLoadingIndicator.setVisibility(View.VISIBLE);
        attachDatabaseReadListener();
    }


    @Override
    public void onResume(){
        super.onResume();
    }


    @Override
    public void onPause(){
        super.onPause();
    }


    @Override
    public void onDestroy(){
        detachDatabaseReadListener();
        super.onDestroy();
    }

    static final Comparator<HashMap<String, Object>> byDate = new Comparator<HashMap<String, Object>>() {
        SimpleDateFormat sdf = new SimpleDateFormat(DF_SIMPLE_STRING_WITH_DETAILS);

        public int compare(HashMap<String, Object> ord1, HashMap<String, Object> ord2) {
            Date d1 = new Date();
            Date d2 = new Date();
            try {
                d1 = sdf.parse((String) ord1.get(kDATE));
                d2 = sdf.parse((String) ord2.get(kDATE));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (d1.getTime() > d2.getTime()) return -1;     //descending
            else if (d1.getTime() < d2.getTime()) return 1;
            else return  0;//descending
        }
    };

    private void attachDatabaseReadListener() {
        if (mValueEventListener == null) {
            mValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    try{
                        mRecents.clear();
                        mUserList.clear();
                        mChatRoomIDs.clear();
                        mWithUserIds.clear();
                        mPostIds.clear();

                        HashMap<String, Object> snapshotValue = (HashMap<String, Object>) dataSnapshot.getValue();

                        ArrayList<HashMap<String, Object>> map = new ArrayList<>();
                        if (snapshotValue != null) {
                            for (Object object : snapshotValue.values()){
                                map.add((HashMap<String, Object>) object);
                            }
                        }

                        Collections.sort(map, byDate);

                        for (Object recent : map){

                            final HashMap<String, Object> currentRecent = (HashMap<String, Object>) recent;

                            if((currentRecent.get(kTYPE)).equals(kPRIVATE)) {

                                    mWithUserIds.add(currentRecent.get(kWITHUSERUSERID));
                                    mPostIds.add(currentRecent.get(kPOSTID));
                                    mRecents.add(currentRecent);
                                    mChatRoomIDs.add(currentRecent.get(kCHATROOMID));

                                if(mRecents.size() == map.size()) {
                                    for (int i = 0; i < mRecents.size(); i ++){
                                        if (((HashMap<String, Object>) mRecents.get(i)).get(kLASTMESSAGE).toString().isEmpty()){
                                            mRecents.remove(i);
                                            mPostIds.remove(i);
                                            mWithUserIds.remove(i);
                                            mChatRoomIDs.remove(i);
                                        }
                                    }
                                    loadUser(getApplicationContext(), mPostIds, mWithUserIds, mRecents, mChatRoomIDs);
                                }


                                mRecentsDatabaseReference.orderByChild(kCHATROOMID).equalTo((String) currentRecent.get(kCHATROOMID))
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
                    }catch(Exception e){
                        Snackbar.make(mCoordinatorLayout, R.string.error_fetching_data_string, Snackbar.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mRecentsDatabaseReference.orderByChild(kUSERID).equalTo(mCurrentUserUid)
                    .addValueEventListener(mValueEventListener);

            mRecentsDatabaseReference.orderByChild(kUSERID).equalTo(mCurrentUserUid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
        if (mValueEventListener != null) {
            mRecentsDatabaseReference.removeEventListener(mValueEventListener);
            mValueEventListener = null;
        }
    }

    //Helper method for fetching post
    public void loadPost(final Context context, final ArrayList<String> postIds,
                         final ArrayList<User> userList, final ArrayList<HashMap<String, Object>> recents,
                         final ArrayList<String> chatRoomIDs){

        final ArrayList postList = new ArrayList<Post>();
        for (int i = 0; i < postIds.size(); i++) {
            final int finalI = i;
            firebase.child(kPOST).child(postIds.get(i))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {

                                Post post = new Post((HashMap<String, Object>) dataSnapshot.getValue());
                                postList.add(post);

                                if (finalI == (postIds.size() - 1)) {
                                    mAdapter = new PostAdapter(context, userList, postList, recents,
                                            chatRoomIDs);
                                    FreeganItemTouchHelperCallback callback = new FreeganItemTouchHelperCallback(mAdapter);
                                    ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                                    touchHelper.attachToRecyclerView(mRecentRecyclerView);
                                    mRecentRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                                    mRecentRecyclerView.hasFixedSize();
                                    mRecentRecyclerView.setAdapter(mAdapter);

                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }

    //Helper method for fetching user
    private void loadUser(final Context context, final ArrayList<String> postIds, final ArrayList<String> withUserIds,
                          final ArrayList<HashMap<String, Object>> recents, final ArrayList<String> chatRoomIDs){

        for (int i = 0; i < withUserIds.size(); i++) {
            final int finalI = i;
            firebase.child(kUSER).child(withUserIds.get(i))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {

                                User chatMate = new User((HashMap<String, Object>) dataSnapshot.getValue());
                                mUserList.add(chatMate);

                                if ((withUserIds.size() - 1) == finalI) {
                                    loadPost(context, postIds, mUserList, recents, chatRoomIDs);
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }

    }

    class Item extends RecyclerView.ViewHolder{

        Item(View itemView) {
            super(itemView);
        }

        private void bindData(User user, Post post){


            TextView lbl = itemView.findViewById(R.id.tv_recent_name);
            lbl.setMaxLines(1);
            lbl.setText(user.getUserName());


            Glide.with(getApplicationContext())
                    .load(user.getUserImgUrl()).apply(centerInsideTransform()
                    .placeholder(R.drawable.ic_account_circle_black_24dp))
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
                    .load(post.getImageUrl().get(0)).apply(centerInsideTransform()
                    .placeholder(R.drawable.ic_account_circle_black_24dp))
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


    class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
            implements ItemTouchHelperAdapter{

        View myView = null;
        Context mContext;
        ArrayList<User> uList;
        ArrayList<Post> uPosts;
        ArrayList<HashMap<String, Object>> uRecents;
        ArrayList<String> uChatRoomIDs;

        PostAdapter(Context context, ArrayList<User> userList, ArrayList<Post> posts,
                    ArrayList<HashMap<String, Object>> recents, ArrayList<String> chatRoomIDs) {
            mContext = context;
            uList = userList;
            uPosts = posts;
            uRecents = recents;
            uChatRoomIDs = chatRoomIDs;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            myView = LayoutInflater.from(mContext).inflate(R.layout.recent_item, parent,
                    false);
            return new Item(myView);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position){

            if (uList.size() != 0 && uPosts.size() != 0) {

                ((Item) holder).bindData(uList.get(holder.getAdapterPosition()), uPosts.get(holder.getAdapterPosition()));
                holder.itemView.setTag(holder.getAdapterPosition());

                TextView lbl = holder.itemView.findViewById(R.id.tv_recent_message_date);

                try {
                    lbl.setText(DateUtils.getRelativeDateTimeString(getApplicationContext(),
                            Utils.DateHelper.DF_SIMPLE_FORMAT
                                    .get().parse((String) (uRecents.get(holder.getAdapterPosition()))
                                    .get(kDATE)).getTime(), DateUtils.SECOND_IN_MILLIS,
                            DateUtils.SECOND_IN_MILLIS, 0));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                lbl = holder.itemView.findViewById(R.id.tv_recent_message);
                lbl.setMaxLines(1);

                String decrypted = mRncryptor.decrypt((String) ((HashMap<String, Object>) mRecents.get(holder.getAdapterPosition()))
                                .get(kLASTMESSAGE),
                        (String) (uRecents.get(holder.getAdapterPosition())).get(kCHATROOMID));

                if (decrypted.equals(getString(R.string.error_decrypting_string))) {
                    decrypted = "";
                }
                lbl.setText(decrypted);

                lbl = holder.itemView.findViewById(R.id.tv_recent_counter);
                lbl.setText("");

                if ((long) (uRecents.get(holder.getAdapterPosition())).get(kCOUNTER) > 0) {

                    lbl.setText(MessageFormat.format("{0} New  ", (uRecents.get(holder.getAdapterPosition())).get(kCOUNTER)
                            .toString()));
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent =
                                new Intent(getApplicationContext(), MessageActivity.class);

                        intent.putExtra(kCURRENTUSERID, mCurrentUserUid);
                        intent.putExtra(kCHATROOMID, uChatRoomIDs.get(holder.getAdapterPosition()));
                        intent.putExtra(kPOST, uPosts.get(holder.getAdapterPosition()));
                        intent.putExtra(kUSER, uList.get(holder.getAdapterPosition()));

                        startActivityForResult(intent, MESSAGE_ACTIVITY);
                    }
                });
            }
        }


        @Override
        public long getItemId(int p0) {
            return (long) p0;
        }

        @Override
        public int getItemCount(){
            if(uPosts == null){
                return 0;
            }
            return uPosts.size();
        }

        @Override
        public void onItemDismiss(int position){
            deleteWarning(position);
            mAdapter.notifyDataSetChanged();
        }
    }


    private void deleteWarning(int position){

        DeleteDialogFragment deleteDialog = DeleteDialogFragment.newInstance(getString(R.string.attention_alert_title),
                ((String) ((HashMap<String, Object>) mRecents.get(position)).get(kRECENTID)), kRECENT, position);
        deleteDialog.show(getSupportFragmentManager(), getString(R.string.delete_fragment_alert_tag));
    }

    @Override
    public void onComplete(int position) {
        mRecentRecyclerView.removeViewAt(position);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MESSAGE_ACTIVITY){
            recreate();
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
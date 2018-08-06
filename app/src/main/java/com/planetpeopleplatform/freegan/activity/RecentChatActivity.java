package com.planetpeopleplatform.freegan.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.model.User;
import com.planetpeopleplatform.freegan.utils.ItemTouchHelperAdapter;
import com.planetpeopleplatform.freegan.utils.MyItemTouchHelperCallback;
import com.planetpeopleplatform.freegan.utils.Utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import tgio.rncryptor.RNCryptorNative;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kCOUNTER;
import static com.planetpeopleplatform.freegan.utils.Constants.kDATE;
import static com.planetpeopleplatform.freegan.utils.Constants.kLASTMESSAGE;
import static com.planetpeopleplatform.freegan.utils.Constants.kPRIVATE;
import static com.planetpeopleplatform.freegan.utils.Constants.kRECENT;
import static com.planetpeopleplatform.freegan.utils.Constants.kTYPE;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kWITHUSERUSERID;

public class RecentChatActivity extends CustomActivity{


        private final int CHAT_ACTIVITY = 777;

        private String mCurrentUserUID;

        private ArrayList mRecents = new ArrayList<HashMap<String, Object>>();

        /**
         * The Recent chat list.
         */
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

            mCurrentUserUID = getIntent().getStringExtra("currentUserUID");
            //  Toast.makeText(getApplicationContext(), mCurrentUserUID,Toast.LENGTH_LONG).show();


            firebase.child(kUSER).child(mCurrentUserUID).addValueEventListener(new ValueEventListener() {
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

            mAdapater = new PostAdapater(this, mUserList);

            RecyclerView recentRecyclerView = findViewById(R.id.recent_recyclerview);
            MyItemTouchHelperCallback callback = new MyItemTouchHelperCallback(mAdapater);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(recentRecyclerView);
            recentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            recentRecyclerView.hasFixedSize();
            recentRecyclerView.setAdapter(mAdapater);
        }


        @Override
        public void onResume(){
            super.onResume();
            loadRecent();
            isRunning = true;
            }


            @Override
            public void onPause(){
            super.onPause();
            isRunning = false;
            }


            @Override
            public void onDestroy(){
            super.onDestroy();
            updateUserStatus(false);
            }


            private void updateUserStatus(boolean online){
                mCurrentUser.put("online", online);
            }

            /**
             * Load list of users.
             */
            private void loadRecent(){



                firebase.child(kRECENT).orderByChild(kUSERID).equalTo(mCurrentUserUID)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                try{
                                    mRecents.clear();
                                    mUserList.clear();

                                    Collection<Object> sortedArray =
                                            ((HashMap<String, Object>) dataSnapshot.getValue()).values();



                                    for (Object recent : sortedArray){

                                        final HashMap<String, Object> currentRecent = (HashMap<String, Object>) recent;
                                        Log.d("TAG", currentRecent.toString());

                                        firebase.child(kRECENT).orderByChild(kCHATROOMID).equalTo((String) currentRecent.get(kCHATROOMID))
                                                .addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                    }
                                                });

                                        if((((HashMap<String, Object>) recent).get(kTYPE)).equals(kPRIVATE)) {

                                            String withUserId = (String) currentRecent.get(kWITHUSERUSERID);
                                            //Log.d("TAG", withUserId);

                                            firebase.child(kUSER).child(withUserId)
                                                    .addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if(dataSnapshot.exists()){

                                                                User fUser = new User((HashMap<String, Object>) dataSnapshot.getValue());

                                                                mUserList.add(fUser);
                                                                mRecents.add(currentRecent);

                                                                }
                                                            mAdapater.notifyDataSetChanged();
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                        }
                                    }
                                }catch(Exception e){
                                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                }


                class Item extends RecyclerView.ViewHolder{

                    public Item(View itemView) {
                        super(itemView);
                    }

                    private void bindData(User user){


                            TextView lbl = itemView.findViewById(R.id.tv_recent_name);
                            lbl.setMaxLines(1);
                            lbl.setText(user.getUserName());

                            lbl.setCompoundDrawablesWithIntrinsicBounds((user.getBoolean("online")) ?
                                                R.drawable.ic_online
                                             : R.drawable.ic_offline,0,0,0);

                            firebase.child("users").child(user.getObjectId())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            try{

                                                HashMap<String, Object> td = (HashMap<String, Object>) dataSnapshot.getValue();

                                                for(Object key : td.keySet()){
                                                    if(key.equals("userImgUrl")){
                                                        String userPicUrl = (String) td.get(key);
                                                        Glide.with(getApplicationContext())
                                                                .load(userPicUrl)
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
                                                    }
                                                }

                                            }catch(Exception exception){}
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });



                    }
                }



                class PostAdapater extends RecyclerView.Adapter<RecyclerView.ViewHolder>
                    implements ItemTouchHelperAdapter{


                    View myView = null;
                    Context context;
                    ArrayList<User> uList;

                    public PostAdapater(Context c, ArrayList userList) {
                        context = c;
                        uList = userList;
                    }

                    @NonNull
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        myView = LayoutInflater.from(context).inflate(R.layout.recent_item, parent, false);
                        return new Item(myView);
                    }

                    @Override
                    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position){

                    ((Item) holder).bindData(uList.get(position));
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

                    if(decrypted.equals("error decrypting")){
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
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), ChatActivity.class);

                            User user2 = (User) uList.get(position);

                            String chatRoomId = Utils.startChat(mCurrentUser, user2);

                            intent.putExtra("currentUserUID", mCurrentUserUID);
                            intent.putExtra(kCHATROOMID, chatRoomId);


                            startActivityForResult(intent, CHAT_ACTIVITY);
                        }
                    });

                    }


                    @Override
                    public long getItemId(int p0) {
                        return (long) p0;
                    }

                    @Override
                        public int getItemCount(){
                        return mRecents.size();
                    }

                @Override
                public void onItemDismiss(int position){
                    deleteWarning(position);
                    mAdapater.notifyDataSetChanged();
                    }

                }


                private void deleteWarning(int position){


                }

                @Override
                public void onActivityResult(int requestCode, int resultCode, Intent data){
                    super.onActivityResult(requestCode,resultCode,data);
                }

                @Override
                public boolean onOptionsItemSelected(MenuItem item) {
                    if(item.getItemId()==android.R.id.home){
                        finish();
                    }
                    return super.onOptionsItemSelected(item);
                }

                @Override
                public void onBackPressed(){
                    super.onBackPressed();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }

}

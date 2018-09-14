package com.planetpeopleplatform.freegan.fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.activity.MessageActivity;
import com.planetpeopleplatform.freegan.activity.ProfileActivity;
import com.planetpeopleplatform.freegan.adapter.MainChildViewPagerAdapter;
import com.planetpeopleplatform.freegan.data.FreeganContract;
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;
import com.planetpeopleplatform.freegan.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.MODE_PRIVATE;
import static com.bumptech.glide.request.RequestOptions.centerInsideTransform;
import static com.planetpeopleplatform.freegan.utils.Constants.JSONARRAYKEY;
import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Utils.closeOnError;

public class MainChildImagePagerFragment extends Fragment {

    private static final String KEY_POST_RES = "com.planetpeopleplatform.freegan.key.postRes";
    private static final int RC_PROFILE_ACTIVITY = 777;
    private String mChatMateId;
    private String mChatRoomId = null;
    private User mCurrentUser = null;
    public String mCurrentUserUid = null;
    private FirebaseAuth mAuth;
    private Post mPost = null;
    private MainChildViewPagerAdapter mMainChildViewPagerAdapter;


    @BindView(R.id.nestedViewPager)
    fr.castorflex.android.verticalviewpager.VerticalViewPager mNestedViewPager;

    @BindView(R.id.text_view)
    TextView mTextView;

    @BindView(R.id.main_content)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.back_arrow)
    ImageButton mBackArrow;

    @BindView(R.id.poster_image_button)
    de.hdodenhof.circleimageview.CircleImageView mPosterImageButton;

    @BindView(R.id.favorite_image)
    ImageView mFavoriteImageButton;

    @BindView(R.id.contact_button_view)
    android.support.design.widget.FloatingActionButton mContactButtonView;


    public MainChildImagePagerFragment() {
    }

    public static MainChildImagePagerFragment newInstance(Post post) {
        MainChildImagePagerFragment fragment = new MainChildImagePagerFragment();
        Bundle argument = new Bundle();
        argument.putParcelable(KEY_POST_RES, post);
        fragment.setArguments(argument);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_pager, container, false);
        ButterKnife.bind(this, rootView);

        mAuth= FirebaseAuth.getInstance();

        Bundle arguments = getArguments();

        if (arguments == null) {
            closeOnError(mCoordinatorLayout, getActivity());
        }
        mPost = arguments.getParcelable(KEY_POST_RES);
        String postDescription = mPost.getDescription();

        mMainChildViewPagerAdapter = new MainChildViewPagerAdapter(this, mPost);
        mNestedViewPager.setAdapter(mMainChildViewPagerAdapter);

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        final SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences(
                getString(R.string.com_planetpeopleplatform_freegan_preference_favorite_file_key), MODE_PRIVATE);

        final boolean[] mIsFavorited = { sharedPref.getBoolean(mPost.getPostId(), false) };
        if (mIsFavorited[0]){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mFavoriteImageButton.setImageDrawable(getActivity().getDrawable(android.R.drawable.star_big_on));
            } else {
                mFavoriteImageButton.setImageResource(android.R.drawable.star_big_on);
            }
        }

        mFavoriteImageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mIsFavorited[0]){
                    Uri uri = FreeganContract.FreegansEntry.buildFreeganUriWithFreeganId(mPost.getPostId());
                    deleteData(uri);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mFavoriteImageButton.setImageDrawable(getActivity().getDrawable(android.R.drawable.star_big_off));
                    } else {
                        mFavoriteImageButton.setImageResource(android.R.drawable.star_big_off);
                    }
                    mIsFavorited[0] = false;
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean(mPost.getPostId(), mIsFavorited[0]);
                    editor.apply();
                }else {
                    insertData();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mFavoriteImageButton.setImageDrawable(getActivity().getDrawable(android.R.drawable.star_big_on));
                    } else {
                        mFavoriteImageButton.setImageResource(android.R.drawable.star_big_on);
                    }
                    mIsFavorited[0] = true;
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean(mPost.getPostId(), mIsFavorited[0]);
                    editor.apply();
                }
            }
        });

        mContactButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Intent intent = new Intent(getActivity(), MessageActivity.class);
                mChatMateId = mPost.getPostUserObjectId();

                firebase.child(kUSER).child(mChatMateId).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            User chatMate = new User((HashMap<String, Object>) dataSnapshot.getValue());
                            if (!(chatMate.getObjectId().equals(mCurrentUserUid))) {

                                if (mCurrentUser != null) {
                                    mChatRoomId = Utils.startChat(mCurrentUser, chatMate, mPost.getPostId());

                                    intent.putExtra(kCURRENTUSERID, mCurrentUserUid);
                                    intent.putExtra(kCHATROOMID, mChatRoomId);
                                    intent.putExtra(kPOST, mPost);
                                    intent.putExtra(kUSER, chatMate);

                                    startActivity(intent);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        mPosterImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profileIntent = new Intent(getActivity(), ProfileActivity.class);
                profileIntent.putExtra(kPOSTERID, mPost.getPostUserObjectId());
                startActivityForResult(profileIntent, RC_PROFILE_ACTIVITY);
            }
        });

        mTextView.setText(postDescription);
        mCurrentUserUid = mAuth.getCurrentUser().getUid();
        loadUserProfilePicture(rootView, this, mPost.getPostUserObjectId());
        if (mPost.getPostUserObjectId().equals(mCurrentUserUid)) {
            mContactButtonView.setVisibility(View.GONE);
        }
        getCurrentUser(mCurrentUserUid);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        android.support.design.widget.AppBarLayout mToolbarContainer = getActivity().findViewById(R.id.toolbar_container);
        mToolbarContainer.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PROFILE_ACTIVITY) {

        }
    }

    private void loadUserProfilePicture(final View view, final Fragment fragment, String posterId){
        firebase.child(kUSER).child(posterId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    User user = new User((java.util.HashMap<String, Object>) dataSnapshot.getValue());

                    Glide.with(fragment)
                            .load(user.getUserImgUrl())
                            .apply(centerInsideTransform()
                                    .placeholder(R.drawable.person_icon))
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
                            .into(mPosterImageButton);


                }catch (Exception ex){}
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private User getCurrentUser(String currentUserUid) {
        firebase.child(kUSER).child(currentUserUid).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    mCurrentUser = new User((HashMap<String, Object>) dataSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return mCurrentUser;
    }

    // insert data into database
    private void insertData(){
        ContentValues movieValues = new ContentValues();

        movieValues.put(FreeganContract.FreegansEntry.COLUMN_POST_DESCRIPTION, mPost.getDescription());
        movieValues.put(FreeganContract.FreegansEntry.COLUMN_FREEGAN_ID, mPost.getPostId());

        JSONObject json = new JSONObject();
        try {
            json.put(JSONARRAYKEY, new JSONArray(mPost.getImageUrl()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String postImageUrls = json.toString();

        movieValues.put(FreeganContract.FreegansEntry.COLUMN_POST_PICTURE_PATH, postImageUrls);
        movieValues.put(FreeganContract.FreegansEntry.COLUMN_POSTER_NAME, mPost.getUserName());
        movieValues.put(FreeganContract.FreegansEntry.COLUMN_POSTER_ID, mPost.getPostUserObjectId());
        movieValues.put(FreeganContract.FreegansEntry.COLUMN_POSTER_PICTURE_PATH, mPost.getProfileImgUrl());
        movieValues.put(FreeganContract.FreegansEntry.COLUMN_POST_DATE, mPost.getPostDate());

        getActivity().getApplicationContext().getContentResolver().insert(FreeganContract.FreegansEntry.CONTENT_URI,
                movieValues);
    }

    // delete data from database
    private void deleteData(Uri uri){
        getActivity().getApplicationContext().getContentResolver().delete(uri,
                null, null);
    }
}
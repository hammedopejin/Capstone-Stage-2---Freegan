package com.planetpeopleplatform.freegan.fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;
import com.planetpeopleplatform.freegan.utils.Utils;
import com.planetpeopleplatform.freegan.utils.VerticalViewPager;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.request.RequestOptions.centerInsideTransform;
import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;

public class MainChildImagePagerFragment extends Fragment {

    private static final String KEY_POST_RES = "com.planetpeopleplatform.freegan.key.postRes";
    private static final int RC_PROFILE_ACTIVITY = 777;
    private String mChatMateId;
    private String mChatRoomId = null;
    private User mCurrentUser = null;
    public String mCurrentUserUid = null;
    private FirebaseAuth mAuth;
    private Post mPost = null;


    @BindView(R.id.nestedViewPager)
    VerticalViewPager nestedViewPager;

    @BindView(R.id.text_view)
    TextView mTextView;

    @BindView(R.id.back_arrow)
    ImageButton mBackArrow;

    @BindView(R.id.poster_image_button)
    de.hdodenhof.circleimageview.CircleImageView mPosterImageButton;

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
        mPost = arguments.getParcelable(KEY_POST_RES);
        String postDescription = mPost.getDescription();

        nestedViewPager.setAdapter(new MainChildViewPagerAdapter(this, mPost));



        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
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
}
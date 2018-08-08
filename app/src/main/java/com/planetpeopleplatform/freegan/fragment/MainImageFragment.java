package com.planetpeopleplatform.freegan.fragment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.planetpeopleplatform.freegan.activity.ChatActivity;
import com.planetpeopleplatform.freegan.activity.ProfileActivity;
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;
import com.planetpeopleplatform.freegan.utils.Utils;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERID;

public class MainImageFragment extends Fragment {

    private static final String KEY_POST_RES = "com.planetpeopleplatform.freegan.key.postRes";
    private String mChatMateId;
    private Post mPost = null;
    private String mChatRoomId = null;
    private User mCurrentUser = null;
    public String mCurrentUserUid = null;
    private FirebaseAuth mAuth;

    @BindView(R.id.text_view)
    TextView mTextView;

    @BindView(R.id.back_arrow)
    ImageButton mBackArrow;

    @BindView(R.id.poster_image_button)
    de.hdodenhof.circleimageview.CircleImageView mPosterImageButton;

    @BindView(R.id.contact_button_view)
    android.support.design.widget.FloatingActionButton mContactButtonView;

    public static MainImageFragment newInstance(Post post) {
        MainImageFragment fragment = new MainImageFragment();
        Bundle argument = new Bundle();
        argument.putParcelable(KEY_POST_RES, post);
        fragment.setArguments(argument);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_image, container, false);
        ButterKnife.bind(this, view);

        mAuth= FirebaseAuth.getInstance();

        Bundle arguments = getArguments();
        mPost = arguments.getParcelable(KEY_POST_RES);
        String postImage = mPost.getImageUrl();
        String postDescription = mPost.getDescription();

        // Just like we do when binding views at the grid, we set the transition name to be the string
        // value of the image res.
        view.findViewById(R.id.image).setTransitionName(String.valueOf(postImage));

        // Load the image with Glide to prevent OOM error when the image drawables are very large.
        Glide.with(this)
                .load(postImage)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable>
                            target, boolean isFirstResource) {
                        // The postponeEnterTransition is called on the parent MainImagePagerFragment, so the
                        // startPostponedEnterTransition() should also be called on it to get the transition
                        // going in case of a failure.
                        getParentFragment().startPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable>
                            target, DataSource dataSource, boolean isFirstResource) {
                        // The postponeEnterTransition is called on the parent MainImagePagerFragment, so the
                        // startPostponedEnterTransition() should also be called on it to get the transition
                        // going when the image is ready.
                        getParentFragment().startPostponedEnterTransition();
                        return false;
                    }
                })
                .into((ImageView) view.findViewById(R.id.image));



        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        mContactButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Intent intent = new Intent(getActivity(), ChatActivity.class);
                mChatMateId = mPost.getPostUserObjectId();

                firebase.child(kUSER).child(mChatMateId).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            User chatMate = new User((HashMap<String, Object>) dataSnapshot.getValue());
                            if (!(chatMate.getObjectId().equals(mCurrentUserUid))) {

                                if (mCurrentUser != null) {
                                    mChatRoomId = Utils.startChat(mCurrentUser, chatMate);

                                    //  Toast.makeText(applicationContext, chatRoomId, Toast.LENGTH_LONG).show()

                                    intent.putExtra("currentUserUID", mCurrentUserUid);
                                    intent.putExtra(kCHATROOMID, mChatRoomId);

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
                profileIntent.putExtra(kUSER, mPost);
                startActivity(profileIntent);
            }
        });

        mTextView.setText(postDescription);
        mCurrentUserUid = mAuth.getCurrentUser().getUid();
        loadUserProfilePicture(view, this, mPost.getPostUserObjectId());
        getCurrentUser(mCurrentUserUid);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        android.support.design.widget.AppBarLayout mToolbarContainer = getActivity().findViewById(R.id.toolbar_container);
        mToolbarContainer.setVisibility(View.GONE);
    }

    private void loadUserProfilePicture(final View view, final Fragment fragment, String posterId){
        firebase.child(kUSER).child(posterId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    User user = new User((java.util.HashMap<String, Object>) dataSnapshot.getValue());

                    Glide.with(fragment)
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
package com.planetpeopleplatform.freegan.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.SharedElementCallback;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.activity.ProfileActivity;
import com.planetpeopleplatform.freegan.adapter.ProfileGridAdapter;
import com.planetpeopleplatform.freegan.model.Post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTUSEROBJECTID;

public class ProfileGridFragment extends Fragment {

    private static final String TAG = ProfileGridFragment.class.getSimpleName();

    private Fragment mFragment = null;

    private ArrayList<Post> mListPosts = new ArrayList<Post>();
    public Post mPost = null;

    private static final String KEY_POSTER_UID = "com.planetpeopleplatform.freegan.key.posterUid";

    @BindView(R.id.profile_image_view)
    de.hdodenhof.circleimageview.CircleImageView mProfileImageView;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    @BindView(R.id.back_arrow)
    ImageButton mBackArrow;

    @BindView(R.id.collapsing_toolbar)
    android.support.design.widget.CollapsingToolbarLayout mCollapsingToolbarLayout;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    public static ProfileGridFragment newInstance(Post post) {
        ProfileGridFragment fragment = new ProfileGridFragment();
        Bundle argument = new Bundle();
        argument.putParcelable(KEY_POSTER_UID, post);
        fragment.setArguments(argument);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile_grid, container, false);
        ButterKnife.bind(this, rootView);

        Bundle arguments = getArguments();
        mPost = arguments.getParcelable(KEY_POSTER_UID);

        mFragment = this;

        prepareTransitions();
        postponeEnterTransition();
        Glide.with(this).load(mPost.getProfileImgUrl()).into(mProfileImageView);
        mCollapsingToolbarLayout.setCollapsedTitleTextColor(Color.parseColor("#ffffff"));
        mCollapsingToolbarLayout.setExpandedTitleColor(Color.parseColor("#DD2C00"));
        mCollapsingToolbarLayout.setTitle(mPost.getUserName());
        showLoading();
        loadPost();
        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scrollToPosition();
    }

    @Override
    public void onResume(){
        super.onResume();
        loadPost();
    }

    @Override
    public void onPause(){
        super.onPause();
        mListPosts.clear();
    }

    /**
     * Scrolls the recycler view to show the last viewed item in the grid. This is important when
     * navigating back from the grid.
     */
    private void scrollToPosition() {
        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left,
                                       int top,
                                       int right,
                                       int bottom,
                                       int oldLeft,
                                       int oldTop,
                                       int oldRight,
                                       int oldBottom) {
                mRecyclerView.removeOnLayoutChangeListener(this);
                final RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
                View viewAtPosition = layoutManager.findViewByPosition(ProfileActivity.currentPosition);
                // Scroll to position if the view for the current position is null (not currently part of
                // layout manager children), or it's not completely visible.
                if (viewAtPosition == null || layoutManager
                        .isViewPartiallyVisible(viewAtPosition, false, true)) {
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            layoutManager.scrollToPosition(ProfileActivity.currentPosition);
                        }
                    });
                }
            }
        });
    }

    /**
     * Prepares the shared element transition to the pager fragment, as well as the other transitions
     * that affect the flow.
     */
    private void prepareTransitions() {
        setExitTransition(TransitionInflater.from(getContext())
                .inflateTransition(R.transition.grid_exit_transition));


        setExitSharedElementCallback(
                new SharedElementCallback() {
                    @Override
                    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                        // Locate the ViewHolder for the clicked position.
                        RecyclerView.ViewHolder selectedViewHolder = mRecyclerView
                                .findViewHolderForAdapterPosition(ProfileActivity.currentPosition);
                        if (selectedViewHolder == null || selectedViewHolder.itemView == null) {
                            return;
                        }

                        // Map the first shared element name to the child ImageView.
                        sharedElements
                                .put(names.get(0), selectedViewHolder.itemView.findViewById(R.id.card_image));
                    }
                });
    }


    private void loadPost(){

        firebase.child(kPOST).orderByChild(kPOSTUSEROBJECTID).equalTo(mPost.getPostUserObjectId())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    mListPosts.clear();

                    HashMap<String, Object> postData= (HashMap<String, Object>) dataSnapshot.getValue();

                    for (String key : postData.keySet()){
                        HashMap<String, Object> post = (HashMap<String, Object>) postData.get(key);
                        mListPosts.add(new Post(post));
                    }

                    mRecyclerView.setAdapter(new ProfileGridAdapter(mFragment, mListPosts));
                    showDataView();
                }catch (Exception e){
                    Log.d(TAG, "onDataChange: " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void showDataView() {
        /* First, hide the loading indicator */
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        /* Finally, make sure the data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        /* Then, hide the data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Finally, show the loading indicator */
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

}

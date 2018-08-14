package com.planetpeopleplatform.freegan.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.activity.MainActivity;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.activity.PostActivity;
import com.planetpeopleplatform.freegan.adapter.MainGridAdapter;
import com.planetpeopleplatform.freegan.model.Post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTLOCATION;

/**
 * A fragment for displaying a grid of images.
 */
public class MainGridFragment extends Fragment {

    private static final String TAG = MainGridFragment.class.getSimpleName();
    private Fragment mFragment = null;
    private ArrayList<Post> mListPosts = new ArrayList<Post>();
    ArrayList<String> mPostIds = new ArrayList<>();
    GeoQuery mGeoQuery;
    private GeoFire mGeoFire;

    private static final int RC_POST_ITEM = 1;

    SearchView mSearchView;
    @BindView(R.id.main_content_swipe_refresh_layout)
    SwipeRefreshLayout mSwipeContainer;

    @BindView(R.id.new_item_button_view)
    android.support.design.widget.FloatingActionButton mPhotoPickerButton;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_grid, container, false);
        ButterKnife.bind(this, rootView);

        mGeoFire = new GeoFire(firebase.child(kPOSTLOCATION));


        mSearchView = getActivity().findViewById(R.id.searchView);
        mFragment = this;

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return searchFeed(newText);
            }
        });



        // ImagePickerButton shows an image picker to upload a image
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PostActivity.class);
                startActivityForResult(intent, RC_POST_ITEM);
            }
        });

        prepareTransitions();
        postponeEnterTransition();
        showLoading();
        checkPosts();
        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        scrollToPosition();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_POST_ITEM && data!=null && resultCode == RESULT_OK) {
            checkPosts();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        android.support.design.widget.AppBarLayout mToolbarContainer = getActivity().findViewById(R.id.toolbar_container);
        mToolbarContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGeoQuery.removeAllListeners();
    }

    /**
     * Scrolls the recycler view to show the last viewed item in the grid. This is important when
     * navigating back from the grid.
     */
    private void scrollToPosition() {
        mRecyclerView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
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
                View viewAtPosition = layoutManager.findViewByPosition(MainActivity.currentPosition);
                // Scroll to position if the view for the current position is null (not currently part of
                // layout manager children), or it's not completely visible.
                if (viewAtPosition == null || layoutManager
                        .isViewPartiallyVisible(viewAtPosition, false, true)) {
                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            layoutManager.scrollToPosition(MainActivity.currentPosition);
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

        // A similar mapping is set at the MainImagePagerFragment with a setEnterSharedElementCallback.
        setExitSharedElementCallback(
                new SharedElementCallback() {
                    @Override
                    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                        // Locate the ViewHolder for the clicked position.
                        RecyclerView.ViewHolder selectedViewHolder = mRecyclerView
                                .findViewHolderForAdapterPosition(MainActivity.currentPosition);
                        if (selectedViewHolder == null || selectedViewHolder.itemView == null) {
                            return;
                        }

                        // Map the first shared element name to the child ImageView.
                        sharedElements
                                .put(names.get(0), selectedViewHolder.itemView.findViewById(R.id.card_image));
                    }
                });
    }

    private void checkPosts(){
        mGeoQuery = mGeoFire.queryAtLocation(new GeoLocation(32.7871725, -117.14520703124998), 50);
        mPostIds.clear();
        mGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                mPostIds.add(key);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                loadPosts();
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void loadPosts(){

        mListPosts.clear();
        for (String postId : mPostIds) {

            firebase.child(kPOST).child(postId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                            HashMap<String, Object> post = (HashMap<String, Object>) dataSnapshot.getValue();
                            mListPosts.add(new Post(post));


                        mRecyclerView.setAdapter(new MainGridAdapter(mFragment, mListPosts));
                        showDataView();
                        if (mSwipeContainer.isRefreshing()) {
                            mSwipeContainer.setRefreshing(false);
                        }
                    } catch (Exception e) {
                        Log.d(TAG, "onDataChange: " + e.getLocalizedMessage());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            mSwipeContainer.setColorSchemeResources(android.R.color.holo_orange_dark);
            mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
                @Override
                public void onRefresh(){
                    checkPosts();
                }
            });
        }

    }

    private Boolean searchFeed(String newText) {
        MainGridAdapter gridAdapter = new MainGridAdapter(mFragment, mListPosts);
        if (newText != null && newText.length() > 0) {
            gridAdapter = new MainGridAdapter(mFragment, filter(newText));
            mRecyclerView.setAdapter(gridAdapter);
        } else if (newText.isEmpty()){
            checkPosts();
        }
        gridAdapter.notifyDataSetChanged();
        return true;
    }

    private ArrayList<Post> filter(String text) {
        ArrayList<Post> filterdPost = new ArrayList<Post>();
        for (Post post : mListPosts) {
            if (post.getDescription().toLowerCase().contains(text.toLowerCase())) {
                filterdPost.add(post);
            }
        }
        mListPosts.clear();
        mListPosts.addAll(filterdPost);
        return mListPosts;
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

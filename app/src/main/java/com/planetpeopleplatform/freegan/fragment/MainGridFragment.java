package com.planetpeopleplatform.freegan.fragment;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.activity.LoginActivity;
import com.planetpeopleplatform.freegan.activity.MainActivity;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.activity.PostActivity;
import com.planetpeopleplatform.freegan.adapter.MainGridAdapter;
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.ContextCompat.checkSelfPermission;
import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kLATITUDE;
import static com.planetpeopleplatform.freegan.utils.Constants.kLONGITUDE;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTLOCATION;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;

/**
 * A fragment for displaying a grid of images.
 */
public class MainGridFragment extends Fragment {

    private static final String TAG = MainGridFragment.class.getSimpleName();
    private Fragment mFragment = null;
    private ArrayList<Post> mListPosts = new ArrayList<Post>();
    ArrayList<String> mPostIds = new ArrayList<>();
    private String mCurrentUserUid = null;
    private User mCurrentUser = null;
    private FirebaseAuth mAuth;
    private MainGridAdapter mMainGridAdapter;
    GeoQuery mGeoQuery;
    private GeoFire mGeoFire;
    private FusedLocationProviderClient mFusedLocationClient;

    private static final int RC_POST_ITEM = 1;
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;

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

        if (savedInstanceState != null) {
            mCurrentUser = savedInstanceState.getParcelable(kCURRENTUSER);
            mListPosts = savedInstanceState.getParcelableArrayList(kPOST);
        } else {

            mAuth = FirebaseAuth.getInstance();
            mCurrentUserUid = mAuth.getCurrentUser().getUid();

            firebase.child(kUSER).child(mCurrentUserUid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        mCurrentUser = new User((java.util.HashMap<String, Object>) dataSnapshot.getValue());

                        if (mCurrentUser.getLatitude() == null) {
                            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

                            if (checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSIONS_REQUEST_FINE_LOCATION);
                            }else {
                                mFusedLocationClient.getLastLocation()
                                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                                            @Override
                                            public void onSuccess(Location location) {
                                                // Got last known location. In some rare situations this can be null.
                                                if (location != null) {
                                                    updateUserLocation(location);
                                                }
                                            }
                                        });
                            }
                        } else {
                            checkPosts();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            showLoading();

        }

        prepareTransitions();
        postponeEnterTransition();

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
                ChoosePictureSourceDialogFragment choosePictureSourceDialogFragment
                        = new ChoosePictureSourceDialogFragment();
                choosePictureSourceDialogFragment.show(getFragmentManager(),getString(R.string.choose_fragment_alert_tag));
            }
        });

        mSwipeContainer.setColorSchemeResources(android.R.color.holo_orange_dark);
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkPosts();
            }
        });

        mMainGridAdapter = new MainGridAdapter(mFragment, mListPosts);
        mRecyclerView.setAdapter(mMainGridAdapter);

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
        if (requestCode == RC_POST_ITEM && data != null && resultCode == RESULT_OK) {
            checkPosts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getContext(), "Permission needed to complete action", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        startActivity(new Intent(getActivity(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        return;
                    }
                    mFusedLocationClient.getLastLocation()
                            .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        updateUserLocation(location);
                                    }
                                }
                            });

                } else {
                    Toast.makeText(getContext(), "Permission needed to complete action", Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                    startActivity(new Intent(getActivity(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                }
            }

        }
    }


    @Override
    public void onResume() {
        super.onResume();
        android.support.design.widget.AppBarLayout mToolbarContainer = getActivity().findViewById(R.id.toolbar_container);
        mToolbarContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(kCURRENTUSER, mCurrentUser);
        outState.putParcelableArrayList(kPOST, mListPosts);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGeoQuery != null) {
            mGeoQuery.removeAllListeners();
        }
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
        mGeoFire = new GeoFire(firebase.child(kPOSTLOCATION));
        mGeoQuery = mGeoFire.queryAtLocation(new GeoLocation(mCurrentUser.getLatitude(),
                mCurrentUser.getLongitude()), 50);
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

                            mMainGridAdapter.notifyDataSetChanged();
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
        }
        mPostIds.clear();
        mMainGridAdapter.notifyDataSetChanged();

    }

    private Boolean searchFeed(String newText) {
        if (newText != null && newText.length() > 0) {
            mRecyclerView.setAdapter(new MainGridAdapter(mFragment, filter(newText)));
        } else if (newText.isEmpty()){
            checkPosts();
        }
        mMainGridAdapter.notifyDataSetChanged();
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

    /* Update Geofire */
    private void updateUserLocation(Location location) {


        HashMap<String, Object> newLocation = new HashMap<String, Object>();
        newLocation.put(kLATITUDE, location.getLatitude());
        newLocation.put(kLONGITUDE, location.getLongitude());


        firebase.child(kUSER).child(mCurrentUserUid).updateChildren(newLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Location updated");
                    Toast.makeText(getContext(), "Location successfully updated!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Error location not updated");
                    Toast.makeText(getContext(), "Location failed to update!", Toast.LENGTH_SHORT).show();
                }
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

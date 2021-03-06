package com.planetpeopleplatform.freegan.fragment;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.activity.MainActivity;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.adapter.MainGridAdapter;
import com.planetpeopleplatform.freegan.data.FreeganContract;
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;
import com.planetpeopleplatform.freegan.utils.EndlessRecyclerViewScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.ContextCompat.checkSelfPermission;
import static com.planetpeopleplatform.freegan.activity.MainActivity.KEY_CURRENT_POSITION;
import static com.planetpeopleplatform.freegan.utils.Constants.JSONARRAYKEY;
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
public class MainGridFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private ArrayList<Post> mListPosts = new ArrayList<>();
    private ArrayList<String> mPostIds = new ArrayList<>();
    private String mCurrentUserUid = null;
    private User mCurrentUser = null;
    private MainGridAdapter mMainGridAdapter;
    private GeoQuery mGeoQuery;
    private DatabaseReference postRef = firebase.child(kPOST);
    private FusedLocationProviderClient mFusedLocationClient;
    private static Boolean mSortByFavorite = false;
    private Cursor mCursor;
    private Boolean mAskLocationFlag = false;
    private final int GEOGRAPHIC_RADIUS = 50;

    // Store a member variable for the listener
    private EndlessRecyclerViewScrollListener mScrollListener;

    private static final int RC_POST_ITEM = 1;
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    private static final int CURSOR_LOADER_ID = 0;

    private final int PAGE_LOAD_SIZE = 10;
    private int mTotalLoadSize = 0;

    private SwipeRefreshLayout mSwipeContainer;
    private SearchView mSearchView;
    private RecyclerView mRecyclerView;
    private TextView mEmptyTextView;
    Fragment mFragment;
    StaggeredGridLayoutManager mStaggeredGridLayoutManager;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.new_item_button_view)
    android.support.design.widget.FloatingActionButton mPhotoPickerButton;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_grid, container, false);
        ButterKnife.bind(this, rootView);

        initializeViews();
        setupSharedPreferences();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        mCurrentUserUid = auth.getCurrentUser().getUid();

        mFragment = this;

        prepareTransitions();
        postponeEnterTransition();

        if (savedInstanceState != null) {
            mCurrentUser = savedInstanceState.getParcelable(kCURRENTUSER);
            mListPosts = savedInstanceState.getParcelableArrayList(kPOST);
            MainActivity.currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION, 0);
        }

        setupRecyclerView();
        checkNetworkAndFireUpFreegan();

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
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_FINE_LOCATION: {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (!(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED)) {
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
                    mAskLocationFlag = true;
                }
            }

        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(kCURRENTUSER, mCurrentUser);
        outState.putParcelableArrayList(kPOST, mListPosts);
        outState.putInt(KEY_CURRENT_POSITION, mRecyclerView.getChildLayoutPosition(mRecyclerView.getFocusedChild()));
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (mGeoQuery != null) {
            mGeoQuery.removeAllListeners();
        }
        // Unregister the listener
        PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext())
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        mRecyclerView.removeOnScrollListener(mScrollListener);
        super.onPause();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setExitTransition(TransitionInflater.from(getContext())
                    .inflateTransition(R.transition.grid_exit_transition));
        }

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

    private void setupRecyclerView() {
        mMainGridAdapter = new MainGridAdapter(mFragment, mListPosts);
        mRecyclerView.setLayoutManager(mStaggeredGridLayoutManager);
        mRecyclerView.hasFixedSize();
        mRecyclerView.setAdapter(mMainGridAdapter);
        mRecyclerView.addOnScrollListener(mScrollListener);
    }

    private void initializeViews() {
        android.support.design.widget.AppBarLayout mToolbarContainer = getActivity().findViewById(R.id.toolbar_container);
        mToolbarContainer.setVisibility(View.VISIBLE);

        mStaggeredGridLayoutManager = new StaggeredGridLayoutManager
                (getResources().getInteger(R.integer.grid_span_count), GridLayoutManager.VERTICAL);
        mSearchView = getActivity().findViewById(R.id.searchView);
        mSwipeContainer = getActivity().findViewById(R.id.main_content_swipe_refresh_layout);
        mRecyclerView = getActivity().findViewById(R.id.recycler_view);
        mEmptyTextView = getActivity().findViewById(R.id.empty_freegen_text);


        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return searchFeed(query);
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return searchFeed(newText);
            }
        });

        mSwipeContainer.setColorSchemeResources(android.R.color.holo_orange_dark);
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkNetworkAndFireUpFreegan();
            }
        });

        // Retain an instance so that you can call `resetState()` for fresh searches
        mScrollListener = new EndlessRecyclerViewScrollListener(mStaggeredGridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (totalItemsCount > mTotalLoadSize) {
                    if (PAGE_LOAD_SIZE < mPostIds.size()) {
                        loadPosts(PAGE_LOAD_SIZE, totalItemsCount);
                        mTotalLoadSize += PAGE_LOAD_SIZE;
                    } else {
                        loadPosts(mPostIds.size(), totalItemsCount);
                    }
                }
            }
        };

        // ImagePickerButton shows an image picker to upload a image
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentUser.getLatitude() == null || mCurrentUser.getLongitude() == null) {
                    checkPosts();
                } else {
                    ChoosePictureSourceDialogFragment choosePictureSourceDialogFragment
                            = new ChoosePictureSourceDialogFragment();
                    if (getFragmentManager() != null) {
                        choosePictureSourceDialogFragment.show(getFragmentManager(), getString(R.string.choose_fragment_alert_tag));
                    }
                }
            }
        });
    }

    private  void checkNetworkAndFireUpFreegan() {

        showLoading();
        if (isNetworkAvailable()) {
            if (mSortByFavorite) {
                loadFavorite();
            } else {

                firebase.child(kUSER).child(mCurrentUserUid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            mCurrentUser = new User((java.util.HashMap<String, Object>) dataSnapshot.getValue());

                            if (mCurrentUser.getLatitude() == null && !mAskLocationFlag) {
                                if (checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    requestPermissions(
                                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                            PERMISSIONS_REQUEST_FINE_LOCATION);
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
            }

        } else {
            Snackbar.make(mCoordinatorLayout, R.string.err_no_network_connection_string, Snackbar.LENGTH_LONG).show();
        }

    }

    private void checkPosts() {

        if (mSwipeContainer.isRefreshing()) {
            mSwipeContainer.setRefreshing(false);
        }

        if (mCurrentUser.getLatitude() == null || mCurrentUser.getLongitude() == null) {
            Snackbar.make(mCoordinatorLayout,
                    R.string.alert_permission_needed_to_post_and_view_freegan_string, Snackbar.LENGTH_LONG).show();
            showDataView();

            return;
        }

        GeoFire geoFire = new GeoFire(firebase.child(kPOSTLOCATION));

        mGeoQuery = geoFire.queryAtLocation(new GeoLocation(mCurrentUser.getLatitude(),
                mCurrentUser.getLongitude()), GEOGRAPHIC_RADIUS);

        mTotalLoadSize = 0;
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

                if (!(mPostIds.size() > 0)) {
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    mEmptyTextView.setVisibility(View.VISIBLE);
                } else {
                    mEmptyTextView.setVisibility(View.INVISIBLE);
                    if (PAGE_LOAD_SIZE < mPostIds.size()) {
                        loadPosts(PAGE_LOAD_SIZE, 0);
                    } else {
                        loadPosts(mPostIds.size(), 0);
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void loadPosts(int page_load_size, int offset) {
        showDataView();

        int maxBoundary = page_load_size + offset;
        if (maxBoundary > mPostIds.size()) {
            maxBoundary = mPostIds.size();
        }

        for (int i = offset; i < maxBoundary; i++) {
            mListPosts.clear();

            postRef.child(mPostIds.get(i)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        HashMap<String, Object> post = (HashMap<String, Object>) dataSnapshot.getValue();
                        if (post != null) {
                            mListPosts.add(new Post(post));
                        }
                        mMainGridAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Snackbar.make(mCoordinatorLayout, getString(R.string.error_fetching_data_string),
                                Snackbar.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            postRef.child(mPostIds.get(i))
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mLoadingIndicator.setVisibility(View.INVISIBLE);
                            if (!dataSnapshot.exists()) {
                                mEmptyTextView.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }

    }

    private Boolean searchFeed(String newText) {
        if (newText != null && newText.length() > 0) {
            filter(newText);
        } else {
            onResume();
        }
        return true;
    }

    private void filter(String text) {
        ArrayList<Post> filteredPost = new ArrayList<>();
        for (Post post : mListPosts) {
            if (post.getDescription().toLowerCase().contains(text.toLowerCase())) {
                filteredPost.add(post);
            }
        }
        mListPosts.clear();
        mListPosts.addAll(filteredPost);
        mMainGridAdapter.notifyDataSetChanged();
    }

    /* Update Geofire */
    private void updateUserLocation(Location location) {

        HashMap<String, Object> newLocation = new HashMap();
        newLocation.put(kLATITUDE, location.getLatitude());
        newLocation.put(kLONGITUDE, location.getLongitude());


        firebase.child(kUSER).child(mCurrentUserUid).updateChildren(newLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    onResume();
                } else {
                    Snackbar.make(mCoordinatorLayout, getString(R.string.error_fetching_your_location_string),
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupSharedPreferences() {
        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext().getApplicationContext());
        loadSortFromPreferences(sharedPreferences);

        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void loadSortFromPreferences(SharedPreferences sharedPreferences) {
        mSortByFavorite = sharedPreferences.getBoolean(getString(R.string.pref_sort_list_key),
                false);
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        mSortByFavorite = sharedPreferences.getBoolean(getString(R.string.pref_sort_list_key),
                false);
        onResume();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(getActivity(),
                FreeganContract.FreegansEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        boolean cursorHasValidData = false;
        if (cursor != null && cursor.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }
        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }
        mCursor = cursor;
        mCursor.moveToFirst();
        DatabaseUtils.dumpCursor(cursor);
        mListPosts.clear();
        for (int i = 0; i < mCursor.getCount(); i++) {

            String description = mCursor.getString(mCursor.getColumnIndex(FreeganContract.FreegansEntry.COLUMN_POST_DESCRIPTION));
            String userName = mCursor.getString(mCursor.getColumnIndex(FreeganContract.FreegansEntry.COLUMN_POSTER_NAME));
            String postUserObjectId = mCursor.getString(mCursor.getColumnIndex(FreeganContract.FreegansEntry.COLUMN_POSTER_ID));
            String profileImgUrl = mCursor.getString(mCursor.getColumnIndex(FreeganContract.FreegansEntry.COLUMN_POSTER_PICTURE_PATH));
            String postDate = mCursor.getString(mCursor.getColumnIndex(FreeganContract.FreegansEntry.COLUMN_POST_DATE));
            String postId = mCursor.getString(mCursor.getColumnIndex(FreeganContract.FreegansEntry.COLUMN_FREEGAN_ID));

            String imageUrlString = mCursor.getString(mCursor.getColumnIndex(FreeganContract.FreegansEntry.COLUMN_POST_PICTURE_PATH));

            ArrayList<String> imageUrls = new ArrayList<>();
            JSONObject json = null;
            try {
                json = new JSONObject(imageUrlString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray items = null;
            if (json != null) {
                items = json.optJSONArray(JSONARRAYKEY);
            }
            if (items != null) {
                for (int j = 0; j < items.length(); j++) {
                    String value = items.optString(j);
                    imageUrls.add(value);
                }
            }

            mListPosts.add(new Post(postId, postUserObjectId, description, imageUrls, profileImgUrl, userName, postDate));
            mMainGridAdapter.notifyDataSetChanged();

            mCursor.moveToNext();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCursor = null;
    }

    private boolean isNetworkAvailable() {
        // Using ConnectivityManager to check for Network Connection
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity()
                .getSystemService(getContext().getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager
                    .getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void loadFavorite() {

        boolean cursorHasValidData = false;
        Cursor cursor =
                getActivity().getContentResolver().query(FreeganContract.FreegansEntry.CONTENT_URI,
                        new String[]{FreeganContract.FreegansEntry._ID},
                        null,
                        null,
                        null);

        if (cursor != null && cursor.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }
        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            Snackbar.make(mCoordinatorLayout, getString(R.string.no_favorites_saved), Snackbar.LENGTH_SHORT).show();
        }

        mListPosts.clear();
        // initialize loader
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        showDataView();
        mMainGridAdapter.notifyDataSetChanged();
        if (mSwipeContainer.isRefreshing()) {
            mSwipeContainer.setRefreshing(false);
        }
        DatabaseUtils.dumpCursor(cursor);
    }
}
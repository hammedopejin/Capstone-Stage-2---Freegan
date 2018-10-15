package com.planetpeopleplatform.freegan.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.SharedElementCallback;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.activity.ProfileActivity;
import com.planetpeopleplatform.freegan.activity.ReportUserActivity;
import com.planetpeopleplatform.freegan.activity.SettingsActivity;
import com.planetpeopleplatform.freegan.adapter.ProfileGridAdapter;
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static com.bumptech.glide.request.RequestOptions.centerInsideTransform;
import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kBLOCKEDUSERSLIST;
import static com.planetpeopleplatform.freegan.utils.Constants.kBUNDLE;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTER;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTUSEROBJECTID;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Utils.closeOnError;

public class ProfileGridFragment extends Fragment {

    private static final String TAG = ProfileGridFragment.class.getSimpleName();
    private static final int REPORT_USER_REQUEST_CODE = 234;

    private Fragment mFragment = null;
    public String mCurrentUserUid = null;

    private ArrayList<Post> mListPosts = new ArrayList<Post>();
    private User mPoster = null;
    private User mCurrentUser = null;


    private static final String KEY_POSTER = "com.planetpeopleplatform.freegan.key.poster";
    private static final String KEY_CURRENT_USER = "com.planetpeopleplatform.freegan.key.current_user";

    @BindView(R.id.profile_image_view)
    de.hdodenhof.circleimageview.CircleImageView mProfileImageView;

    @BindView(R.id.settings)
    ImageButton mSettings;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    @BindView(R.id.back_arrow)
    ImageButton mBackArrow;

    @BindView(R.id.fragment_container)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.collapsing_toolbar)
    android.support.design.widget.CollapsingToolbarLayout mCollapsingToolbarLayout;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    public static ProfileGridFragment newInstance(User poster, User currentUser) {
        ProfileGridFragment fragment = new ProfileGridFragment();
        Bundle argument = new Bundle();
        argument.putParcelable(KEY_POSTER, poster);
        argument.putParcelable(KEY_CURRENT_USER, currentUser);
        fragment.setArguments(argument);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile_grid, container, false);
        ButterKnife.bind(this, rootView);

        if (savedInstanceState != null) {
            mCurrentUser = savedInstanceState.getParcelable(kCURRENTUSER);
            mListPosts = savedInstanceState.getParcelableArrayList(kPOST);
            mPoster = savedInstanceState.getParcelable(kPOSTER);
        } else {

            Bundle arguments = getArguments();
            if (arguments == null) {
                closeOnError(mCoordinatorLayout, getActivity());
            }
            if (arguments != null) {
                mPoster = arguments.getParcelable(KEY_POSTER);
            }
            if (arguments != null) {
                mCurrentUser = arguments.getParcelable(KEY_CURRENT_USER);
            }
        }

        mFragment = this;

        prepareTransitions();
        postponeEnterTransition();

        Glide.with(this).load(mPoster.getUserImgUrl()).apply(centerInsideTransform()
                .placeholder(R.drawable.person_icon)).into(mProfileImageView);

        mCollapsingToolbarLayout.setCollapsedTitleTextColor(Color.parseColor("#ffffff"));
        mCollapsingToolbarLayout.setTitle(mPoster.getUserName());
        mCollapsingToolbarLayout.setExpandedTitleGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        mCollapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.collapsing_tool_bar_layout_textview);

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        mCurrentUserUid = mCurrentUser.getObjectId();
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPoster.getObjectId().equals(mCurrentUserUid)){
                    Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(settingsIntent);
                } else {
                    showUserSettingsPopup(view);
                }
            }
        });
        showLoading();
        loadPosts();

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
        if (data != null && resultCode == RESULT_OK) {

            if (requestCode == REPORT_USER_REQUEST_CODE) {
                Snackbar.make(mCoordinatorLayout,
                        R.string.alert_message_sent_successfully, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(kCURRENTUSER, mCurrentUser);
        outState.putParcelableArrayList(kPOST, mListPosts);
        outState.putParcelable(kPOSTER, mPoster);
        super.onSaveInstanceState(outState);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setExitTransition(TransitionInflater.from(getContext())
                    .inflateTransition(R.transition.grid_exit_transition));
        }


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


    private void loadPosts(){
        showDataView();
        firebase.child(kPOST).orderByChild(kPOSTUSEROBJECTID).equalTo(mPoster.getObjectId())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    mListPosts.clear();

                    HashMap<String, Object> postData= (HashMap<String, Object>) dataSnapshot.getValue();

                    if (postData != null) {
                        for (String key : postData.keySet()){
                            HashMap<String, Object> post = (HashMap<String, Object>) postData.get(key);
                            mListPosts.add(new Post(post));
                        }
                    }

                    mRecyclerView.setAdapter(new ProfileGridAdapter(mFragment, mListPosts, mPoster, mCurrentUser));

                }catch (Exception e){
                    Log.d(TAG, "onDataChange: " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void showUserSettingsPopup(final View view) {

        firebase.child(kUSER).child(mPoster.getObjectId()).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    mPoster = new User((HashMap<String, Object>) dataSnapshot.getValue());
                    PopupMenu popup = new PopupMenu(getContext(), view);
                    if (mPoster.getBlockedUsersList().contains(mCurrentUser.getObjectId())) {
                        popup.inflate(R.menu.popup_user_settings_unblock_option);
                    } else {
                        popup.inflate(R.menu.popup_user_settings_block_option);
                    }

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.action_report_user:
                                    Intent reportUserIntent = new Intent(getContext(), ReportUserActivity.class);
                                    Bundle data = new Bundle();
                                    data.putParcelable(kCURRENTUSER, mCurrentUser);
                                    data.putParcelable(kPOSTER, mPoster);
                                    reportUserIntent.putExtra(kBUNDLE, data);
                                    startActivityForResult(reportUserIntent, REPORT_USER_REQUEST_CODE);
                                    return true;

                                case R.id.action_block_user:

                                    ArrayList<String> blockedList = mPoster.getBlockedUsersList();
                                    blockedList.add(mCurrentUserUid);
                                    mPoster.addBlockedUser(mCurrentUserUid);
                                    HashMap<String, Object> newBlockedUser = new HashMap<String, Object>();
                                    newBlockedUser.put(kBLOCKEDUSERSLIST, blockedList);
                                    firebase.child(kUSER).child(mPoster.getObjectId()).updateChildren(newBlockedUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                getActivity().recreate();
                                                Snackbar.make(mCoordinatorLayout,
                                                        R.string.alert_user_blocked_successfully_string, Snackbar.LENGTH_SHORT).show();
                                            } else {
                                                Snackbar.make(mCoordinatorLayout,
                                                        R.string.err_user_block_failed_string, Snackbar.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                    return true;

                                case R.id.action_unblock_user:

                                    ArrayList<String> blockedLists = mPoster.getBlockedUsersList();
                                    int blockedPosition = blockedLists.indexOf(mCurrentUserUid);
                                    mPoster.removeBlockedUser(mCurrentUserUid);
                                    firebase.child(kUSER).child(mPoster.getObjectId()).child(kBLOCKEDUSERSLIST).child(String.valueOf(blockedPosition)).removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        getActivity().recreate();
                                                        Snackbar.make(mCoordinatorLayout,
                                                                R.string.alert_user_unblocked_successfully_string, Snackbar.LENGTH_SHORT).show();
                                                    } else {
                                                        Snackbar.make(mCoordinatorLayout,
                                                                R.string.err_user_unblock_failed_string, Snackbar.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                    return true;

                                default:
                                    return true;
                            }
                        }
                    });
                    popup.show();
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
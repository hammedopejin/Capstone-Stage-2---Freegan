package com.planetpeopleplatform.freegan.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.SharedElementCallback;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.activity.MainActivity;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.activity.PostActivity;
import com.planetpeopleplatform.freegan.adapter.GridAdapter;
import com.planetpeopleplatform.freegan.model.Post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static com.planetpeopleplatform.freegan.utils.Constants.firebase;

/**
 * A fragment for displaying a grid of images.
 */
public class GridFragment extends Fragment {


    private Fragment mFragment = null;

    private ArrayList<Post> listPosts = new ArrayList<Post>();

    private static final int RC_POST_ITEM = 1;


    @BindView(R.id.item_photo_image_button)
    ImageButton mPhotoPickerButton;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_grid, container, false);

        ButterKnife.bind(this, rootView);

        mFragment = this;

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
        loadPost();
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
            loadPost();
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

        // A similar mapping is set at the ImagePagerFragment with a setEnterSharedElementCallback.
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

    private void loadPost(){

        firebase.child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {

                    listPosts.clear();
                    HashMap<String, Object> td= (HashMap<String, Object>) dataSnapshot.getValue();

                    for (String key : td.keySet()){
                        HashMap<String, Object> post = (HashMap<String, Object>) td.get(key);
                        listPosts.add(new Post(post));
                    }

                    mRecyclerView.setAdapter(new GridAdapter(mFragment, getContext(), listPosts));
                }catch (Exception ex){
                    String exception = ex.getLocalizedMessage();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


}

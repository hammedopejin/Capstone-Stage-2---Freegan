package com.planetpeopleplatform.freegan.fragment;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.planetpeopleplatform.freegan.activity.MainActivity;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.adapter.GridAdapter;
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.storageRef;

/**
 * A fragment for displaying a grid of images.
 */
public class GridFragment extends Fragment {

    private static final String KEY_USER_ID = "com.google.samples.gridtopager.key.userId";


    private String mCurrentUserUid = null;
    private User mCurrentUser = null;

    private Fragment mFragment = null;

    public boolean mImageSelected;
    private String mPostDownloadURL;

    private ArrayList<Post> listPosts = new ArrayList<Post>();

    private static final int RC_PHOTO_PICKER = 2;


    @BindView(R.id.item_photo_image_button)
    ImageButton mPhotoPickerButton;

    @BindView(R.id.item_description_edit_text)
    EditText mItemDescriptionEditText;

    @BindView(R.id.item_post_button)
    Button mItemPostButton;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_grid, container, false);

        ButterKnife.bind(this, rootView);

        mCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mFragment = this;

        loadPost();



        firebase.child(kUSER).child(mCurrentUserUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mCurrentUser = new User((java.util.HashMap<String, Object>) dataSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        // ImagePickerButton shows an image picker to upload a image
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

        mItemPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String description = mItemDescriptionEditText.getText().toString();
                if(description.equals("")){
                    Toast.makeText(getActivity(),"Item must have description", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!mImageSelected){
                    Toast.makeText(getActivity(),"An image must be selected", Toast.LENGTH_LONG).show();
                    return;
                }
                postToFirebase();
            }
        });

        prepareTransitions();
        postponeEnterTransition();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scrollToPosition();
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




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == RC_PHOTO_PICKER && data!=null && resultCode == RESULT_OK) {
             Uri selectedImageUri = data.getData();

             // Load the image with Glide to prevent OOM error when the image drawables are very large.
             Glide.with(this)
                     .load(selectedImageUri)
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
                     .into(mPhotoPickerButton);


             mPhotoPickerButton.setBackgroundResource(R.color.transparent);
             mImageSelected = true;


             SimpleDateFormat df = new SimpleDateFormat("ddMMyyHHmmss");
             Date dataobj = new Date();

             StorageReference ImageRef = storageRef.child("post_pics");
             // Get a reference to store file at post-pics/<FILENAME>
             final StorageReference photoRef = ImageRef.child(selectedImageUri.getLastPathSegment());

             // Upload file to Firebase Storage
             photoRef.putFile(selectedImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                 @Override
                 public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                     if (!task.isSuccessful()) {
                         throw task.getException();
                     }
                     return photoRef.getDownloadUrl();
                 }
             }).addOnCompleteListener(new OnCompleteListener<Uri>() {

                 @Override
                 public void onComplete(@NonNull Task<Uri> task) {
                     if (task.isSuccessful()) {
                         Uri downloadUri = task.getResult();
                         mPostDownloadURL = downloadUri.toString();
                     } else {
                         Toast.makeText(getContext(), "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                     }
                 }
             });
         }
    }


    private void postToFirebase() {


        SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");
        Date dataobj= new Date();

        HashMap<String, Object> post = new HashMap<String, Object>();
        post.put("description", (Object) mItemDescriptionEditText.getText().toString() );
        post.put("imageUrl", (Object) mPostDownloadURL);
        post.put("profileImgUrl", (Object) mCurrentUser.getUserImgUrl());
        post.put("userName", (Object) mCurrentUser.getUserName());
        post.put("postDate", (Object) sfd.format(dataobj));
        post.put("postUserObjectId", (Object) mCurrentUserUid);

        firebase.child("posts").push().setValue(post);

        mPhotoPickerButton.setImageResource(R.drawable.common_google_signin_btn_icon_dark);

        mItemDescriptionEditText.getText().clear();
        mItemDescriptionEditText.clearFocus();
        mImageSelected = false;


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

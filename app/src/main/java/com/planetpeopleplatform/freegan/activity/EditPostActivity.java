package com.planetpeopleplatform.freegan.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.fragment.ChoosePictureSourceDialogFragment;
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kBUNDLE;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kDESCRIPTION;
import static com.planetpeopleplatform.freegan.utils.Constants.kIMAGEURL;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTDATE;
import static com.planetpeopleplatform.freegan.utils.Constants.storage;
import static com.planetpeopleplatform.freegan.utils.Constants.storageRef;
import static com.planetpeopleplatform.freegan.utils.Utils.SplitString;
import static com.planetpeopleplatform.freegan.utils.Utils.getOutputMediaFile;

public class EditPostActivity extends AppCompatActivity
        implements ChoosePictureSourceDialogFragment.OnCompleteListener {

    private static final String TAG = EditPostActivity.class.getSimpleName();

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2;
    private static final int RC_TAKE_CAMERA_PHOTO_CODE = 100;
    private static final int RC_PHOTO_GALLERY_PICKER_CODE = 200;
    private static final int FLAG_DELETE = 1000;
    private static final int FLAG_NEW_PIC = 2000;

    private boolean mNewImageSelected = false;
    private User mCurrentUser;
    private String mCurrentUserUid;
    private FirebaseAuth mAuth;

    private String mPostDownloadURL;
    private ArrayList mPostDownloadURLs = new ArrayList<String>(4);
    private Uri mSelectedImageUri = null;
    private File destFile;
    private int mCurrentIndex;

    private Post mPost;

    private ImageView mTempImg;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.item_photo_frame1)
    ImageView mItemPhotoFrame1;

    @BindView(R.id.item_photo_frame2)
    ImageView mItemPhotoFrame2;

    @BindView(R.id.item_photo_frame3)
    ImageView mItemPhotoFrame3;

    @BindView(R.id.item_photo_frame4)
    ImageView mItemPhotoFrame4;

    @BindView(R.id.item_description_edit_text)
    EditText mItemDescriptionEditText;

    @BindView(R.id.item_post_button)
    Button mItemPostButton;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.edit_post_title);

        Bundle argument = getIntent().getBundleExtra(kBUNDLE);
        mCurrentUser = argument.getParcelable(kCURRENTUSER);
        mPost = argument.getParcelable(kPOST);
        mItemDescriptionEditText.setText(mPost.getDescription());

        Glide.with(this).load(mPost.getImageUrl().get(0)).into(mItemPhotoFrame1);
        mPostDownloadURLs.add(0, mPost.getImageUrl().get(0));
        if(mPost.getImageUrl().size() > 1) {
            Glide.with(this).load(mPost.getImageUrl().get(1)).into(mItemPhotoFrame2);
            mPostDownloadURLs.add(1, mPost.getImageUrl().get(1));
        }
        if(mPost.getImageUrl().size() > 2) {
            Glide.with(this).load(mPost.getImageUrl().get(2)).into(mItemPhotoFrame3);
            mPostDownloadURLs.add(2, mPost.getImageUrl().get(2));
        }
        if(mPost.getImageUrl().size() > 3) {
            Glide.with(this).load(mPost.getImageUrl().get(3)).into(mItemPhotoFrame4);
            mPostDownloadURLs.add(3, mPost.getImageUrl().get(3));
        }


        mItemDescriptionEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(60)});

        // ImagePickerButton shows an image picker to upload a image
        mItemPhotoFrame1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTempImg = mItemPhotoFrame1;
                mCurrentIndex = 0;
                if (mTempImg.getDrawable() == null){
                    mNewImageSelected = true;
                    editPostPicture(String.valueOf(1), FLAG_NEW_PIC);
                } else {
                    if (mPost.getImageUrl().size() > 1) {
                        mNewImageSelected = false;
                        editPostPicture(String.valueOf(0), FLAG_DELETE);
                    } else {
                        Snackbar.make(mCoordinatorLayout, R.string.alert_at_least_one_image_needed_string, Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });

        mItemPhotoFrame2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTempImg = mItemPhotoFrame2;
                mCurrentIndex = 1;
                if (mTempImg.getDrawable() == null){
                    mNewImageSelected = true;
                    editPostPicture(String.valueOf(1), FLAG_NEW_PIC);
                } else {
                    mNewImageSelected = false;
                    editPostPicture(String.valueOf(1), FLAG_DELETE );
                }
            }
        });

        mItemPhotoFrame3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTempImg = mItemPhotoFrame3;
                mCurrentIndex = 2;
                if (mTempImg.getDrawable() == null){
                    mNewImageSelected = true;
                    editPostPicture(String.valueOf(2),  FLAG_NEW_PIC);
                } else {
                    mNewImageSelected = false;
                    editPostPicture(String.valueOf(2), FLAG_DELETE );
                }
            }
        });

        mItemPhotoFrame4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTempImg = mItemPhotoFrame4;
                mCurrentIndex = 3;
                if (mTempImg.getDrawable() == null){
                    mNewImageSelected = true;
                    editPostPicture(String.valueOf(3), FLAG_NEW_PIC);
                } else {
                    mNewImageSelected = false;
                    editPostPicture(String.valueOf(3), FLAG_DELETE );
                }
            }
        });

        mItemPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String description = mItemDescriptionEditText.getText().toString();
                if(description.equals("")){
                    Snackbar.make(mCoordinatorLayout,
                            R.string.err_description_missing_string, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                postIt();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_GALLERY_PICKER_CODE && data!=null && resultCode == RESULT_OK) {
            mSelectedImageUri = data.getData();

            // Load the image with Glide to prevent OOM error when the image drawables are very large.
            Glide.with(this)
                    .load(mSelectedImageUri)
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
                    .into(mTempImg);


            mTempImg.setBackgroundResource(R.color.transparent);
            postToFirebase();



        }else if (requestCode == RC_TAKE_CAMERA_PHOTO_CODE  && resultCode == RESULT_OK){
            // Load the image with Glide to prevent OOM error when the image drawables are very large.
            Glide.with(this)
                    .load(mSelectedImageUri)
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
                    .into(mTempImg);

            mTempImg.setBackgroundResource(R.color.transparent);
            postToFirebase();

        } else if (requestCode == RC_PHOTO_GALLERY_PICKER_CODE || requestCode == RC_TAKE_CAMERA_PHOTO_CODE){
            if (resultCode == RESULT_CANCELED) {

            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions, grantResults);

        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE : {
                if (grantResults.length > 0 &&
                        ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    destFile = getOutputMediaFile(this);
                    mSelectedImageUri = FileProvider.getUriForFile(
                            this,
                            "com.planetpeopleplatform.freegan.provider",
                            destFile);

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mSelectedImageUri);
                    startActivityForResult(intent, RC_TAKE_CAMERA_PHOTO_CODE);

                } else {
                    Snackbar.make(mCoordinatorLayout,
                            R.string.alert_permission_needed_string, Snackbar.LENGTH_SHORT).show();
                }
            }
            break;
            case READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {

                    Intent intentGalley = new Intent(Intent.ACTION_GET_CONTENT);
                    intentGalley.setType("image/jpeg");
                    intentGalley.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    startActivityForResult(Intent.createChooser(intentGalley,
                            getString(R.string.alert_complete_action_using_string)), RC_PHOTO_GALLERY_PICKER_CODE);
                } else {
                    Snackbar.make(mCoordinatorLayout,
                            R.string.alert_permission_needed_string, Snackbar.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }

    private void takeCameraPicture(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, CAMERA_PERMISSION_REQUEST_CODE);
        }else {


            Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            destFile = getOutputMediaFile(this);
            mSelectedImageUri = FileProvider.getUriForFile(
                    this,
                    "com.planetpeopleplatform.freegan.provider",
                    destFile);
            intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, mSelectedImageUri);
            startActivityForResult(intentCamera, RC_TAKE_CAMERA_PHOTO_CODE);
        }
    }

    private void captureGalleryImage(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE }, READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        } else {

            Intent intentGalley = new Intent(Intent.ACTION_GET_CONTENT);
            intentGalley.setType("image/jpeg");
            intentGalley.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intentGalley,
                    getString(R.string.alert_complete_action_using_string)), RC_PHOTO_GALLERY_PICKER_CODE);
        }
    }

    private void postToFirebase() {
        mLoadingIndicator.setVisibility(View.VISIBLE);
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyHHmmss");

            final Date dataobj= new Date();

            String imagePath = SplitString(mCurrentUser.getEmail()) + "."+ df.format(dataobj)+ ".jpg";

            final StorageReference imageRef = storageRef.child("post_pics/"+imagePath );

            // Upload file to Firebase Storage
            imageRef.putFile(mSelectedImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {

                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        mPostDownloadURL = downloadUri.toString();
                        mPostDownloadURLs.add(mCurrentIndex, mPostDownloadURL);

                        if (!mNewImageSelected) {
                            firebase.child(kPOST).child(mPost.getPostId()).child(kIMAGEURL).child(String.valueOf(mCurrentIndex)).removeValue();
                            StorageReference toDelete = storage.getReferenceFromUrl(mPost.getImageUrl().get(mCurrentIndex));
                            toDelete.delete();
                        }

                    } else {
                        mLoadingIndicator.setVisibility(View.INVISIBLE);
                        Snackbar.make(mCoordinatorLayout,
                                R.string.err_post_upload_fail_string, Snackbar.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void postIt(){

        SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");
        DatabaseReference reference = firebase.child(kPOST).child(mPost.getPostId());
        HashMap<String, Object> post = new HashMap<String, Object>();

        post.put(kDESCRIPTION, mItemDescriptionEditText.getText().toString() );
        post.put(kIMAGEURL, mPostDownloadURLs);
        post.put(kPOSTDATE, sfd.format(new Date()));

        reference.updateChildren(post);

        mItemDescriptionEditText.getText().clear();
        mItemDescriptionEditText.clearFocus();

        mLoadingIndicator.setVisibility(View.INVISIBLE);

        Snackbar.make(mCoordinatorLayout,
                R.string.alert_post_update_successful, Snackbar.LENGTH_SHORT).show();

        finish();
    }

    private void editPostPicture(String position, int flag) {

        ChoosePictureSourceDialogFragment dialogFragment = ChoosePictureSourceDialogFragment.newInstance(position,
                mPost.getPostId(), kPOST, flag);
        dialogFragment.show(getSupportFragmentManager(), getString(R.string.choose_fragment_alert_tag));

    }

    @Override
    public void onComplete(int source) {
        if (source == 1){
            takeCameraPicture();
        } else if (source == 2){
            captureGalleryImage();
        } else if (source == 3){
            StorageReference toDelete = storage.getReferenceFromUrl(mPost.getImageUrl().get(mCurrentIndex));
            toDelete.delete();
            mTempImg.setBackground(getResources().getDrawable(R.color.cardview_dark_background));
            mTempImg.setImageDrawable(null);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

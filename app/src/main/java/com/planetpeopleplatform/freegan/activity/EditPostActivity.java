package com.planetpeopleplatform.freegan.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.fragment.ChoosePictureSourceDialogFragment;
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;
import com.planetpeopleplatform.freegan.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import id.zelory.compressor.Compressor;

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

    private User mCurrentUser;

    private ArrayList<StorageReference> mImageRef = new ArrayList<>();
    private ArrayList mPostDownloadURLs = new ArrayList<String>(4);
    private ArrayList mToDeletePostDownloadURLs = new ArrayList<String>();
    private Uri mSelectedImageUri = null;
    private ArrayList<Uri> mSelectedImageUris = new ArrayList<>();
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

        destFile = getOutputMediaFile(this);
        if (destFile != null) {
            mSelectedImageUri = FileProvider.getUriForFile(
                    this,
                    getString(R.string.file_provider_authority),
                    destFile);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.edit_post_title);

        Bundle argument = getIntent().getBundleExtra(kBUNDLE);
        mCurrentUser = argument.getParcelable(kCURRENTUSER);
        mPost = argument.getParcelable(kPOST);
        if (mPost != null) {
            mItemDescriptionEditText.setText(mPost.getDescription());
        }

        int photoSize = 0;
        if (mPost != null) {
            photoSize = mPost.getImageUrl().size();
        }

        for (int i = photoSize; i < 4; i++) {
            if (mPost != null) {
                mPost.getImageUrl().add(i, getString(R.string.place_holder_string));
            }
        }

        Glide.with(this).load(mPost.getImageUrl().get(0)).into(mItemPhotoFrame1);
        mPostDownloadURLs.add(0, mPost.getImageUrl().get(0));
        if(!(mPost.getImageUrl().get(1).equals(getString(R.string.place_holder_string)))) {
            Glide.with(this).load(mPost.getImageUrl().get(1)).into(mItemPhotoFrame2);
            mPostDownloadURLs.add(1, mPost.getImageUrl().get(1));
        }
        if(!(mPost.getImageUrl().get(2).equals(getString(R.string.place_holder_string)))) {
            Glide.with(this).load(mPost.getImageUrl().get(2)).into(mItemPhotoFrame3);
            mPostDownloadURLs.add(2, mPost.getImageUrl().get(2));
        }
        if(!(mPost.getImageUrl().get(3).equals(getString(R.string.place_holder_string)))) {
            Glide.with(this).load(mPost.getImageUrl().get(3)).into(mItemPhotoFrame4);
            mPostDownloadURLs.add(3, mPost.getImageUrl().get(3));
        }

        mItemDescriptionEditText.clearFocus();
        mItemDescriptionEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(80)});

        // ImagePickerButton shows an image picker to upload a image
        mItemPhotoFrame1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTempImg = mItemPhotoFrame1;
                mCurrentIndex = 0;
                if (mTempImg.getDrawable() == null){
                    editPostPicture(String.valueOf(1), FLAG_NEW_PIC);
                } else {
                    if (mPost.getImageUrl().size() > 1) {
                        mToDeletePostDownloadURLs.add(mPost.getImageUrl().get(mCurrentIndex));
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
                    editPostPicture(String.valueOf(1), FLAG_NEW_PIC);
                } else {
                    mToDeletePostDownloadURLs.add(mPost.getImageUrl().get(mCurrentIndex));
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
                    editPostPicture(String.valueOf(2),  FLAG_NEW_PIC);
                } else {
                    mToDeletePostDownloadURLs.add(mPost.getImageUrl().get(mCurrentIndex));
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
                    editPostPicture(String.valueOf(3), FLAG_NEW_PIC);
                } else {
                    mToDeletePostDownloadURLs.add(mPost.getImageUrl().get(mCurrentIndex));
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
                postToFirebase();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_GALLERY_PICKER_CODE && data!=null && resultCode == RESULT_OK) {
            mSelectedImageUri = data.getData();

            try {
                destFile = new File(Utils.getPathFromGooglePhotosUri(mSelectedImageUri, getApplicationContext()));
            } catch (NullPointerException ex){
                try {
                    destFile = new File(Utils.getPathFromUri(mSelectedImageUri, getApplicationContext()));

                } catch (NullPointerException e){
                    Snackbar.make(mCoordinatorLayout, R.string.err_image_source_unrecognized_string, Snackbar.LENGTH_SHORT).show();
                }
            }

            try {
                File compressedImageFile = new Compressor(this).compressToFile(destFile);
                mSelectedImageUri = Uri.fromFile(compressedImageFile);
            } catch (IOException e) {
                Snackbar.make(mCoordinatorLayout, R.string.err_image_compression_string, Snackbar.LENGTH_SHORT).show();
            }

            mSelectedImageUris.add(mSelectedImageUri);

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

        }else if (requestCode == RC_TAKE_CAMERA_PHOTO_CODE  && resultCode == RESULT_OK){

            try {
                File compressedImageFile = new Compressor(this).compressToFile(destFile);
                mSelectedImageUri = Uri.fromFile(compressedImageFile);
            } catch (IOException e) {
                Snackbar.make(mCoordinatorLayout, R.string.err_image_compression_string, Snackbar.LENGTH_SHORT).show();
            }

            mSelectedImageUris.add(mSelectedImageUri);
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
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions, grantResults);

        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE : {
                if (grantResults.length > 0 &&
                        ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

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

            intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, mSelectedImageUri);

            //Crash Point for 2nd camerra in roll
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
        mItemPostButton.setVisibility(View.INVISIBLE);
        SimpleDateFormat df = new SimpleDateFormat(getString(R.string.time_format_decending));

        if (mSelectedImageUris.size() > 0) {

            for (int i = 0; i < mSelectedImageUris.size(); i++) {

                String imagePath = SplitString(mCurrentUser.getEmail()) + String.valueOf(i) + "." + df.format(new Date()) + ".jpg";

                mImageRef.add(storageRef.child("post_pics/" + imagePath));

                    // Upload file to Firebase Storage
                final int finalI = i;
                mImageRef.get(finalI).putFile(mSelectedImageUris.get(finalI))
                            .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return mImageRef.get(finalI).getDownloadUrl();
                        }
                    })

                        .addOnCompleteListener(new OnCompleteListener<Uri>() {

                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                mPostDownloadURLs.add(task.getResult().toString());

                                if (finalI == (mSelectedImageUris.size() - 1)) {
                                    postIt();
                                }
                            } else {
                                mLoadingIndicator.setVisibility(View.INVISIBLE);
                                Snackbar.make(mCoordinatorLayout,
                                        R.string.err_post_upload_fail_string, Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });

            }

        } else {
            postIt();
        }

    }

    private void postIt(){

        mPostDownloadURLs.removeAll(mToDeletePostDownloadURLs);

        if (mPostDownloadURLs.size() < 1) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            mItemPostButton.setVisibility(View.VISIBLE);
            Snackbar.make(mCoordinatorLayout, R.string.alert_at_least_one_image_needed_string, Snackbar.LENGTH_SHORT).show();
            return;
        }

        mSelectedImageUris.clear();
        mImageRef.clear();

        for (int i = 0; i < mToDeletePostDownloadURLs.size(); i ++){
            StorageReference toDelete = storage.getReferenceFromUrl((String) mToDeletePostDownloadURLs.get(i));
            toDelete.delete();
        }

        SimpleDateFormat sfd = new SimpleDateFormat(getString(R.string.date_format_decending));
        DatabaseReference reference = firebase.child(kPOST).child(mPost.getPostId());
        DatabaseReference imagesReference = firebase.child(kPOST).child(mPost.getPostId()).child(kIMAGEURL);
        HashMap<String, Object> post = new HashMap<String, Object>();

        post.put(kDESCRIPTION, mItemDescriptionEditText.getText().toString() );
        post.put(kPOSTDATE, sfd.format(new Date()));

        reference.updateChildren(post);
        imagesReference.setValue(mPostDownloadURLs);

        mItemDescriptionEditText.getText().clear();
        mItemDescriptionEditText.clearFocus();
        mPostDownloadURLs.clear();
        mToDeletePostDownloadURLs.clear();

        post.clear();

        mLoadingIndicator.setVisibility(View.INVISIBLE);

        Snackbar.make(mCoordinatorLayout,
                R.string.alert_post_update_successful, Snackbar.LENGTH_SHORT).show();

        finish();
    }

    private void editPostPicture(String position, int flag) {

        ChoosePictureSourceDialogFragment dialogFragment = ChoosePictureSourceDialogFragment.newInstance(flag);
        dialogFragment.show(getSupportFragmentManager(), getString(R.string.choose_fragment_alert_tag));

    }

    @Override
    public void onComplete(int source) {
        if (source == 1){
            takeCameraPicture();
        } else if (source == 2){
            captureGalleryImage();
        } else if (source == 3){
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
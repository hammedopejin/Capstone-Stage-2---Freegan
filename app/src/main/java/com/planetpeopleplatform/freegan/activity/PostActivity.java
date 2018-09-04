package com.planetpeopleplatform.freegan.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
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
import android.text.InputFilter;
import android.util.Log;
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
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.model.User;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kDESCRIPTION;
import static com.planetpeopleplatform.freegan.utils.Constants.kIMAGEURL;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTDATE;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTLOCATION;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTUSEROBJECTID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPROFILEIMAGEURL;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERNAME;
import static com.planetpeopleplatform.freegan.utils.Constants.storageRef;
import static com.planetpeopleplatform.freegan.utils.Utils.SplitString;

public class PostActivity extends AppCompatActivity {

    private static final String TAG = PostActivity.class.getSimpleName();

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.item_photo_frame)
    ImageView mItemPhotoFrame;

    @BindView(R.id.item_description_edit_text)
    EditText mItemDescriptionEditText;

    @BindView(R.id.item_post_button)
    Button mItemPostButton;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2;
    private static final int RC_TAKE_CAMERA_PHOTO_CODE = 100;
    private static final int RC_PHOTO_GALLERY_PICKER_CODE = 200;

    private String mCurrentUserUid;
    private User mCurrentUser = null;
    private boolean mImageSelected;
    private ArrayList<String> mPostDownloadURL = new ArrayList<>();
    private Uri mSelectedImageUri = null;
    private GeoFire mGeoFire;
    private File destFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ButterKnife.bind(this);
        int source = getIntent().getIntExtra(getString(R.string.source_string), 1);
        if (source == 1){
            takeCameraPicture();
        } else {
            captureGalleryImage();
        }

        mCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mGeoFire = new GeoFire(firebase.child(kPOSTLOCATION));

        if (savedInstanceState != null) {
            mCurrentUser = savedInstanceState.getParcelable(kCURRENTUSER);
            mSelectedImageUri = Uri.parse(savedInstanceState.getString(kIMAGEURL));
        } else {
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
        }

        mItemDescriptionEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(60)});
        mItemPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String description = mItemDescriptionEditText.getText().toString();
                if(description.equals("")){
                    Snackbar.make(mCoordinatorLayout,
                            R.string.err_description_missing_string, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(!mImageSelected){
                    Snackbar.make(mCoordinatorLayout,
                            R.string.err_image_missing_string, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                postToFirebase();
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(kCURRENTUSER, mCurrentUser);
        outState.putString(kIMAGEURL, String.valueOf(mSelectedImageUri));
        super.onSaveInstanceState(outState);
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
                    .into(mItemPhotoFrame);


            mItemPhotoFrame.setBackgroundResource(R.color.transparent);
            mImageSelected = true;



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
                    .into(mItemPhotoFrame);

            mItemPhotoFrame.setBackgroundResource(R.color.transparent);
            mImageSelected = true;

        } else if (requestCode == RC_PHOTO_GALLERY_PICKER_CODE || requestCode == RC_TAKE_CAMERA_PHOTO_CODE){
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }




    private void postToFirebase() {

        if (mCurrentUser.getLatitude() == null){
            Snackbar.make(mCoordinatorLayout,
                    R.string.alert_location_missing_string, Snackbar.LENGTH_SHORT).show();
            return;
        }

        final SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyHHmmss");
        final Date dataobj= new Date();

        String imagePath = SplitString(mCurrentUser.getEmail()) + "."+ df.format(dataobj)+ ".jpg";

        final StorageReference imageRef = storageRef.child("post_pics/"+imagePath );

        showLoading();

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
                    mPostDownloadURL.add(downloadUri.toString());

                    DatabaseReference reference = firebase.child(kPOST).push();
                    String postId = reference.getKey();

                    HashMap<String, Object> post = new HashMap<String, Object>();
                    post.put((kPOSTID), postId);
                    post.put(kDESCRIPTION, mItemDescriptionEditText.getText().toString() );
                    post.put(kIMAGEURL, mPostDownloadURL);
                    post.put(kPROFILEIMAGEURL, mCurrentUser.getUserImgUrl());
                    post.put(kUSERNAME, mCurrentUser.getUserName());
                    post.put(kPOSTDATE, sfd.format(dataobj));
                    post.put(kPOSTUSEROBJECTID, mCurrentUserUid);

                    mItemDescriptionEditText.getText().clear();
                    mItemDescriptionEditText.clearFocus();
                    mImageSelected = false;

                    reference.setValue(post);

                    mLoadingIndicator.setVisibility(View.INVISIBLE);

                    mGeoFire.setLocation(postId, new GeoLocation(mCurrentUser.getLatitude(),
                            mCurrentUser.getLongitude()), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error != null) {
                                Snackbar.make(mCoordinatorLayout,
                                        R.string.err_location_saving_string, Snackbar.LENGTH_SHORT).show();
                                Log.d(TAG, "There was an error saving the location to GeoFire: " + error);
                            } else {
                                Log.d(TAG, "Location saved on server successfully!");
                            }
                        }
                    });
                    Snackbar.make(mCoordinatorLayout,
                            R.string.alert_post_upload_successful, Snackbar.LENGTH_SHORT).show();
                    finish();
                } else {
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    Snackbar.make(mCoordinatorLayout,
                            R.string.err_post_upload_fail_string, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showLoading() {
        /* Then, hide the data */
        mItemDescriptionEditText.setVisibility(View.INVISIBLE);
        mItemPostButton.setVisibility(View.INVISIBLE);
        /* Finally, show the loading indicator */
        mLoadingIndicator.setVisibility(View.VISIBLE);
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

    private void takeCameraPicture(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, CAMERA_PERMISSION_REQUEST_CODE);
        }else {


            Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            destFile = getOutputMediaFile();
            mSelectedImageUri = FileProvider.getUriForFile(
                    this,
                    "com.planetpeopleplatform.freegan.provider",
                    destFile);
            intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, mSelectedImageUri);
            startActivityForResult(intentCamera, RC_TAKE_CAMERA_PHOTO_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    destFile = getOutputMediaFile();

                    mSelectedImageUri = FileProvider.getUriForFile(
                            this,
                            "com.planetpeopleplatform.freegan.provider",
                            destFile);

                    intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, mSelectedImageUri);
                    startActivityForResult(intentCamera, RC_TAKE_CAMERA_PHOTO_CODE);

                } else {
                    Snackbar.make(mCoordinatorLayout,
                            R.string.alert_permission_needed_string, Snackbar.LENGTH_SHORT).show();
                    finish();
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
                    finish();
                }
            }
            break;
        }
    }

    private File getOutputMediaFile(){
        File mediaStorageDir = new File(getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), getString(R.string.app_name));

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
    }

}
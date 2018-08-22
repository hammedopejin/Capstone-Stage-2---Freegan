package com.planetpeopleplatform.freegan.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceActivity;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.fragment.ChoosePictureSourceDialogFragment;
import com.planetpeopleplatform.freegan.model.User;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kIMAGEURL;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERIMAGEURL;
import static com.planetpeopleplatform.freegan.utils.Constants.storage;
import static com.planetpeopleplatform.freegan.utils.Constants.storageRef;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity
        implements ChoosePictureSourceDialogFragment.OnCompleteListener {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2;
    private static final int RC_TAKE_CAMERA_PHOTO_CODE = 100;
    private static final int RC_PHOTO_GALLERY_PICKER_CODE = 200;

    private String mPostDownloadURL;
    private Uri mSelectedImageUri = null;
    private User mCurrentUser;
    private String mCurrentUserUid;
    private FirebaseAuth mAuth;
    private File destFile;

    @BindView(R.id.fragment_container)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserUid = mAuth.getCurrentUser().getUid();

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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_GALLERY_PICKER_CODE && data!=null && resultCode == RESULT_OK) {
            mSelectedImageUri = data.getData();
            postPictureToFirebase();

        } else if (requestCode == RC_TAKE_CAMERA_PHOTO_CODE  && resultCode == RESULT_OK){
            postPictureToFirebase();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(kCURRENTUSER, mCurrentUser);
        outState.putString(kIMAGEURL, String.valueOf(mSelectedImageUri));
        super.onSaveInstanceState(outState);
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

                    destFile = getOutputMediaFile();
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

    @Override
    public void onComplete(int source) {
        if (source == 1){
            takeCameraPicture();
        } else {
            captureGalleryImage();
        }
    }

    private void postPictureToFirebase() {

        SimpleDateFormat df = new SimpleDateFormat("ddMMyyHHmmss");
        final Date dataobj= new Date();

        String imagePath = SplitString(mCurrentUser.getEmail()) + "."+ df.format(dataobj)+ ".jpg";

        final StorageReference imageRef = storageRef.child("user_images/"+imagePath );
        mLoadingIndicator.setVisibility(View.VISIBLE);

        StorageReference toReplace = storage.getReferenceFromUrl(mCurrentUser.getUserImgUrl());
        toReplace.delete();

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
                    firebase.child("users").child(mCurrentUser.getObjectId()).child(kUSERIMAGEURL).setValue(mPostDownloadURL);
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    Snackbar.make(mCoordinatorLayout,
                            R.string.alert_successfully_uploaded_photo_string, Snackbar.LENGTH_SHORT).show();

                } else {
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    Snackbar.make(mCoordinatorLayout,
                            R.string.err_upload_failed_string, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
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
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
    private String SplitString(String email) {
        String[] split= email.split("@");
        return split[0];
    }
}
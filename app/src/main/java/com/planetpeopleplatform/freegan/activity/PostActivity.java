package com.planetpeopleplatform.freegan.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.model.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.storageRef;

public class PostActivity extends AppCompatActivity {

    @BindView(R.id.item_photo_frame)
    ImageView mItemPhotoFrame;

    @BindView(R.id.item_description_edit_text)
    EditText mItemDescriptionEditText;

    @BindView(R.id.item_post_button)
    Button mItemPostButton;

    private static final int RC_PHOTO_PICKER = 2;

    private String mCurrentUserUid;
    private User mCurrentUser = null;
    private boolean mImageSelected;
    private String mPostDownloadURL;
    private Uri mSelectedImageUri = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ButterKnife.bind(this);

        captureImage();

        mCurrentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

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


        mItemPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String description = mItemDescriptionEditText.getText().toString();
                if(description.equals("")){
                    Toast.makeText(getApplicationContext(),"Item must have description", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!mImageSelected){
                    Toast.makeText(getApplicationContext(),"An image must be selected", Toast.LENGTH_LONG).show();
                    return;
                }
                postToFirebase();
                finish();
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER && data!=null && resultCode == RESULT_OK) {
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



        } else if (requestCode == RC_PHOTO_PICKER){
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    private String SplitString(String email) {
        String[] split= email.split("@");
        return split[0];
    }

    private void captureImage(){

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
    }


    private void postToFirebase() {

        final SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");
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

                    HashMap<String, Object> post = new HashMap<String, Object>();
                    post.put("description", (Object) mItemDescriptionEditText.getText().toString() );
                    post.put("imageUrl", (Object) mPostDownloadURL);
                    post.put("profileImgUrl", (Object) mCurrentUser.getUserImgUrl());
                    post.put("userName", (Object) mCurrentUser.getUserName());
                    post.put("postDate", (Object) sfd.format(dataobj));
                    post.put("postUserObjectId", (Object) mCurrentUserUid);

                    firebase.child("posts").push().setValue(post);

                    mItemDescriptionEditText.getText().clear();
                    mItemDescriptionEditText.clearFocus();
                    mImageSelected = false;
                } else {
                    Toast.makeText(getApplicationContext(), "upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
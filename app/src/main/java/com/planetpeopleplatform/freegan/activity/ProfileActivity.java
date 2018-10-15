package com.planetpeopleplatform.freegan.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.fragment.DeleteDialogFragment;
import com.planetpeopleplatform.freegan.fragment.ProfileGridFragment;
import com.planetpeopleplatform.freegan.model.User;

import java.util.HashMap;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTER;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;

public class ProfileActivity extends AppCompatActivity implements DeleteDialogFragment.OnCompleteListener{

    public static int currentPosition;
    private static final String KEY_CURRENT_POSITION = "com.planetpeopleplatform.freegan.key.currentPosition";
    private static final String KEY_CURRENT_USER_ID = "com.planetpeopleplatform.freegan.key.currentUserUid";
    private static final String KEY_POSTER_ID = "com.planetpeopleplatform.freegan.key.posterUid";

    private User mPoster = null;
    private User mCurrentUser = null;
    private String mCurrentUserUid = null;
    private String mPosterId;
    private ValueEventListener mPosterValueEventListener;
    private ValueEventListener mCurrentUserValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION, 0);
            mCurrentUserUid = savedInstanceState.getString(KEY_CURRENT_USER_ID);
            mPosterId = savedInstanceState.getString(KEY_POSTER_ID);
            mCurrentUser = savedInstanceState.getParcelable(kCURRENTUSER);
            mPoster = savedInstanceState.getParcelable(kPOSTER);
            return;
        }

        mCurrentUserUid = auth.getCurrentUser().getUid();
        if (getIntent() != null) {
            mPosterId = getIntent().getStringExtra(kPOSTERID);
        }
        getCurrentUser(mCurrentUserUid);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_CURRENT_POSITION, currentPosition);
        outState.putString(KEY_CURRENT_USER_ID, mCurrentUserUid);
        outState.putString(KEY_POSTER_ID, mPosterId);
        outState.putParcelable(kCURRENTUSER, mCurrentUser);
        outState.putParcelable(kPOSTER, mPoster);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int id = item.getItemId();
        switch (id) {

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onComplete(int position) {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getCurrentUser(String currentUserUid) {
        if(mCurrentUserValueEventListener == null) {
            mCurrentUserValueEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        mCurrentUser = new User((HashMap<String, Object>) dataSnapshot.getValue());
                        getPoster(mPosterId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            firebase.child(kUSER).child(currentUserUid).addListenerForSingleValueEvent(mCurrentUserValueEventListener);
        }
    }

    private void getPoster(String posterUserUid) {
        if (mPosterValueEventListener == null) {
            mPosterValueEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        mPoster = new User((HashMap<String, Object>) dataSnapshot.getValue());
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager
                                .beginTransaction()
                                .replace(R.id.fragment_container, ProfileGridFragment.newInstance(mPoster, mCurrentUser), ProfileGridFragment.class.getSimpleName())
                                .commit();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            firebase.child(kUSER).child(posterUserUid).addListenerForSingleValueEvent(mPosterValueEventListener);
        }
    }

    @Override
    protected void onPause() {
        if (mPosterValueEventListener != null) {
            firebase.child(kUSER).child(mPosterId).removeEventListener(mPosterValueEventListener);
            mPosterValueEventListener = null;
        }
        if (mCurrentUserValueEventListener != null){
            firebase.child(kUSER).child(mCurrentUserUid).removeEventListener(mCurrentUserValueEventListener);
            mCurrentUserValueEventListener = null;
        }
        super.onPause();
    }
}

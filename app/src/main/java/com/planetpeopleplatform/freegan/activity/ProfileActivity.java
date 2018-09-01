package com.planetpeopleplatform.freegan.activity;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.storage.StorageReference;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.fragment.DeleteDialogFragment;
import com.planetpeopleplatform.freegan.fragment.ProfileGridFragment;
import com.planetpeopleplatform.freegan.model.Post;

import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.storage;

public class ProfileActivity extends AppCompatActivity implements DeleteDialogFragment.OnCompleteListener{

    public static int currentPosition;
    private static final String KEY_CURRENT_POSITION = "com.planetpeopleplatform.freegan.key.currentPosition";

    private Post mPost = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mPost = getIntent().getParcelableExtra(kPOST);

        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION, 0);
            // Return here to prevent adding additional GridFragments when changing orientation.
            return;
        }


        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, ProfileGridFragment.newInstance(mPost), ProfileGridFragment.class.getSimpleName())
                .commit();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_CURRENT_POSITION, currentPosition);
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
        StorageReference toDelete = storage.getReferenceFromUrl(mPost.getImageUrl());
        toDelete.delete();
        getSupportFragmentManager().popBackStack();
    }
}

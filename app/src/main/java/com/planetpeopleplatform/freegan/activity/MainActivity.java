package com.planetpeopleplatform.freegan.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.fragment.ChoosePictureSourceDialogFragment;
import com.planetpeopleplatform.freegan.fragment.MainGridFragment;

import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSERID;


public class MainActivity extends AppCompatActivity implements ChoosePictureSourceDialogFragment.OnCompleteListener {

    private static final int RC_POST_ITEM = 1;
    public static int currentPosition;
    public static final String KEY_CURRENT_POSITION = "com.planetpeopleplatform.freegan.key.currentPosition";
    private String mCurrentUserUid = null;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            mCurrentUserUid = savedInstanceState.getString(kCURRENTUSERID);
            currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION, 0);
            // Return here to prevent adding additional GridFragments when changing orientation.
            return;
        }

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserUid = mAuth.getCurrentUser().getUid();


        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, new MainGridFragment(), MainGridFragment.class.getSimpleName())
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(kCURRENTUSERID, mCurrentUserUid);
        outState.putInt(KEY_CURRENT_POSITION, currentPosition);
        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        int id = item.getItemId();
        switch (id) {

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.action_recent_chats:
                startActivity(new Intent(this, RecentChatActivity.class)
                        .putExtra(kCURRENTUSERID, mCurrentUserUid));
                return true;

            case R.id.action_sort_by:
                return true;


            case R.id.action_sort_by_all_post:
                sharedPreferences.edit().putBoolean(getString(R.string.pref_sort_list_key), false).apply();
                return true;

            case R.id.action_sort_by_favorite_post:
                sharedPreferences.edit().putBoolean(getString(R.string.pref_sort_list_key), true).apply();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onComplete(int source) {
        Intent intent = new Intent(this, PostActivity.class);
        intent.putExtra(getString(R.string.source_string), source);
        startActivityForResult(intent, RC_POST_ITEM);
    }
}


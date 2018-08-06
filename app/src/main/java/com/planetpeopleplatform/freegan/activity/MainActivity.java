package com.planetpeopleplatform.freegan.activity;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.fragment.GridFragment;


public class MainActivity extends AppCompatActivity {

    public static int currentPosition;
    private static final String KEY_CURRENT_POSITION = "com.planetpeopleplatform.freegan.key.currentPosition";
    public String mCurrentUserUid = null;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth= FirebaseAuth.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION, 0);
            // Return here to prevent adding additional GridFragments when changing orientation.
            return;
        }


        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, new GridFragment(), GridFragment.class.getSimpleName())
                .commit();
        mCurrentUserUid = mAuth.getCurrentUser().getUid();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_POSITION, currentPosition);
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

            case R.id.action_recent_chats:
                startActivity(new Intent(this, RecentChatActivity.class)
                        .putExtra("currentUserUID", mCurrentUserUid));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}


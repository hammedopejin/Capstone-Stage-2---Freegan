package com.planetpeopleplatform.freegan.activity;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.fragment.MainGridFragment;

import java.util.HashMap;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kLATITUDE;
import static com.planetpeopleplatform.freegan.utils.Constants.kLONGITUDE;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    public static int currentPosition;
    private static final String KEY_CURRENT_POSITION = "com.planetpeopleplatform.freegan.key.currentPosition";
    public String mCurrentUserUid = null;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserUid = mAuth.getCurrentUser().getUid();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION, 0);
            // Return here to prevent adding additional GridFragments when changing orientation.
            return;
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);



        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, new MainGridFragment(), MainGridFragment.class.getSimpleName())
                .commit();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CURRENT_POSITION, currentPosition);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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



    /* Update Geofire */
    private void updateUserLocation(Location location) {


        HashMap<String, Object> newLocation = new HashMap<String, Object>();
        newLocation.put(kLATITUDE, location.getLatitude());
        newLocation.put(kLONGITUDE, location.getLongitude());


        firebase.child(kUSER).child(mCurrentUserUid).updateChildren(newLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Location updated");
                    Toast.makeText(getApplicationContext(), "Location successfully updated!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Error location not updated");
                    Toast.makeText(getApplicationContext(), "Location failed to update!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}


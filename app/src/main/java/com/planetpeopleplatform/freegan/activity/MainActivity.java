package com.planetpeopleplatform.freegan.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.fragment.MainGridFragment;

import java.util.HashMap;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kLATITUDE;
import static com.planetpeopleplatform.freegan.utils.Constants.kLONGITUDE;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    public static int currentPosition;
    public static final String KEY_CURRENT_POSITION = "com.planetpeopleplatform.freegan.key.currentPosition";
    private String mCurrentUserUid = null;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient mFusedLocationClient;


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
        } else {
            mAuth = FirebaseAuth.getInstance();
            mCurrentUserUid = mAuth.getCurrentUser().getUid();
        }

//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    PERMISSIONS_REQUEST_FINE_LOCATION);
//        } else {
//            mFusedLocationClient.getLastLocation()
//                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                        @Override
//                        public void onSuccess(Location location) {
//                            // Got last known location. In some rare situations this can be null.
//                            if (location != null) {
//                                updateUserLocation(location);
//                            }
//                        }
//                    });
//        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, new MainGridFragment(), MainGridFragment.class.getSimpleName())
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
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int id = item.getItemId();
        switch (id) {

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.action_recent_chats:
                startActivity(new Intent(this, RecentChatActivity.class)
                        .putExtra(kCURRENTUSERID, mCurrentUserUid));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case PERMISSIONS_REQUEST_FINE_LOCATION: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                            != PackageManager.PERMISSION_GRANTED
//                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//                            != PackageManager.PERMISSION_GRANTED) {
//                        return;
//                    }
//                    mFusedLocationClient.getLastLocation()
//                            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                                @Override
//                                public void onSuccess(Location location) {
//                                    // Got last known location. In some rare situations this can be null.
//                                    if (location != null) {
//                                        updateUserLocation(location);
//                                    }
//                                }
//                            });
//                } else {
//                    Toast.makeText(this, "Permission needed to complete action", Toast.LENGTH_SHORT).show();
//
//                }
//            }
//
//        }
//    }

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


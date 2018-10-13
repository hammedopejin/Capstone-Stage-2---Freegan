package com.planetpeopleplatform.freegan.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.fragment.ChoosePictureSourceDialogFragment;
import com.planetpeopleplatform.freegan.fragment.MainGridFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSERID;


public class MainActivity extends AppCompatActivity implements ChoosePictureSourceDialogFragment.OnCompleteListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_POST_ITEM = 1;
    private static final int REQUEST_INVITE = 2;
    public static int currentPosition;
    public static final String KEY_CURRENT_POSITION = "com.planetpeopleplatform.freegan.key.currentPosition";
    private String mCurrentUserUid = null;
    Fragment mFragment;

    @BindView(R.id.fragment_container)
    CoordinatorLayout mCoordinatorLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        if (savedInstanceState != null) {
            mCurrentUserUid = savedInstanceState.getString(kCURRENTUSERID);
            currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION, 0);
            // Return here to prevent adding additional GridFragments when changing orientation.
            if (mFragment == null){
                mFragment = new MainGridFragment();
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.fragment_container, mFragment, MainGridFragment.class.getSimpleName())
                        .commit();
            }
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        mCurrentUserUid = auth.getCurrentUser().getUid();

        mFragment = new MainGridFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, mFragment, MainGridFragment.class.getSimpleName())
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

            case R.id.invite_menu:
                sendInvitation();

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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Snackbar.make(mCoordinatorLayout, R.string.err_google_play_service_error_string, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                Snackbar.make(mCoordinatorLayout, R.string.alert_invitation_sent, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(mCoordinatorLayout, R.string.err_invitation_failed, Snackbar.LENGTH_SHORT).show();
            }
        }

    }

    private void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }
}
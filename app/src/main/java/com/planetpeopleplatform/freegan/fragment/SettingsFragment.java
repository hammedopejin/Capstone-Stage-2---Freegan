package com.planetpeopleplatform.freegan.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.BuildConfig;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.activity.LoginActivity;
import com.planetpeopleplatform.freegan.activity.PrivacyPolicyActivity;
import com.planetpeopleplatform.freegan.activity.TermsAndConditionsActivity;
import com.planetpeopleplatform.freegan.activity.UpdateEmailActivity;
import com.planetpeopleplatform.freegan.activity.UpdatePasswordActivity;
import com.planetpeopleplatform.freegan.activity.UpdateUserNameActivity;
import com.planetpeopleplatform.freegan.model.User;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.ContextCompat.checkSelfPermission;
import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kEMAIL;
import static com.planetpeopleplatform.freegan.utils.Constants.kLATITUDE;
import static com.planetpeopleplatform.freegan.utils.Constants.kLONGITUDE;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERNAME;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private FirebaseAuth mAuth;
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 3;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 300;
    private User mCurrentUser;
    private String mCurrentUserUid;
    private CoordinatorLayout mCoordinatorLayout;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

        addPreferencesFromResource(R.xml.pref_general);

        PreferenceScreen prefScreen = getPreferenceScreen();
        SharedPreferences sharedPreferences = prefScreen.getSharedPreferences();
        int count = prefScreen.getPreferenceCount();

        // Go through all of the preferences, and set up their preference summary.
        for (int i = 0; i < count; i++) {
            Preference preference = prefScreen.getPreference(i);

            if (preference instanceof ListPreference) {
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            }
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Figure out which preference was changed
        Preference preference = findPreference(key);
        if (null != preference) {
            // Updates the summary for the preference
            if (preference instanceof ListPreference) {
                String value = sharedPreferences.getString(preference.getKey(), "");
                setPreferenceSummary(preference, value);
            }
        }
    }

    /**
     * Updates the summary for the preference
     *
     * @param preference The preference to be updated
     * @param value      The value that the preference was updated to
     */
    private void setPreferenceSummary(Preference preference, String value) {
        if (preference instanceof ListPreference) {
            // For list preferences, figure out the label of the selected value
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0) {
                // Set the summary to that label
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserUid = mAuth.getCurrentUser().getUid();
        mCoordinatorLayout = getActivity().findViewById(R.id.fragment_container);

        firebase.child(kUSER).child(mCurrentUserUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mCurrentUser = new User((java.util.HashMap<String, Object>) dataSnapshot.getValue());
                    if(mCurrentUser.getLongitude() != null && mCurrentUser.getLatitude() != null && getActivity() != null){
                        Geocoder geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
                        try {
                            findPreference(getString(R.string.user_location_key))
                                    .setSummary(geocoder.getFromLocation(mCurrentUser.getLatitude(),
                                            mCurrentUser.getLongitude(), 1).get(0)
                                            .getAddressLine(0).substring(geocoder.getFromLocation(mCurrentUser.getLatitude(),
                                            mCurrentUser.getLongitude(), 1).get(0).getAddressLine(0).indexOf(",") + 2));

                        } catch (IOException e) {

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (!Places.isInitialized()) {
            Places.initialize(getContext().getApplicationContext(), BuildConfig.ApiKey);
        }

        Preference button = findPreference(getString(R.string.logout_button_key));

        button.setOnPreferenceClickListener(new android.support.v7.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mAuth.signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                getActivity().finish();
                return true;
            }
        });

        findPreference(getString(R.string.user_profile_image_key))
                .setOnPreferenceClickListener(new android.support.v7.preference.Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        ChoosePictureSourceDialogFragment choosePictureSourceDialogFragment
                                = new ChoosePictureSourceDialogFragment();
                        if (getFragmentManager() != null) {
                            choosePictureSourceDialogFragment.show(getFragmentManager(), getString(R.string.choose_fragment_alert_tag));
                        }
                        return true;
                    }
                });

        findPreference(getString(R.string.user_name_key))
                .setOnPreferenceClickListener(new android.support.v7.preference.Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent updateUserNameIntent = new Intent(getContext(), UpdateUserNameActivity.class);
                        updateUserNameIntent.putExtra(kUSERNAME, mCurrentUser.getUserName());
                        getActivity().startActivity(updateUserNameIntent);

                        return true;
                    }
                });

        findPreference(getString(R.string.user_password_key))
                .setOnPreferenceClickListener(new android.support.v7.preference.Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent updatePasswordIntent = new Intent(getContext(), UpdatePasswordActivity.class);
                        getActivity().startActivity(updatePasswordIntent);

                        return true;
                    }
                });

        findPreference(getString(R.string.user_email_key))
                .setOnPreferenceClickListener(new android.support.v7.preference.Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent updateEmailIntent = new Intent(getContext(), UpdateEmailActivity.class);
                        updateEmailIntent.putExtra(kEMAIL, mCurrentUser.getEmail());
                        getActivity().startActivity(updateEmailIntent);

                        return true;
                    }
                });

        findPreference(getString(R.string.user_location_key))
                .setOnPreferenceClickListener(new android.support.v7.preference.Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    PERMISSIONS_REQUEST_FINE_LOCATION);
                        } else {
                            addNewLocation();
                        }

                        return true;
                    }
                });

        findPreference(getString(R.string.privacy_policy_key))
                .setOnPreferenceClickListener(new android.support.v7.preference.Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent privacyPolicyIntent = new Intent(getContext(), PrivacyPolicyActivity.class);
                        getActivity().startActivity(privacyPolicyIntent);

                        return true;
                    }
                });

        findPreference(getString(R.string.terms_and_conditions_key))
                .setOnPreferenceClickListener(new android.support.v7.preference.Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Intent termsAndConditionsIntent = new Intent(getContext(), TermsAndConditionsActivity.class);
                        getActivity().startActivity(termsAndConditionsIntent);

                        return true;
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,
                permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addNewLocation();
                } else {
                    CoordinatorLayout coordinatorLayout = getActivity().findViewById(R.id.fragment_container);
                    Snackbar.make(coordinatorLayout,
                            R.string.alert_permission_needed_string, Snackbar.LENGTH_SHORT).show();
                }
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CoordinatorLayout coordinatorLayout = getActivity().findViewById(R.id.fragment_container);

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                updateUserLocation(place);
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {

                Status status = Autocomplete.getStatusFromIntent(data);
                Snackbar.make(coordinatorLayout,
                        R.string.err_location_saving_string, Snackbar.LENGTH_SHORT).show();

            }
        }
    }

    public void addNewLocation() {

        // Set the fields to specify which types of place data to return.
        List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG);

        // Start the autocomplete intent.
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(getContext().getApplicationContext());
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);

    }

    /* Update Geofire */
    private void updateUserLocation(Place place) {

        if (place == null) {
            return;
        }

        findPreference(getString(R.string.user_location_key)).setSummary(place.getAddress());

        HashMap<String, Object> newLocation = new HashMap<String, Object>();
        newLocation.put(kLATITUDE, place.getLatLng().latitude);
        newLocation.put(kLONGITUDE, place.getLatLng().longitude);


        firebase.child(kUSER).child(mCurrentUserUid).updateChildren(newLocation).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                CoordinatorLayout coordinatorLayout = getActivity().findViewById(R.id.fragment_container);
                if (task.isSuccessful()) {
                    Snackbar.make(coordinatorLayout,
                            R.string.alert_location_successfully_updated_string, Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(coordinatorLayout,
                            R.string.err_location_saving_string, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }
}
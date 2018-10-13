package com.planetpeopleplatform.freegan.activity;

import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.planetpeopleplatform.freegan.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kEMAIL;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;

public class UpdateEmailActivity extends AppCompatActivity {

    private static final String TAG = UpdateEmailActivity.class.getSimpleName();
    private FirebaseUser mUser;
    private String mCurrentUserUid = null;

    @BindView(R.id.fragment_container)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.back_arrow)
    ImageButton mBackArrow;

    @BindView(R.id.email_edit_text)
    EditText mEmailEditText;

    @BindView(R.id.update_button)
    Button mUpdateButton;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_email);
        ButterKnife.bind(this);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        mUser = auth.getCurrentUser();
        mCurrentUserUid = mUser.getUid();
        String currentUserEmail = getIntent().getStringExtra(kEMAIL);
        mEmailEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        mEmailEditText.setText(currentUserEmail);

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoadingIndicator.setVisibility(View.VISIBLE);
                String newEmail = mEmailEditText.getText().toString();

                if (!(newEmail.length() > 0)){
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    return;
                }

                updateUserEmail(newEmail);
            }
        });


    }


    private void updateUserEmail(final String newEmail) {

        mUser.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Email updated");
                    firebase.child(kUSER).child(mCurrentUserUid).child(kEMAIL).setValue(newEmail)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        mLoadingIndicator.setVisibility(View.INVISIBLE);
                                        Snackbar.make(mCoordinatorLayout,
                                                R.string.alert_email_successfully_updated_string, Snackbar.LENGTH_SHORT).show();
                                    }else {
                                        mLoadingIndicator.setVisibility(View.INVISIBLE);
                                        Snackbar.make(mCoordinatorLayout,
                                                R.string.err_email_failed_to_update_string, Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Log.d(TAG, "Error email not updated");
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    Snackbar.make(mCoordinatorLayout,
                            R.string.err_email_failed_to_update_string, Snackbar.LENGTH_SHORT).show();
                }
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                finish();
            }
        });
    }
}

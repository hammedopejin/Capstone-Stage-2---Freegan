package com.planetpeopleplatform.freegan.activity;

import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.planetpeopleplatform.freegan.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERNAME;

public class UpdateUserNameActivity extends AppCompatActivity {

    private String mCurrentUserUid = null;

    @BindView(R.id.fragment_container)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.back_arrow)
    ImageButton mBackArrow;

    @BindView(R.id.user_name_edit_text)
    EditText mUserNameEditText;

    @BindView(R.id.update_button)
    Button mUpdateButton;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user_name);
        ButterKnife.bind(this);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        mCurrentUserUid = auth.getCurrentUser().getUid();

        String currentUserName = getIntent().getStringExtra(kUSERNAME);
        mUserNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});
        mUserNameEditText.setText(currentUserName);

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
                String newUserName = mUserNameEditText.getText().toString();
                if (!(newUserName.length() > 0)){
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    return;
                }
                firebase.child(kUSER).child(mCurrentUserUid).child(kUSERNAME).setValue(newUserName)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mLoadingIndicator.setVisibility(View.INVISIBLE);
                            Snackbar.make(mCoordinatorLayout,
                                    R.string.alert_username_successfully_updated_string, Snackbar.LENGTH_SHORT).show();
                        }else {
                            mLoadingIndicator.setVisibility(View.INVISIBLE);
                            Snackbar.make(mCoordinatorLayout,
                                    R.string.err_username_failed_to_update_string, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        finish();
                    }
                });
            }
        });

    }

}
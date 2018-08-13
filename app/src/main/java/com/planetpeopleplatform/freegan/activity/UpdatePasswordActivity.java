package com.planetpeopleplatform.freegan.activity;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.planetpeopleplatform.freegan.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UpdatePasswordActivity extends AppCompatActivity {

    private static final String TAG = UpdatePasswordActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String mCurrentUserUid = null;

    @BindView(R.id.back_arrow)
    ImageButton mBackArrow;

    @BindView(R.id.password_edit_text)
    EditText mPasswordEditText;

    @BindView(R.id.password_edit_text_2)
    EditText mPasswordEditText2;

    @BindView(R.id.update_button)
    Button mUpdateButton;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mCurrentUserUid = mUser.getUid();
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
                String newPassword = mPasswordEditText.getText().toString();
                String newPassword2 = mPasswordEditText2.getText().toString();
                if ((!(newPassword.length() > 0)) || (!(newPassword2.length() > 0))){
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    return;
                }
                if (!(newPassword.equals(newPassword2))){
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Paswword mismatch!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateUserPassword(newPassword);
            }
        });


    }


    private void updateUserPassword(String newPassword) {

        mUser.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Password updated");
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Password successfully updated!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.d(TAG, "Error password not updated");
                    mLoadingIndicator.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Password failed to update!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }
}

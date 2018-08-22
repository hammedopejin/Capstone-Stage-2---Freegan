package com.planetpeopleplatform.freegan.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.model.User;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    String mUserName = "";

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.register_data)
    android.support.constraint.ConstraintLayout mRegisterData;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    @BindView(R.id.email_edit_text)
    EditText mEmailEditText;

    @BindView(R.id.password_edit_ext)
    EditText mPasswordEditText;

    @BindView(R.id.user_name_edit_text)
    EditText mUserNameEditText;

    private int RC_REGISTER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        mUserNameEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(18)});
        mEmailEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        mAuth= FirebaseAuth.getInstance();
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) {
            Intent intent = new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, RC_REGISTER);
        }
    }


    private void createUserInFireBase(String email, String password){

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Log.d("TAG", "createUserWithEmail: success");

                    saveUserAndLogIn();

                }else
                {
                    Log.d("TAG", "createUserWithEmail: Failed");
                    Snackbar.make(mCoordinatorLayout,
                            R.string.err_registration_failed_string, Snackbar.LENGTH_SHORT).show();
                    showDataView();
                }
            }
        });
    }

    private void saveUserAndLogIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        User.registerUserWith(currentUser.getEmail(), currentUser.getUid(), mUserName);
        Intent intent = new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, RC_REGISTER);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_REGISTER){
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    private void showDataView() {
        /* First, hide the loading indicator */
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        /* Finally, make sure the data is visible */
        mRegisterData.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        /* Then, hide the data */
        mRegisterData.setVisibility(View.INVISIBLE);
        /* Finally, show the loading indicator */
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }



    public void registerButtonTapped(View view) {
        if (mUserNameEditText.getText() == null || mEmailEditText.getText() == null || mPasswordEditText.getText() == null){
            Snackbar.make(mCoordinatorLayout,
                    R.string.alert_all_text_field_must_be_entered_string, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!(mUserNameEditText.getText().toString().length() > 0) || !(mEmailEditText.getText().toString().length() > 0)
                || !(mPasswordEditText.getText().toString().length() > 0)){
            Snackbar.make(mCoordinatorLayout,
                    R.string.alert_all_text_field_must_be_entered_string, Snackbar.LENGTH_SHORT).show();
            return;
        }
        showLoading();
        mUserName = mUserNameEditText.getText().toString();
        createUserInFireBase(mEmailEditText.getText().toString(), mPasswordEditText.getText().toString());
    }

    public void goToLogin(View view) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, RC_REGISTER);
    }
}

package com.planetpeopleplatform.freegan.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.planetpeopleplatform.freegan.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.login_data)
    android.support.constraint.ConstraintLayout mLoginData;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;
    @BindView(R.id.email_edit_text)
    TextView mEmailEditText;
    @BindView(R.id.password_edit_text)
    TextView mPasswordEditText;

    private int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mAuth= FirebaseAuth.getInstance();
    }


    @Override
    protected void onStart() {
        super.onStart();
        loadTweets();
    }


    private void loadTweets(){

        if (mAuth == null){
            finish();
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null) {

            Intent intent = new Intent(this, MainActivity.class);
            startActivityForResult(intent, RC_SIGN_IN);
        }
    }



    private void loginToFireBase(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    loadTweets();
                }else
                {
                    Log.d("TAG", "loginToFireBase: Failed");
                    Snackbar.make(mCoordinatorLayout,
                            R.string.err_login_failed_string, Snackbar.LENGTH_SHORT).show();
                    showDataView();
                }
            }
        });

    }

    public void signInTapped(View view) {
        if (mEmailEditText.getText() == null || mPasswordEditText.getText() == null){
            Snackbar.make(mCoordinatorLayout,
                    R.string.alert_all_text_field_must_be_entered_string, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!(mEmailEditText.getText().toString().length() > 0) || !(mPasswordEditText.getText().toString().length() > 0)){
            Snackbar.make(mCoordinatorLayout,
                    R.string.alert_all_text_field_must_be_entered_string, Snackbar.LENGTH_SHORT).show();
            return;
        }
        showLoading();
        loginToFireBase(mEmailEditText.getText().toString(), mPasswordEditText.getText().toString());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }


    private void showDataView() {
        /* First, hide the loading indicator */
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        /* Finally, make sure the data is visible */
        mLoginData.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        /* Then, hide the data */
        mLoginData.setVisibility(View.INVISIBLE);
        /* Finally, show the loading indicator */
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }


    public void goToRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivityForResult(intent, RC_SIGN_IN);
    }
}

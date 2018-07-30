package com.planetpeopleplatform.freegan.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

    String userName = "";

    @BindView(R.id.register_data)
    android.support.constraint.ConstraintLayout mLoginData;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;
    @BindView(R.id.email_edit_text)
    TextView emailEditText;
    @BindView(R.id.password_edit_ext)
    TextView passwordEditText;
    @BindView(R.id.user_name_edit_text)
    TextView userNameEditText;

    private int RC_REGISTER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

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
                    Toast.makeText(getApplicationContext(),"Successful login",Toast.LENGTH_LONG).show();

                    saveUserAndLogIn();

                }else
                {
                    Log.d("TAG", "createUserWithEmail: Failed");
                    Toast.makeText(getApplicationContext(),"registration failed",Toast.LENGTH_LONG).show();
                    showDataView();
                }
            }
        });
    }

    private void saveUserAndLogIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        User.registerUserWith(currentUser.getEmail(), currentUser.getUid(), userName);
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
        mLoginData.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        /* Then, hide the data */
        mLoginData.setVisibility(View.INVISIBLE);
        /* Finally, show the loading indicator */
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }



    public void registerButtonTapped(View view) {
        if (userNameEditText.getText() == null || emailEditText.getText() == null || passwordEditText.getText() == null){
            Toast.makeText(getApplicationContext(),"All text fields must be entered properly!",Toast.LENGTH_LONG).show();
            return;
        }
        if (!(userNameEditText.getText().toString().length() > 0) || !(emailEditText.getText().toString().length() > 0)
                || !(passwordEditText.getText().toString().length() > 0)){
            Toast.makeText(getApplicationContext(),"All text fields must be entered properly!",Toast.LENGTH_LONG).show();
            return;
        }
        showLoading();
        userName = userNameEditText.getText().toString();
        createUserInFireBase(emailEditText.getText().toString(), passwordEditText.getText().toString());
    }

    public void goToLogin(View view) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, RC_REGISTER);
    }
}

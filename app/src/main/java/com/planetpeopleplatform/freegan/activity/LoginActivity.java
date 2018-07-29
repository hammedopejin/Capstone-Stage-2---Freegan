package com.planetpeopleplatform.freegan.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

    @BindView(R.id.emailEditText)
    TextView emailEditText;
    @BindView(R.id.passwordEditText)
    TextView passwordEditText;

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
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null) {

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("uid", currentUser.getUid());

            startActivityForResult(intent, RC_SIGN_IN);
        }
    }



    private void loginToFireBase(String email, String password){
        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Successful login",Toast.LENGTH_LONG).show();
                    loadTweets();

                }else
                {
                    Log.d("TAG", "loginToFireBase: Failed");
                    Toast.makeText(getApplicationContext(),"login failed",Toast.LENGTH_LONG).show();
                }
            }
        });


        hideProgressDialog();
    }

    public void signInTapped(View view) {
        loginToFireBase(emailEditText.getText().toString(), passwordEditText.getText().toString());
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

    // loading display

    ProgressDialog mProgressDialog = null;

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("uploading");
            mProgressDialog.isIndeterminate();
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


}

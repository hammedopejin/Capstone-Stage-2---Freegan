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
import com.planetpeopleplatform.freegan.model.User;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    String userName = "";


    @BindView(R.id.emailEditText)
    TextView emailEditText;
    @BindView(R.id.passwordEditText)
    TextView passwordEditText;
    @BindView(R.id.userNameEditText)
    TextView userNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        mAuth= FirebaseAuth.getInstance();
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
                }
            }
        });
    }

    private void saveUserAndLogIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        showProgressDialog();
        User.registerUserWith(currentUser.getEmail(), currentUser.getUid(), userName);
        Intent intent = new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        hideProgressDialog();
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
    };



    public void registerButtonTapped(View view) {
        userName = userNameEditText.getText().toString();
        createUserInFireBase(emailEditText.getText().toString(), passwordEditText.getText().toString());
    }
}

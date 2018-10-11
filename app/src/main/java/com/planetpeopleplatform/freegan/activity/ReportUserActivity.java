package com.planetpeopleplatform.freegan.activity;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.model.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kBUNDLE;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kMESSAGE;
import static com.planetpeopleplatform.freegan.utils.Constants.kMESSAGEID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTDATE;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTER;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTERNAME;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTID;
import static com.planetpeopleplatform.freegan.utils.Constants.kREPORTDATE;
import static com.planetpeopleplatform.freegan.utils.Constants.kREPORTMESSAGE;
import static com.planetpeopleplatform.freegan.utils.Constants.kSENDERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kSENDERNAME;
import static com.planetpeopleplatform.freegan.utils.Constants.kTYPE;
import static com.planetpeopleplatform.freegan.utils.Utils.closeOnError;

public class ReportUserActivity extends AppCompatActivity {

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.report_description_edit_text)
    EditText mReportDescriptionEditText;

    @BindView(R.id.report_user_button)
    Button mReportUserButton;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    private User mCurrentUser = null;
    private User mPoster = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_user);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if (savedInstanceState != null) {
            mCurrentUser = savedInstanceState.getParcelable(kCURRENTUSER);
            mPoster = savedInstanceState.getParcelable(kPOSTER);

        } else {
            Bundle argument = getIntent().getBundleExtra(kBUNDLE);
            if (argument == null) {
                closeOnError(mCoordinatorLayout, this);
            }
            mCurrentUser = argument.getParcelable(kCURRENTUSER);
            mPoster = argument.getParcelable(kPOSTER);
        }

        mReportDescriptionEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(120)});
        mReportUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String description = mReportDescriptionEditText.getText().toString();
                if(description.equals("")){
                    Snackbar.make(mCoordinatorLayout,
                            R.string.alert_reason_for_report_missing_string, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                postToFirebase();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(kCURRENTUSER, mCurrentUser);
        outState.putParcelable(kPOSTER, mPoster);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void postToFirebase() {
        showLoading();
        final SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");
        final Date dataobj= new Date();

        DatabaseReference reference = firebase.child(kREPORTMESSAGE).push();
        String messageId = reference.getKey();

        HashMap<String, Object> message = new HashMap<String, Object>();

        message.put(kMESSAGE, mReportDescriptionEditText.getText().toString() );
        message.put(kMESSAGEID, messageId);
        message.put(kSENDERID, mCurrentUser.getObjectId());
        message.put(kSENDERNAME, mCurrentUser.getUserName());
        message.put(kPOSTERID, mPoster.getObjectId());
        message.put(kPOSTERNAME, mPoster.getUserName());
        message.put(kREPORTDATE, sfd.format(dataobj));
        message.put(kTYPE, kTYPE);

        mReportDescriptionEditText.getText().clear();
        mReportDescriptionEditText.clearFocus();
        mLoadingIndicator.setVisibility(View.INVISIBLE);

        reference.setValue(message).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Snackbar.make(mCoordinatorLayout,
                                R.string.alert_message_sent_successfully, Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(mCoordinatorLayout,
                                R.string.err_message_sending_failed_string, Snackbar.LENGTH_SHORT).show();
                    }
                    task.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            finish();
                        }
                    });
                }
            });
    }

    private void showLoading() {
            /* Then, hide the data */
            mReportDescriptionEditText.setVisibility(View.INVISIBLE);
            mReportUserButton.setVisibility(View.INVISIBLE);
            /* Finally, show the loading indicator */
            mLoadingIndicator.setVisibility(View.VISIBLE);
    }
}


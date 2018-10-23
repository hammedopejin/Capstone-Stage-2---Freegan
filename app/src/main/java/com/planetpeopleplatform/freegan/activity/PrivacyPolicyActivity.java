package com.planetpeopleplatform.freegan.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import com.planetpeopleplatform.freegan.R;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        WebView myWebView = new WebView(this);
        setContentView(myWebView);
        myWebView.loadUrl(getString(R.string.freegan_privacy_policies_url_string));
    }
}

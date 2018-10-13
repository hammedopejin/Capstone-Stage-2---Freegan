package com.planetpeopleplatform.freegan.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

import com.planetpeopleplatform.freegan.R;

public class TermsAndConditionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);
        WebView myWebView = new WebView(this);
        setContentView(myWebView);
        myWebView.loadUrl(getString(R.string.google_map_platform_terms_url_string));
    }
}

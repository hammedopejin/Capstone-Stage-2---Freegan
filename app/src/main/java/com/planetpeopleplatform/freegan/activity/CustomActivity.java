package com.planetpeopleplatform.freegan.activity;


import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import static android.view.View.*;

/**
 * This is a common activity that all other activities of the app can extend to
 * inherit the common behaviors like implementing a common interface that can be
 * used in all child activities.
 */
public class CustomActivity extends AppCompatActivity implements OnClickListener {



         OnTouchListener TOUCH;



        @Override
        public void setContentView(int layoutResID) {
            super.setContentView(layoutResID);
            setupActionBar();
        }

        void setupActionBar() {
            ActionBar actionBar  = (ActionBar) getSupportActionBar();
            if (actionBar == null)
                return;
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }


        View setTouchNClick(int id) {
            View v = setClick(id);
            if (v != null)
                v.setOnTouchListener(TOUCH);
            return v;
        }


        View setClick(int id) {
            View v  = findViewById(id);
            if (v != null)
                v.setOnClickListener(this);
            return v;
        }

        @Override
            public void onClick(View view) {

        }
}


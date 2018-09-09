package com.planetpeopleplatform.freegan.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.planetpeopleplatform.freegan.fragment.MainImageFragment;
import com.planetpeopleplatform.freegan.model.Post;


public class MainChildViewPagerAdapter extends FragmentPagerAdapter {

    Post mPost;

    public MainChildViewPagerAdapter(Fragment fm, Post post) {
        super(fm.getChildFragmentManager());
        this.mPost = post;
    }

    @Override
    public int getCount() {
        return mPost.getImageUrl().size();
    }

    @Override
    public Fragment getItem(int position) {

        return MainImageFragment.newInstance(mPost, position);

    }

}
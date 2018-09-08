package com.planetpeopleplatform.freegan.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.planetpeopleplatform.freegan.fragment.ProfileImageFragment;
import com.planetpeopleplatform.freegan.model.Post;

public class ProfileChildViewPagerAdaper extends FragmentPagerAdapter {


    Post mPost;

    public ProfileChildViewPagerAdaper(FragmentManager fm, Post post) {
        super(fm);
        this.mPost = post;
    }

    @Override
    public int getCount() {
        return mPost.getImageUrl().size();
    }

    @Override
    public Fragment getItem(int position) {

        return ProfileImageFragment.newInstance(mPost.getImageUrl(), position);

    }

}
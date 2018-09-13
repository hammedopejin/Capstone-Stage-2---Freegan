package com.planetpeopleplatform.freegan.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import com.planetpeopleplatform.freegan.fragment.ProfileImageFragment;
import com.planetpeopleplatform.freegan.model.Post;

public class ProfileChildViewPagerAdaper extends FragmentPagerAdapter {


    Post mPost;

    public ProfileChildViewPagerAdaper(Fragment fragment, Post post) {
        super(fragment.getChildFragmentManager());
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
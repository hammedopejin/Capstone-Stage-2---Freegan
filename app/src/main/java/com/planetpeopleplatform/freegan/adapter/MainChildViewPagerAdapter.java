package com.planetpeopleplatform.freegan.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.planetpeopleplatform.freegan.fragment.MainImageFragment;
import com.planetpeopleplatform.freegan.model.Post;


public class MainChildViewPagerAdapter extends FragmentStatePagerAdapter {

    Post mPost;

    public MainChildViewPagerAdapter(Fragment fragment, Post post) {
        super(fragment.getChildFragmentManager());
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
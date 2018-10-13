package com.planetpeopleplatform.freegan.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import com.planetpeopleplatform.freegan.fragment.ProfileImageFragment;
import com.planetpeopleplatform.freegan.model.Post;

public class ProfileChildViewPagerAdaper extends FragmentPagerAdapter {

    private Post mPost;

    public ProfileChildViewPagerAdaper(Fragment fragment, Post post) {
        super(fragment.getChildFragmentManager());
        this.mPost = post;
    }

    @Override
    public int getCount() {
        if (mPost == null){
            return 0;
        }
        return mPost.getImageUrl().size();
    }

    @Override
    public Fragment getItem(int position) {
        if (mPost == null){
            return null;
        }
        return ProfileImageFragment.newInstance(mPost.getImageUrl(), position);

    }
}
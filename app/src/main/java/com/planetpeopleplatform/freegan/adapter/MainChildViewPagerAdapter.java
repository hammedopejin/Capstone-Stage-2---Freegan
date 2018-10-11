package com.planetpeopleplatform.freegan.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.planetpeopleplatform.freegan.fragment.MainImageFragment;
import com.planetpeopleplatform.freegan.model.Post;


public class MainChildViewPagerAdapter extends FragmentStatePagerAdapter {

    private Post mPost;

    public MainChildViewPagerAdapter(Fragment fragment, Post post) {
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
        return MainImageFragment.newInstance(mPost, position);

    }

}
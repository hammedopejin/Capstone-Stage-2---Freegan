package com.planetpeopleplatform.freegan.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.planetpeopleplatform.freegan.fragment.MainChildImagePagerFragment;
import com.planetpeopleplatform.freegan.model.Post;

import java.util.ArrayList;

public class MainImagePagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Post> mListPosts;

    public MainImagePagerAdapter(Fragment fragment, ArrayList<Post> listPosts) {
        // Note: Initialize with the child fragment manager.
        super(fragment.getChildFragmentManager());

        this.mListPosts = listPosts;
    }

    @Override
    public int getCount() {
        if (mListPosts == null) {
            return 0;
        }
        return mListPosts.size();
    }

    @Override
    public Fragment getItem(int position) {
        if (mListPosts != null) {
            return MainChildImagePagerFragment.newInstance(mListPosts.get(position));
        }
        return null;
    }
}
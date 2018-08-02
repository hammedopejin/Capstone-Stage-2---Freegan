package com.planetpeopleplatform.freegan.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.planetpeopleplatform.freegan.fragment.ImageFragment;
import com.planetpeopleplatform.freegan.model.Post;

import java.util.ArrayList;


public class ImagePagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Post> listPosts;

    public ImagePagerAdapter(Fragment fragment, ArrayList<Post> listPosts) {
        // Note: Initialize with the child fragment manager.
        super(fragment.getChildFragmentManager());

        this.listPosts =  listPosts;
    }

    @Override
    public int getCount() {
        return listPosts.size();
    }

    @Override
    public Fragment getItem(int position) {
        return ImageFragment.newInstance(listPosts.get(position));
    }
}

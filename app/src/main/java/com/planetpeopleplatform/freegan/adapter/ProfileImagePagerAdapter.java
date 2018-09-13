package com.planetpeopleplatform.freegan.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.planetpeopleplatform.freegan.fragment.ProfileChildImagePagerFragment;
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;

import java.util.ArrayList;

public class ProfileImagePagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Post> listPosts;
    private User mPoster;
    private User mCurrentUser;

    public ProfileImagePagerAdapter(Fragment fragment, ArrayList<Post> listPosts, User poster, User currentUser) {
        // Note: Initialize with the child fragment manager.
        super(fragment.getChildFragmentManager());
        this.listPosts =  listPosts;
        this.mPoster = poster;
        this.mCurrentUser = currentUser;
    }

    @Override
    public int getCount() {
        return listPosts.size();
    }

    @Override
    public Fragment getItem(int position) {
        return ProfileChildImagePagerFragment.newInstance(listPosts.get(position), mPoster, mCurrentUser);
    }
}
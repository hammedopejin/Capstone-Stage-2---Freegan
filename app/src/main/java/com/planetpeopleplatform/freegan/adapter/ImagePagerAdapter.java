package com.planetpeopleplatform.freegan.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.fragment.ImageFragment;


public class ImagePagerAdapter extends FragmentStatePagerAdapter {

    private String mImageUrl;

    public ImagePagerAdapter(Fragment fragment, String imageUrl) {
        // Note: Initialize with the child fragment manager.
        super(fragment.getChildFragmentManager());

        this.mImageUrl = imageUrl;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Fragment getItem(int position) {
        return ImageFragment.newInstance(mImageUrl);
    }
}

package com.planetpeopleplatform.freegan.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.planetpeopleplatform.freegan.R;

import java.util.ArrayList;

public class ProfileImageFragment extends Fragment {

    private static final String KEY_POST_RES = "com.planetpeopleplatform.freegan.key.postRes";
    private static final String KEY_POST_IMAGE_POSITION = "com.planetpeopleplatform.freegan.key.postImagePosition";

    private ArrayList<String> mImageUrl = null;
    private int mPosition;


    public static ProfileImageFragment newInstance(ArrayList<String> imageUrl, int position) {
        ProfileImageFragment fragment = new ProfileImageFragment();
        Bundle argument = new Bundle();
        argument.putStringArrayList(KEY_POST_RES, imageUrl);
        argument.putInt(KEY_POST_IMAGE_POSITION, position);
        fragment.setArguments(argument);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_image, container, false);


        Bundle arguments = getArguments();
        mImageUrl = arguments.getStringArrayList(KEY_POST_RES);
        mPosition = arguments.getInt(KEY_POST_IMAGE_POSITION, 0);
        String postImage = mImageUrl.get(mPosition);

        // Just like we do when binding views at the grid, we set the transition name to be the string
        // value of the image res.
        view.findViewById(R.id.image).setTransitionName(String.valueOf(postImage));

        // Load the image with Glide to prevent OOM error when the image drawables are very large.
        Glide.with(this)
                .load(postImage)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable>
                            target, boolean isFirstResource) {
                        // The postponeEnterTransition is called on the parent ProfileImagePagerFragment, so the
                        // startPostponedEnterTransition() should also be called on it to get the transition
                        // going in case of a failure.
                        getParentFragment().startPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable>
                            target, DataSource dataSource, boolean isFirstResource) {
                        // The postponeEnterTransition is called on the parent ProfileImagePagerFragment, so the
                        // startPostponedEnterTransition() should also be called on it to get the transition
                        // going when the image is ready.
                        getParentFragment().startPostponedEnterTransition();
                        return false;
                    }
                })
                .into((ImageView) view.findViewById(R.id.image));



        return view;
    }


}
package com.planetpeopleplatform.freegan.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.view.ViewPager;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.planetpeopleplatform.freegan.activity.MainActivity;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.adapter.ImagePagerAdapter;
import com.planetpeopleplatform.freegan.model.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A fragment for displaying a pager of images.
 */
public class ImagePagerFragment extends Fragment {

    private static final String KEY_ARRAY_LIST = "com.planetpeopleplatform.freegan.key.listPostArray";

    private ViewPager mViewPager;
    private ArrayList<Post> listPosts =  new ArrayList<Post>();
    private ArrayList<? extends Parcelable> list;

    public static ImagePagerFragment newInstance(ArrayList<Post> listPosts) {
        ImagePagerFragment fragment = new ImagePagerFragment();
        Bundle argument = new Bundle();
        argument.putParcelableArrayList(KEY_ARRAY_LIST, listPosts);
        fragment.setArguments(argument);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewPager = (ViewPager) inflater.inflate(R.layout.fragment_pager, container, false);

        Bundle arguments = getArguments();
        list = arguments.getParcelableArrayList(KEY_ARRAY_LIST);

        for (Parcelable item : list){
            Post post = (Post) item;
            listPosts.add(post);
        }

        mViewPager.setAdapter(new ImagePagerAdapter(this, listPosts));
        // Set the current position and add a listener that will update the selection coordinator when
        // paging the images.
        mViewPager.setCurrentItem(MainActivity.currentPosition);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                MainActivity.currentPosition = position;
            }
        });

        prepareSharedElementTransition();

        // Avoid a postponeEnterTransition on orientation change, and postpone only of first creation.
        if (savedInstanceState == null) {
            postponeEnterTransition();
        }

        return mViewPager;
    }

    /**
     * Prepares the shared element transition from and back to the grid fragment.
     */
    private void prepareSharedElementTransition() {
        Transition transition =
                TransitionInflater.from(getContext())
                        .inflateTransition(R.transition.image_shared_element_transition);
        setSharedElementEnterTransition(transition);

        // A similar mapping is set at the GridFragment with a setExitSharedElementCallback.
        setEnterSharedElementCallback(
                new SharedElementCallback() {
                    @Override
                    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                        // Locate the image view at the primary fragment (the ImageFragment that is currently
                        // visible). To locate the fragment, call instantiateItem with the selection position.
                        // At this stage, the method will simply return the fragment at the position and will
                        // not create a new one.
                        Fragment currentFragment = (Fragment) mViewPager.getAdapter()
                                .instantiateItem(mViewPager, MainActivity.currentPosition);
                        View view = currentFragment.getView();
                        if (view == null) {
                            return;
                        }

                        // Map the first shared element name to the child ImageView.
                        sharedElements.put(names.get(0), view.findViewById(R.id.image));
                    }
                });
    }
}

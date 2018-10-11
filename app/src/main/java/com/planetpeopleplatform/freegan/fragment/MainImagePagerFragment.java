package com.planetpeopleplatform.freegan.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
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
import com.planetpeopleplatform.freegan.adapter.MainImagePagerAdapter;
import com.planetpeopleplatform.freegan.model.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A fragment for displaying a pager of images.
 */
public class MainImagePagerFragment extends Fragment {

    private static final String KEY_ARRAY_LIST = "com.planetpeopleplatform.freegan.key.listPostArray";
    private ArrayList<Post> mListPosts =  new ArrayList<>();
    private MainImagePagerAdapter mMainImagePagerAdapter;
    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    public static MainImagePagerFragment newInstance(ArrayList<Post> listPosts) {
        MainImagePagerFragment fragment = new MainImagePagerFragment();
        Bundle argument = new Bundle();
        argument.putParcelableArrayList(KEY_ARRAY_LIST, listPosts);
        fragment.setArguments(argument);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pager, container, false);
        ButterKnife.bind(this, rootView);

        Bundle arguments = getArguments();
        mListPosts = arguments.getParcelableArrayList(KEY_ARRAY_LIST);

        mMainImagePagerAdapter = new MainImagePagerAdapter(this, mListPosts);
        mViewPager.setAdapter(mMainImagePagerAdapter);

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager, true);

        // Set the current position and add a listener that will update the selection coordinator when
        // paging the images.
        mViewPager.setCurrentItem(MainActivity.currentPosition);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                MainActivity.currentPosition = position;
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                MainActivity.currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // Avoid a postponeEnterTransition on orientation change, and postpone only of first creation.
        if (savedInstanceState == null) {
            postponeEnterTransition();
        }

        prepareSharedElementTransition();

        return rootView;
    }

    /**
     * Prepares the shared element transition from and back to the grid fragment.
     */
    private void prepareSharedElementTransition() {
        Transition transition =
                TransitionInflater.from(getContext())
                        .inflateTransition(R.transition.image_shared_element_transition);
        setSharedElementEnterTransition(transition);

        // A similar mapping is set at the MainGridFragment with a setExitSharedElementCallback.
        setEnterSharedElementCallback(
                new SharedElementCallback() {
                    @Override
                    public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                        // Locate the image view at the primary fragment (the MainImageFragment that is currently
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
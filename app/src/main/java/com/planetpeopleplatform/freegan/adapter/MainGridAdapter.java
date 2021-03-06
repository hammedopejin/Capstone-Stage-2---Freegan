package com.planetpeopleplatform.freegan.adapter;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.planetpeopleplatform.freegan.activity.MainActivity;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.fragment.MainImagePagerFragment;
import com.planetpeopleplatform.freegan.model.Post;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;


public class MainGridAdapter extends RecyclerView.Adapter<MainGridAdapter.ImageViewHolder> {

    /**
     * A listener that is attached to all ViewHolders to handle image loading events and clicks.
     */
    private interface ViewHolderListener {

        void onLoadCompleted(ImageView view, int adapterPosition);

        void onItemClicked(View view, int adapterPosition, ArrayList<Post> listPosts);
    }

    private final RequestManager mRequestManager;
    private final MainGridAdapter.ViewHolderListener mViewHolderListener;
    private ArrayList<Post> mListPosts;

    /**
     * Constructs a new grid adapter for the given {@link Fragment}.
     */
    public MainGridAdapter(Fragment fragment, ArrayList<Post> listPosts) {
        this.mRequestManager = Glide.with(fragment);
        this.mViewHolderListener = new MainGridAdapter.ViewHolderListenerImpl(fragment);
        this.mListPosts = listPosts;
    }

    @NonNull
    @Override
    public MainGridAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_card, parent, false);
        return new MainGridAdapter.ImageViewHolder(view, mRequestManager, mViewHolderListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MainGridAdapter.ImageViewHolder holder, int position) {
        holder.onBind();
    }

    @Override
    public int getItemCount() {
        if (mListPosts == null) {
            return 0;
        }
        return mListPosts.size();
    }


    private class ViewHolderListenerImpl implements MainGridAdapter.ViewHolderListener {

        private Fragment mFragment;
        private AtomicBoolean mEnterTransitionStarted;

        ViewHolderListenerImpl(Fragment fragment) {
            this.mFragment = fragment;
            this.mEnterTransitionStarted = new AtomicBoolean();
        }

        @Override
        public void onLoadCompleted(ImageView view, int position) {
            if (mFragment != null && mEnterTransitionStarted != null) {
                // Call startPostponedEnterTransition only when the 'selected' image loading is completed.
                if (MainActivity.currentPosition != position) {
                    return;
                }
                if (mEnterTransitionStarted.getAndSet(true)) {
                    return;
                }
                mFragment.startPostponedEnterTransition();
            }
        }

        /**
         * Handles a view click by setting the current position to the given {@code position} and
         * starting a {@link  MainImagePagerFragment} which displays the image at the position.
         *
         * @param view     the clicked {@link ImageView} (the shared element view will be re-mapped at the
         *                 MainGridFragment's SharedElementCallback)
         * @param position the selected view position
         */
        @Override
        public void onItemClicked(View view, int position, ArrayList<Post> listPosts) {
            if (mListPosts != null && mFragment != null) {
                // Update the position.
                MainActivity.currentPosition = position;

                // Exclude the clicked card from the exit transition (e.g. the card will disappear immediately
                // instead of fading out with the rest to prevent an overlapping animation of fade and move).
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    ((TransitionSet) Objects.requireNonNull(mFragment.getExitTransition())).excludeTarget(view, true);
                }

                ImageView transitioningView = view.findViewById(R.id.card_image);


                if (mFragment.getFragmentManager() != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mFragment.getFragmentManager()
                                .beginTransaction()
                                .setReorderingAllowed(true) // Optimize for shared element transition
                                .addSharedElement(transitioningView, transitioningView.getTransitionName())
                                .replace(R.id.fragment_container, MainImagePagerFragment.newInstance(mListPosts), MainImagePagerFragment.class
                                        .getSimpleName())
                                .addToBackStack(null)
                                .commit();
                    }
                }
            }
        }
    }

    /**
     * ViewHolder for the grid's images.
     */
    class ImageViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        private final ImageView mImage;
        private final RequestManager mRequestManager;
        private final MainGridAdapter.ViewHolderListener mViewHolderListener;

        ImageViewHolder(View itemView, RequestManager requestManager,
                        MainGridAdapter.ViewHolderListener viewHolderListener) {
            super(itemView);
            this.mImage = itemView.findViewById(R.id.card_image);
            this.mRequestManager = requestManager;
            this.mViewHolderListener = viewHolderListener;
            itemView.findViewById(R.id.card_view).setOnClickListener(this);
        }

        /**
         * Binds this view holder to the given adapter position.
         * <p>
         * The binding will load the image into the image view, as well as set its transition name for
         * later.
         */
        void onBind() {
            if (mImage != null && mListPosts != null) {
                int adapterPosition = getAdapterPosition();
                setImage(adapterPosition);
                // Set the string value of the image resource as the unique transition name for the view.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mImage.setTransitionName(((mListPosts.get(adapterPosition).getImageUrl())).get(0));
                }
            }
        }

        void setImage(final int adapterPosition) {
            if (mRequestManager != null && mImage != null && mListPosts != null && mViewHolderListener != null) {
                // Load the image with Glide to prevent OOM error when the image drawables are very large.
                mRequestManager
                        .load(mListPosts.get(adapterPosition).getImageUrl().get(0))
                        //.error(R.drawable.person_icon)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                        Target<Drawable> target, boolean isFirstResource) {
                                mViewHolderListener.onLoadCompleted(mImage, adapterPosition);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable>
                                    target, DataSource dataSource, boolean isFirstResource) {
                                mViewHolderListener.onLoadCompleted(mImage, adapterPosition);
                                return false;
                            }
                        })
                        .into(mImage);
            }
        }

        @Override
        public void onClick(View view) {
            if (mViewHolderListener != null && mListPosts != null) {
                // Let the listener start the MainImagePagerFragment.
                mViewHolderListener.onItemClicked(view, getAdapterPosition(), mListPosts);
            }
        }
    }

}
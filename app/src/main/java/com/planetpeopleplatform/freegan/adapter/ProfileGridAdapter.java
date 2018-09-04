package com.planetpeopleplatform.freegan.adapter;

import android.graphics.drawable.Drawable;
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
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.activity.ProfileActivity;
import com.planetpeopleplatform.freegan.fragment.ProfileImagePagerFragment;
import com.planetpeopleplatform.freegan.model.Post;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProfileGridAdapter extends RecyclerView.Adapter<ProfileGridAdapter.ImageViewHolder> {

    /**
     * A listener that is attached to all ViewHolders to handle image loading events and clicks.
     */
    private interface ViewHolderListener {

        void onLoadCompleted(ImageView view, int adapterPosition);

        void onItemClicked(View view, int adapterPosition, ArrayList<Post> listPosts);
    }

    private final RequestManager mRequestManager;
    private final ProfileGridAdapter.ViewHolderListener mViewHolderListener;
    private  ArrayList<Post> mListPosts;

    /**
     * Constructs a new grid adapter for the given {@link Fragment}.
     */
    public ProfileGridAdapter(Fragment fragment, ArrayList<Post> listPosts) {
        this.mRequestManager = Glide.with(fragment);
        this.mViewHolderListener = new ProfileGridAdapter.ViewHolderListenerImpl(fragment);
        this.mListPosts = listPosts;
    }

    @NonNull
    @Override
    public ProfileGridAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_card, parent, false);
        return new ProfileGridAdapter.ImageViewHolder(view, mRequestManager, mViewHolderListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileGridAdapter.ImageViewHolder holder, int position) {
        holder.onBind();
    }

    @Override
    public int getItemCount() {
        return mListPosts.size();
    }



    private class ViewHolderListenerImpl implements ProfileGridAdapter.ViewHolderListener {

        private Fragment mFragment;
        private AtomicBoolean mEnterTransitionStarted;

        ViewHolderListenerImpl(Fragment fragment) {
            this.mFragment = fragment;
            this.mEnterTransitionStarted = new AtomicBoolean();
        }

        @Override
        public void onLoadCompleted(ImageView view, int position) {
            // Call startPostponedEnterTransition only when the 'selected' image loading is completed.
            if (ProfileActivity.currentPosition != position) {
                return;
            }
            if (mEnterTransitionStarted.getAndSet(true)) {
                return;
            }
            mFragment.startPostponedEnterTransition();
        }


        @Override
        public void onItemClicked(View view, int position, ArrayList<Post> listPosts) {
            // Update the position.
            ProfileActivity.currentPosition = position;

            // Exclude the clicked card from the exit transition (e.g. the card will disappear immediately
            // instead of fading out with the rest to prevent an overlapping animation of fade and move).
            ((TransitionSet) mFragment.getExitTransition()).excludeTarget(view, true);

            ImageView transitioningView = view.findViewById(R.id.card_image);


            mFragment.getFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true) // Optimize for shared element transition
                    .addSharedElement(transitioningView, transitioningView.getTransitionName())
                    .replace(R.id.fragment_container, ProfileImagePagerFragment.newInstance(mListPosts), ProfileImagePagerFragment.class
                            .getSimpleName())
                    .addToBackStack(null)
                    .commit();
        }
    }

    /**
     * ViewHolder for the grid's images.
     */
    class ImageViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {

        private final ImageView mImage;
        private final RequestManager mRequestManager;
        private final ProfileGridAdapter.ViewHolderListener mViewHolderListener;

        ImageViewHolder(View itemView, RequestManager requestManager,
                        ProfileGridAdapter.ViewHolderListener viewHolderListener) {
            super(itemView);
            this.mImage = itemView.findViewById(R.id.card_image);
            this.mRequestManager = requestManager;
            this.mViewHolderListener = viewHolderListener;
            itemView.findViewById(R.id.card_view).setOnClickListener(this);
        }

        /**
         * Binds this view holder to the given adapter position.
         *
         * The binding will load the image into the image view, as well as set its transition name for
         * later.
         */
        void onBind() {
            int adapterPosition = getAdapterPosition();
            setImage(adapterPosition);
            // Set the string value of the image resource as the unique transition name for the view.
            mImage.setTransitionName(((mListPosts.get(adapterPosition).getImageUrl())).get(0));
        }

        void setImage(final int adapterPosition) {
            // Load the image with Glide to prevent OOM error when the image drawables are very large.
            mRequestManager
                    .load(mListPosts.get(adapterPosition).getImageUrl().get(0))
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

        @Override
        public void onClick(View view) {
            // Let the listener start the ProfileImagePagerFragment.
            mViewHolderListener.onItemClicked(view, getAdapterPosition(), mListPosts);
        }
    }

}
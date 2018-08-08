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
import com.planetpeopleplatform.freegan.activity.MainActivity;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.fragment.MainImagePagerFragment;
import com.planetpeopleplatform.freegan.model.Post;

import java.util.ArrayList;
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
    private final ViewHolderListener mViewHolderListener;
    private  ArrayList<Post> mListPosts;

    /**
     * Constructs a new grid adapter for the given {@link Fragment}.
     */
    public MainGridAdapter(Fragment fragment, ArrayList<Post> listPosts) {
        this.mRequestManager = Glide.with(fragment);
        this.mViewHolderListener = new ViewHolderListenerImpl(fragment);
        this.mListPosts = listPosts;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_card, parent, false);
        return new ImageViewHolder(view, mRequestManager, mViewHolderListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.onBind();
    }

    @Override
    public int getItemCount() {
        return mListPosts.size();
    }


    /**
     * Default {@link ViewHolderListener} implementation.
     */
    private class ViewHolderListenerImpl implements ViewHolderListener {

        private Fragment fragment;
        private AtomicBoolean enterTransitionStarted;

        ViewHolderListenerImpl(Fragment fragment) {
            this.fragment = fragment;
            this.enterTransitionStarted = new AtomicBoolean();
        }

        @Override
        public void onLoadCompleted(ImageView view, int position) {
            // Call startPostponedEnterTransition only when the 'selected' image loading is completed.
            if (MainActivity.currentPosition != position) {
                return;
            }
            if (enterTransitionStarted.getAndSet(true)) {
                return;
            }
            fragment.startPostponedEnterTransition();
        }

        /**
         * Handles a view click by setting the current position to the given {@code position} and
         * starting a {@link  MainImagePagerFragment} which displays the image at the position.
         *
         * @param view the clicked {@link ImageView} (the shared element view will be re-mapped at the
         * MainGridFragment's SharedElementCallback)
         * @param position the selected view position
         */
        @Override
        public void onItemClicked(View view, int position, ArrayList<Post> listPosts) {
            // Update the position.
            MainActivity.currentPosition = position;

            // Exclude the clicked card from the exit transition (e.g. the card will disappear immediately
            // instead of fading out with the rest to prevent an overlapping animation of fade and move).
            ((TransitionSet) fragment.getExitTransition()).excludeTarget(view, true);

            ImageView transitioningView = view.findViewById(R.id.card_image);


            fragment.getFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true) // Optimize for shared element transition
                    .addSharedElement(transitioningView, transitioningView.getTransitionName())
                    .replace(R.id.fragment_container, MainImagePagerFragment.newInstance(mListPosts), MainImagePagerFragment.class
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

        private final ImageView image;
        private final RequestManager requestManager;
        private final ViewHolderListener viewHolderListener;

        ImageViewHolder(View itemView, RequestManager requestManager,
                        ViewHolderListener viewHolderListener) {
            super(itemView);
            this.image = itemView.findViewById(R.id.card_image);
            this.requestManager = requestManager;
            this.viewHolderListener = viewHolderListener;
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
            image.setTransitionName(String.valueOf(R.drawable.ic_launcher_background));
        }

        void setImage(final int adapterPosition) {
            // Load the image with Glide to prevent OOM error when the image drawables are very large.
            requestManager
                    .load(mListPosts.get(adapterPosition).getImageUrl())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            viewHolderListener.onLoadCompleted(image, adapterPosition);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable>
                                target, DataSource dataSource, boolean isFirstResource) {
                            viewHolderListener.onLoadCompleted(image, adapterPosition);
                            return false;
                        }
                    })
                    .into(image);
        }

        @Override
        public void onClick(View view) {
            // Let the listener start the MainImagePagerFragment.
            viewHolderListener.onItemClicked(view, getAdapterPosition(), mListPosts);
        }
    }

}
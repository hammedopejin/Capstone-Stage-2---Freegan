package com.planetpeopleplatform.freegan.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.model.Post;

public class VerticalPagerTabAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    private final Post mPost;
    private final ListView mListView;
    private OnItemClickListener mListener;

    private int currentSelected = 0;

    public VerticalPagerTabAdapter(Post data,  ListView listView, OnItemClickListener listener){
        this.mPost = data;
        this.mListView = listView;
        this.mListener = listener;

        listView.setOnItemClickListener(this);
    }

    @Override
    public int getCount() {
        if (mPost == null){
            return 0;
        }
        if(mPost.getImageUrl().size() > 1) {
            return mPost.getImageUrl().size();
        }
        return 0;
    }

    @Override
    public Object getItem(int i) {
        if (mPost == null){
            return null;
        }
        return mPost.getImageUrl().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // Currently not using viewHolder pattern cause there aren't too many tabs yet.


        if(view == null){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_tab, viewGroup, false);
        }

        ImageView tabTitle = (ImageView) view.findViewById(R.id.image_tab_icon);

        if(i == currentSelected){
            // change the appearance
            tabTitle.setImageResource((R.drawable.ic_brightness_1_custom_red_24dp));
        }else{
            // change the appearance
            tabTitle.setImageResource((R.drawable.ic_brightness_1_dark_gray_24dp));
        }

        return view;
    }


    /**
     * Return item view at the given position or null if position is not visible.
     */
    private View getViewByPosition(int pos) {
        if(mListView == null){
            return  null;
        }
        final int firstListItemPosition = mListView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + mListView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return null;
        } else {
            final int childIndex = pos - firstListItemPosition;
            return mListView.getChildAt(childIndex);
        }
    }


    private void select(int position){
        if(currentSelected >= 0){
            deselect(currentSelected);
        }

        View targetView = getViewByPosition(position);
        if(targetView != null) {
            // change the appearance
            ((ImageView)(targetView.findViewById(R.id.image_tab_icon))).setImageResource((R.drawable.ic_brightness_1_custom_red_24dp));


        }

        if(mListener != null){
            mListener.selectItem(position);
        }

        currentSelected = position;

    }

    private void deselect(int position) {
        if(getViewByPosition(position) != null){
            View targetView = getViewByPosition(position);
            if(targetView != null) {
                // change the appearance
                ((ImageView)(targetView.findViewById(R.id.image_tab_icon))).setImageResource((R.drawable.ic_brightness_1_dark_gray_24dp));

            }
        }

        currentSelected = -1;
    }


    // OnClick Events


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        select(i);
    }

    public void OnItemClickListener(VerticalPagerTabAdapter.OnItemClickListener listener){
        this.mListener = listener;
    }

    public void setCurrentSelected(int i) {
        select(i);
    }

    public interface OnItemClickListener{
        void selectItem(int position);
    }

}

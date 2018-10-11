package com.planetpeopleplatform.freegan.adapter;

import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.model.Post;

public class VerticalPagerTabAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    private final Post data;
    private final ListView listView;
    private OnItemClickListener listener;

    private int currentSelected = 0;

    public VerticalPagerTabAdapter(Post data, ListView listView, OnItemClickListener listener){
        this.data = data;
        this.listView = listView;
        this.listener = listener;

        listView.setOnItemClickListener(this);
    }

    // Override other Adpter method here.

    @Override
    public int getCount() {
        return data.getImageUrl().size();
    }

    @Override
    public Object getItem(int i) {
        return data.getImageUrl().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // Currently not using viewHolder pattern cause there aren't too many tabs in the demo project.


        if(view == null){
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_tab, viewGroup, false);
        }

        ImageView tabTitle = (ImageView) view.findViewById(R.id.txt_tab_title);


        if(i == currentSelected){
            // change the appearance
            tabTitle.setImageResource(R.drawable.tab_selector);

        }else{
            // change the appearance

        }

        return view;
    }


    /**
     * Return item view at the given position or null if position is not visible.
     */
    public View getViewByPosition(int pos) {
        if(listView == null){
            return  null;
        }
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return null;
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }


    private void select(int position){
        if(currentSelected >= 0){
            deselect(currentSelected);
        }

        View targetView = getViewByPosition(position);
        if(targetView != null) {
            // change the appearance

        }

        if(listener != null){
            listener.selectItem(position);
        }

        currentSelected = position;

    }

    private void deselect(int position) {
        if(getViewByPosition(position) != null){
            View targetView = getViewByPosition(position);
            if(targetView != null) {
                // change the appearance

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
        this.listener = listener;
    }

    public void setCurrentSelected(int i) {
        select(i);
    }

    public interface OnItemClickListener{
        void selectItem(int position);
    }

}

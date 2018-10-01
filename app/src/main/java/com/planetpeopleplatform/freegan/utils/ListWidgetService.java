package com.planetpeopleplatform.freegan.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.data.FreeganContract;
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.planetpeopleplatform.freegan.utils.Constants.JSONARRAYKEY;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kMESSAGE;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERNAME;

public class ListWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new GridRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Cursor mCursor;

    private String mMessage = null;
    private String mUserName = null;
    private String mCurrentUserUid = null;
    private String mChatRoomId = null;
    private Post mPost = null;
    private User mChatMate = null;

    GridRemoteViewsFactory(Context applicationContext, Intent intent) {
        mContext = applicationContext;
        mMessage = intent.getStringExtra(kMESSAGE);
        mUserName = intent.getStringExtra(kUSERNAME);
        mCurrentUserUid = intent.getStringExtra(kCURRENTUSERID);
        mChatRoomId = intent.getStringExtra(kCHATROOMID);
        mPost = intent.getParcelableExtra(kPOST);
        mChatMate = intent.getParcelableExtra(kUSER);

        Log.d("TAG", "GridRemoteViewsFactory: " + mChatRoomId);
    }

    @Override
    public void onCreate() {

    }

    //called on start and when notifyAppWidgetViewDataChanged is called
    @Override
    public void onDataSetChanged() {
        boolean cursorHasValidData = false;
        if (mCursor != null && mCursor.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }
        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }

        mCursor.moveToFirst();
        //mListPosts.clear();
        for (int i = 0; i < mCursor.getCount(); i++) {

            String description = mCursor.getString(mCursor.getColumnIndex(FreeganContract.FreegansEntry.COLUMN_POST_DESCRIPTION));
            String userName = mCursor.getString(mCursor.getColumnIndex(FreeganContract.FreegansEntry.COLUMN_POSTER_NAME));
            String postUserObjectId = mCursor.getString(mCursor.getColumnIndex(FreeganContract.FreegansEntry.COLUMN_POSTER_ID));
            String profileImgUrl = mCursor.getString(mCursor.getColumnIndex(FreeganContract.FreegansEntry.COLUMN_POSTER_PICTURE_PATH));
            String postDate = mCursor.getString(mCursor.getColumnIndex(FreeganContract.FreegansEntry.COLUMN_POST_DATE));
            String postId = mCursor.getString(mCursor.getColumnIndex(FreeganContract.FreegansEntry.COLUMN_FREEGAN_ID));

            String imageUrlString = mCursor.getString(mCursor.getColumnIndex(FreeganContract.FreegansEntry.COLUMN_POST_PICTURE_PATH));

            ArrayList<String> imageUrls = new ArrayList<>();
            JSONObject json = null;
            try {
                json = new JSONObject(imageUrlString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONArray items =  json.optJSONArray(JSONARRAYKEY);
            for (int j = 0; j < items.length(); j++) {
                String value = items.optString(j);
                imageUrls.add(value);
            }

            //mListPosts.add(new Post (postId, postUserObjectId, description, imageUrls, profileImgUrl, userName, postDate));

            mCursor.moveToNext();
        }
    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    /**
     * This method acts like the onBindViewHolder method in an Adapter
     *
     * @param position The current position of the item in the GridView to be displayed
     * @return The RemoteViews object to display for the provided postion
     */
    @Override
    public RemoteViews getViewAt(int position) {
        if (mCurrentUserUid == null) return null;

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.freegan_app_widget);


        views.setTextViewText(R.id.appwidget_user_name_text, mUserName);
        views.setTextViewText(R.id.appwidget_message_text, mMessage);

        // Fill in the onClick PendingIntent Template using the specific plant Id for each item individually
        Bundle extras = new Bundle();

        extras.putString(kCURRENTUSERID, mCurrentUserUid);
        extras.putString(kCHATROOMID, mChatRoomId);
        extras.putParcelable(kPOST, mPost);
        extras.putParcelable(kUSER, mChatMate);


        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        views.setOnClickFillInIntent(R.id.appwidget_message_text, fillInIntent);

        return views;

    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1; // Treat all items in the ListView the same
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}


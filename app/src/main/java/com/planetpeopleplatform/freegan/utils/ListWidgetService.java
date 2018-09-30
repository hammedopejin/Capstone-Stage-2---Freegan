package com.planetpeopleplatform.freegan.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;

import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kMESSAGE;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERNAME;

public class ListWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        Log.d("TAG", "onGetViewFactory:  Called!!!!!!");

        String message = intent.getStringExtra(kMESSAGE);
        String userName = intent.getStringExtra(kUSERNAME);
        String currentUserUid = intent.getStringExtra(kCURRENTUSERID);
        String chatRoomId = intent.getStringExtra(kCHATROOMID);
        Post post = intent.getParcelableExtra(kPOST);
        User chatMate = intent.getParcelableExtra(kUSER);

        //Log.d("TAG", "onGetViewFactory: " + message + ": " + "by " + userName);

        return new GridRemoteViewsFactory(this.getApplicationContext()
                ,
                message, userName, currentUserUid, chatRoomId, post,
                chatMate
        );
    }
}

class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context context;

    private String mMessage = null;
    private String mUserName = null;
    private String mCurrentUserUid = null;
    private String mChatRoomId = null;
    private Post mPost = null;
    private User mChatMate = null;

    GridRemoteViewsFactory(Context applicationContext
            , String message,
                           String userName, String currentUserUid,
                           String chatRoomId, Post post,
                           User chatMate
    ) {

        context = applicationContext;
        mMessage = message;
        mUserName = userName;
        mCurrentUserUid = currentUserUid;
        mChatRoomId = chatRoomId;
        mPost = post;
        mChatMate = chatMate;
    }

    @Override
    public void onCreate() {

    }

    //called on start and when notifyAppWidgetViewDataChanged is called
    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        if (mCurrentUserUid == null) return 0;
        return 1;
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

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.freegan_app_widget);


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


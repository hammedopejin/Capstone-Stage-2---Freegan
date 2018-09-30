package com.planetpeopleplatform.freegan.free;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.activity.MessageActivity;
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;
import com.planetpeopleplatform.freegan.utils.ListWidgetService;

import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kMESSAGE;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERNAME;

/**
 * Implementation of App Widget functionality.
 */
public class FreeganAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String userName, String message,
                                String currentUserUid,
                                String chatRoomId,
                                Post post, User chatMate) {

        // Construct the RemoteViews object
        RemoteViews views = getFreeganListRemoteView(context, userName, message,
                currentUserUid, chatRoomId, post, chatMate);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
    }

    public static void updateFreeganWidgets(Context context, AppWidgetManager appWidgetManager,
                                            int[] appWidgetIds,
                                            String userName, String message,
                                            String currentUserUid,
                                            String chatRoomId,
                                            Post post, User chatMate) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, userName, message,
                    currentUserUid, chatRoomId, post, chatMate);
        }
    }


    /**
     * Creates and returns the RemoteViews to be displayed in the GridView mode widget
     *
     * @param context The context
     * @return The RemoteViews for the GridView mode widget
     */
    private static RemoteViews getFreeganListRemoteView(Context context, String userName, String message,
                                                        String currentUserUid,
                                                        String chatRoomId,
                                                        Post post, User chatMate) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_list_view);
        // Set the ListWidgetService intent to act as the adapter for the ListView
        Intent intent = new Intent(context, ListWidgetService.class);

        intent.putExtra(kMESSAGE, message);
        intent.putExtra(kUSERNAME, userName);
        intent.putExtra(kCURRENTUSERID, currentUserUid);
        intent.putExtra(kCHATROOMID, chatRoomId);
        intent.putExtra(kPOST, post);
        intent.putExtra(kUSER, chatMate);

        views.setRemoteAdapter(R.id.widget_list_view, intent);
        // Set the MessageActivity intent to launch when clicked
        Intent appIntent = new Intent(context, MessageActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_list_view, appPendingIntent);
        // Handle empty message
        views.setEmptyView(R.id.widget_list_view, R.id.empty_view);

        Log.d("TAG", "getFreeganListRemoteView: " + message + " by : " + userName);

        return views;
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // Perform any action when one or more AppWidget instances have been deleted
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}


package com.planetpeopleplatform.freegan.free;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.activity.MessageActivity;
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;

import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;

/**
 * Implementation of App Widget functionality.
 */
public class FreeganAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String userName, String message,
                                String date,
                                String currentUserUid,
                                String chatRoomId,
                                Post post, User chatMate,
                                int counter) {


        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.freegan_app_widget);


        views.setTextViewText(R.id.appwidget_user_name_text, userName);
        views.setTextViewText(R.id.appwidget_message_text, message);
        views.setTextViewText(R.id.appwidget_date_text, date);
        String numberOfUnreadMessages;
        if (counter < 0) {
            counter = 0;
        }
        if (counter == 0) {
            numberOfUnreadMessages = context.getString(R.string.no_unread_message_string);
        } else if (counter == 1) {
            numberOfUnreadMessages = context.getString(R.string.one_unread_message_string);
        } else {
            numberOfUnreadMessages = String.valueOf(counter) + " " + context.getString(R.string.unread_messages_string);
        }
        views.setTextViewText(R.id.number_of_unread_messages, numberOfUnreadMessages);

        // Set the MessageActivity intent to launch when clicked
        Intent appIntent = new Intent(context, MessageActivity.class);

        appIntent.putExtra(kCURRENTUSERID, currentUserUid);
        appIntent.putExtra(kCHATROOMID, chatRoomId);
        appIntent.putExtra(kPOST, post);
        appIntent.putExtra(kUSER, chatMate);

        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.message_layout, appPendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.layout.freegan_app_widget);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
    }

    public static void updateFreeganWidgets(Context context, AppWidgetManager appWidgetManager,
                                            int[] appWidgetIds,
                                            String userName, String message,
                                            String date,
                                            String currentUserUid,
                                            String chatRoomId,
                                            Post post, User chatMate,
                                            int counter) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, userName, message,
                    date, currentUserUid, chatRoomId, post, chatMate, counter);
        }
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
package com.planetpeopleplatform.freegan.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FreeganDBHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "freegans.db";


        public FreeganDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {

            final String SQL_CREATE_FREEGAN_TABLE =

                    "CREATE TABLE " + FreeganContract.FreegansEntry.TABLE_NAME + " (" +
                            FreeganContract.FreegansEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            FreeganContract.FreegansEntry.COLUMN_FREEGAN_ID + " TEXT UNIQUE NOT NULL, " +
                            FreeganContract.FreegansEntry.COLUMN_POST_DESCRIPTION + " TEXT NOT NULL, " +
                            FreeganContract.FreegansEntry.COLUMN_POST_PICTURE_PATH + " TEXT NOT NULL, " +
                            FreeganContract.FreegansEntry.COLUMN_POSTER_NAME + " TEXT NOT NULL, " +
                            FreeganContract.FreegansEntry.COLUMN_POSTER_ID + " TEXT NOT NULL, " +
                            FreeganContract.FreegansEntry.COLUMN_POSTER_PICTURE_PATH + " TEXT NOT NULL, " +
                            FreeganContract.FreegansEntry.COLUMN_POST_DATE + " TEXT NOT NULL " +
                            "); ";
            sqLiteDatabase.execSQL(SQL_CREATE_FREEGAN_TABLE);

            final String SQL_CREATE_MESSAGE_TABLE =

                    "CREATE TABLE " + FreeganContract.MessagesEntry.TABLE_NAME + " (" +
                            FreeganContract.MessagesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            FreeganContract.MessagesEntry.COLUMN_MESSAGE_ID + " TEXT UNIQUE NOT NULL, " +
                            FreeganContract.MessagesEntry.COLUMN_MESSAGE + " TEXT NOT NULL, " +
                            FreeganContract.MessagesEntry.COLUMN_CURRENT_USER_ID + " TEXT NOT NULL, " +
                            FreeganContract.MessagesEntry.COLUMN_CHAT_ROOM_ID + " TEXT NOT NULL, " +
                            FreeganContract.MessagesEntry.COLUMN_USER_NAME + " TEXT NOT NULL, " +
                            FreeganContract.MessagesEntry.COLUMN_CHAT_MATE_ID + " TEXT NOT NULL " +
                            "); ";
            sqLiteDatabase.execSQL(SQL_CREATE_MESSAGE_TABLE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FreeganContract.FreegansEntry.TABLE_NAME);
            sqLiteDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                    FreeganContract.FreegansEntry.TABLE_NAME + "'");

            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FreeganContract.MessagesEntry.TABLE_NAME);
            sqLiteDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                    FreeganContract.MessagesEntry.TABLE_NAME + "'");


            onCreate(sqLiteDatabase);
        }

}

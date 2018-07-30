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
                            FreeganContract.FreegansEntry.COLUMN_FREEGAN_ID + " INTEGER UNIQUE , " +
                            FreeganContract.FreegansEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                            FreeganContract.FreegansEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                            FreeganContract.FreegansEntry.COLUMN_POST_DATE + " TEXT NOT NULL, " +
                            FreeganContract.FreegansEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL" +
                            "); ";
            sqLiteDatabase.execSQL(SQL_CREATE_FREEGAN_TABLE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FreeganContract.FreegansEntry.TABLE_NAME);
            sqLiteDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                    FreeganContract.FreegansEntry.TABLE_NAME + "'");
            onCreate(sqLiteDatabase);
        }

}

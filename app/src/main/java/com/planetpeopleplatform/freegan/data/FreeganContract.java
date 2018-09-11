package com.planetpeopleplatform.freegan.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class FreeganContract {


    public static final String CONTENT_AUTHORITY = "com.planetpeopleplatform.freegan";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String FREEGAN_PATH = "freegans";


    public static final class FreegansEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(FREEGAN_PATH)
                .build();

        // for building URIs on insertion
        public static Uri buildFreeganUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildFreeganUriWithFreeganId(String freegan_id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(freegan_id)
                    .build();
        }

        public static long getIdFromUri(Uri uri) {
            return ContentUris.parseId(uri);
        }

        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + FREEGAN_PATH;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + FREEGAN_PATH;

        public static final String TABLE_NAME = "freegans";

        public static final String COLUMN_POST_DESCRIPTION = "description";
        public static final String COLUMN_POST_PICTURE_PATH = "post_images_url_path";
        public static final String COLUMN_POSTER_NAME = "poster_name";
        public static final String COLUMN_POSTER_ID = "poster_id";
        public static final String COLUMN_POSTER_PICTURE_PATH = "poster_image_url_path";
        public static final String COLUMN_POST_DATE = "post_date";
        public static final String COLUMN_FREEGAN_ID = "post_id";
        public static final String _ID = "_id";

    }

}

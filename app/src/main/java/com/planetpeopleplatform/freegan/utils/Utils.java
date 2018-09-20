package com.planetpeopleplatform.freegan.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.model.Message;
import com.planetpeopleplatform.freegan.model.User;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static butterknife.internal.Utils.listOf;
import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kCOUNTER;
import static com.planetpeopleplatform.freegan.utils.Constants.kDATE;
import static com.planetpeopleplatform.freegan.utils.Constants.kLASTMESSAGE;
import static com.planetpeopleplatform.freegan.utils.Constants.kMEMBERS;
import static com.planetpeopleplatform.freegan.utils.Constants.kMESSAGE;
import static com.planetpeopleplatform.freegan.utils.Constants.kMESSAGEID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPRIVATE;
import static com.planetpeopleplatform.freegan.utils.Constants.kRECENT;
import static com.planetpeopleplatform.freegan.utils.Constants.kRECENTID;
import static com.planetpeopleplatform.freegan.utils.Constants.kSTATUS;
import static com.planetpeopleplatform.freegan.utils.Constants.kTYPE;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kWITHUSERUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kWITHUSERUSERNAME;

public class Utils {

    private static final String SCHEME_FILE = "file";
    private static final String SCHEME_CONTENT = "content";
    private static final String TAG = Utils.class.getSimpleName();

    public static String startChat(User user1, User user2, String postId) {

        String userId1 = user1.getObjectId();
        String userId2 = user2.getObjectId();

        String chatRoomId = "";

        int value = userId1.compareTo(userId2);

        if (value < 0) {
            chatRoomId = userId1 + userId2 + postId;
        } else {
            chatRoomId = userId2 + userId1 + postId;
        }

        List<String> members = listOf(userId1, userId2);

        createRecent( userId1,  chatRoomId,  members,  userId2,  user2.getUserName(), postId,  kPRIVATE);
        createRecent( userId2,  chatRoomId,  members,  userId1,  user1.getUserName(), postId,  kPRIVATE);


        return chatRoomId;
    }

    public static void updateChatStatus(HashMap<String, Object> message, String chatRoomId) {

        HashMap<String, Object> values = new HashMap<String, Object>();
        values.put(kSTATUS, Message.STATUS_READ);
        firebase.child(kMESSAGE).child(chatRoomId).child(((String)message.get(kMESSAGEID))).updateChildren(values);

    }

    public static void createRecent(final String userId, final String chatRoomId, final List<String> members,
                                    final String withUserUserId, final String withUserUsername,
                                    final String postId, final String type) {

        firebase.child(kRECENT).orderByChild(kCHATROOMID).equalTo(chatRoomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Boolean create = true;

                if (dataSnapshot.exists()) {
                    for (Object recent : ( (HashMap<String, Object>) dataSnapshot.getValue()).values() ){
                        HashMap<String, Object> currentRecent = (HashMap<String, Object>) recent;
                        if (currentRecent.get(kUSERID).equals(userId)){
                            create = false;
                        }

                        firebase.child(kRECENT).orderByChild(kCHATROOMID).equalTo((String)currentRecent.get(kCHATROOMID))
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }
                }
                if (create && !(userId.equals(withUserUserId))) {

                    createRecentItem(userId, chatRoomId, members, withUserUserId, withUserUsername, postId, type);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static void createRecentItem(String userId, String chatRoomId, List<String> members, String withUserUserId,
                                String withUserUsername, String postId, String type) {

        DatabaseReference reference = firebase.child(kRECENT).push();

        String recentId = reference.getKey();
        SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        String date = sfd.format(new Date());

        HashMap<String, Object> recent = new HashMap<String, Object>();
        recent.put(kRECENTID, recentId);
        recent.put(kUSERID, userId);
        recent.put(kCHATROOMID, chatRoomId);
        recent.put(kMEMBERS, members);
        recent.put(kWITHUSERUSERNAME, withUserUsername);
        recent.put(kWITHUSERUSERID, withUserUserId);
        recent.put(kLASTMESSAGE, "");
        recent.put(kCOUNTER, 0);
        recent.put(kDATE, date);
        recent.put(kPOSTID, postId);
        recent.put(kTYPE, type);

        reference.setValue(recent);
    }

    public static class DateHelper{
        final static String DF_SIMPLE_STRING = "yyyy-MM-dd hh:mm:ss a";
        public static ThreadLocal<DateFormat> DF_SIMPLE_FORMAT = new ThreadLocal<DateFormat>() {
            @Override
            protected DateFormat initialValue()  {
                return new SimpleDateFormat(DF_SIMPLE_STRING, Locale.US);
            }
        };
    }


    public static void updateRecents(String chatRoomId, final String lastMessage){

        firebase.child(kRECENT).orderByChild(kCHATROOMID).equalTo(chatRoomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    for (Object recent : (((HashMap<String, Object>)dataSnapshot.getValue()).values())) {

                        updateRecentItem((HashMap<String, Object>)recent, lastMessage);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private static void updateRecentItem(HashMap<String, Object> recent, String lastMessage) {

        SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a");
        String date = sfd.format(new Date());

        Long counter = (Long) recent.get(kCOUNTER);

        if (!(recent.get(kUSERID).equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))) {
            counter += 1;
        }

        HashMap<String, Object> newRecent = new HashMap<String, Object>();
        newRecent.put(kLASTMESSAGE, lastMessage);
        newRecent.put(kCOUNTER, counter);
        newRecent.put(kDATE, date);

        firebase.child(kRECENT).child(((String) recent.get(kRECENTID))).updateChildren(newRecent);

    }



    public static void clearRecentCounter(String chatRoomID) {

        firebase.child(kRECENT).orderByChild(kCHATROOMID).equalTo(chatRoomID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (Object recent : (((HashMap<String, Object>) dataSnapshot.getValue()).values())) {
                        HashMap<String, Object> currentRecent = (HashMap<String, Object>) recent;
                        if (currentRecent.get(kUSERID).equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                            clearRecentCounterItem(currentRecent);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static void clearRecentCounterItem(HashMap<String, Object> recent) {

        firebase.child(kRECENT).child(((String) recent.get(kRECENTID))).child(kCOUNTER).setValue(0);

    }

    public static File decodeFile(File f) {
        Bitmap b = null;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int IMAGE_MAX_SIZE = 1024;
        int scale = 1;
        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        try {
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileOutputStream out = new FileOutputStream(f);
            b.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return f;
    }

    /**
     * This is useful when an image is available in sdcard physically.
     *
     * @param uriPhoto
     * @return
     */
    public static String getPathFromUri(Uri uriPhoto, Context context) {
        if (uriPhoto == null)
            return null;

        if (SCHEME_FILE.equals(uriPhoto.getScheme())) {
            return uriPhoto.getPath();
        } else if (SCHEME_CONTENT.equals(uriPhoto.getScheme())) {
            final String[] filePathColumn = {MediaStore.MediaColumns.DATA,
                    MediaStore.MediaColumns.DISPLAY_NAME};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uriPhoto, filePathColumn, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int columnIndex = (uriPhoto.toString()
                            .startsWith("content://com.google.android.gallery3d")) ? cursor
                            .getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                            : cursor.getColumnIndex(MediaStore.MediaColumns.DATA);

                    if (columnIndex != -1) {
                        String filePath = cursor.getString(columnIndex);
                        if (!TextUtils.isEmpty(filePath)) {
                            return filePath;
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                // Nothing we can do
                Log.d(TAG, "IllegalArgumentException");
                e.printStackTrace();
            } catch (SecurityException ignored) {
                Log.d(TAG, "SecurityException");
                // Nothing we can do
                ignored.printStackTrace();
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
        return null;
    }

    public static String getPathFromGooglePhotosUri(Uri uriPhoto, Context context) {
        if (uriPhoto == null)
            return null;

        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uriPhoto, "r");
            FileDescriptor fd = pfd.getFileDescriptor();
            input = new FileInputStream(fd);

            String tempFilename = getTempFilename(context);
            output = new FileOutputStream(tempFilename);

            int read;
            byte[] bytes = new byte[4096];
            while ((read = input.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
            return tempFilename;
        } catch (IOException ignored) {
            // Nothing we can do
        } finally {
            closeSilently(input);
            closeSilently(output);
        }
        return null;
    }

    public static void closeSilently(Closeable c) {
        if (c == null)
            return;
        try {
            c.close();
        } catch (Throwable t) {
            // Do nothing
        }
    }

    private static String getTempFilename(Context context) throws IOException {
        File outputDir = context.getCacheDir();
        File outputFile = File.createTempFile("image", "tmp", outputDir);
        return outputFile.getAbsolutePath();
    }

    public static String SplitString(String email) {
        String[] split= email.split("@");
        return split[0];
    }

    public static File getOutputMediaFile(Context context){
        File mediaStorageDir = new File(context.getExternalFilesDir(
                Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name));

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_"+ timeStamp + ".jpg");
    }

    public static void closeOnError(CoordinatorLayout coordinatorLayout, Activity activity) {
        Snackbar.make(coordinatorLayout, R.string.err_data_not_available_string, Snackbar.LENGTH_SHORT).show();
        activity.finish();
    }

}

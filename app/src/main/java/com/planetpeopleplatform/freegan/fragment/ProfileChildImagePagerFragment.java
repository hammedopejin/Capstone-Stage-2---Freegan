package com.planetpeopleplatform.freegan.fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.activity.EditPostActivity;
import com.planetpeopleplatform.freegan.activity.MessageActivity;
import com.planetpeopleplatform.freegan.adapter.ProfileChildViewPagerAdaper;
import com.planetpeopleplatform.freegan.data.FreeganContract;
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;
import com.planetpeopleplatform.freegan.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.planetpeopleplatform.freegan.utils.Constants.JSONARRAYKEY;
import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kBLOCKEDUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kBUNDLE;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;

public class ProfileChildImagePagerFragment extends Fragment {

    private static final int EDIT_POST_REQUEST_CODE = 123;
    private static final String KEY_POST_RES = "com.planetpeopleplatform.freegan.key.postRes";
    private static final String KEY_POSER = "com.planetpeopleplatform.freegan.key.poster";
    private Post mPost = null;
    private User mPoster = null;

    private String mChatMateId;
    private String mChatRoomId = null;
    private User mCurrentUser = null;
    private String mCurrentUserUid = null;
    private FirebaseAuth mAuth;

    @BindView(R.id.nestedViewPager)
    fr.castorflex.android.verticalviewpager.VerticalViewPager mNestedViewPager;

    @BindView(R.id.text_view)
    TextView mTextView;

    @BindView(R.id.back_arrow)
    ImageButton mBackArrow;

    @BindView(R.id.favorite_image)
    ImageView mFavoriteImageButton;

    @BindView(R.id.poster_image_button)
    de.hdodenhof.circleimageview.CircleImageView mOptionImageButton;

    @BindView(R.id.contact_button_view)
    android.support.design.widget.FloatingActionButton mContactButtonView;

    public ProfileChildImagePagerFragment() {
    }

    public static ProfileChildImagePagerFragment newInstance(Post post, User poster) {
        ProfileChildImagePagerFragment fragment = new ProfileChildImagePagerFragment();
        Bundle argument = new Bundle();
        argument.putParcelable(KEY_POST_RES, post);
        argument.putParcelable(KEY_POSER, poster);
        fragment.setArguments(argument);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_pager, container, false);
        ButterKnife.bind(this, rootView);

        mAuth = FirebaseAuth.getInstance();

        Bundle arguments = getArguments();
        mPoster = arguments.getParcelable(KEY_POSER);
        mPost = arguments.getParcelable(KEY_POST_RES);
        String postDescription = mPost.getDescription();

        //        mOptionImageButton.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_share));
        mOptionImageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_settings_white_24dp));

        mNestedViewPager.setAdapter(new ProfileChildViewPagerAdaper(this, mPost));

        mTextView.setText(postDescription);
        mCurrentUserUid = mAuth.getCurrentUser().getUid();
        if (mPost.getPostUserObjectId().equals(mCurrentUserUid)) {
            mContactButtonView.setVisibility(View.GONE);
        }
        getCurrentUser(mCurrentUserUid);
        mOptionImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showUserSettingsPopup(view);
            }
        });

        mBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        final SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences(
                getString(R.string.com_planetpeopleplatform_freegan_preference_favorite_file_key), MODE_PRIVATE);

        final boolean[] mIsFavorited = { sharedPref.getBoolean(mPost.getPostId(), false) };
        if (mIsFavorited[0]){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mFavoriteImageButton.setImageDrawable(getActivity().getDrawable(android.R.drawable.star_big_on));
            } else {
                mFavoriteImageButton.setImageResource(android.R.drawable.star_big_on);
            }
        }

        mFavoriteImageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (mIsFavorited[0]){
                    Uri uri = FreeganContract.FreegansEntry.buildFreeganUriWithFreeganId(mPost.getPostId());
                    deleteData(uri);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mFavoriteImageButton.setImageDrawable(getActivity().getDrawable(android.R.drawable.star_big_off));
                    } else {
                        mFavoriteImageButton.setImageResource(android.R.drawable.star_big_off);
                    }
                    mIsFavorited[0] = false;
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean(mPost.getPostId(), mIsFavorited[0]);
                    editor.apply();
                }else {
                    insertData();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mFavoriteImageButton.setImageDrawable(getActivity().getDrawable(android.R.drawable.star_big_on));
                    } else {
                        mFavoriteImageButton.setImageResource(android.R.drawable.star_big_on);
                    }
                    mIsFavorited[0] = true;
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean(mPost.getPostId(), mIsFavorited[0]);
                    editor.apply();
                }
            }
        });


        mContactButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Intent intent = new Intent(getActivity(), MessageActivity.class);
                mChatMateId = mPost.getPostUserObjectId();


                            if (!(mPoster.getObjectId().equals(mCurrentUserUid))) {

                                if (mCurrentUser != null) {
                                    mChatRoomId = Utils.startChat(mCurrentUser, mPoster, mPost.getPostId());

                                    intent.putExtra(kCURRENTUSERID, mCurrentUserUid);
                                    intent.putExtra(kCHATROOMID, mChatRoomId);
                                    intent.putExtra(kPOST, mPost);
                                    intent.putExtra(kUSER, mPoster);

                                    startActivity(intent);
                                }
                            }
            }
        });

        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_POST_REQUEST_CODE && resultCode ==RESULT_OK){
            getActivity().onBackPressed();
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private void showUserSettingsPopup(View view) {

        PopupMenu popup = new PopupMenu(getContext(), view);
        if (mCurrentUserUid.equals(mPost.getPostUserObjectId())) {
            popup.inflate(R.menu.popup_post_settings);
        } else {
            if (mPoster.getBlockedUsersList().contains(mCurrentUserUid)) {
                popup.inflate(R.menu.popup_post_visitor_settings_unblock_option);
            } else {
                popup.inflate(R.menu.popup_post_visitor_settings);
            }
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_edit_post:
                        Intent editPostActivityIntent = new Intent(getContext(), EditPostActivity.class);
                        Bundle argument = new Bundle();
                        argument.putParcelable(kPOST, mPost);
                        argument.putParcelable(kCURRENTUSER, mCurrentUser);
                        editPostActivityIntent.putExtra(kBUNDLE, argument);
                        startActivityForResult(editPostActivityIntent, EDIT_POST_REQUEST_CODE);
                        return true;

                    case R.id.action_share_post:
                        Intent shareIntent = createShareForecastIntent();
                        startActivity(shareIntent);
                        return true;

                    case R.id.action_delete_post:
                        deleteWarning(0);
                        return true;

                    case R.id.action_report_user:
                        return true;

                    case R.id.action_block_user:
                        ArrayList<String> blockedList = mPoster.getBlockedUsersList();

                        blockedList.add(mCurrentUserUid);
                        mPoster.addBlockedUser(mCurrentUserUid);
                        HashMap<String, Object> newBlockedUser = new HashMap<String, Object>();
                        newBlockedUser.put(kBLOCKEDUSER, blockedList);
                        firebase.child(kUSER).child(mPoster.getObjectId()).updateChildren(newBlockedUser);

                        return true;

                    case R.id.action_unblock_user:
                        ArrayList<String> blockedLists = mPoster.getBlockedUsersList();
                        int blockedPosition = blockedLists.indexOf(mCurrentUserUid);
                        mPoster.removeBlockedUser(mCurrentUserUid);
                        firebase.child(kUSER).child(mPoster.getObjectId()).child(kBLOCKEDUSER).child(String.valueOf(blockedPosition)).removeValue();

                        return true;

                    default:
                        return false;
                }
            }
        });
        popup.show();


    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = ShareCompat.IntentBuilder.from(getActivity())
                .setType("text/plain")
                .setText("" + "")
                .getIntent();
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        return shareIntent;
    }

    private void deleteWarning(int position) {

        DeleteDialogFragment deleteDialog = DeleteDialogFragment.newInstance(getString(R.string.attention_alert_title),
                mPost.getPostId(), kPOST, position);
        deleteDialog.show(getActivity().getSupportFragmentManager(), getString(R.string.delete_fragment_alert_tag));

    }

    private User getCurrentUser(String currentUserUid) {
        firebase.child(kUSER).child(currentUserUid).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mCurrentUser = new User((HashMap<String, Object>) dataSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return mCurrentUser;
    }

    // insert data into database
    private void insertData(){
        ContentValues movieValues = new ContentValues();

        movieValues.put(FreeganContract.FreegansEntry.COLUMN_POST_DESCRIPTION, mPost.getDescription());
        movieValues.put(FreeganContract.FreegansEntry.COLUMN_FREEGAN_ID, mPost.getPostId());

        JSONObject json = new JSONObject();
        try {
            json.put(JSONARRAYKEY, new JSONArray(mPost.getImageUrl()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String postImageUrls = json.toString();

        movieValues.put(FreeganContract.FreegansEntry.COLUMN_POST_PICTURE_PATH, postImageUrls);
        movieValues.put(FreeganContract.FreegansEntry.COLUMN_POSTER_NAME, mPost.getUserName());
        movieValues.put(FreeganContract.FreegansEntry.COLUMN_POSTER_ID, mPost.getPostUserObjectId());
        movieValues.put(FreeganContract.FreegansEntry.COLUMN_POSTER_PICTURE_PATH, mPost.getProfileImgUrl());
        movieValues.put(FreeganContract.FreegansEntry.COLUMN_POST_DATE, mPost.getPostDate());

        getActivity().getApplicationContext().getContentResolver().insert(FreeganContract.FreegansEntry.CONTENT_URI,
                movieValues);
    }

    // delete data from database
    private void deleteData(Uri uri){
        getActivity().getApplicationContext().getContentResolver().delete(uri,
                null, null);
    }
}
package com.planetpeopleplatform.freegan.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.planetpeopleplatform.freegan.model.Post;
import com.planetpeopleplatform.freegan.model.User;
import com.planetpeopleplatform.freegan.utils.Utils;
import com.planetpeopleplatform.freegan.utils.VerticalViewPager;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kBUNDLE;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHATROOMID;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSER;
import static com.planetpeopleplatform.freegan.utils.Constants.kCURRENTUSERID;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kUSER;

public class ProflieChildImagePagerFragment extends Fragment {

    private static final int EDIT_POST_REQUEST_CODE = 123;
    private static final String KEY_POST_RES = "com.planetpeopleplatform.freegan.key.postRes";
    private Post mPost = null;

    private String mChatMateId;
    private String mChatRoomId = null;
    private User mCurrentUser = null;
    private String mCurrentUserUid = null;
    private FirebaseAuth mAuth;

    @BindView(R.id.nestedViewPager)
    VerticalViewPager nestedViewPager;

    @BindView(R.id.text_view)
    TextView mTextView;

    @BindView(R.id.back_arrow)
    ImageButton mBackArrow;

    @BindView(R.id.poster_image_button)
    de.hdodenhof.circleimageview.CircleImageView mOptionImageButton;

    @BindView(R.id.contact_button_view)
    android.support.design.widget.FloatingActionButton mContactButtonView;

    public ProflieChildImagePagerFragment() {
    }

    public static ProflieChildImagePagerFragment newInstance(Post post) {
        ProflieChildImagePagerFragment fragment = new ProflieChildImagePagerFragment();
        Bundle argument = new Bundle();
        argument.putParcelable(KEY_POST_RES, post);
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
        mPost = arguments.getParcelable(KEY_POST_RES);
        String postDescription = mPost.getDescription();

        //        mOptionImageButton.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_menu_share));
        mOptionImageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_settings_white_24dp));

        nestedViewPager.setAdapter(new ProfileChildViewPagerAdaper(getFragmentManager(), mPost));




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

        mContactButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Intent intent = new Intent(getActivity(), MessageActivity.class);
                mChatMateId = mPost.getPostUserObjectId();

                firebase.child(kUSER).child(mChatMateId).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            User chatMate = new User((HashMap<String, Object>) dataSnapshot.getValue());
                            if (!(chatMate.getObjectId().equals(mCurrentUserUid))) {

                                if (mCurrentUser != null) {
                                    mChatRoomId = Utils.startChat(mCurrentUser, chatMate, mPost.getPostId());

                                    intent.putExtra(kCURRENTUSERID, mCurrentUserUid);
                                    intent.putExtra(kCHATROOMID, mChatRoomId);
                                    intent.putExtra(kPOST, mPost);
                                    intent.putExtra(kUSER, chatMate);

                                    startActivity(intent);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
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
            popup.inflate(R.menu.popup_post_visitor_settings);
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
}
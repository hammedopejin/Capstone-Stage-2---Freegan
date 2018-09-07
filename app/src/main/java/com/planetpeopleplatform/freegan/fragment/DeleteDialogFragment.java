package com.planetpeopleplatform.freegan.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.planetpeopleplatform.freegan.R;
import com.planetpeopleplatform.freegan.model.Post;

import java.util.HashMap;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHILDREF;
import static com.planetpeopleplatform.freegan.utils.Constants.kKEY;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSITION;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOST;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSTLOCATION;
import static com.planetpeopleplatform.freegan.utils.Constants.kTITLE;
import static com.planetpeopleplatform.freegan.utils.Constants.storage;

public class DeleteDialogFragment extends DialogFragment {

    OnCompleteListener mListener = null;



    public interface OnCompleteListener {
        void onComplete(int position);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnCompleteListener) activity;
        } catch (ClassCastException exception) {
            throw new ClassCastException(activity.toString() + getString(R.string.class_cast_exception_string));
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = getArguments().getString(kTITLE);
        final String key = getArguments().getString(kKEY);
        final String childRef = getArguments().getString(kCHILDREF);
        final int position = getArguments().getInt(kPOSITION);

        //For Post and post location delete
        if(childRef.equals(kPOST)) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(title);
            alertDialogBuilder.setMessage(R.string.delete_message);
            alertDialogBuilder.setPositiveButton(R.string.capital_yes_string, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    firebase.child(kPOST).child(key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Post post = new Post((HashMap<String, Object>) dataSnapshot.getValue());
                                for (int i = 0; i < post.getImageUrl().size(); i++) {
                                    StorageReference toDelete = storage.getReferenceFromUrl(post.getImageUrl().get(i));
                                    toDelete.delete();
                                }
                                firebase.child(kPOST).child(key).removeValue();
                                firebase.child(kPOSTLOCATION).child(key).removeValue();
                                mListener.onComplete(position);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });

            alertDialogBuilder.setNegativeButton(R.string.capital_no_string, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            return alertDialogBuilder.create();

        } else { //For Recent chat delete
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle(title);
            alertDialogBuilder.setMessage(R.string.delete_message);
            alertDialogBuilder.setPositiveButton(R.string.capital_yes_string, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    firebase.child(childRef).child(key).removeValue();
                    mListener.onComplete(position);
                }
            });

            alertDialogBuilder.setNegativeButton(R.string.capital_no_string, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            return alertDialogBuilder.create();
        }

    }


    public static DeleteDialogFragment newInstance(String title, String key, String childRef, int position) {
        DeleteDialogFragment fragment = new DeleteDialogFragment();
        Bundle argument = new Bundle();
        argument.putString(kTITLE, title);
        argument.putString(kKEY, key);
        argument.putString(kCHILDREF, childRef);
        argument.putInt(kPOSITION, position);
        fragment.setArguments(argument);
        return fragment;
    }


}
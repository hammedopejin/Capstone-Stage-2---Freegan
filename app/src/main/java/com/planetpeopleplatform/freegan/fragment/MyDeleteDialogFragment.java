package com.planetpeopleplatform.freegan.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;


import static com.planetpeopleplatform.freegan.utils.Constants.firebase;

public class MyDeleteDialogFragment extends DialogFragment {

    OnCompleteListener mListener = null;



    public interface OnCompleteListener {
        void onComplete(int position);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (OnCompleteListener) activity;
        } catch (ClassCastException exception) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = getArguments().getString("title");
        final String key = getArguments().getString("key");
        final String childRef = getArguments().getString("childRef");
        final int position = getArguments().getInt("position");


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage("Are you sure you want to delete?");
        alertDialogBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                firebase.child(childRef).child(key).removeValue();
                mListener.onComplete(position);
            }
        });

        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        return alertDialogBuilder.create();
    }


    public static MyDeleteDialogFragment newInstance(String title, String key, String childRef, int position) {
        MyDeleteDialogFragment fragment = new MyDeleteDialogFragment();
        Bundle argument = new Bundle();
        argument.putString("title", title);
        argument.putString("key", key);
        argument.putString("childRef", childRef);
        argument.putInt("position", position);
        fragment.setArguments(argument);
        return fragment;
    }


}

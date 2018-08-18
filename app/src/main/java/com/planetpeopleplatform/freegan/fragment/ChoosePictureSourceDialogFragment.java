package com.planetpeopleplatform.freegan.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.planetpeopleplatform.freegan.R;


public class ChoosePictureSourceDialogFragment extends DialogFragment {

    ChoosePictureSourceDialogFragment.OnCompleteListener mListener = null;



    public interface OnCompleteListener {
        void onComplete(int source);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mListener = (ChoosePictureSourceDialogFragment.OnCompleteListener) activity;
        } catch (ClassCastException exception) {
            throw new ClassCastException(activity.toString() + getString(R.string.class_cast_exception_string));
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {



        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("Complete action using");
        alertDialogBuilder.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.onComplete(1);
            }
        });

        alertDialogBuilder.setNegativeButton("Galerry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.onComplete(2);
            }
        });

        return alertDialogBuilder.create();
    }
}

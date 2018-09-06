package com.planetpeopleplatform.freegan.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.planetpeopleplatform.freegan.R;

import static com.planetpeopleplatform.freegan.utils.Constants.firebase;
import static com.planetpeopleplatform.freegan.utils.Constants.kCHILDREF;
import static com.planetpeopleplatform.freegan.utils.Constants.kFLAG;
import static com.planetpeopleplatform.freegan.utils.Constants.kIMAGEURL;
import static com.planetpeopleplatform.freegan.utils.Constants.kKEY;
import static com.planetpeopleplatform.freegan.utils.Constants.kPOSITION;


public class ChoosePictureSourceDialogFragment extends DialogFragment {

    private static final int FLAG_DELETE = 1000;

    private String mPosition;
    private String mKey;
    private String mChildRef;
    private int mFlag;

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

        if(!(getArguments() == null)) {
            mPosition = getArguments().getString(kPOSITION);
            mKey = getArguments().getString(kKEY);
            mChildRef = getArguments().getString(kCHILDREF);
            mFlag = getArguments().getInt(kFLAG);
        }



        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage(R.string.alert_complete_action_using_string);
        alertDialogBuilder.setPositiveButton(R.string.camera_string, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.onComplete(1);
            }
        });

        alertDialogBuilder.setNegativeButton(R.string.gallery_string, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.onComplete(2);
            }
        });

        if (mFlag == FLAG_DELETE){
            alertDialogBuilder.setNeutralButton(R.string.delete_string, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    firebase.child(mChildRef).child(mKey).child(kIMAGEURL).child(mPosition).removeValue();
                    mListener.onComplete(3);
                }
            });
        }

        return alertDialogBuilder.create();
    }

    public static ChoosePictureSourceDialogFragment newInstance (String position, String key, String childRef, int flag) {
        ChoosePictureSourceDialogFragment fragment = new ChoosePictureSourceDialogFragment();
        Bundle argument = new Bundle();
        argument.putString(kPOSITION, position);
        argument.putString(kKEY, key);
        argument.putString(kCHILDREF, childRef);
        argument.putInt(kFLAG, flag);
        fragment.setArguments(argument);
        return fragment;
    }
}

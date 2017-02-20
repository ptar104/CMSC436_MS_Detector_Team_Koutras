package com.capstone.petros.cmsc436msdetector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;


public class InstructionFragment extends DialogFragment {

    public InstructionFragment() {
        // Required empty public constructor

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String handedness = getArguments().getString(BallActivity.HANDEDNESS_KEY);

        builder.setMessage("Use your "+handedness+" hand to keep the ball in the center of the screen")
                .setPositiveButton(R.string.continue_string, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //show the view

                        //testing functionality
                        BallActivity ballActivity = (BallActivity) getActivity();
                        ballActivity.startPrepTimer();

                        // getActivity().findViewById(R.id.ballView).setBackgroundColor(Color.RED);
                    }
                })
                .setNegativeButton(R.string.back_string, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().finish();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}

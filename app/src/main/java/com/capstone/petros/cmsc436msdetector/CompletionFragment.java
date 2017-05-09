package com.capstone.petros.cmsc436msdetector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CompletionFragment extends DialogFragment {

    public static final String MESSAGE_KEY = "MESSAGE";

    public CompletionFragment() {
        // Required empty public constructor

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String message = getArguments().getString(MESSAGE_KEY);

        builder.setMessage(message)
                .setPositiveButton(R.string.continue_string, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // getActivity().findViewById(R.id.ballView).setBackgroundColor(Color.RED);
                        getActivity().finish();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}

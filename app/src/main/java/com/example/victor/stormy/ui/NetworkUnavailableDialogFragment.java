package com.example.victor.stormy.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

import com.example.victor.stormy.R;


public class NetworkUnavailableDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.network_unavailable_dialog_fragment_title)
                .setMessage(R.string.network_unavailable_dialog_fragment_message)
                .setPositiveButton(
                        R.string.network_unavailable_dialog_fragment_postive_button, null);

        AlertDialog dialog = builder.create();
        return  dialog;
    }
}

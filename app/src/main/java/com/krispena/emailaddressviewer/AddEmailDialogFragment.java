package com.krispena.emailaddressviewer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class AddEmailDialogFragment extends DialogFragment {
    /**
     * Listener to post new email address back to activity
     */
    public static interface OnEmailEnteredListener {
        /**
         * Callback to notify listener an email address has been entered
         * @param email email address from DialogFragment
         */
        public void onEmailEntered(String email);
    }

    private OnEmailEnteredListener listener;

    public static AddEmailDialogFragment newInstance(OnEmailEnteredListener listener) {
        AddEmailDialogFragment fragment = new AddEmailDialogFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_add_email, null);
        final EditText editText = (EditText) view.findViewById(R.id.editText);
        return new AlertDialog.Builder(getActivity()).setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(listener != null) {
                            listener.onEmailEntered(editText.getText().toString());
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //allow dialog to cancel
                    }
                }).create();
    }
}

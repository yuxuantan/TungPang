package com.shrmn.is416.tumpang;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.SetOptions;

public class FirstRunDialog extends DialogFragment {
    private static final String TAG = "FRDialog";
    User user = MyApplication.user;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_firstrun, null);
        final EditText inputTelegramUsername = dialogView.findViewById(R.id.input_telegram_username);
        final EditText inputName = dialogView.findViewById(R.id.input_name);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        user.setName(inputName.getText().toString());
                        user.setTelegramUsername(inputTelegramUsername.getText().toString());

                        Log.d(TAG, "Confirmed first run dialog. Confirmed credentials: telegram_username=" + user.getTelegramUsername() + "; name=" + user.getName());
                        updateUser();
                    }
                })
                .setNegativeButton("Skip", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FirstRunDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    private void updateUser() {
        MyApplication.db.collection(MyApplication.USERS_COLLECTION).document(user.getIdentifier())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User updated.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating user", e);
                    }
                });
    }
}

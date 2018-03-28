package com.shrmn.is416.tumpang;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.shrmn.is416.tumpang.utilities.VariableChangeListener;

import static com.shrmn.is416.tumpang.MyApplication.firstRunVariable;
import static com.shrmn.is416.tumpang.MyApplication.user;

public class MainActivity extends AppCompatActivity implements FirstRunDialog.FirstRunDialogListener {

    private static final String TAG = "Main";
    private TextView labelWelcome;

    public void showFirstRunDialog() {
        DialogFragment dialog = new FirstRunDialog();
        dialog.show(getSupportFragmentManager(), "FirstRunDialog");

        // Do not automatically open intent
        // openLoginIntent();
    }

    public void openLoginIntent() {
        Log.i(TAG, "openLoginIntent: Opening login URL " + MyApplication.loginUrl);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(MyApplication.loginUrl));
        startActivity(i);
    }

    public void setNegativeWelcomeText() {
        labelWelcome.setText("We require you to login to Telegram for full app functionality.");
        labelWelcome.setTypeface(labelWelcome.getTypeface(), Typeface.ITALIC);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Log.d(TAG, "onDialogPositiveClick: Refreshing user from Firestore");
        MyApplication.getUserFromFirestore();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Log.d(TAG, "onDialogNegativeClick: User cancelled");
        setNegativeWelcomeText();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        labelWelcome = findViewById(R.id.label_welcome);

        firstRunVariable.setVariableChangeListener(new VariableChangeListener() {
            @Override
            public void onVariableChanged(Object... variableThatHasChanged) {
                boolean isFirstRun = (Boolean) variableThatHasChanged[0];
                if (isFirstRun) {
                    Log.d(TAG, "onVariableChanged: Detected first-run, presenting FirstRunDialog");
                    showFirstRunDialog();
                } else {
                    String displayName = user.displayName();
                    if (displayName == null) {
                        setNegativeWelcomeText();
                        showFirstRunDialog();
                    } else {
                        labelWelcome.setText("Welcome, " + displayName + "!");
                    }
                    Log.d(TAG, "onVariableChanged: Not a first run.");


                    // Subscribe to user's identifier as topic name
                    String identifier = user.getIdentifier();
                    if (identifier != null) {
                        FirebaseMessaging.getInstance().subscribeToTopic(identifier);
                        Log.i("Subscribed to topic: ", identifier);
                    }
                }
            }
        });

    }

    public void newOrderRequest(View view) {
        Intent ptg = new Intent(this, NewOrderRequestActivity.class);
        startActivity(ptg);
    }

    public void fulfilOrderOrderRequest(View view) {
        Intent ptg = new Intent(this, FulfilOrdersActivity.class);
        startActivity(ptg);
    }

    public void onClickLoginButton(View view) {
        Log.d(TAG, "onClickLoginButton: Pressed.");
        openLoginIntent();
    }
}

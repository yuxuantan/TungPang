package com.shrmn.is416.tumpang;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.shrmn.is416.tumpang.utilities.FCMRestClient;
import com.shrmn.is416.tumpang.utilities.VariableChangeListener;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import com.google.gson.Gson;

import cz.msebera.android.httpclient.Header;

import static com.shrmn.is416.tumpang.MyApplication.user;

public class MainActivity extends AppCompatActivity implements FirstRunDialog.FirstRunDialogListener {

    private static final String TAG = "Main";
    private TextView labelWelcome;

    public void showFirstRunDialog() {
        DialogFragment dialog = new FirstRunDialog();
        dialog.show(getSupportFragmentManager(), "FirstRunDialog");
    }

    public void setNegativeWelcomeText() {
        labelWelcome.setText("We require your Telegram username for full app functionality.");
        labelWelcome.setTypeface(labelWelcome.getTypeface(), Typeface.ITALIC);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        setNegativeWelcomeText();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//
        labelWelcome = findViewById(R.id.label_welcome);

        MyApplication.firstRunVariable.setVariableChangeListener(new VariableChangeListener() {
            @Override
            public void onVariableChanged(Object... variableThatHasChanged) {
                boolean isFirstRun = (Boolean) variableThatHasChanged[0];
                if(isFirstRun) {
                    Log.d(TAG, "onVariableChanged: Detected first-run, presenting FirstRunDialog");
                    showFirstRunDialog();
                } else {

                    String displayName = user.displayName();
                    if(displayName == null) {
                        setNegativeWelcomeText();
                    } else {
                        labelWelcome.setText("Welcome, " + displayName + "!");
                    }
                    Log.d(TAG, "onVariableChanged: Not a first run.");


                    // Subscibe to user's identifier as topic name
                    String identifier = user.getIdentifier();
                    if(identifier!=null){
                        FirebaseMessaging.getInstance().subscribeToTopic(identifier);
                        Log.e("Subscribed to topic:", identifier);
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
        sendNotif("1bc4bdb4-3574-4b5b-82e1-2d6d0997fadb");
        Intent ptg = new Intent(this, FulfilOrdersActivity.class);
        startActivity(ptg);
    }

    public void sendNotif(String targetIdentifier){
        // Test HTTP Request
        JsonObject innerObj = new JsonObject();
        innerObj.addProperty("message", "This is a Firebase Cloud Messaging Topic Message!");

        RequestParams params = new RequestParams();
        params.put("to", "/topics/"+ targetIdentifier);
        params.put("data", innerObj);
        FCMRestClient client = new FCMRestClient();
        client.post("", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e("status", "Success: " + responseBody.toString());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("status", "Failure:" +error);
            }
        });
    }


}

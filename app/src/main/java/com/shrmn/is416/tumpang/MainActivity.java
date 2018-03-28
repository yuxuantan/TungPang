package com.shrmn.is416.tumpang;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.shrmn.is416.tumpang.utilities.FCMRestClient;
import com.shrmn.is416.tumpang.utilities.MyJavascriptInterface;
import com.shrmn.is416.tumpang.utilities.VariableChangeListener;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

import static com.shrmn.is416.tumpang.MyApplication.user;

public class MainActivity extends AppCompatActivity implements FirstRunDialog.FirstRunDialogListener {

    private static final String TAG = "Main";
    private TextView labelWelcome;

    public void showFirstRunDialog() {
//        DialogFragment dialog = new FirstRunDialog();
//        dialog.show(getSupportFragmentManager(), "FirstRunDialog");
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Title here");

        WebView wv = new WebView(this);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setSupportZoom(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.loadUrl("javascript:HtmlViewer.showHTML" +
                        "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
        });
        wv.loadUrl("https://us-central1-tumpang-app.cloudfunctions.net/telegramLogin?doc_id" + MyApplication.user.getIdentifier());
        wv.addJavascriptInterface(new MyJavascriptInterface(this), "HtmlViewer");

        alert.setView(wv);
        alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alert.show();
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
        Intent ptg = new Intent(this, FulfilOrdersActivity.class);
        startActivity(ptg);
    }




}

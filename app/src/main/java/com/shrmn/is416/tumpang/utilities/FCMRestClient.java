package com.shrmn.is416.tumpang.utilities;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.loopj.android.http.*;
import com.shrmn.is416.tumpang.MyApplication;

import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by Xuan on 20/3/18.
 */

public class FCMRestClient {
    private static final String BASE_URL = "https://fcm.googleapis.com/fcm/send";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(Context context, String url, StringEntity entity, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", "key=AIzaSyAkZWt36cz9XNPZtRuy58E9I6zDWxXpkl8");

        client.post(context, getAbsoluteUrl(url), entity, "application/json", responseHandler );
//        client.post(getAbsoluteUrl(url), params, responseHandler);
        Log.e("entity", entity.toString());
    }


    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}

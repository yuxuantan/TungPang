package com.shrmn.is416.tumpang.utilities;

import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.loopj.android.http.*;

/**
 * Created by Xuan on 20/3/18.
 */

public class FCMRestClient {
    private static final String BASE_URL = "https://fcm.googleapis.com/fcm/send";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {

        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", "key=AIzaSyAkZWt36cz9XNPZtRuy58E9I6zDWxXpkl8");
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}

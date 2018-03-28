package com.shrmn.is416.tumpang.utilities;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

public class MyJavascriptInterface {
    private Context ctx;
    private final String TAG = "Main";

    public MyJavascriptInterface(Context ctx) {
        this.ctx = ctx;
    }

    @JavascriptInterface
    public void showHTML(String html) {
        Log.d(TAG, "showHTML: " + html);
    }

    @JavascriptInterface
    public void showJSON(String html) {
        Log.d(TAG, "showJSON: " + html);
    }
}

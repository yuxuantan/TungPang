package com.shrmn.is416.tumpang.utilities;

import android.content.Context;
import android.webkit.JavascriptInterface;

public class MyJavascriptInterface {
    private Context ctx;

    public MyJavascriptInterface(Context ctx) {
        this.ctx = ctx;
    }

    @JavascriptInterface
    public void showHTML(String html) {
        System.out.println(html);
    }
}

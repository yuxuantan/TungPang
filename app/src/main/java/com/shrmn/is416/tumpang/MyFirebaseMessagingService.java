package com.shrmn.is416.tumpang;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage == null)
            return;

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            showNotification("Notif: ",remoteMessage.getNotification().getBody());

//            Handler handler = new Handler(Looper.getMainLooper());
//
//            handler.post(new Runnable() {
//
//                @Override
//                public void run() {
//                    Toast.makeText(MyFirebaseMessagingService.this.getApplicationContext(), "New Notification Received",Toast.LENGTH_LONG).show();
//                }
//            });
        }
        else if(remoteMessage.getData()!=null){
            Log.e(TAG, "Notification Body: " + remoteMessage.getData().toString());
            showNotification("Notif: ",remoteMessage.getData().toString());
        }


    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.man_only)
                .setContentTitle(title)
                .setContentText(message)
//                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();

        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }


}

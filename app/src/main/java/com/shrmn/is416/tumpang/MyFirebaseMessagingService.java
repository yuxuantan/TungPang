package com.shrmn.is416.tumpang;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

//import com.firebase.jobdispatcher.FirebaseJobDispatcher;
//import com.firebase.jobdispatcher.GooglePlayDriver;
//import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            SendNotification(remoteMessage);



            if (/* Check if data needs to be processed by long running job */ true) {

            } else {
                // Handle message within 10 seconds
//                handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

    }
//    @RequiresApi(api = Build.VERSION_CODES.O)
    void SendNotification(RemoteMessage messageBody)
    {

//        // ANDROID VERSION MUST BE 26!
        String CHANNEL_ID = "channel_01";
//        CharSequence name = "hi1";
//        int importance = NotificationManager.IMPORTANCE_HIGH;
//        NotificationChannel nChannel = new NotificationChannel(channel_id, name, importance);
//        Intent notif = new Intent(this, MainActivity.class);
//        PendingIntent pending = PendingIntent.getActivity(this, 0, notif, PendingIntent.FLAG_CANCEL_CURRENT); // 2nd one will override 1st notif
//        Notification notification = new Notification.Builder(this, channel_id)
//                .setContentTitle("New Msg")
//                .setContentText(messageBody.toString())
//                .setSmallIcon(R.drawable.man_only)
//                .setChannelId(channel_id)
//                .setContentIntent(pending)
//                .setWhen(System.currentTimeMillis())
//                .setShowWhen(true)
//                .build();
//        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        nManager.createNotificationChannel(nChannel);
//        nManager.notify(100, notification);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.man_only)
                .setContentTitle("Notif")
                .setContentText(messageBody.toString())
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(100, mBuilder.build());

    }
}

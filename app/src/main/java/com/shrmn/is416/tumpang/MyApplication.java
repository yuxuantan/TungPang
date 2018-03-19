package com.shrmn.is416.tumpang;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.estimote.coresdk.common.config.EstimoteSDK;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.service.BeaconManager;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.shrmn.is416.tumpang.utilities.FirstRunVariable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MyApplication extends Application {

    /** User-set constants **/
    // Debugging tag
    private static final String TAG = "app";
    // Firestore collection for Users
    public static final String USERS_COLLECTION = "users";
    // Whether to show first-run dialog as long as no telegram_username is given
    private static final boolean TREAT_NULL_TELEGRAM_USERNAME_AS_FIRST_RUN = true;
    // SharedPreferences key to store installation Unique ID
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    /** Runtime-set variables **/
    // Holds the Estimote Beacon Manager
    public static BeaconManager beaconManager;
    // Holds the uniqueID retrieved from SharedPreferences (or created on first-run)
    private static String uniqueID = null;
    // Holds the User object for the current user
    public static User user;
    // Holds the Firestore database instance. Used from a static context
    public static FirebaseFirestore db;
    // Holds utility FirstRunVariable to take event handler for MainActivity
    public static FirstRunVariable firstRunVariable;
    // Holds retrieved locations
    public static HashMap<String, Location> locations;

    // Extracted from https://medium.com/@ssaurel/how-to-retrieve-an-unique-id-to-identify-android-devices-6f99fd5369eb
    public synchronized static String id(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.commit();
            }
        }
        return uniqueID;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Obtain Unique ID and save into this.uniqueID
        id(getApplicationContext());
        Log.d(TAG, "User Unique ID: " + uniqueID);

        firstRunVariable = new FirstRunVariable();
        locations = new HashMap<>();

        // Abstracted these parts into subroutines
        initialiseBeaconSubsystem();
        // Also loads the current User's record into this.User
        initialiseFirebaseDatabase();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private void initialiseBeaconSubsystem() {

        beaconManager = new BeaconManager(getApplicationContext());

        // !!!! As long as there is any beacon currently in range, will not detect exit/entry event
        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener() {
            @Override
            public void onEnteredRegion(BeaconRegion region, List<Beacon> beacons) {
                showNotification(
                        "ENTERED!",
                        "Just entered the range of a beacon");
                Log.d("Beacons", beacons.toString());

            }
            @Override
            public void onExitedRegion(BeaconRegion region) {
                showNotification(
                        "Exited!",
                        "Just exited the range of a beacon");
            }

        });
        beaconManager.setBackgroundScanPeriod(6000, 0);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new BeaconRegion(
                        "monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null));

            }
        });
    }

    private void initialiseFirebaseDatabase() {
        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection(USERS_COLLECTION).document(uniqueID);

        docRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                user = document.toObject(User.class);
                                Log.d(TAG, "Existing user; Loaded: " + user);

                                // Force a token retrieval if there is no token currently associated with this user
                                if(user.getFirebaseInstanceID() == null) {
                                    String token = FirebaseInstanceId.getInstance().getToken();
                                    user.setFirebaseInstanceID(token);
                                    Log.d(TAG, "onComplete: Token Retrieved = " + token);
                                    user.save();
                                }
                            } else {
                                Log.d(TAG, "No User document with ID " + uniqueID + "; Adding to database.");
                                addUserToDB();
                            }
                            firstRunVariable.setFirstRun(user == null || (user.getTelegramUsername() == null && TREAT_NULL_TELEGRAM_USERNAME_AS_FIRST_RUN));
                        }
                    }
                });
    }

    private void addUserToDB() {
        db.collection(USERS_COLLECTION).document(uniqueID)
                .set(new User(uniqueID))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

}

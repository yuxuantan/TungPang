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

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.shrmn.is416.tumpang.utilities.FirstRunVariable;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MyApplication extends Application {

    /**
     * User-set constants
     **/
    // Debugging tag
    private static final String TAG = "app";
    // Firestore collection for Users
    public static final String USERS_COLLECTION = "users";
    // Whether to show first-run dialog as long as no telegram_username is given
    public static final boolean TREAT_NULL_TELEGRAM_USERNAME_AS_FIRST_RUN = true;
    // SharedPreferences key to store installation Unique ID
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";
    // RemoteConfig key to store telegram api Bot key
    private static final String TELEGRAM_API_KEY_KEY = "telegram_api_key";
    // RemoteConfig cache expiration in seconds
    private static final long REMOTE_CONFIG_CACHE_EXPIRATION = 3600;

    /**
     * Runtime-set variables
     **/
    // Holds the Estimote Beacon Manager
    public static BeaconManager beaconManager;
    // Holds the uniqueID retrieved from SharedPreferences (or created on first-run)
    public static String uniqueID = null;
    // Holds the User object for the current user
    public static User user;
    // Holds the Firestore database instance. Used from a static context
    public static FirebaseFirestore db;
    // Holds utility FirstRunVariable to take event handler for MainActivity
    public static FirstRunVariable firstRunVariable;
    // Holds retrieved locations
    public static HashMap<String, Location> locations;
    public static ArrayList<String> locationIDs;
    public static ArrayList<String> locationNames;
    // Holds the currently being-built order item
    public static Order pendingOrder;
    public static String loginUrl;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    public static String telegramApiKey;

    public static RequestQueue mRequestQueue;

//    private BroadcastReceiver mRegistrationBroadcastReceiver;

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

        loginUrl = "https://us-central1-tumpang-app.cloudfunctions.net/telegramLogin?doc_id=" + uniqueID;
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
        locationIDs = new ArrayList<>();
        locationNames = new ArrayList<>();

        // Abstracted these parts into subroutines
        initialiseBeaconSubsystem();
        // Prepare Firebase Firestore Database
        initialiseFirestore();
        // Load the current User's record into this.User
        getUserFromFirestore();
        retrieveLocations();
        initialiseRemoteConfig();
        initialiseVolleyQueue();
    }

    private void initialiseVolleyQueue() {
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);

        // Start the queue
        mRequestQueue.start();
    }

    private void initialiseRemoteConfig() {
        Log.d(TAG, "initialiseRemoteConfig");
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        mFirebaseRemoteConfig.fetch(REMOTE_CONFIG_CACHE_EXPIRATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: mFirebaseRemoteConfig.fetch()");
                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                            Log.e(TAG, "onComplete:mFirebaseRemoteConfig.fetch() ", task.getException());
                        }
                        telegramApiKey = mFirebaseRemoteConfig.getString(TELEGRAM_API_KEY_KEY);
                        Log.d(TAG, "onComplete: Updated telegramApiKey = " + telegramApiKey);
                    }
                });
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
                .setAutoCancel(false)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
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
                Log.d(TAG, "Beacons: " + beacons.toString());
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

    private void initialiseFirestore() {
        db = FirebaseFirestore.getInstance();
    }

    public static void getUserFromFirestore() {
        Log.d(TAG, "getUserFromFirestore: Reading User from Firestore");
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
                                if (user.getFirebaseInstanceID() == null) {
                                    String token = FirebaseInstanceId.getInstance().getToken();
                                    user.setFirebaseInstanceID(token);
                                    Log.d(TAG, "onComplete: Token Retrieved = " + token);
                                    user.save();
                                }
                            } else {
                                Log.d(TAG, "User - No User document with ID " + uniqueID + "; Adding to database.");
                                addUserToDB();
                            }
                            firstRunVariable.setFirstRun(user == null || (user.getTelegramUsername() == null && TREAT_NULL_TELEGRAM_USERNAME_AS_FIRST_RUN));
                        }
                    }
                });
    }

    private static void addUserToDB() {
        db.collection(USERS_COLLECTION).document(uniqueID)
                .set(new User(uniqueID))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User - DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "User - Error writing document", e);
                    }
                });
    }

    public static void retrieveLocations() {
        if (!locations.isEmpty()) {
            return;
        }

        MyApplication.db.collection("locations").get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String locationID = document.getId();
                                Map<String, Object> data = document.getData();
                                ArrayList<MenuItem> items = new ArrayList<>();

                                Log.d(TAG, document.getId() + " => " + data);

                                Map<String, Object> menuObj = (Map<String, Object>) data.get("menu");
                                ArrayList<Map<String, Object>> foodObj = (ArrayList<Map<String, Object>>) menuObj.get("food");
                                ArrayList<Map<String, Object>> drinksObj = (ArrayList<Map<String, Object>>) menuObj.get("drinks");

                                String path = document.getReference().getPath();

                                for (int i = 0; i < foodObj.size(); i++) {
                                    Map<String, Object> foodItem = foodObj.get(i);
                                    items.add(new Food(path + "/food[" + i + "]", foodItem.get("name").toString(), Double.parseDouble(foodItem.get("unitPrice").toString())));
                                }

                                for (int i = 0; i < drinksObj.size(); i++) {
                                    Map<String, Object> drinkItem = drinksObj.get(i);
                                    items.add(new Drink(path + "/drinks[" + i + "]", drinkItem.get("name").toString(), Double.parseDouble(drinkItem.get("unitPrice").toString())));
                                }

                                MyApplication.locations.put(locationID,
                                        new Location(
                                                locationID,
                                                data.get("name").toString(),
                                                data.get("address").toString(),
                                                data.get("beaconMacAddress").toString(),
                                                new Menu(items)
                                        )
                                );
                                locationIDs.add(locationID);
                                locationNames.add(data.get("name").toString());
                            }
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                }
        );
    }

    // Simple method to send notification to a given chat.
    public static void sendTelegramNotification(String message, int chatId) {
        Log.i(TAG, "sendTelegramNotification: " + chatId + ": " + message);
        String url = "https://api.telegram.org/bot" + telegramApiKey + "/sendMessage";
        try {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, url, new JSONObject("{\"chat_id\": " + chatId + ", \"parse_mode\": \"Markdown\", \"text\": " + escapeForJson(message, true) + "}"), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(TAG, "onResponse: " + response.toString());
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                            Log.e(TAG, "onErrorResponse: Volley Error", error);
                        }
                    });
            mRequestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            Log.e(TAG, "sendTelegramNotification: Error in converting JSON", e);
        }
    }

    // Overloaded method to send telegram notification to the CURRENT user via the chatbot
    public static void sendTelegramNotification(String message) {
        sendTelegramNotification(message, Integer.parseInt(user.getTelegram().get("id").toString()));
    }

    // Overloaded method to send telegram notification to a given User Identifier via the chatbot
    public static void sendTelegramNotification(final String message, final String userIdentifier) {
        actionOnUser(userIdentifier, new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        User otherUser = document.toObject(User.class);
                        Map<String, Object> telegram = otherUser.getTelegram();

                        // Force a token retrieval if there is no token currently associated with this user
                        if (telegram == null || telegram.isEmpty()) {
                            Log.e(TAG, "sendTelegramNotification: Unable to send to " + userIdentifier + " as he/she has not logged into Telegram");
                        } else {
                            sendTelegramNotification(message, Integer.parseInt(telegram.get("id").toString()));
                        }
                    } else {
                        Log.e(TAG, "sendTelegramNotification: Unable to send to " + userIdentifier + " as he/she has not logged into Telegram");
                    }
                }
            }
        });
    }

    public static void actionOnUser(final String userIdentifier, OnCompleteListener<DocumentSnapshot> onComplete) {
        DocumentReference docRef = db.collection(USERS_COLLECTION).document(userIdentifier);
        docRef.get().addOnCompleteListener(onComplete);
    }

    public static String escapeForJson( String value, boolean quote )
    {
        StringBuilder builder = new StringBuilder();
        if( quote )
            builder.append( "\"" );
        for( char c : value.toCharArray() )
        {
            if( c == '\'' )
                builder.append( "\\'" );
            else if ( c == '\"' )
                builder.append( "\\\"" );
            else if( c == '\r' )
                builder.append( "\\r" );
            else if( c == '\n' )
                builder.append( "\\n" );
            else if( c == '\t' )
                builder.append( "\\t" );
            else if( c < 32 || c >= 127 )
                builder.append( String.format( "\\u%04x", (int)c ) );
            else
                builder.append( c );
        }
        if( quote )
            builder.append( "\"" );
        return builder.toString();
    }

}

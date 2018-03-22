package com.shrmn.is416.tumpang;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.shrmn.is416.tumpang.utilities.FCMRestClient;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

public class FulfilOrdersActivity extends AppCompatActivity {

    //LIST OF ORDERS BEFORE LOCATION FILTER - For now is static list
    private static List<Order> allOrders;

    private static List<Order> unassignedOrders;
    private static List<String> unassignedRestaurantNames = new ArrayList<>();

    public BeaconManager beaconManager;
    public BeaconRegion region;

    private static final String TAG = "FulfilOrderRequest";

    static {

        // HARD CODED PORTION
        // ** Add only orders that have status==0 ie. "unassigned" into the lists
        allOrders = new ArrayList<>();
//
//        allOrders.add(new Order(1,
//                101,
//                "Chicken Rice",
//                3.0,
//                0.3,
//                20,
//                13,
//                24,
//                6722,
//                37991,
//                "SIS GSR 2-3",
//                0,
//                "Koufu"));
//
//        allOrders.add(new Order(1,
//                102,
//                "Hokkien Mee",
//                3.0,
//                0.3,
//                20,
//                13,
//                24,
//                16466,
//                55391,
//                "SIS GSR 2-3",
//                0,
//                "Waterloo"));

        unassignedOrders = new ArrayList<>();
        unassignedRestaurantNames = new ArrayList<>();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fulfil_orders);

        MyApplication.retrieveLocations();
        retrieveOrders();

        // Initialize List View
        ListView lv = (ListView) findViewById(R.id.orders_list);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,//context
                R.layout.mylistlayout,//custom_layout
                R.id.mylistitem,// referring the widget (TextView) where the items to be displayed
                unassignedRestaurantNames//items
        );

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                // Pause refresh
                beaconManager.stopRanging(region);

                String selectedOrderTitle = parent.getItemAtPosition(position).toString();
                Order selectedOrder = unassignedOrders.get(position);

                AlertDialog alertDialog = new AlertDialog.Builder(FulfilOrdersActivity.this).create();
                alertDialog.setTitle(selectedOrderTitle);
                alertDialog.setMessage(selectedOrder.toString());
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                // Start Ranging after dismiss
                                beaconManager.startRanging(region);
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Accept Job",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // ACCEPT JOB!!
                                // Remove from list
                                unassignedRestaurantNames.remove(position);
                                unassignedOrders.remove(position);
                                adapter.notifyDataSetChanged();

                                // Send notification to customer with Certain UserID - temp send to self
                                if (MyApplication.user != null && MyApplication.user.getIdentifier() != null) {
                                    sendNotif(MyApplication.user.getIdentifier());
                                }
                                // Remove from actual DB using API - after API is set up

                                //Start ranging after done
                                beaconManager.startRanging(region);
                            }
                        });
                alertDialog.show();
                alertDialog.setCanceledOnTouchOutside(false);


            }
        });

//        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener() {
//            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
//            @Override
//            public void onEnteredRegion(BeaconRegion region, List<Beacon> beacons) {
//                // Update list
//                Log.d("Status", "enter: " + beacons.toString());
//                adapter.clear();
//                refreshList(beacons);
//                adapter.notifyDataSetChanged();
//
//            }
//
//            @Override
//            public void onExitedRegion(BeaconRegion region) {
//                // Update List  + beacons.get(0).getMajor() + " Minor - " + beacons.get(0).getMinor()
//                Log.d("Status", "Exit");
//                adapter.clear();
//                refreshList(new ArrayList<Beacon>());
//                adapter.notifyDataSetChanged();
//
//            }
//        });


        beaconManager = new BeaconManager(getApplicationContext());
        region = new BeaconRegion("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    // TODO: update the UI here
                    Log.d("Nearest beacon", "Nearest places: " + nearestBeacon.toString());
                    Log.d("All beacons", list.toString());
                    // update list
                    // Once detected, stop ranging, until 10 seconds later - adaptive duty cycling - only triggered when enter/exit events
                }
                refreshList(list);
                startDutyCyclingCd();
                adapter.notifyDataSetChanged();
            }
        });

    }

    private void sendNotif(String identifier) {
        // Test HTTP Request
        JsonObject finalObj = new JsonObject();
        finalObj.addProperty("to", "/topics/" + identifier);

        JsonObject msgObj = new JsonObject();
        msgObj.addProperty("message", "This is a Firebase Cloud Messaging Topic Message!");
        finalObj.add("data", msgObj);

        StringEntity entity = null;
        try {
            entity = new StringEntity(finalObj.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        FCMRestClient client = new FCMRestClient();
        client.post(FulfilOrdersActivity.this, "", entity, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e("status", "Success:");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("status", "Failure:" + error);
            }
        });
    }


    private void refreshList(List<Beacon> beacons) {

        //CONNECT TO DB, pull list of unassigned and set here!! unassignedOrders = ??, Filter again
//        Log.d("Refreshed", "" + allOrders.size());
        unassignedOrders.clear();
        for (Order o : allOrders) {
            for (Beacon beacon : beacons) {
//                if (beacon.getMajor() == o.getbeaconIDMajor() && beacon.getMinor() == o.getBeaconIDMinor()) {
//                    unassignedOrders.add(o);
//                }
            }
        }
        unassignedRestaurantNames.clear();

        for (Order o : unassignedOrders) {
//            unassignedRestaurantNames.add(o.getRestaurantName());
        }
        Log.d("Refreshed", unassignedRestaurantNames.toString());

    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.disconnect();

        super.onPause();
    }

    public void finishActivity(View view) {
        beaconManager.disconnect();
        finish();
    }

    public void startDutyCyclingCd() {
        beaconManager.stopRanging(region);
        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                beaconManager.startRanging(region);
            }
        }.start();

    }


    private void retrieveOrders() {
        MyApplication.db.collection("orders").whereEqualTo("status", 0).get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
//                                String locationID = document.getId();
                                Map<String, Object> data = document.getData();

//                                ArrayList<Drink> drinks = new ArrayList<>();
//                                ArrayList<Food> food = new ArrayList<>();
//                                ArrayList<MenuItem> items = new ArrayList<>();

//                                Log.e(TAG, document.getId() + " => " + data);
                                allOrders.clear();
                                // ArrayList of HashMaps, 1 for each item. Key(item and qty)
                                ArrayList<Map<String, String>> orderMenuItemsObj = (ArrayList<Map<String, String>>) data.get("menuItems");

                                // Menu Item includes more details of the menu like name. what is stored in DB is links
                                /**Can we push list of drinks into static list?**/
                                HashMap<MenuItem, Integer> menuItems = new HashMap<>();
                                for (Map<String, String> menuItem : orderMenuItemsObj) {

                                    // Change item string reference (menuItem.get("item")) to locations table into a menu item object, and put in menuitems hashmap
                                    String[] references = menuItem.get("item").split("/");
                                    Log.e("ref", Arrays.toString(references));

                                    String locationID = references[1];
                                    String tmp[] = references[2].split("\\[|\\]");
                                    Log.d(TAG, "onComplete: " + Arrays.toString(tmp));

                                    Location location = MyApplication.locations.get(locationID);
                                    MenuItem item = null;

                                    if(tmp[0].equals("food")) {
//                                        item = location.getMenu().getFood().get(Integer.parseInt(tmp[1]));
                                    } else if(tmp[0].equals("drinks")) {
//                                        item = location.getMenu().getFood().get(Integer.parseInt(tmp[1]));
                                    }

//                                    menuItems.put(item, Integer.parseInt(menuItem.get("qty")));
                                }

//                                        menuItems.put(,menuItem);
//                                    }
//                                    Log.e("Order: ", object.toString());
//                                    allOrders.add(new Order(object.));

//                                Map<String, Object> menuObj = (Map<String, Object>) data.get("menu");
//                                ArrayList<Map<String, Object>> foodObj = (ArrayList<Map<String, Object>>) menuObj.get("food");
//                                ArrayList<Map<String, Object>> drinksObj = (ArrayList<Map<String, Object>>) menuObj.get("drinks");
//
//                                String path = document.getReference().getPath();
//
//                                for (int i = 0; i < foodObj.size(); i++) {
//                                    Map<String, Object> foodItem = foodObj.get(i);
//                                    food.add(new Food(path + "/food[" + i + "]", foodItem.get("name").toString(), Double.parseDouble(foodItem.get("unitPrice").toString())));
//                                    items.add(new Food(path + "/food[" + i + "]", foodItem.get("name").toString(), Double.parseDouble(foodItem.get("unitPrice").toString())));
//                                }
//
//                                for (int i = 0; i < drinksObj.size(); i++) {
//                                    Map<String, Object> drinkItem = drinksObj.get(i);
//                                    drinks.add(new Drink(path + "/drinks[" + i + "]", drinkItem.get("name").toString(), Double.parseDouble(drinkItem.get("unitPrice").toString())));
//                                    items.add(new Drink(path + "/drinks[" + i + "]", drinkItem.get("name").toString(), Double.parseDouble(drinkItem.get("unitPrice").toString())));
//                                }
//
//
//                                MyApplication.locations.put(locationID,
//                                        new Location(
//                                                locationID,
//                                                data.get("name").toString(),
//                                                data.get("address").toString(),
//                                                123,
//                                                456,
//                                                new Menu(items)
//                                        )
//                                );
//                                locationIDs.add(locationID);
//                                locationNames.add(data.get("name").toString());
                            }
//                            Log.d(TAG, "retrieveLocations: " + MyApplication.locations.get("tea-party"));
//                            setAdapterContents();
                        } else {
//                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                }
        );
    }


    public void goToFulfilAcceptedOrders(View view) {
        Intent fulfilAcceptedOrders = new Intent(this, FulfilAcceptedOrdersActivity.class);
        startActivity(fulfilAcceptedOrders);
    }
}

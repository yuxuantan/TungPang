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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
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
    private static List<Order> allUnassignedOrders = new ArrayList<>();

    private static List<Order> allUnassignedOrdersInRange = new ArrayList<>();
    private static List<String> unassignedRestaurantNamesInRange = new ArrayList<>();

    public BeaconManager beaconManager;
    public BeaconRegion region;

    private static final String TAG = "FulfilOrderRequest";
    public static FirebaseFirestore db;

    static {



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fulfil_orders);

        retrieveOrders();

        // Testing Stubb Data
        // unassignedRestaurantNamesInRange.add("Testing Subway");
        // unassignedRestaurantNamesInRange.add("Testing Tea Party");
        // unassignedRestaurantNamesInRange.add("Testing Food Republic");

        HashMap<MenuItem, Integer> menuHash = new HashMap<>();
        Order testingOrder =
                new Order(
                        "TestingStringId",
                        "StringLocationID",
                        "TestingStringLocationName",
                        10.0,
                        Long.parseLong("15"),
                        "DeliverManUserId",
                        "TestingCustomerUserId",
                        menuHash,
                        "DeliveryLocation",
                        1);
        MenuItem item1 = new Food("FakePath","Testing 1", 20.0);
        MenuItem item2 = new Food("FakePath","Testing 2", 30.0);
        MenuItem item3 = new Food("FakePath","Testing 3", 40.0);
        MenuItem item4 = new Food("FakePath","Testing 4", 50.0);

        testingOrder.addMenuItem(item1,5);
        testingOrder.addMenuItem(item2, 10);
        testingOrder.addMenuItem(item3, 15);
        testingOrder.addMenuItem(item4, 20);

        allUnassignedOrdersInRange.add(testingOrder);

        // Initialize List View
        ListView lv = (ListView) findViewById(R.id.orders_list);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,//context
                R.layout.mylistlayout,//custom_layout
                R.id.UnAssignedRestaurantNames,// referring the widget (TextView) where the items to be displayed
                unassignedRestaurantNamesInRange//items
        );

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                // Pause refresh
                beaconManager.stopRanging(region);

                String selectedOrderTitle = parent.getItemAtPosition(position).toString();
                Order selectedOrder = allUnassignedOrdersInRange.get(position);

                // Go to Order Details activity
                Intent it = new Intent(FulfilOrdersActivity.this, OrderDetailsActivity.class);
                it.putExtra("selectedOrder", selectedOrder);
                startActivity(it);


//                AlertDialog alertDialog = new AlertDialog.Builder(FulfilOrdersActivity.this).create();
//                alertDialog.setTitle(selectedOrderTitle);
//                alertDialog.setMessage(selectedOrder.toString());
//                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                                // Start Ranging after dismiss
//                                beaconManager.startRanging(region);
//                            }
//                        });
//                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Accept Job",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                // ACCEPT JOB!! Remove from list
//                                unassignedRestaurantNames.remove(position);
//                                allUnassignedOrders.remove(position);
//                                adapter.notifyDataSetChanged();
//
//                                // Send notification to customer with Certain UserID - temp send to self
//                                if (MyApplication.user != null && MyApplication.user.getIdentifier() != null) {
//                                    sendNotif(MyApplication.user.getIdentifier());
//                                }
//                                //Start ranging after done
//                                beaconManager.startRanging(region);
//                            }
//                        });
//                alertDialog.show();
//                alertDialog.setCanceledOnTouchOutside(false);


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

        // Every second will get called if there is beacons nearby. startDutyCyclingCd will pause it for 10 secs
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


    private void refreshList(List<Beacon> beacons) {

        //CONNECT TO DB, pull list of unassigned and set here!! unassignedOrders = ??, Filter again
        retrieveOrders();
        allUnassignedOrdersInRange.clear();
        for (Order o : allUnassignedOrders) {
            for (Beacon beacon : beacons) {
                Log.e("BEACON DETECTED", beacon.getMacAddress().toString());
                // Only show orders matching macadd
                if(beacon.getMacAddress().toString().equals("["+MyApplication.locations.get(o.getLocationID()).getBeaconMacAddress()+"]")){
                    // Only show orders that are not created by me
                    if(!o.getCustomerUserID().equals(MyApplication.user.getIdentifier())){
                        allUnassignedOrdersInRange.add(o);
//                        Log.e("TEST", o.getCustomerUserID()+"||"+MyApplication.user.getIdentifier());
                    }

                }
            }
        }
        unassignedRestaurantNamesInRange.clear();

        for (Order o : allUnassignedOrdersInRange) {
            unassignedRestaurantNamesInRange.add(o.getLocationName());
        }
        Log.d("Restaurant Names", unassignedRestaurantNamesInRange.toString());



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
                            allUnassignedOrders.clear();

                            // For each entry ie. order
                            for (DocumentSnapshot document : task.getResult()) {
                                String orderId = document.getId();
                                Map<String, Object> data = document.getData();

//                                Log.e(TAG, document.getId() + " => " + data);

                                // ArrayList of HashMaps, 1 for each item. Key(item and qty)
//                                ArrayList<Map<String, String>> dbOrderMenuItems = (ArrayList<Map<String, String>>) data.get("menuItems");
                                ArrayList<Object> dbOrderMenuItems = (ArrayList<Object>)data.get("menuItems");
                                String locID = (String) data.get("locationID").toString().split("/")[2];
//                                Log.e("locID", locID);
                                Location location = null;
                                String locationName = "";
                                if(locID!=null){
                                    location = MyApplication.locations.get(locID);
                                    if(location!=null)
                                        locationName = location.getName();
                                }



                                // Menu Item includes more details of the menu like name. what is stored in DB is links
                                /**Can we push list of drinks into static list?**/
                                HashMap<MenuItem, Integer> menuItems = new HashMap<>();


                                for (Object dbMenuItem : dbOrderMenuItems) {
                                    long qty = ((Map<String, Long>)dbMenuItem).get("qty");
                                    // Change item string reference (menuItem.get("item")) to locations table into a menu item object, and put in menuitems hashmap
                                    String[] references = ((Map<String, String>)dbMenuItem).get("item").split("/");
//                                    Log.e("ref", Arrays.toString(references));
                                    // eg. get "Food[0]" --> (Food , 0)
                                    String tmp[] = references[2].split("\\[|\\]");
//                                    String type = tmp[0];
//                                    Log.e("Location", location.toString());
                                    MenuItem item = null;
                                    if(location!=null)
                                        item = location.getMenu().getItems().get(Integer.parseInt(tmp[1]));
//                                        Log.e("qty" , qty+"");
                                    menuItems.put(item, (int)qty);
                                }

                                // Actually this will never happen, since if filter order = unassigned, est time delivery WILL be null
                                if(data.get("estimatedTimeOfDelivery")!=null && data.get("deliveryManUserID")==null){
                                    allUnassignedOrders.add(
                                            new Order(
                                                    orderId,
                                                    locID,
                                                    locationName,
                                                    Double.parseDouble(data.get("tipAmount").toString()),
                                                    Long.parseLong(data.get("estimatedTimeOfDelivery").toString()),
                                                    data.get("deliveryManUserID").toString(),
                                                    data.get("customerUserID").toString(),
                                                    menuItems,
                                                    data.get("deliveryLocation").toString(),
                                                    Integer.parseInt(data.get("status").toString())
                                            )
                                    );
                                }
                                else{
                                    allUnassignedOrders.add(
                                            new Order(
                                                    orderId,
                                                    locID,
                                                    locationName,
                                                    Double.parseDouble(data.get("tipAmount").toString()),
                                                    0,
                                                    "",
                                                    data.get("customerUserID").toString(),
                                                    menuItems,
                                                    data.get("deliveryLocation").toString(),
                                                    Integer.parseInt(data.get("status").toString())
                                            )
                                    );
                                }

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                }
        );
    }
    //Update Order's status and ETA
    private void updateStatus(String status, double ETA, Order order){
        Map<String, Object> data = new HashMap<>();
        data.put("customerUserID",order.getCustomerUserID());
        data.put("deliveryLocation",order.getDeliveryLocation());
        data.put("locationID", order.getLocationID());
        data.put("menuItems",order.getMenuItems());
        data.put("status", status);
        data.put("ETA", ETA);
        db.collection("orders").document(order.getCustomerUserID()).set(data, SetOptions.merge());

    }


    public void goToFulfilAcceptedOrders(View view) {
        Intent fulfilAcceptedOrders = new Intent(this, FulfilAcceptedOrdersActivity.class);
        startActivity(fulfilAcceptedOrders);
    }
}

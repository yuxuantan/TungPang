package com.shrmn.is416.tumpang;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FulfilOrdersActivity extends AppCompatActivity {

    //LIST OF ORDERS BEFORE LOCATION FILTER - For now is static list
    private static List<Order> allUnassignedOrders;
    private static List<Order> allUnassignedOrdersInRange;

    public BeaconManager beaconManager;
    public BeaconRegion region;

    private static final String TAG = "FulfilOrderRequest";
    private FulfilOrderItemAdapter adapter;
    private ListView fulfilled_OrderListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fulfil_orders);

        allUnassignedOrders = new ArrayList<>();
        allUnassignedOrdersInRange = new ArrayList<>();

        Log.d(TAG, "DOES IT REACH ONCREATE !!!!!!");
        retrieveOrders();

        // Initialize List View
        fulfilled_OrderListView = findViewById(R.id.fulfil_orders_listView);
        adapter = new FulfilOrderItemAdapter(this, 0, allUnassignedOrdersInRange);

        fulfilled_OrderListView.setAdapter(adapter);
        fulfilled_OrderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                // Pause refresh
                beaconManager.stopRanging(region);
                Order selectedOrder = allUnassignedOrdersInRange.get(position);

                // Go to Order Details activity
                Intent it = new Intent(FulfilOrdersActivity.this, OrderDetailsActivity.class);
                it.putExtra("selectedOrder", selectedOrder);
                startActivity(it);
            }
        });

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
            }
        });

        // Send notification regarding number of new orders

    }


    private void refreshList(List<Beacon> beacons) {
        //CONNECT TO DB, pull list of unassigned and set here!! unassignedOrders = ??, Filter again
        retrieveOrders();
        allUnassignedOrdersInRange.clear();
        for (Order o : allUnassignedOrders) {
            for (Beacon beacon : beacons) {
                Log.e("BEACON DETECTED", beacon.getMacAddress().toString());
                // Only show orders matching macadd
                if (beacon.getMacAddress().toString().equals("[" + MyApplication.locations.get(o.getLocationID()).getBeaconMacAddress() + "]")) {
                    // Only show orders that are not created by me
                    if (!o.getCustomerUserID().equals(MyApplication.user.getIdentifier())) {
                        allUnassignedOrdersInRange.add(o);
//                        Log.e("TEST", o.getCustomerUserID()+"||"+MyApplication.user.getIdentifier());
                    }

                }
            }
        }

        adapter.notifyDataSetChanged();
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
        MyApplication.db.collection("orders")
                .whereEqualTo("status", 0)
                .get()
                .addOnCompleteListener(
                        new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    allUnassignedOrders.clear();

                                    // For each entry ie. order
                                    for (DocumentSnapshot document : task.getResult()) {
                                        String orderId = document.getId();
                                        Map<String, Object> data = document.getData();

                                        // ArrayList of HashMaps, 1 for each item. Key(item and qty)
                                        ArrayList<Object> dbOrderMenuItems = (ArrayList<Object>) data.get("menuItems");
                                        String locID = data.get("locationID").toString().split("/")[2];
                                        Location location = null;
                                        String locationName = "";
                                        if (locID != null) {
                                            location = MyApplication.locations.get(locID);
                                            if (location != null)
                                                locationName = location.getName();
                                        }

                                        // Menu Item includes more details of the menu like name. what is stored in DB is links
                                        HashMap<MenuItem, Integer> menuItems = new HashMap<>();
                                        for (Object dbMenuItem : dbOrderMenuItems) {
                                            long qty = ((Map<String, Long>) dbMenuItem).get("qty");
                                            // Change item string reference (menuItem.get("item")) to locations table into a menu item object, and put in menuitems hashmap
                                            String[] references = ((Map<String, String>) dbMenuItem).get("item").split("/");
                                            String tmp[] = references[2].split("\\[|\\]");
                                            if (location != null) {
                                                menuItems.put(location.getMenu().getItems().get(Integer.parseInt(tmp[1])), (int) qty);
                                            }
                                        }

                                        // Actually this will never happen, since if filter order = unassigned, est time delivery WILL be null
                                        if (data.get("estimatedTimeOfDelivery") != null && data.get("deliveryManUserID") == null) {
                                            allUnassignedOrders.add(
                                                    new Order(
                                                            orderId,
                                                            locID,
                                                            locationName,
                                                            location,
                                                            Double.parseDouble(data.get("tipAmount").toString()),
                                                            Long.parseLong(data.get("estimatedTimeOfDelivery").toString()),
                                                            data.get("deliveryManUserID").toString(),
                                                            data.get("customerUserID").toString(),
                                                            menuItems,
                                                            data.get("deliveryLocation").toString(),
                                                            Integer.parseInt(data.get("status").toString())
                                                    )
                                            );
                                        } else {
                                            allUnassignedOrders.add(
                                                    new Order(
                                                            orderId,
                                                            locID,
                                                            locationName,
                                                            location,
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

    public void goToFulfilAcceptedOrders(View view) {
        Intent fulfilAcceptedOrders = new Intent(this, FulfilAcceptedOrdersActivity.class);
        startActivity(fulfilAcceptedOrders);
    }
}

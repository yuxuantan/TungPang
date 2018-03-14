package com.shrmn.is416.tumpang;

import android.content.DialogInterface;
import android.os.Build;
import android.os.CountDownTimer;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FulfilOrdersActivity extends AppCompatActivity {

    //LIST OF ORDERS BEFORE LOCATION FILTER - For now is static list
    private static List<Order> allOrders;

    private static List<Order> unassignedOrders;
    private static List<String> unassignedRestaurantNames = new ArrayList<>();

    public BeaconManager beaconManager;
    public BeaconRegion region;

    static {

        // HARD CODED PORTION
        // ** Add only orders that have status==0 ie. "unassigned" into the lists
        allOrders = new ArrayList<>();

        allOrders.add(new Order(1,
                101,
                "Chicken Rice",
                3.0,
                0.3,
                20,
                13,
                24,
                6722,
                37991,
                "SIS GSR 2-3",
                0,
                "Koufu"));

        allOrders.add(new Order(1,
                102,
                "Hokkien Mee",
                3.0,
                0.3,
                20,
                13,
                24,
                16466,
                55391,
                "SIS GSR 2-3",
                0,
                "Waterloo"));

        unassignedOrders = new ArrayList<>();
        unassignedRestaurantNames = new ArrayList<>();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fulfil_orders);


//************************ LIST VIEW SET


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
                                // Remove from actual DB using API - after API is set up

                                //Start ranging after done
                                beaconManager.startRanging(region);
                            }
                        });
                alertDialog.show();
                alertDialog.setCanceledOnTouchOutside(false);


            }
        });
        //***************************


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


        beaconManager= new BeaconManager(getApplicationContext());
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


    private void refreshList(List<Beacon> beacons) {

        //CONNECT TO DB, pull list of unassigned and set here!! unassignedOrders = ??, Filter again
        Log.d("Refreshed", "" + allOrders.size());
        unassignedOrders.clear();
        for (Order o : allOrders) {
            for (Beacon beacon : beacons) {
                if (beacon.getMajor() == o.getbeaconIDMajor() && beacon.getMinor() == o.getBeaconIDMinor()) {
                    unassignedOrders.add(o);
                }
            }
        }
        unassignedRestaurantNames.clear();

        for (Order o : unassignedOrders) {
            unassignedRestaurantNames.add(o.getRestaurantName());
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

    public void startDutyCyclingCd(){
        beaconManager.stopRanging(region);
        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                beaconManager.startRanging(region);
            }
        }.start();

    }

}

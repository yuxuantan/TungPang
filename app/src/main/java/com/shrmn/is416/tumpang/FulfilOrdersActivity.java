package com.shrmn.is416.tumpang;

import android.content.DialogInterface;
import android.os.Build;
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

public class FulfilOrdersActivity extends AppCompatActivity {

    //LIST OF ORDERS BEFORE LOCATION FILTER - For now is static list
    private static List<Order> allOrders;

    private static List<Order> unassignedOrders;
    private static List<String> unassignedRestaurantNames = new ArrayList<>();

    //    private static final Map<String, List<String>> PLACES_BY_BEACONS;

    //    // TODO: replace "<major>:<minor>" strings to match your own beacons.
    static {
//
//        // At the start populate from restaurant DB - get all restaurant name, restaurant ID and beaconID
//
//        Map<String, List<String>> placesByBeacons = new HashMap<>();
//        placesByBeacons.put("6722:37991", new ArrayList<String>() {{
//            add("Koufu");
//            add("101");
//        }});
//        placesByBeacons.put("648:12", new ArrayList<String>() {{
//            add("Waterloo");
//            add("102");
//        }});
//        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
        // Get Orders
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
                648,
                123,
                "SIS GSR 2-3",
                0,
                "Waterloo"));

        unassignedOrders = new ArrayList<>();
        unassignedRestaurantNames = new ArrayList<>();
//        for(Order o: unassignedOrders){
//            unassignedRestaurantNames.add(o.getRestaurantName());
//        }
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
                String selectedOrderTitle = parent.getItemAtPosition(position).toString();
                Order selectedOrder = unassignedOrders.get(position);

                AlertDialog alertDialog = new AlertDialog.Builder(FulfilOrdersActivity.this).create();
                alertDialog.setTitle(selectedOrderTitle);
                alertDialog.setMessage(selectedOrder.toString());
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
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
                            }
                        });
                alertDialog.show();
            }
        });
        //***************************

//        beaconManager= new BeaconManager(getApplicationContext());
        MyApplication.beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onEnteredRegion(BeaconRegion region, List<Beacon> beacons) {
                // Update list
                Log.d("Status", "enter: Major - " + beacons.get(0).getMajor() + " Minor - " + beacons.get(0).getMinor());
                adapter.clear();
                refreshList(beacons);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onExitedRegion(BeaconRegion region) {
                // Update List  + beacons.get(0).getMajor() + " Minor - " + beacons.get(0).getMinor()
                Log.d("Status", "Exit: Major - ");
                adapter.clear();
                refreshList(new ArrayList<Beacon>());
                adapter.notifyDataSetChanged();

            }
        });

//        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
//            @Override
//            public void onServiceReady() {
//                beaconManager.startMonitoring(new BeaconRegion(
//                        "monitored region",
//                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null));
//
////                                6722, 37991));
//
//            }
//        });

    }


    private void refreshList(List<Beacon> beacons) {

        //CONNECT TO DB, pull list of unassigned and set here!! unassignedOrders = ??, Filter again
        // Set ArrayLists
        Log.d("Refreshed", "" + allOrders.size());
        unassignedOrders.clear();

        for (Order o : allOrders) {
//            Log.d("Refreshed", ""+o.getbeaconIDMajor()+", "+beacon.getMajor());
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
    }
}

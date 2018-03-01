package com.example.xuan.tungpangapp;

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
import android.widget.TextView;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FulfilOrdersActivity extends AppCompatActivity {

    private List<Order> unassignedOrders;
    private List<Order> allOrders;

    private List<String> unassignedRestaurantNames;
    private BeaconManager beaconManager;
    private BeaconRegion region;

    private static final Map<String, List<String>> PLACES_BY_BEACONS;

    // TODO: replace "<major>:<minor>" strings to match your own beacons.
    static {

        // At the start populate from restaurant DB - get all restaurant name, restaurant ID and beaconID

        Map<String, List<String>> placesByBeacons = new HashMap<>();
        placesByBeacons.put("6722:37991", new ArrayList<String>() {{
            add("Koufu");
            add("101");
        }});
        placesByBeacons.put("648:12", new ArrayList<String>() {{
            add("Waterloo");
            add("102");
        }});
        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fulfil_orders);

        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onEnteredRegion(BeaconRegion region, List<Beacon> beacons) {
                Log.d("Status","ENTER");
            }
            @Override
            public void onExitedRegion(BeaconRegion region) {
                // could add an "exit" notification too if you want (-:
                Log.d("Status", "Exit");
            }
        });

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new BeaconRegion(
                        "monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null));

//                                6722, 37991));

            }
        });

        // Get Orders
        // HARD CODED PORTION
        // ** Add only orders that have status==0 ie. "unassigned" into the lists
        allOrders = new ArrayList<>();
        unassignedOrders = new ArrayList<>();
        unassignedRestaurantNames = new ArrayList<>();
        allOrders.add(new Order(1,
                101,
                "Chicken Rice",
                3.0,
                0.3,
                20,
                13,
                24,
                "ABCDEFG1234asdf",
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
                "ABCDEFG1234asdf",
                "SIS GSR 2-3",
                0,
                "Waterloo"));

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
                                // Remove from actual DB - after API is set up
                            }
                        });
                alertDialog.show();
            }
        });
        //***************************



    }

    private List<String> placesNearBeacon(Beacon beacon) {
        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());
        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
            return PLACES_BY_BEACONS.get(beaconKey);
        }
        return Collections.emptyList();
    }


    private void refreshList(){
        // Stop monitoring

        //CONNECT TO DB, pull list of unassigned and set here!!

        //

    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }
}

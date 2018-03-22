package com.shrmn.is416.tumpang;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.service.BeaconManager;

import java.util.ArrayList;
import java.util.List;

public class FulfilAcceptedOrdersActivity extends AppCompatActivity {

    //LIST OF ORDERS BEFORE LOCATION FILTER - For now is static list
    private static List<Order> allOrders;

    private static List<Order> unassignedOrders;
    private static List<String> unassignedRestaurantNames = new ArrayList<>();

    public BeaconManager beaconManager;
    public BeaconRegion region;

    private static final String TAG = "FulfilAcceptedOrderRequest";

    static {

        // HARD CODED PORTION
        unassignedOrders = new ArrayList<>();
        unassignedRestaurantNames = new ArrayList<>();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fulfil_orders);

//LIST VIEW SET

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
                goToOrderDetails();

            }
        });
    }


    public void finishActivity(View view) {
        finish();
    }

    public void goToOrderDetails(){
        Intent orderDetailsIntent = new Intent(this, OrderDetailsActivity.class);
        startActivity(orderDetailsIntent);

    }
}

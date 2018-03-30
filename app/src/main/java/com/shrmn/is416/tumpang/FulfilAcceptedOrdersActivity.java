package com.shrmn.is416.tumpang;

import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.service.BeaconManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FulfilAcceptedOrdersActivity extends AppCompatActivity {

    //LIST OF ORDERS BEFORE LOCATION FILTER - For now is static list

    private static List<Order> acceptedOrders;
    private static List<String> acceptedOrdersNames = new ArrayList<>();

    public BeaconManager beaconManager;
    public BeaconRegion region;

    private static final String TAG = "FulfilAcceptedOrders";
    private ArrayAdapter<String> adapter;

    public String selectedTeleUsername;

    static {

        // HARD CODED PORTION
        acceptedOrders = new ArrayList<>();
        acceptedOrdersNames = new ArrayList<>();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fulfil_accepted_orders);

        retrieveOrders();
//LIST VIEW SET
        acceptedOrdersNames.add("Testing Order 1");
        acceptedOrdersNames.add("Testing Tea Party");
        acceptedOrdersNames.add("Testing Food Republic");

        ListView lv = (ListView) findViewById(R.id.orders_list);
        adapter = new ArrayAdapter<String>(
                this,//context
                R.layout.mylistlayout,//custom_layout
                R.id.DefaultItemList1,// referring the widget (TextView) where the items to be displayed
                acceptedOrdersNames//items
        );

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                String selectedOrderTitle = parent.getItemAtPosition(position).toString();
                final Order selectedOrder = acceptedOrders.get(position);


                AlertDialog alertDialog = new AlertDialog.Builder(FulfilAcceptedOrdersActivity.this).create();
                alertDialog.setTitle(selectedOrderTitle);
                alertDialog.setMessage(selectedOrder.toString());
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Purchased!",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Update DB
                                updateDBStatus(selectedOrder);

                            }
                        });
                alertDialog.show();
                alertDialog.setCanceledOnTouchOutside(false);

            }
        });

    }


    public void finishActivity(View view) {
        finish();
    }

    public void goToOrderDetails() {
        Intent orderDetailsIntent = new Intent(this, OrderDetailsActivity.class);
        startActivity(orderDetailsIntent);

    }

    private void retrieveOrders() {
        MyApplication.db.collection("orders").whereEqualTo("status", 1).whereEqualTo("deliveryManUserID", MyApplication.user.getIdentifier()).get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            acceptedOrders.clear();

                            // For each entry ie. order
                            for (DocumentSnapshot document : task.getResult()) {

                                String orderId = document.getId();
                                Log.d(TAG, "onComplete: " + orderId);
                                Map<String, Object> data = document.getData();

//                                Log.e(TAG, document.getId() + " => " + data);

                                // ArrayList of HashMaps, 1 for each item. Key(item and qty)
//                                ArrayList<Map<String, String>> dbOrderMenuItems = (ArrayList<Map<String, String>>) data.get("menuItems");
                                ArrayList<Object> dbOrderMenuItems = (ArrayList<Object>) data.get("menuItems");
                                String locID = (String) data.get("locationID").toString().split("/")[2];
//                                Log.e("locID", locID);
                                Location location = null;
                                String locationName = "";
                                if (locID != null) {
                                    location = MyApplication.locations.get(locID);
                                    if (location != null)
                                        locationName = location.getName();
                                }


                                // Menu Item includes more details of the menu like name. what is stored in DB is links
                                /**Can we push list of drinks into static list?**/
                                HashMap<MenuItem, Integer> menuItems = new HashMap<>();


                                for (Object dbMenuItem : dbOrderMenuItems) {
                                    long qty = ((Map<String, Long>) dbMenuItem).get("qty");
                                    // Change item string reference (menuItem.get("item")) to locations table into a menu item object, and put in menuitems hashmap
                                    String[] references = ((Map<String, String>) dbMenuItem).get("item").split("/");
//                                    Log.e("ref", Arrays.toString(references));
                                    // eg. get "Food[0]" --> (Food , 0)
                                    String tmp[] = references[2].split("\\[|\\]");
//                                    String type = tmp[0];
//                                    Log.e("Location", location.toString());
                                    MenuItem item = null;
                                    if (location != null)
                                        item = location.getMenu().getItems().get(Integer.parseInt(tmp[1]));
//                                        Log.e("qty" , qty+"");
                                    menuItems.put(item, (int) qty);
                                }

                                // Actually this will never happen, since if filter order = unassigned, est time delivery WILL be null
                                if (data.get("estimatedTimeOfDelivery") != null && data.get("deliveryManUserID") == null) {
                                    acceptedOrders.add(
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
                                } else {
                                    acceptedOrders.add(
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

                        acceptedOrdersNames.clear();

                        for (Order o : acceptedOrders) {
                            acceptedOrdersNames.add(o.getLocationName());
                        }
                        Log.d(TAG, "onComplete: " + acceptedOrdersNames);
                        Log.d(TAG, "onComplete: " + acceptedOrders);
                        adapter.notifyDataSetChanged();

                    }
                }
        );
    }

    public void updateDBStatus(final Order order) {
        //data.put("ETA", ETA); Don't we need to ask user for their ETA as well?
        MyApplication.db.collection("orders").document(order.getOrderID()).update("status", 2)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e(TAG, "OrderDetailsActivity: Order " + order.getOrderID() + " updated.");

                        MyApplication.actionOnUser(order.getCustomerUserID(), new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document != null && document.exists()) {
                                        User otherUser = document.toObject(User.class);
                                        Map<String, Object> telegram = otherUser.getTelegram();

                                        if (telegram == null || telegram.isEmpty()) {
                                            Log.e(TAG, "OrderDetailsActivity: The receiving party (" + document.getId() + ") of the order has not completed telegram setup.");
                                        } else {
                                            String displayMsg = "Order details: ";
                                            for(MenuItem item: order.getMenuItems().keySet()){
                                                displayMsg+=(item.getName()+", qty: "+order.getMenuItems().get(item)+"\n");
                                            }
                                            // Send telegram notification to yourself with the customer's ID
                                            MyApplication.sendTelegramNotification(
                                                    String.format(
                                                            "*Order Purchase Completed*\nThank you for completing the order with ID: %s! \n%sPlease deliver the purchases to @%s",
                                                            order.getOrderID(),
                                                            displayMsg,
                                                            otherUser.getTelegramUsername()
                                                    )
                                            );

                                            // Send telegram notification to the customer
                                            MyApplication.sendTelegramNotification(
                                                    String.format(
                                                            "*Order Delivering*\nYour order with ID: %s has been purchased and is on the way!\n%sYour delivery man's telegram: @%s.",
                                                            order.getOrderID(),
                                                            displayMsg,
                                                            MyApplication.user.getTelegramUsername()
                                                    ),
                                                    order.getCustomerUserID()
                                            );

                                            Log.i(TAG, "OrderDetailsActivity: Telegram Messages sent");
                                        }
                                    } else {
                                        Log.e(TAG, "OrderDetailsActivity: The receiving party (" + document.getId() + ") of the order does not seem to be available on the database.");
                                    }
                                }
                            }
                        });

                        retrieveOrders();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("OrderDetailsActivity", "Error updating Order", e);
                    }
                });
    }

}


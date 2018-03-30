package com.shrmn.is416.tumpang;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyPlacedOrdersActivity extends AppCompatActivity {
//    private ArrayList<String> myPlacedOrdersNames = new ArrayList<>();
    private ArrayList<Order> myPlacedOrders = new ArrayList<>();
    private static final String TAG = "MyPlacedOrdersActivity";
    private static FulfilOrderItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_placed_orders);


        // Initialize List View
        ListView lv = (ListView) findViewById(R.id.my_placed_orders_list);
        adapter = new FulfilOrderItemAdapter(
                this,//context
                0, // referring the widget (TextView) where the items to be displayed
                myPlacedOrders //items
        );

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                // Pause refresh

                String selectedOrderTitle = parent.getItemAtPosition(position).toString();
                final Order selectedOrder = myPlacedOrders.get(position);

//                // Go to Order Details activity
//                Intent it = new Intent(MyPlacedOrdersActivity.this, OrderDetailsActivity.class);
//                it.putExtra("selectedOrder", selectedOrder);
//                startActivity(it);
                AlertDialog alertDialog = new AlertDialog.Builder(MyPlacedOrdersActivity.this).create();
                alertDialog.setTitle(selectedOrderTitle);
                alertDialog.setMessage(selectedOrder.toString());
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Delete Order",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Update DB
                                deleteOrder(selectedOrder.getOrderID());

                            }
                        });
                alertDialog.show();
                alertDialog.setCanceledOnTouchOutside(false);

            }
        });
        if(MyApplication.user!=null)
            retrieveOrders();
    }

    private void retrieveOrders() {
        MyApplication.db.collection("orders").whereEqualTo("status", 0).whereEqualTo("customerUserID", MyApplication.user.getIdentifier()).get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
//                            myPlacedOrdersNames.clear();
                            myPlacedOrders.clear();
                            // For each entry ie. order
                            for (DocumentSnapshot document : task.getResult()) {
                                String orderId = document.getId();
                                Map<String, Object> data = document.getData();

                                Log.e("TESTING", document.getId() + " => " + data);

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
                                    myPlacedOrders.add(
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
                                    myPlacedOrders.add(
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
//                            Log.d("TESTING2", myPlacedOrders.toString());
//                            for (Order o : myPlacedOrders) {
//                                myPlacedOrdersNames.add(o.getLocationName());
//                            }
                            adapter.notifyDataSetChanged();


                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                }
        );
    }

    public void deleteOrder(String orderID){
        MyApplication.db.collection("orders").document(orderID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        retrieveOrders();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }
}

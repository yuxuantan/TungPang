package com.shrmn.is416.tumpang;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

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

    private static List<Order> acceptedOrders;

    private static final String TAG = "FulfilAcceptedOrders";
    private FulfilOrderItemAdapter adapter;
    private ListView fulfilledAccepted_OrderListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fulfil_accepted_orders);

        acceptedOrders = new ArrayList<>();
        fulfilledAccepted_OrderListView = findViewById(R.id.fulfil_accepted_ordersListView);
        adapter = new FulfilOrderItemAdapter(this, 0, acceptedOrders);

        retrieveOrders();

        fulfilledAccepted_OrderListView.setAdapter(adapter);
        fulfilledAccepted_OrderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final Order selectedOrder = acceptedOrders.get(position);

                String outletAddress = selectedOrder.getLocation().getAddress();
                String deliveryLocation = selectedOrder.getDeliveryLocation();

                String strTipAmount = String.format("%.2f", selectedOrder.getTipAmount());
                String strOrderBill = String.format("%.2f", selectedOrder.getBill());
                String strOrderTotal = String.format("%.2f", selectedOrder.getTotalOrderBill());

                AlertDialog builder = new AlertDialog.Builder(FulfilAcceptedOrdersActivity.this).create();
                // Get the layout inflater
                LayoutInflater inflater = FulfilAcceptedOrdersActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_orderconfirmation, null);

//                TextView orderUser = dialogView.findViewById(R.id.order_user);
//                orderUser.setText("Order Username: " + user.getTelegramUsername());

                TextView headerText = dialogView.findViewById(R.id.order_dialog_header_text);
                headerText.setText("Order Details");

                TextView startingText = dialogView.findViewById(R.id.order_dialog_starting_text);
                startingText.setText("ID: " + selectedOrder.getOrderID());

                TextView outletLocation = dialogView.findViewById(R.id.outlet_name);
                outletLocation.setText("Outlet:\n" + selectedOrder.getLocationName());

                TextView outletAdd = dialogView.findViewById(R.id.outlet_address);
                outletAdd.setText(outletAddress);

                TextView deliveryLoc = dialogView.findViewById(R.id.delivery_location);
                deliveryLoc.setText("Delivery Location: " + deliveryLocation);

                TextView tip = dialogView.findViewById(R.id.final_tip_amount);
                tip.setText("Tip Amount: $ " + strTipAmount);

                TextView orderQty = dialogView.findViewById(R.id.order_qty);
                orderQty.setText("Total Quantity: " +selectedOrder.getTotalQuantity());

                TextView orderBill = dialogView.findViewById(R.id.order_bill);
                orderBill.setText("Order Bill: $ " + strOrderBill);

                TextView totalBill = dialogView.findViewById(R.id.total_orderbill);
                totalBill.setText("Order Total: $ " + strOrderTotal);

                // Inflate and set the layout for the dialog
                // Pass null as the parent view because its going in the dialog layout
                builder.setView(dialogView);
                builder.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        updateDBStatus(selectedOrder);
                    }
                });
                builder.setButton(AlertDialog.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });

    }


    public void finishActivity(View view) {
        finish();
    }

    private void retrieveOrders() {
        MyApplication.db.collection("orders")
                .whereEqualTo("status", 1)
                .whereEqualTo("deliveryManUserID", MyApplication.user.getIdentifier())
                .get()
                .addOnCompleteListener(
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
                                    acceptedOrders.add(
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
                                    acceptedOrders.add(
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

                        Log.d(TAG, "onComplete: " + acceptedOrders);
                        adapter.notifyDataSetChanged();
                    }
                }
        );
    }

    public void updateDBStatus(final Order order) {
        MyApplication.db.collection("orders").document(order.getOrderID()).update("status", 2)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "OrderDetailsActivity: Order " + order.getOrderID() + " updated.");

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
                                            for (MenuItem item : order.getMenuItems().keySet()) {
                                                displayMsg += (item.getName() + ", qty: " + order.getMenuItems().get(item) + "\n");
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
                                            Snackbar.make(fulfilledAccepted_OrderListView, "Order confirmation successful. Thank you for using Tumpang!", Snackbar.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Log.e(TAG, "OrderDetailsActivity: The receiving party (" + document.getId() + ") of the order does not seem to be available on the database.");
                                    }
                                } else {
                                    Log.e("OrderDetailsActivity", "Error updating Order", task.getException());
                                    Snackbar.make(fulfilledAccepted_OrderListView, "Error updating Order. Please try again later.", Snackbar.LENGTH_SHORT).show();
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
                        Snackbar.make(fulfilledAccepted_OrderListView, "Error updating Order. Please try again later.", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

}

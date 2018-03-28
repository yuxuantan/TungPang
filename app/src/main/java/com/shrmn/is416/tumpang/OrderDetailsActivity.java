package com.shrmn.is416.tumpang;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OrderDetailsActivity extends AppCompatActivity {

    // Holds the Firestore database instance. Used from a static context
    public static FirebaseFirestore db;
    private static Order order;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        // Get order ID
        String orderId = getIntent().getStringExtra("orderId");
        // Query db for this orderId
        retrieveOrder(orderId);
        Log.d("SelectedOrder", order.toString());
        /*Use this way of retrieving order if retrieveOrder(orderId) doesn't work
        for(Order order: FulfilOrdersActivity.allUnassignedOrders) {
            if (order.getOrderID().toString().equals(orderId)) {
                Log.e("OrderDetailsActivity", order.toString());
            }
        }*/

    }

    public void finishActivity(View view) {
        finish();
    }


    public void deliveredOrder(View view){
        updateDBStatus(3);
        // Go back to fulfilAcceptedOrdersActivity
        Intent intent = new Intent(this, FulfilAcceptedOrdersActivity.class);
        startActivity(intent);
        finish();
    }
    public void purchasedOrder(View view){
        updateDBStatus(2);
        // Go back to fulfilAcceptedOrdersActivity
        Intent intent = new Intent(this, FulfilAcceptedOrdersActivity.class);
        startActivity(intent);
        finish();
    }
    public void acceptedOrder(View view) {
        updateDBStatus(1);
        // Go back to fulfilAcceptedOrdersActivity
        Intent intent = new Intent(this, FulfilAcceptedOrdersActivity.class);
        startActivity(intent);
        finish();
    }

    public void updateDBStatus(int updatedStatus){
        // Code to update DB status to updatedStatus
        Map<String, Object> data = new HashMap<>();
        data.put("customerUserID",order.getCustomerUserID());
        data.put("deliveryLocation",order.getDeliveryLocation());
        data.put("tipAmount",order.getTipAmount());
        data.put("locationID", order.getLocationID());
        data.put("menuItems",order.getMenuItems());
        data.put("status", updatedStatus);
        //data.put("ETA", ETA); Don't we need to ask user for their ETA as well?
        MyApplication.db.collection("orders").document(order.getOrderID()).set(data, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e("OrderDetailsActivity", "Order updated.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("OrderDetailsActivity", "Error updating Order", e);
                    }
                });

    }
    //used to retrieve a single order, loop the ArrayList of unAssignedOrders if this does not work
    private void retrieveOrder(String orderId) {

        db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("orders").document(orderId);

        docRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                order = document.toObject(Order.class);
                                Log.d("", "Order loaded: " + order);
                            } else {
                                Log.d("OrderDetailsActivity", "No order found");
                            }
                        }
                    }
                });
    }

}

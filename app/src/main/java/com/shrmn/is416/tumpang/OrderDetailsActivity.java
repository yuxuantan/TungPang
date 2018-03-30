package com.shrmn.is416.tumpang;

import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.shrmn.is416.tumpang.utilities.FCMRestClient;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

public class OrderDetailsActivity extends AppCompatActivity {

    // Holds the Firestore database instance. Used from a static context
    public static FirebaseFirestore db;
    private static Order order;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        // Get order ID
//        String orderId = getIntent().getStringExtra("orderId");
        order = (Order)getIntent().getSerializableExtra("selectedOrder");
        // Query db for this orderId
//        retrieveOrder(orderId);
        Log.d("SelectedOrder", order.toString());
        /*Use this way of retrieving order if retrieveOrder(orderId) doesn't work
        for(Order order: FulfilOrdersActivity.allUnassignedOrders) {
            if (order.getOrderID().toString().equals(orderId)) {
                Log.e("OrderDetailsActivity", order.toString());
            }
        }*/
        double OrderBill = 0;
        for(MenuItem menuItem: order.getMenuItems().keySet() ) {
            OrderBill += menuItem.getUnitPrice() * menuItem.getQuantity();
        }

        String menuItemDetails = "Order Details:";
        for(MenuItem item: order.getMenuItems().keySet()){
            menuItemDetails+=("\n- " + order.getMenuItems().get(item)+ "x "+item.getName());
        }
        OrderBill+= order.getTipAmount();

        String detailsText = "<b>Order ID: </b> " + order.getOrderID()+ "<br/>" + "<b>Customer UserID: </b>" + order.getCustomerUserID() +
                "<br/>" + "<b> Location Name: </b>" + order.getLocationName() + "<br/>" + "<b> Deliver Location: </b>" + order.getDeliveryLocation()
                +"<br/>" + "<b> Menu Item Details: </b>" + menuItemDetails + "<br/>" + "<b>Tip Amount: </b>$ " + order.getTipAmount() +
                "<br/>" + "<b>Total OrderBill: </b>$ " + OrderBill;


        TextView detailsTv = (TextView) findViewById(R.id.order_detailsTextView);
        detailsTv.setText(Html.fromHtml(detailsText));

    }

    public void finishActivity(View view) {
        finish();
    }


    public void acceptedOrder(View view) {
        updateDBStatus(1);
        sendNotif(order.getCustomerUserID());
        // Go back to fulfilAcceptedOrdersActivity
        Intent intent = new Intent(this, FulfilAcceptedOrdersActivity.class);
        startActivity(intent);
        finish();
    }

    public void updateDBStatus(int updatedStatus){
        //data.put("ETA", ETA); Don't we need to ask user for their ETA as well?
        MyApplication.db.collection("orders").document(order.getOrderID()).update("status", 1, "deliveryManUserID", MyApplication.user.getIdentifier())
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
//    private void retrieveOrder(String orderId) {
//
//        db = FirebaseFirestore.getInstance();
//        DocumentReference docRef = db.collection("orders").document(orderId);
//
//        docRef.get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful()) {
//                            DocumentSnapshot document = task.getResult();
//                            if (document != null && document.exists()) {
//                                order = document.toObject(Order.class);
//                                Log.d("", "Order loaded: " + order);
//                            } else {
//                                Log.d("OrderDetailsActivity", "No order found");
//                            }
//                        }
//                    }
//                });
//    }

    private void sendNotif(String identifier) {
        // Test HTTP Request
        JsonObject finalObj = new JsonObject();
        finalObj.addProperty("to", "/topics/" + identifier);

        JsonObject msgObj = new JsonObject();

        String displayMsg = "Order details: ";
        for(MenuItem item: order.getMenuItems().keySet()){
            displayMsg+=(item.getName()+", qty: "+order.getMenuItems().get(item)+"\n");
        }
        msgObj.addProperty("message", displayMsg);
        finalObj.add("data", msgObj);

        StringEntity entity = null;
        try {
            entity = new StringEntity(finalObj.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        FCMRestClient client = new FCMRestClient();
        client.post(OrderDetailsActivity.this, "", entity, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e("status", "Success:");
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("status", "Failure:" + error);
            }
        });
    }

}

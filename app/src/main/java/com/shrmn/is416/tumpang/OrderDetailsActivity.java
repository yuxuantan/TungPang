package com.shrmn.is416.tumpang;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.shrmn.is416.tumpang.utilities.FCMRestClient;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

public class OrderDetailsActivity extends AppCompatActivity {

    // Holds the Firestore database instance. Used from a static context
    private static Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        // Get order ID
        order = (Order) getIntent().getSerializableExtra("selectedOrder");
        // Query db for this orderId
        Log.d("SelectedOrder", order.toString());
        double OrderBill = 0;

        String menuItemDetails = "<br/>";
        for (MenuItem item : order.getMenuItems().keySet()) {
            menuItemDetails += (order.getMenuItems().get(item) + "x " + item.getName() + " - $" + Math.round(item.getUnitPrice() * order.getMenuItems().get(item)) + "<br/>");
            OrderBill += (Math.round(item.getUnitPrice() * order.getMenuItems().get(item)));
        }
        menuItemDetails += "</ul>";
        OrderBill += order.getTipAmount();

        String detailsText = "<b>Location Name: </b><br/>" + order.getLocationName() + "<br/><br/>" + "<b> Delivery Location: </b><br/>" + order.getDeliveryLocation()
                + "<br/><br/>" + "<b> Menu Item Details: </b><br/>" + menuItemDetails + "<br/><br/>" + "<b>Order ID: </b><br/> " + order.getOrderID() +
                "<br/><br/>" + "<b>Tip Amount: </b><br/>$" + order.getTipAmount() +
                "<br/><br/>" + "<b>Total Bill: </b><br/>$" + OrderBill;


        TextView detailsTv = findViewById(R.id.order_detailsTextView);
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

    public void updateDBStatus(int updatedStatus) {
        //data.put("ETA", ETA); Don't we need to ask user for their ETA as well?
        MyApplication.db.collection("orders").document(order.getOrderID()).update("status", updatedStatus, "deliveryManUserID", MyApplication.user.getIdentifier())
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

    private void sendNotif(String identifier) {
        // Test HTTP Request
        JsonObject finalObj = new JsonObject();
        finalObj.addProperty("to", "/topics/" + identifier);

        JsonObject msgObj = new JsonObject();

        String displayMsg = "Order details: ";
        for (MenuItem item : order.getMenuItems().keySet()) {
            displayMsg += (item.getName() + ", qty: " + order.getMenuItems().get(item) + "\n");
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

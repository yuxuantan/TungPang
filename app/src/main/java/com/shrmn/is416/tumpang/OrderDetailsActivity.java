package com.shrmn.is416.tumpang;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class OrderDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        // Get order ID
        getIntent().getStringExtra("orderId");
        // Query db for this orderId
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

    }

}

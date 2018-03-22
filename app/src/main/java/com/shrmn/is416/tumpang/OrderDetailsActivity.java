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
    }

    public void finishActivity(View view) {
        finish();
    }

    public void fulfilOrder(View view) {
        // Update status on DB

        // Go back to fulfilAcceptedOrdersActivity
        Intent intent = new Intent(this, FulfilAcceptedOrdersActivity.class);
        startActivity(intent);
        finish();
    }
}

package com.example.xuan.tungpangapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void newOrderRequest(View view){
        Intent ptg = new Intent( this, NewOrderRequestActivity.class);
        startActivity(ptg);
    }

    public void fulfilOrderOrderRequest(View view){
        Intent ptg = new Intent(this, FulfilOrdersActivity.class);
        startActivity(ptg);
    }
}

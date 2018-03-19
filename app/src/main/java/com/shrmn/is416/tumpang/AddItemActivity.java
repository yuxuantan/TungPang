package com.shrmn.is416.tumpang;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Date;

public class AddItemActivity extends AppCompatActivity {

    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        location = MyApplication.pendingOrder.getLocation();
    }

    public void back(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void addItem(View view) {
        Intent it = getIntent();

        // Obtain Menu Item
//        it.putExtra("menuItem", menuItem);

        // Obtain Quantity
        EditText etQuantity = findViewById(R.id.quantity_et);
        String quantity = etQuantity.getText().toString();
        it.putExtra("quantity", quantity);

        // Return result
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

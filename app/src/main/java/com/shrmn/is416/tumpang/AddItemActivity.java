package com.shrmn.is416.tumpang;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class AddItemActivity extends AppCompatActivity {

    private EditText quantityEditText;
    private Spinner dynamicSpinner;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> itemNames;
    private Menu menu;
    private Location location;
    private MenuItem selectedMenuItem;

    private static final String TAG = "AddItem";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        quantityEditText = findViewById(R.id.quantity_et);
        quantityEditText.setFilters(new InputFilter[]{new MinMaxFilter("1", "20")});

        dynamicSpinner = findViewById(R.id.item_name);

        // Set a default order ID value for
        location = MyApplication.pendingOrder.getLocation();
        menu = location.getMenu();
        Log.d(TAG, "onCreate: " + location.getMenu().getItems());

        itemNames = new ArrayList<>();
        for (MenuItem item : menu.getItems()) {
            itemNames.add(item.getName());
        }

        setAdapterContents();
    }

    private void setAdapterContents() {

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, itemNames);

        dynamicSpinner.setAdapter(adapter);

        dynamicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                TextView label = findViewById(R.id.unit_price_value);
                selectedMenuItem = menu.getItems().get(position);
                label.setText("$ " + selectedMenuItem.getUnitPrice());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    public void back(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void addItem(View view) {
        Log.d(TAG, "addItem: Called.");
        Intent output = getIntent();

        try {
            MyApplication.pendingOrder.addMenuItem(selectedMenuItem, Integer.parseInt(quantityEditText.getText().toString()));
        } catch (NumberFormatException ex) { // handle your exception
            back(view);
        }

        setResult(RESULT_OK, output);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

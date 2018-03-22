package com.shrmn.is416.tumpang;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class NewOrderRequestActivity extends AppCompatActivity {

    private static final String TAG = "OrderRequest";
    private final int REQ_CODE_ADD_ITEM = 1;

    private Spinner dynamicSpinner;
    private ArrayAdapter<String> adapter;
    private Map<String, String> values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order_request);

        dynamicSpinner = findViewById(R.id.food_Outlet);

        values = new HashMap<>();

        setTipAmountAdapterContents();
        retrieveLocations();
    }

    private void retrieveLocations() {
        if(!MyApplication.locations.isEmpty()) {
            setAdapterContents();
            return;
        }

        MyApplication.db.collection("locations").get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String locationID = document.getId();
                                Map<String, Object> data = document.getData();
                                ArrayList<Drink> drinks = new ArrayList<>();
                                ArrayList<Food> food = new ArrayList<>();
                                ArrayList<MenuItem> items = new ArrayList<>();

                                Log.d(TAG, document.getId() + " => " + data);

                                Map<String, Object> menuObj = (Map<String, Object>) data.get("menu");
                                ArrayList<Map<String, Object>> foodObj = (ArrayList<Map<String, Object>>) menuObj.get("food");
                                ArrayList<Map<String, Object>> drinksObj = (ArrayList<Map<String, Object>>) menuObj.get("drinks");

                                String path = document.getReference().getPath();

                                for (int i = 0; i < foodObj.size(); i++) {
                                    Map<String, Object> foodItem = foodObj.get(i);
                                    food.add(new Food(path + "/food[" + i + "]", foodItem.get("name").toString(), Double.parseDouble(foodItem.get("unitPrice").toString())));
                                    items.add(new Food(path + "/food[" + i + "]", foodItem.get("name").toString(), Double.parseDouble(foodItem.get("unitPrice").toString())));
                                }

                                for (int i = 0; i < drinksObj.size(); i++) {
                                    Map<String, Object> drinkItem = drinksObj.get(i);
                                    drinks.add(new Drink(path + "/drinks[" + i + "]", drinkItem.get("name").toString(), Double.parseDouble(drinkItem.get("unitPrice").toString())));
                                    items.add(new Drink(path + "/drinks[" + i + "]", drinkItem.get("name").toString(), Double.parseDouble(drinkItem.get("unitPrice").toString())));
                                }


                                MyApplication.locations.put(locationID,
                                        new Location(
                                                locationID,
                                                data.get("name").toString(),
                                                data.get("address").toString(),
                                                123,
                                                456,
                                                new Menu(items)
                                        )
                                );
                                MyApplication.locationIDs.add(locationID);
                                MyApplication.locationNames.add(data.get("name").toString());
                            }
                            Log.d(TAG, "retrieveLocations: " + MyApplication.locations.get("tea-party"));
                            setAdapterContents();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                }
        );
    }

    private void setAdapterContents() {

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, MyApplication.locationNames);

        dynamicSpinner.setAdapter(adapter);

        dynamicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.v(TAG, (String) parent.getItemAtPosition(position));
                Log.d(TAG, "onItemSelected: " + MyApplication.locationIDs.get(position));
                values.put("locationID", MyApplication.locationIDs.get(position));
                values.put("locationName", (String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void setTipAmountAdapterContents() {

        String[] tipAmounts = {"0.50", "1.00", "2.00", "3.00", "4.00", "5.00"};
        ArrayAdapter adapterTipAmount = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, tipAmounts);

        Spinner tipAmountSpinner = findViewById(R.id.tip_Amount);

        tipAmountSpinner.setAdapter(adapterTipAmount);

        tipAmountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.v(TAG, (String) parent.getItemAtPosition(position));
                values.put("tipAmount", (String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }

    public void next(View view) {
        EditText editView = findViewById(R.id.meetup_location);

        if (MyApplication.pendingOrder == null) {
            MyApplication.pendingOrder = new Order(
                    MyApplication.locations.get(values.get("locationID")),
                    values.get("locationID"),
                    values.get("locationName"),
                    Double.parseDouble(values.get("tipAmount")),
                    editView.getText().toString()
            );
        } else {
            MyApplication.pendingOrder.setLocation(MyApplication.locations.get(values.get("locationID")));
            MyApplication.pendingOrder.setLocationID(values.get("locationID"));
            MyApplication.pendingOrder.setLocationName(values.get("locationName"));
            MyApplication.pendingOrder.setTipAmount(Double.parseDouble(values.get("tipAmount")));
            MyApplication.pendingOrder.setDeliveryLocation(editView.getText().toString());
        }

        Intent newOrderRequestMenu = new Intent(this, NewOrderRequestMenuActivity.class);
        startActivity(newOrderRequestMenu);
    }
}

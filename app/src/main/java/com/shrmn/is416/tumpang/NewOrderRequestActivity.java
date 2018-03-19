package com.shrmn.is416.tumpang;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewOrderRequestActivity extends AppCompatActivity {

    private static final String TAG = "OrderRequest";
    private Spinner dynamicSpinner;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> locationIDs;
    private ArrayList<String> locationNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order_request);

        dynamicSpinner = findViewById(R.id.food_Outlet);

        locationNames = new ArrayList<>();
        locationIDs = new ArrayList<>();
        retrieveLocations();
    }

    private void retrieveLocations() {
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

                                Log.d(TAG, document.getId() + " => " + data);

                                Map<String, Object> menuObj = (Map<String, Object>) data.get("menu");
                                ArrayList<Map<String, Object>> foodObj = (ArrayList<Map<String, Object>>) menuObj.get("food");
                                ArrayList<Map<String, Object>> drinksObj = (ArrayList<Map<String, Object>>) menuObj.get("drinks");


                                for(Map<String, Object> foodItem : foodObj) {
                                    food.add(new Food(foodItem.get("name").toString(), Double.parseDouble(foodItem.get("unitPrice").toString())));
                                }

                                for(Map<String, Object> drinkItem : drinksObj) {
                                    drinks.add(new Drink(drinkItem.get("name").toString(), Double.parseDouble(drinkItem.get("unitPrice").toString())));
                                }

                                MyApplication.locations.put(locationID,
                                        new Location(
                                                locationID,
                                                data.get("name").toString(),
                                                data.get("address").toString(),
                                                123,
                                                456,
                                                new Menu(drinks, food)
                                        )
                                );
                                locationIDs.add(locationID);
                                locationNames.add(data.get("name").toString());
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

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, locationNames);

        dynamicSpinner.setAdapter(adapter);

        dynamicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.v(TAG, (String) parent.getItemAtPosition(position));
                Log.d(TAG, "onItemSelected: " + locationIDs.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
    }
}

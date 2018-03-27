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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class NewOrderRequestActivity extends AppCompatActivity {

    private static final String TAG = "OrderRequest";
    private final int REQ_CODE_ADD_ITEM = 1;

    private Spinner dynamicSpinner;
    private ArrayAdapter<String> adapter;
    private Map<String, String> values;
    private static List<Order> allOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order_request);

        dynamicSpinner = findViewById(R.id.food_Outlet);

        values = new HashMap<>();

        setTipAmountAdapterContents();
        retrieveLocations();
        retrieveOrders();
        //testing update status
        Order order = new Order("3FyozOB3L1oyGXcVsUcA","/locations/SMU-subway","SMU-subway",1,10L,"61a3f59a-ca9e-4847-9285-5dfab55db96a","61a3f59a-ca9e-4847-9285-5dfab55db96a", new HashMap<MenuItem, Integer>(),"SIS",0);
        updateStatus(1,5,order);
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
                                                data.get("beaconMacAddress").toString(),
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

        String[] tipAmounts = {"$ 0.50", "$ 1.00", "$ 2.00", "$ 3.00", "$ 4.00", "$ 5.00"};
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
                    Double.parseDouble((values.get("tipAmount").substring(2))),
                    editView.getText().toString()
            );
        } else {
            MyApplication.pendingOrder.setLocation(MyApplication.locations.get(values.get("locationID")));
            MyApplication.pendingOrder.setLocationID(values.get("locationID"));
            MyApplication.pendingOrder.setLocationName(values.get("locationName"));
            MyApplication.pendingOrder.setTipAmount(Double.parseDouble((values.get("tipAmount").substring(2))));
            MyApplication.pendingOrder.setDeliveryLocation(editView.getText().toString());
        }

        Intent newOrderRequestMenu = new Intent(this, NewOrderRequestMenuActivity.class);
        startActivity(newOrderRequestMenu);
    }

    private void retrieveOrders() {
        MyApplication.db.collection("orders").whereEqualTo("status", 0).get().addOnCompleteListener(
                new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            allOrders.clear();
                            for (DocumentSnapshot document : task.getResult()) {
//                                String locationID = document.getId();
                                Map<String, Object> data = document.getData();

//                                ArrayList<Drink> drinks = new ArrayList<>();
//                                ArrayList<Food> food = new ArrayList<>();
//                                ArrayList<MenuItem> items = new ArrayList<>();

//                                Log.e(TAG, document.getId() + " => " + data);

                                // ArrayList of HashMaps, 1 for each item. Key(item and qty)
                                ArrayList<Map<String, String>> orderMenuItemsObj = (ArrayList<Map<String, String>>) data.get("menuItems");

                                // Menu Item includes more details of the menu like name. what is stored in DB is links
                                /**Can we push list of drinks into static list?**/
                                HashMap<MenuItem, Integer> menuItems = new HashMap<>();
                                for (Map<String, String> menuItem : orderMenuItemsObj) {

                                    // Change item string reference (menuItem.get("item")) to locations table into a menu item object, and put in menuitems hashmap
                                    String[] references = menuItem.get("item").split("/");
                                    Log.e("ref", Arrays.toString(references));

                                    String locationID = references[1];

                                    String tmp[] = references[2].split("\\[|\\]");
                                    Log.d(TAG, "onComplete: " + Arrays.toString(tmp));

                                    Location location = MyApplication.locations.get(locationID);
                                    MenuItem item = null;
                                    if(tmp[0].equals("food")) {
                                        Log.e("FoodItem", location.getMenu().getItems().get(Integer.parseInt(tmp[1])).toString());
                                        item = location.getMenu().getItems().get(Integer.parseInt(tmp[1]));
                                    } else if(tmp[0].equals("drinks")) {
                                        Log.e("DrinkItem:", location.getMenu().getItems().get(Integer.parseInt(tmp[1])).toString());
                                        item = location.getMenu().getItems().get(Integer.parseInt(tmp[1]));
                                    }

                                    menuItems.put(item, Integer.parseInt(String.valueOf(menuItem.get("qty"))));
                                    Order order = new Order(document.getId(),"/locations/"+locationID,locationID,(Double)data.get("tipAmount"),0L,"nil",data.get("customerUserID")+"",menuItems,data.get("deliveryLocation")+"",0);
                                    Log.e("Order: ",order.toString());
                                    allOrders.add(order);
                                }
                            }
//                            Log.d(TAG, "retrieveLocations: " + MyApplication.locations.get("tea-party"));
//                            setAdapterContents();
                        } else {
//                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                }
        );
    }
    //Update Order's status and ETA
    private void updateStatus(int status, double ETA, Order order){
        Map<String, Object> data = new HashMap<>();
        data.put("deliveryManUserID",MyApplication.user.getIdentifier());
        data.put("customerUserID",order.getCustomerUserID());
        data.put("deliveryLocation",order.getDeliveryLocation());
        data.put("locationID", order.getLocationID());
        data.put("menuItems",order.getMenuItems());
        data.put("status", status);
        data.put("ETA", ETA);
        MyApplication.db.collection("orders").document(order.getCustomerUserID()).set(data, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                 Log.e(TAG, "User updated.");
            }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error updating user", e);
            }
            });

    }

    public void goToFulfilAcceptedOrders(View view) {
        Intent fulfilAcceptedOrders = new Intent(this, FulfilAcceptedOrdersActivity.class);
        startActivity(fulfilAcceptedOrders);
    }
}

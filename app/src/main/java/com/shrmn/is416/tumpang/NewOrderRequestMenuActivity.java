package com.shrmn.is416.tumpang;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.shrmn.is416.tumpang.utilities.FCMRestClient;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

public class NewOrderRequestMenuActivity extends AppCompatActivity implements OrderConfirmationDialog.OrderConfirmationDialogListener {

    private static final String TAG = "OrderRequestMenu";
    private final int REQ_CODE_ADD_ITEM = 1;

    private ListView menuListView;
    private Intent addItemIntent;
    private MenuItemAdapter adapter;
    private Menu menu;
    private Location location;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order_request_menu);

        location = MyApplication.pendingOrder.getLocation();
        menu = location.getMenu();

        menuListView = findViewById(R.id.menu_list);
        addItemIntent = new Intent(this, AddItemActivity.class);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);

        if (resultCode != RESULT_CANCELED && data != null) {
            if (requestCode == REQ_CODE_ADD_ITEM) {
//                int quantity = data.getIntExtra("quantity", -1);
//                int menuItemPosition = data.getIntExtra("menuItemPosition", -1);
//
//                if(quantity > 0 && menuItemPosition >= 0) {
//                    MenuItem item = menu.getItems().get(menuItemPosition);
//                    orderedItems.put(item, quantity);
//                    Snackbar.make(findViewById(R.id.menu_list), "Added " + quantity + " of " + item.getName() + "!", Snackbar.LENGTH_SHORT).show();
//                } else {
//                    Snackbar.make(findViewById(R.id.menu_list), "Please select an item and quantity!", Snackbar.LENGTH_SHORT).show();
//                }
                refreshList();
            } else {
                Snackbar.make(findViewById(R.id.menu_list), "Adding menu item cancelled!", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void refreshList() {
        HashMap menuMap = MyApplication.pendingOrder.getMenuItems();
        ArrayList<MenuItem> menuItems = new ArrayList<>();
        for(Object o: menuMap.keySet()){
            MenuItem menuItem = (MenuItem) o;
            menuItem.setQuantity((Integer) menuMap.get(menuItem));
            menuItems.add(menuItem);
        }

        MyApplication.pendingOrder.setPendingMenuItems(menuItems);
        adapter = new MenuItemAdapter(this, 0, menuItems);
        menuListView.setAdapter(adapter);

        Snackbar.make(findViewById(R.id.menu_list), "Order list updated!", Snackbar.LENGTH_SHORT).show();
    }

    public void addItemButton(View view) {
        startActivityForResult(addItemIntent, REQ_CODE_ADD_ITEM);
        Log.d(TAG, "addItemButton: Called");
    }

    public void confirmButton(View view) {
        DialogFragment dialog = new OrderConfirmationDialog();
        dialog.show(getSupportFragmentManager(), "OrderConfirmationDialog");
    }


    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Snackbar.make(findViewById(R.id.menu_list), "Confirmation Cancelled!", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Task task = MyApplication.db.collection("orders").add(MyApplication.pendingOrder.composeOrder()).addOnSuccessListener(new OnSuccessListener <DocumentReference>()
        {
            @Override
            public void onSuccess (DocumentReference documentReference){
                Log.d(TAG, "DocumentSnapshot successfully written! ID: " + documentReference.getId());
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error writing document", e);
            }
        });
        Snackbar.make(findViewById(R.id.menu_list), "Order Confirmed!", Snackbar.LENGTH_SHORT).show();
    }


}

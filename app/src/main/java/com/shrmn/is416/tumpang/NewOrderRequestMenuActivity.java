package com.shrmn.is416.tumpang;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;

public class NewOrderRequestMenuActivity extends AppCompatActivity implements OrderConfirmationDialog.OrderConfirmationDialogListener {

    private static final String TAG = "OrderRequestMenu";
    private final int REQ_CODE_ADD_ITEM = 1;

    private ListView menuListView;
    private Intent addItemIntent;
    private ArrayAdapter<MenuItem> adapter;
    private Menu menu;
    private Location location;

    @Override
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

        if (requestCode == REQ_CODE_ADD_ITEM) {
            if (resultCode == RESULT_OK) {
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
                Snackbar.make(findViewById(R.id.menu_list), "Adding item cancelled!", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void refreshList() {

        adapter = new ArrayAdapter<>(
                NewOrderRequestMenuActivity.this,
                R.layout.menu_list_layout,
                R.id.mylistitem,
                new ArrayList<>(MyApplication.pendingOrder.getMenuItems().keySet())
        );

        menuListView.setAdapter(adapter);

        Snackbar.make(findViewById(R.id.menu_list), "Todo List Updated!", Snackbar.LENGTH_SHORT).show();
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
        Snackbar.make(findViewById(R.id.menu_list), "Order Confirmed!", Snackbar.LENGTH_SHORT).show();
    }
}

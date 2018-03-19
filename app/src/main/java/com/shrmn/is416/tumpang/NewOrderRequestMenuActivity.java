package com.shrmn.is416.tumpang;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class NewOrderRequestMenuActivity extends AppCompatActivity {

    private static final String TAG = "OrderRequestMenu";
    private final int REQ_CODE_ADD_ITEM = 1;

    private ListView menuListView;
    private Intent addItemIntent;
    private ArrayAdapter<MenuItem> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order_request_menu);

        menuListView = findViewById(R.id.menu_list);
        addItemIntent = new Intent(this, AddItemActivity.class);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + " resultCode=" + resultCode);

        if (requestCode == REQ_CODE_ADD_ITEM) {
            if (resultCode == RESULT_OK) {
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
}

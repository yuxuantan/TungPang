package com.shrmn.is416.tumpang;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MenuItemAdapter extends ArrayAdapter<MenuItem> {
    private Context context;
    private List<MenuItem> pendingMenuItems = new ArrayList<>();

    public MenuItemAdapter(@NonNull Context context, int resource, @NonNull List<MenuItem> objects) {
        super(context, resource, objects);
        this.pendingMenuItems = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "PLEASE REACH HERE PLEASE");
        View listItem = convertView;
        if (listItem == null) {
            Log.d(TAG, "listItem is null");
            listItem = LayoutInflater.from(context).inflate(R.layout.menu_list_layout, parent, false);
        }

        Log.d(TAG, "listItem populated " + listItem.toString());
        MenuItem detail = pendingMenuItems.get(position);
        TextView foodName = (TextView) listItem.findViewById(R.id.pendingFoodName);
        foodName.setText(detail.getName());

        TextView unitPrice = (TextView) listItem.findViewById(R.id.pendingUnitPrice);
        unitPrice.setText(detail.getUnitPrice()+"");

        TextView quantity = (TextView) listItem.findViewById(R.id.pendingUnitQty);
        quantity.setText("X " + detail.getQuantity()+"");

        return listItem;
    }
}

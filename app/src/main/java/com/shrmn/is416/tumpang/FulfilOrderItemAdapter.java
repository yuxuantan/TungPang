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

public class FulfilOrderItemAdapter extends ArrayAdapter<Order> {
    private Context context;
    private List<Order> fulfilOrderItems = new ArrayList<>();

    public FulfilOrderItemAdapter(@NonNull Context context, int resource, @NonNull List<Order> objects) {
        super(context, resource, objects);
        this.fulfilOrderItems = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.fulfil_orderlist_layout, parent, false);
        }

        Order fulfilOrder_Orderdetail = fulfilOrderItems.get(position);
        TextView foodOutletName = (TextView) listItem.findViewById(R.id.fulfil_order_foodOutlet_Name);
        foodOutletName.setText(fulfilOrder_Orderdetail.getLocationName());

        TextView meetup = (TextView) listItem.findViewById(R.id.fulfil_order_Meetup_location);
        meetup.setText("Meetup: " + fulfilOrder_Orderdetail.getDeliveryLocation());

        String displayMsg = "Order Details:";
        for(MenuItem item: fulfilOrder_Orderdetail.getMenuItems().keySet()){
            displayMsg+=("\n- " + fulfilOrder_Orderdetail.getMenuItems().get(item)+ "x "+item.getName());
        }

        TextView orderDetails = (TextView) listItem.findViewById(R.id.fulfil_order_details);
        orderDetails.setText(displayMsg);

        TextView orderTipAmount = (TextView) listItem.findViewById(R.id.fulfil_order_TipAmount);
        double tipAmount = fulfilOrder_Orderdetail.getTipAmount();
        if (tipAmount == 0.50){
            String fiftyCent = fulfilOrder_Orderdetail.getTipAmount() + "0";
            fiftyCent = fiftyCent.substring(2);
            orderTipAmount.setText(fiftyCent+" C");
        }else {
            orderTipAmount.setText("$ " + fulfilOrder_Orderdetail.getTipAmount());
        }

        return listItem;
    }
}


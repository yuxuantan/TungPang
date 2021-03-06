package com.shrmn.is416.tumpang;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

import static com.shrmn.is416.tumpang.MyApplication.user;

public class OrderConfirmationDialog extends DialogFragment {
    private static final String TAG = "OrderConfirmDlg";
    User user = MyApplication.user;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ArrayList<MenuItem> pendingMenuItems = (ArrayList<MenuItem>) MyApplication.pendingOrder.getPendingMenuItems();
        String outletName = MyApplication.pendingOrder.getLocation().getName();
        String outletAddress = MyApplication.pendingOrder.getLocation().getAddress();
        String deliveryLocation = MyApplication.pendingOrder.getDeliveryLocation();

        double tipAmount = MyApplication.pendingOrder.getTipAmount();
        int totalOrderQuantity = 0;
        double OrderBill = 0;
        for(MenuItem menuItem: pendingMenuItems ) {
            totalOrderQuantity += menuItem.getQuantity();
            OrderBill += menuItem.getUnitPrice() * menuItem.getQuantity();
        }
        String strTipAmount = String.format("%.2f", tipAmount);
        String strOrderBill = String.format("%.2f", OrderBill);

        double totalOrderBill = tipAmount + OrderBill;
        String strTotalOrderBill = String.format("%.2f", totalOrderBill);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_orderconfirmation, null);

        TextView orderUser = dialogView.findViewById(R.id.order_user);
        orderUser.setText("Order Username: " + user.getTelegramUsername());

        TextView outletLocation = dialogView.findViewById(R.id.outlet_name);
        outletLocation.setText("Outlet Name: " + outletName);

        TextView outletAdd = dialogView.findViewById(R.id.outlet_address);
        outletAdd.setText("Outlet Address: " + outletAddress);

        TextView deliveryLoc = dialogView.findViewById(R.id.delivery_location);
        deliveryLoc.setText("Delivery Location: " + deliveryLocation);

        TextView tip = dialogView.findViewById(R.id.final_tip_amount);
        tip.setText("Tip Amount: $ " + strTipAmount);

        TextView orderQty = dialogView.findViewById(R.id.order_qty);
        orderQty.setText("Order Total Quantity: " +totalOrderQuantity+"");

        TextView orderBill = dialogView.findViewById(R.id.order_bill);
        orderBill.setText("Order BillAmount: $ " + strOrderBill);

        TextView totalBill = dialogView.findViewById(R.id.total_orderbill);
        totalBill.setText("Order Total BillAmount: $ " + strTotalOrderBill);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(TAG, "onClick: Confirmed!");
                        mListener.onDialogPositiveClick(OrderConfirmationDialog.this);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        OrderConfirmationDialog.this.getDialog().cancel();
                        mListener.onDialogNegativeClick(OrderConfirmationDialog.this);
                    }
                });
        return builder.create();
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface OrderConfirmationDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    OrderConfirmationDialogListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity activity;
        if(context instanceof Activity) {
            activity = (Activity) context;
            // Verify that the host activity implements the callback interface
            try {
                // Instantiate the NoticeDialogListener so we can send events to the host
                mListener = (OrderConfirmationDialogListener) activity;
            } catch (ClassCastException e) {
                // The activity doesn't implement the interface, throw exception
                throw new ClassCastException(activity.toString()
                        + " must implement OrderConfirmationDialogListener");
            }
        }
    }


}

package com.shrmn.is416.tumpang;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.shrmn.is416.tumpang.utilities.FirstRunVariable;
import com.shrmn.is416.tumpang.utilities.VariableChangeListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyApplication.firstRunVariable.setVariableChangeListener(new VariableChangeListener() {
            @Override
            public void onVariableChanged(Object... variableThatHasChanged) {
                boolean isFirstRun = (Boolean) variableThatHasChanged[0];
                if(isFirstRun) {
                    Log.d(TAG, "onVariableChanged: Detected first-run, presenting FirstRunDialog");
                    DialogFragment dialog = new FirstRunDialog();
                    dialog.show(getSupportFragmentManager(), "FirstRunDialog");
                } else {
                    Log.d(TAG, "onVariableChanged: Not a first run.");
                }
            }
        });
    }

    public void newOrderRequest(View view) {
        Intent ptg = new Intent(this, NewOrderRequestActivity.class);
        startActivity(ptg);
    }

    public void fulfilOrderOrderRequest(View view) {
        Intent ptg = new Intent(this, FulfilOrdersActivity.class);
        startActivity(ptg);
    }
}

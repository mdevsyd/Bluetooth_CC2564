package com.mdevsolutions.cc2564.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mdevsolutions.cc2564.R;

public class OptionSelectActivity extends AppCompatActivity {

    private Button mViewDataBtn;
    private Button mConnectDeviceBtn;
    private String mKey="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option_select);

        mViewDataBtn = (Button) findViewById(R.id.viewDatBtn);
        mConnectDeviceBtn = (Button) findViewById(R.id.connectBtn);

        // If view daat is clicked, open dialog for user to enter key
        mViewDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog();
            }
        });

        // If Connect is clicked, open device list activity
        mConnectDeviceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent connectBTDeviceIntent = new Intent(OptionSelectActivity.this, DeviceListActivity.class);
                startActivity(connectBTDeviceIntent);
            }
        });
    }

    /**
     * Set up and display the custom dialogue and save the key to mKey variable.
     */
    private void showCustomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.api_key_request_dialog,null))
                .setPositiveButton(R.string.go, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Dialog keyDialog = (Dialog) dialog;

                EditText key;
                String keyString;

                key = (EditText) keyDialog.findViewById(R.id.apiKeyEt);
                mKey = key.getText().toString();

                // Create an intent to start the api activity when user hits "GO". Send mKey in intent.
                Intent apiIntent = new Intent(OptionSelectActivity.this, AmlDashboardActivity.class);
                apiIntent.putExtra("Key", mKey);
                startActivity(apiIntent);

            }
        })
                .setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();

    }


}


package com.mdevsolutions.cc2564.Activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mdevsolutions.cc2564.R;
import com.mdevsolutions.cc2564.Services.BtLoggerSPPService;
import com.mdevsolutions.cc2564.Utilities.Constants;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Activity populates views with AML data acquired over Bluetooth connection with
 * the Bluetooth external logger.
 */

public class BluetoothAmlDataActivity extends AppCompatActivity implements View.OnClickListener{

    private BluetoothAdapter mBluetoothAdapter = null;
    private String mConnectedDeviceAddress = null;
    private String mConnectedDeviceName = null;

    private BtLoggerSPPService mBTLoggerSPPService = null;

    //String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    private TextView mBattVTv, mBattTempTv, mSuppVTv, mSuppCTv;

    private Button mTestBtn;
    private Button mStopLiveBtn;
    private TimerTask mLiveDataTask;
    private Handler mSecHandler;
    private Timer mTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_aml_dashboard);

        mBattVTv = (TextView) findViewById(R.id.battVEt);
        mBattTempTv = (TextView) findViewById(R.id.battTempTv);
        mSuppCTv = (TextView) findViewById(R.id.supplyVTv);

        mTestBtn = (Button)findViewById(R.id.testBtn);
        mTestBtn.setOnClickListener(this);
        mStopLiveBtn = (Button)findViewById(R.id.stopLiveBtn);
        mStopLiveBtn.setOnClickListener(this);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(BluetoothAmlDataActivity.this, R.string.bt_unaavailble, Toast.LENGTH_LONG).show();
            finish();
        }

        // Create a local instance of SPPService
        mBTLoggerSPPService = new BtLoggerSPPService(this, mHandler);


        // Get the device name and address from the intent that intiitated this activity
        Intent commsIntent = getIntent();
        mConnectedDeviceAddress = commsIntent.getStringExtra(Constants.EXTRA_DEVICE_ADDRESS);
        mConnectedDeviceName = commsIntent.getStringExtra(Constants.EXTRA_DEVICE_NAME);

        // TODO:
        // get AML serial
        // get batt V, supp V and batt temp from AML over BT

    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case Constants.REQUEST_CONNECT_DEVICE:

                // When DeviceListActivity returns with a device to connect
                if (resultCode == RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(Constants.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mConnectedDeviceAddress = address;

                    // Check for Bluetooth permissions
                    int hasBluetoothPermissions = ContextCompat.checkSelfPermission(BluetoothAmlDataActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION);

                    if (hasBluetoothPermissions != PackageManager.PERMISSION_GRANTED) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(BluetoothAmlDataActivity.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            // add message to allow access to BT

                        }
                        return;
                    }
                    ActivityCompat.requestPermissions(BluetoothAmlDataActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            Constants.REQUEST_CODE_ASK_BT_PERMISSIONS);
                    return;


                }


                break;

            case Constants.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode != RESULT_OK) {
                    Log.d(Constants.DEBUG_TAG, "BT not enabled");

                    // TODO an alert would be better here!
                    finish();
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case Constants.REQUEST_CODE_ASK_BT_PERMISSIONS:
                // If the request gets cancelled, result[] is empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Attempt to connect to the device
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mConnectedDeviceAddress);

                    mBTLoggerSPPService.connect(device);
                }
        }
    }

    /**
     * Handler to handle messages back from the BluetoothSPP Service
     */
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    // Update the status by switching on arg1 --> state of BluetoothChatService
                    switch (msg.arg1) {
                        case BtLoggerSPPService.STATE_CONNECTED:
                            //setStatus(getString(R.string.title_connected) + " " + mConnectedDeviceName);
                            //TODO if the getString doesn't work try getString(int, object)
                            //mDataArrayAdapter.clear();
                            break;
                        case BtLoggerSPPService.STATE_CONNECTING:
                            //setStatus(getString(R.string.title_connecting));
                            break;
                        case BtLoggerSPPService.STATE_LISTEN:
                            //we are listening for a connection in the background
                            break;
                        case BtLoggerSPPService.STATE_NONE:
                            //setStatus(getString(R.string.title_disconnected));

                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuffer = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuffer);
                    //add the message to the listView adapter

                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuffer = (byte[]) msg.obj;
                    String readMeassege = new String(readBuffer);
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mBTLoggerSPPService == null) {
            mBTLoggerSPPService = new BtLoggerSPPService(this, mHandler);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBTLoggerSPPService != null) {
            mBTLoggerSPPService.stop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLiveDataTask != null){
            mTimer.cancel();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLiveDataTask != null){
            mTimer.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mBTLoggerSPPService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mBTLoggerSPPService.getState() == BtLoggerSPPService.STATE_NONE) {
                Log.d(Constants.DEBUG_TAG, " Starting SPP Service (onResume)");

                //start SPP Service
                startSPPService();

            }
        }
    }

    public int getConnectionState() {
        return mBTLoggerSPPService.getState();
    }

    public void startSPPService() {
        if (getConnectionState() == BtLoggerSPPService.STATE_NONE) {
            // There is no instance of the SPP service, start one.
            mBTLoggerSPPService.stop();
            mBTLoggerSPPService.start();


            // Get the BLuetoothDevice object
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mConnectedDeviceAddress);

            // Connect to the BT device
            mBTLoggerSPPService.connect(device);
        }
        else if (getConnectionState() == BtLoggerSPPService.STATE_CONNECTED) {

            //TODO do I need to stop/start here
            mBTLoggerSPPService.stop();
            mBTLoggerSPPService.start();
            Toast.makeText(this, "test method = STATE_CONNECTED", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.testBtn:
                getLiveBattV();
                break;
            case R.id.stopLiveBtn:
                stopLiveBatt();


        }

    }

    private void stopLiveBatt() {
        // If timer task is not null, stop the timer
        if(mLiveDataTask != null){
            mTimer.cancel();
        }

    }

    private void getLiveBattV() {

        mSecHandler = new Handler();
        mTimer = new Timer();


        mLiveDataTask = new TimerTask() {
            @Override
            public void run() {
                mSecHandler.post(new Runnable() {
                    public void run() {

                       // mBTLoggerSPPService.write("AW0");
                        Log.d(Constants.DEBUG_TAG, "Timer set off");
                    }
                });
            }};

        mTimer.schedule(mLiveDataTask, 500, 1000);
    }

    }




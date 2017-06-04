package com.mdevsolutions.cc2564.Activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
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

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Activity populates views with AML data acquired over Bluetooth connection with
 * the Bluetooth external logger.
 */

public class BluetoothAmlDataActivity extends AppCompatActivity implements View.OnClickListener {

    private BluetoothAdapter mBluetoothAdapter = null;
    private String mConnectedDeviceAddress = null;
    private String mConnectedDeviceName = null;

    private BtLoggerSPPService mBTLoggerSPPService = null;

    //String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    private TextView mBattVTv, mBattTempTv, mSuppVTv, mSuppCTv, mSerialTv;

    private Button mTestBtn;
    private Button mStopLiveBtn;
    private TimerTask mLiveDataTask;
    private Handler mSecHandler;
    private Handler mFiveSecHandler;
    private Timer mTimer;
    private TimerTask mUpdateDashTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_aml_dashboard);

        mBattVTv = (TextView) findViewById(R.id.battVTv);
        mBattTempTv = (TextView) findViewById(R.id.battTempTv);
        mSuppVTv = (TextView) findViewById(R.id.supplyVTv);
        mSerialTv = (TextView) findViewById(R.id.amlSerialTv);

        mTestBtn = (Button) findViewById(R.id.testBtn);
        mTestBtn.setOnClickListener(this);
        mStopLiveBtn = (Button) findViewById(R.id.stopLiveBtn);
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

        // This is main loop of this activity
        //getUnitSerial();
        updateBtDashboard();
        //mBTLoggerSPPService.write("AL0".getBytes());

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
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuffer = (byte[]) msg.obj;
                    //byte[] battVolt = Arrays.copyOfRange(readBuffer,3,7);
                    String readMeassege = new String(readBuffer);

                    Log.d(Constants.DEBUG_TAG, "MESSAGE_READ, AML response is: " + readMeassege);

                    byte[] command = Arrays.copyOfRange(readBuffer, 0, 3);
                    String commandStr = new String(command);
                    Log.d(Constants.DEBUG_TAG, "command is: " + commandStr);
                    switch (commandStr) {

                        // al2 --> 8 bytes = overall unit serial number
                        case "al2":
                            String serial = new String(Arrays.copyOfRange(readBuffer, 0,9));
                            Log.d(Constants.DEBUG_TAG, "serial is: " + Arrays.copyOfRange(readBuffer, 0, 9));
                            mSerialTv.setText(serial);
                            break;

                            // aw2 --> returns batt voltage and batt temperature
                            // 2 bytes data: first 4 bytes = battV * 1000, second 4 bytes =  battTemp*10
                        case "aw2":
                            String battV = new String(Arrays.copyOfRange(readBuffer, 3, 7));
                            String battT = new String(Arrays.copyOfRange(readBuffer, 7, 11));
                            try {
                                float battVolts = Float.valueOf(battV);
                                float battTemp = Float.valueOf(battT);
                                battVolts = battVolts / 1000;
                                battTemp = battTemp / 10;
                                Log.d(Constants.DEBUG_TAG, "battV updated: " + battVolts);
                                mBattVTv.setText(String.valueOf(battVolts));
                                mBattTempTv.setText(String.valueOf(battTemp));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                Toast.makeText(BluetoothAmlDataActivity.this, R.string.batt_data_error, Toast.LENGTH_SHORT).show();
                            }
                            break;

                        // ay1 --> returns external supply voltage*1000
                        case "ay1":
                            String extV = new String(Arrays.copyOfRange(readBuffer, 3, 7));
                            try {
                                float extVolts = (Float.valueOf(extV) / 1000);
                                mSuppVTv.setText(String.valueOf(extVolts));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                Toast.makeText(BluetoothAmlDataActivity.this, R.string.ext_data_error, Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
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
        if (mLiveDataTask != null) {
            mTimer.cancel();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLiveDataTask != null) {
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
        } else if (getConnectionState() == BtLoggerSPPService.STATE_CONNECTED) {

            //TODO do I need to stop/start here
            mBTLoggerSPPService.stop();
            mBTLoggerSPPService.start();
            Toast.makeText(this, "test method = STATE_CONNECTED", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.testBtn:
                // First check the SPP service is still active
                if (mBTLoggerSPPService.getState() != BtLoggerSPPService.STATE_CONNECTED) {
                    Toast.makeText(this, R.string.not_connected, Toast.LENGTH_LONG).show();
                    return;
                } else {
                    // SPP service is active, get live data

                    byte[] battVMsg = "RB11".getBytes();
                    getLiveData(battVMsg);
                    break;
                }

            case R.id.stopLiveBtn:
                stopLiveBatt();
                break;
        }

    }

    private void stopLiveBatt() {
        // If timer task is not null, stop the timer
        if (mLiveDataTask != null) {
            mTimer.cancel();
        }
    }

    private void getUnitSerial() {
        Log.d(Constants.DEBUG_TAG, "getUnitSerial called");
        Handler serialHandler = new Handler();
        serialHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(Constants.DEBUG_TAG,"runnung getUnitSerial");
                mBTLoggerSPPService.write("AL0".getBytes());

            }
        });
    }

    private void getLiveData(final byte[] msg) {

        mSecHandler = new Handler();
        mTimer = new Timer();


        mLiveDataTask = new TimerTask() {
            @Override
            public void run() {
                mSecHandler.post(new Runnable() {
                    public void run() {
                        mBTLoggerSPPService.write(msg);
                        // mBTLoggerSPPService.write("AW0");
                        Log.d(Constants.DEBUG_TAG, "Timer set off");
                    }
                });
            }
        };

        mTimer.schedule(mLiveDataTask, 500, 1000);
    }

    /**
     * Method to update AML Dashboard widgets over Bluetooth.
     * Requests are written to the CC2564 once every 5 seconds
     * using the timer.
     */
    public void updateBtDashboard() {

        mFiveSecHandler = new Handler();
        mTimer = new Timer();
        mLiveDataTask = new TimerTask() {
            @Override
            public void run() {
                mFiveSecHandler.post(new Runnable() {
                    public void run() {
                        // Here we wish to update each widget's values once every 5 secs
                        // Request batt voltage and batt temperature
                        mBTLoggerSPPService.write("AW0".getBytes());

                        // Request external supply voltage
                        //mBTLoggerSPPService.write("AY0".getBytes());
                    }
                });
            }
        };

        mTimer.schedule(mLiveDataTask, 500, 5000);


        final Handler extHandler = new Handler();
        Timer externalTimer = new Timer();
        TimerTask externalTimerTask = new TimerTask() {
            @Override
            public void run() {
                extHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Request external supply voltage
                        mBTLoggerSPPService.write("AY0".getBytes());
                    }
                });
            }
        };
        externalTimer.schedule(externalTimerTask, 600, 5000);


        final Handler serialHandler = new Handler();
        Timer serialTimer = new Timer();
        TimerTask serialTimerTask = new TimerTask() {
            @Override
            public void run() {
                serialHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(Constants.DEBUG_TAG,"running getUnitSerial");
                        mBTLoggerSPPService.write("AL0".getBytes());
                        Log.d(Constants.DEBUG_TAG, "getting serial inside serialTimerTask ");
                    }
                });
            }
        };
        serialTimer.schedule(serialTimerTask,0,1000);



    }




}




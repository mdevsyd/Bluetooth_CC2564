package com.mdevsolutions.cc2564.Activities;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

    private BtLoggerSPPService mBTLoggerSPPService = null;

    //String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    private TextView mBattVTv, mBattTempTv, mSuppVTv, mSerialTv, mCommentTv, mNameTv;
    private ImageView mBattVIv, mTempIv, mSuppIv;

    private Button mTestBtn;
    private ImageButton mRefreshBtn;
    private TimerTask mBatteryTask;
    private Handler mSecHandler, mBatteryHandler;
    private Timer mBattTimer, mCommentTimer, mExternalTimer, mSerialTimer, mNameTimer ;
    private TimerTask mExternalTimerTask, mCommentTimerTask, mSerialTimerTask, mNameTimerTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_aml_dashboard);

        mBattVTv = (TextView) findViewById(R.id.battVTv);
        mBattTempTv = (TextView) findViewById(R.id.battTempTv);
        mSuppVTv = (TextView) findViewById(R.id.supplyVTv);
        mSerialTv = (TextView) findViewById(R.id.amlSerialTv);
        mCommentTv = (TextView) findViewById(R.id.unitCommentTv);
        mNameTv = (TextView) findViewById(R.id.unitNameTv);

        mBattVIv = (ImageView) findViewById(R.id.battIv);
        mBattVIv.setOnClickListener(this);
        mTempIv = (ImageView) findViewById(R.id.battTempIv);
        mTempIv.setOnClickListener(this);
        mSuppIv = (ImageView) findViewById(R.id.solarIv);
        mSuppIv.setOnClickListener(this);


        mRefreshBtn = (ImageButton) findViewById(R.id.refreshBtn);
        mRefreshBtn.setOnClickListener(this);
        mTestBtn = (Button) findViewById(R.id.testBtn);
        mTestBtn.setOnClickListener(this);


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
        //mConnectedDeviceName = commsIntent.getStringExtra(Constants.EXTRA_DEVICE_NAME);

        updateBtDashboard();

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

                        // sf7 --> overall unit comment
                        case "sf7":
                            String unitName = new String(Arrays.copyOfRange(readBuffer, 3, 29));
                            mNameTv.setText(unitName);
                            break;

                        // sd7 --> overall unit comment
                        case "sd7":
                            String unitComment = new String(Arrays.copyOfRange(readBuffer, 3, 29));
                            mCommentTv.setText(unitComment);
                            break;

                        // al2 --> 8 bytes = overall unit serial number
                        case "al2":
                            String serial = new String(Arrays.copyOfRange(readBuffer, 0, 9));
                            mSerialTv.setText(serial);
                            break;

                        // aw2 -->  batt voltage and batt temperature
                        // 4 bytes = battV * 1000 then 4 bytes =  battTemp*10
                        case "aw2":
                            String battV = new String(Arrays.copyOfRange(readBuffer, 3, 7));
                            String battT = new String(Arrays.copyOfRange(readBuffer, 7, 11));
                            try {
                                float battVolts = Float.valueOf(battV) / 1000;
                                float battTemp = Float.valueOf(battT) / 10;
                                mBattVTv.setText(String.valueOf(battVolts));
                                mBattTempTv.setText(String.valueOf(battTemp));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                Toast.makeText(BluetoothAmlDataActivity.this, R.string.batt_data_error, Toast.LENGTH_SHORT).show();
                            }
                            break;

                        // ay1 -->  external supply voltage*1000
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
                        case "rb9":
                            Log.d(Constants.DEBUG_TAG,"rb9 response: "+readMeassege);
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
        cancelActiveTimers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelActiveTimers();
        if (mBTLoggerSPPService != null) {
            mBTLoggerSPPService.stop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelActiveTimers();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelActiveTimers();

    }

    @Override
    public void onResume() {
        super.onResume();
        cancelActiveTimers();

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
        updateBtDashboard();

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
            mBTLoggerSPPService.stop();
            mBTLoggerSPPService.start();
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

                    byte[] battVMsg = "RB11,0,0,0".getBytes();
                   // getLiveData(battVMsg);
                    break;
                }

            case R.id.refreshBtn:
                updateBtDashboard();
                break;

            // The following case is for when user click on any of the widget images,
            // they will be prompted if they'd like to go into live mode.
            case (R.id.battIv):
                Log.d(Constants.DEBUG_TAG, "clicked on :"+v.getId());
                showCustomDialog("battery voltage");
                break;
        }

    }


    /**
     * Setup and display dialogue asking user if they want to view live mode.
     */
    private void showCustomDialog(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();
        builder.setTitle(R.string.live_dialogue_titile);
        builder.setMessage("Would you like to view live "+type+ " data now?");

        builder.setPositiveButton(R.string.go, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        // Create an intent to start the api activity when user hits "GO". Send mKey in intent.
                        Intent liveIntent = new Intent(BluetoothAmlDataActivity.this, LiveChartActivity.class);
                        startActivity(liveIntent);
                        cancelActiveTimers();


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

    /**
     * Cancels any active timers and timerTasks, so as to eliminate
     * possibility of running processes when they are not needed.
     */
    private void cancelActiveTimers() {
        if (mBatteryTask != null) {
            mBattTimer.cancel();
            mBattTimer.purge();
            mBatteryTask.cancel();
        }
        if(mCommentTimerTask !=null){
            mCommentTimer.cancel();
            mCommentTimer.purge();
            mCommentTimerTask.cancel();
        }
        if(mExternalTimerTask !=null){
            mExternalTimer.cancel();
            mExternalTimer.purge();
            mExternalTimerTask.cancel();
        }
        if(mSerialTimerTask !=null){
            mSerialTimer.cancel();
            mSerialTimer.purge();
            mSerialTimerTask.cancel();
        }
        if(mNameTimerTask !=null){
            mNameTimer.cancel();
            mNameTimer.purge();
            mNameTimerTask.cancel();
        }
    }

    private void getLiveData(final byte[] msg) {
        mSecHandler = new Handler();
        mBattTimer = new Timer();

        mBatteryTask = new TimerTask() {
            @Override
            public void run() {
                mSecHandler.post(new Runnable() {
                    public void run() {
                        mBTLoggerSPPService.write(msg);
                        // mBTLoggerSPPService.write("AW0");

                    }
                });
            }
        };

        mBattTimer.schedule(mBatteryTask, 500, 1000);
    }

    /**
     * Method to update AML Dashboard widgets over Bluetooth.
     * Requests are written to the CC2564 TaskTimer schedules.
     */
    public void updateBtDashboard() {

        requestBattDetails();
        requestExternalSupply();
        requestSerialNumber();
        requestUnitName();
        requestUnitComment();

    }

    /**
     * Request the overall unit name set by user after 1 second delay.
     */
    private void requestUnitName() {
        final Handler nameHandler = new Handler();
        mNameTimer = new Timer();
        mNameTimerTask = new TimerTask() {
            @Override
            public void run() {
                nameHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mBTLoggerSPPService.write("SF0".getBytes());
                    }
                });
            }
        };
        mNameTimer.schedule(mNameTimerTask, 1000, 1000);
    }

    /**
     * Request the overall unit comment set by user after 1.05 sec delay.
     */
    private void requestUnitComment() {

        final Handler commentHandler = new Handler();
        mCommentTimer = new Timer();
        mCommentTimerTask = new TimerTask() {
            @Override
            public void run() {
                commentHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mBTLoggerSPPService.write("SD0".getBytes());
                    }
                });
            }
        };
        mCommentTimer.schedule(mCommentTimerTask, 1050, 1000);
    }

    /**
     * Request serial number from CC2564 after 1.1 second delay.
     */
    private void requestSerialNumber() {
        final Handler serialHandler = new Handler();
        mSerialTimer = new Timer();
        mSerialTimerTask = new TimerTask() {
            @Override
            public void run() {
                serialHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mBTLoggerSPPService.write("AL0".getBytes());
                    }
                });
            }
        };
        mSerialTimer.schedule(mSerialTimerTask, 1100, 1000);
    }

    /**
     * Request external supply details from CC2564 after 600ms delay
     * once every 5 seconds.
     */
    private void requestExternalSupply() {
        final Handler extHandler = new Handler();
        mExternalTimer = new Timer();
        mExternalTimerTask = new TimerTask() {
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
        mExternalTimer.schedule(mExternalTimerTask, 600, 5000);
    }

    /**
     * Request battery voltage and temperature form CC2564 after 500ms delay
     * once every 5 seconds.
     */
    private void requestBattDetails() {
        mBatteryHandler = new Handler();
        mBattTimer = new Timer();
        mBatteryTask = new TimerTask() {
            @Override
            public void run() {
                mBatteryHandler.post(new Runnable() {
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

        mBattTimer.schedule(mBatteryTask, 500, 5000);
    }

    /**
     *
     */


}

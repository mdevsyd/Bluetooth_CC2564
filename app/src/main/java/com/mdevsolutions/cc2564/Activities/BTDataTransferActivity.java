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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mdevsolutions.cc2564.Services.BluetoothSPPService;
import com.mdevsolutions.cc2564.R;
import com.mdevsolutions.cc2564.Utilities.Constants;

public class BTDataTransferActivity extends AppCompatActivity {

    private TextView mConnectedDevice;
    private String mConnectedDeviceName = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private ListView mDataView;
    private EditText mOutEditText;
    private Button mSendBtn;

    private String mConnectedDeviceAddress = null;

    private BluetoothSPPService mSPPService = null;

    //Array adapter for the data transfer thread
    private ArrayAdapter<String> mDataArrayAdapter;

    //String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_array_view);

        // Setup this activity's actionBar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mOutEditText = (EditText) findViewById(R.id.textOutEt);
        mSendBtn = (Button) findViewById(R.id.sendBtn);
        mDataView = (ListView) findViewById(R.id.dataLv);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(BTDataTransferActivity.this, R.string.bt_unaavailble, Toast.LENGTH_LONG).show();
            finish();
        }

        // Get the device name and address from the intent that intiitated this activity
        Intent commsIntent = getIntent();
        mConnectedDeviceAddress = commsIntent.getStringExtra(Constants.EXTRA_DEVICE_ADDRESS);
        mConnectedDeviceName = commsIntent.getStringExtra(Constants.EXTRA_DEVICE_NAME);
        //TODO bring over all the BT testing if on etc to this activity.

        // Create a local instance of SPPService
        mSPPService = new BluetoothSPPService(this, mHandler, mDataView);
        setupView();

        //TODO get rid of this - currently this method tests BT remote device connection
        //testMethod();



    }

    private void testMethod() {
        if (getConnectionState() == BluetoothSPPService.STATE_NONE) {
            Toast.makeText(this, "test method = STATE_NONE", Toast.LENGTH_SHORT).show();
            // Start the Bluetooth services
            mSPPService.stop();
            mSPPService.start();

            // Get the BLuetoothDevice object
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mConnectedDeviceAddress);

            // Connect to the BT device
            mSPPService.connect(device);
            Log.d(Constants.DEBUG_TAG,"testMethod connected to device: "+ mConnectedDeviceName+", "+ mConnectedDeviceAddress);


            //Intent serverIntent = new Intent(this, DeviceListActivity.class);
            //startActivityForResult(serverIntent, Constants.REQUEST_CONNECT_DEVICE);
        } else {
            if (getConnectionState() == BluetoothSPPService.STATE_CONNECTED) {
                mSPPService.stop();
                mSPPService.start();
                Toast.makeText(this, "test method = STATE_CONNECTED", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "test method = STATE_CONNECTING", Toast.LENGTH_SHORT).show();
            }

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        //TODO
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mSPPService == null) {
            setupView();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSPPService != null) {
            mSPPService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mSPPService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mSPPService.getState() == BluetoothSPPService.STATE_NONE) {
                Log.d(Constants.DEBUG_TAG," Starting SPP Service (onResume)");

                //start SPP Service
                mSPPService.start();

            }
        }
    }

    private void setupView() {
        //initialise array adapter and set it to the listview
        mDataArrayAdapter = new ArrayAdapter<String>(this, R.layout.data_msg);
        mDataView.setAdapter(mDataArrayAdapter);
        //Initialise the edit text and make a listener for when user hits the return key when finished typing text
        //mOutEditText.setOnEditorActionListener(mWriteListener);

        //Initialise the send button with on click listener
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //send message including what user has typed into the edit text
                //TODO what if this s empty?
                String message = mOutEditText.getText().toString();
                sendMessage(message);
            }
        });

        // Initialize the BluetoothSPPService to perform bluetooth connections
        mSPPService = new BluetoothSPPService(this, mHandler, mDataView);

        // Initialise the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
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
                        case BluetoothSPPService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected) + " " + mConnectedDeviceName);
                            //TODO if the getString doesn't work try getString(int, object)
                            //mDataArrayAdapter.clear();
                            break;
                        case BluetoothSPPService.STATE_CONNECTING:
                            setStatus(getString(R.string.title_connecting));
                            break;
                        case BluetoothSPPService.STATE_LISTEN:
                            //we are listening for a connection in the background
                            break;
                        case BluetoothSPPService.STATE_NONE:
                            setStatus(getString(R.string.title_disconnected));
                            mDataArrayAdapter.clear();
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuffer = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuffer);
                    //add the message to the listView adapter
                    mDataArrayAdapter.add(" Transmit:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuffer = (byte[]) msg.obj;
                    String readMeassege = new String(readBuffer);
                    mDataArrayAdapter.add(mConnectedDeviceName + " Receive:  " + readMeassege);
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /**
     * An action listener for the text out edit text. this listens for return key
     */
    /*private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = v.getText().toString();
                Log.d(Constants.DEBUG_TAG,"Trying to send: "+message);
                sendMessage(message);
            }
            return true;
        }
    };
     */

    /**
     * Sends a message from one device to the other
     * @param message - the String of text taken from user input
     */
    private void sendMessage(String message) {

        //Check we are connected to chat service, display toast if not connected to any device.
        if (mSPPService.getState() != BluetoothSPPService.STATE_CONNECTED){
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_LONG).show();
            return;
        }

        //check the message is not empty
        if (message.length() > 0) {
            byte[] msgToSend = message.getBytes();
            mSPPService.write(msgToSend);

            //clear the msg buffer
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * Updates the status on the action bar.
     */
    private void setStatus(CharSequence charSeq) {
        ActionBar actionBar = getSupportActionBar();
        //final app.ActionBar actionBar = getSupportActionBar();
        //ensure there is an actionbar in the activity
        if (null == actionBar) {
            return;
        }
        actionBar.setTitle(charSeq);
    }

    public int getConnectionState() {
        return mSPPService.getState();
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
                    int hasBluetoothPermissions = ContextCompat.checkSelfPermission(BTDataTransferActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION);

                    if(hasBluetoothPermissions != PackageManager.PERMISSION_GRANTED){
                        if(!ActivityCompat.shouldShowRequestPermissionRationale(BTDataTransferActivity.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION)){
                            // TODO add message to allow access to BT

                        }
                        return;
                    }
                    ActivityCompat.requestPermissions(BTDataTransferActivity.this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
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

        switch(requestCode){
            case Constants.REQUEST_CODE_ASK_BT_PERMISSIONS:
                // If the request gets cancelled, result[] is empty
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // Attempt to connect to the device
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mConnectedDeviceAddress);

                    mSPPService.connect(device);
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.device_action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle user selection from the overflow menu
        switch (item.getItemId()){
            case R.id.action_connect:
                testMethod();
                return true;
            case R.id.action_disconnect:
                mSPPService.stop();
                return true;
            case R.id.action_plot:
                // For now, open a new activity to see data plots and options
                Intent plotIntent = new Intent(BTDataTransferActivity.this, DataViewerActivity.class);
                setResult(RESULT_OK);
                startActivity(plotIntent);
                return true;
            case R.id.test_dashboard:
                Intent btAmlDashIntent =  new Intent(BTDataTransferActivity.this, BluetoothAmlDataActivity.class);
                startActivity(btAmlDashIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}


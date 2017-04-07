package com.mdevsolutions.cc2564;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BTDataTransfer extends AppCompatActivity {

    private TextView mConnectedDevice;
    private String mConnectedDeviceName = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private ListView mDataView;
    private EditText mOutEditText;
    private Button mSendBtn;

    //TODO create the SPPService before un commenting the following line
    private static BluetoothSPPService mSPPService = null;

    //Array adapter for the data transfer thread
    private ArrayAdapter<String> mDataArrayAdapter;

    //String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_array_view);

        mOutEditText = (EditText) findViewById(R.id.textOutEt);
        mSendBtn = (Button) findViewById(R.id.sendBtn);
        mDataView = (ListView) findViewById(R.id.dataLv);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(BTDataTransfer.this, R.string.bt_unaavailble, Toast.LENGTH_LONG).show();
            finish();
        }
        //TODO bring over all the BT testing if on etc to this activity.

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
        } else if ( mSPPService == null) {
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
                // Start the Bluetooth chat services
                mSPPService.start();
            }
        }
    }

    private void setupView() {
        //initialise array adapter and set it to the listview
        mDataArrayAdapter = new ArrayAdapter<String>(this, R.layout.data_array_view);
        mDataView.setAdapter(mDataArrayAdapter);

        //Initialise the edit text and make a listener for when user hits the return key when finished typing text
        mOutEditText.setOnEditorActionListener(mWriteListener);

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
        mSPPService = new BluetoothSPPService(this, mHandler);
    }

    /**
     * Hndler to handle mesages back from the BluetoothChat Service
     */
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case Constants.MESSAGE_STATE_CHANGE:
                    // Update the status by switching on arg1 --> state of BluetoothChatService
                    switch(msg.arg1){
                        case BluetoothSPPService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected)+" "+ mConnectedDeviceName);
                            //TODO if the getString doesn't work try getString(int, object)
                            mDataArrayAdapter.clear();
                            break;
                        case BluetoothSPPService.STATE_CONNECTING:
                            setStatus(getString(R.string.title_connecting));
                            break;
                        case BluetoothSPPService.STATE_LISTEN:
                            //we are listening for a connection in the background
                            break;
                        case BluetoothSPPService.STATE_NONE:
                            setStatus(getString(R.string.title_disconnected));
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuffer = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuffer);
                    //add the message to the listView adapter
                    mDataArrayAdapter.add("Transmit:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuffer = (byte[]) msg.obj;
                    String readMeassege = new String(readBuffer);
                    mDataArrayAdapter.add(mConnectedDeviceName +"Receive:  " + readMeassege);
                    break;

            }
        }
    };

    /**
     * An action listener for the text out edit text. this listens for return key
     */
    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener(){

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = v.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    /**
     * Sends a message from one device to the other
     * @param message - the String of text taken from user input
     */
    private void sendMessage(String message) {
        //Check we are connected to chat service, display toast if not connected to any device.
//        if (mSPPService.getState() != BluetoothSPPService.STATE_CONNECTED){
//            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_LONG).show();
//            return;
//        }

        //check the message is not empty
        if (message.length() > 0){
            byte[] msgToSend = message.getBytes();
            mSPPService.write(msgToSend);

            //clear the msg buffer
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * Updates the status on the action bar.
     *
     */
    private void setStatus(CharSequence charSeq){
        final android.app.ActionBar actionBar = getActionBar();
        //ensure there is an actionbar in the activity
        if (null == actionBar){
            return;
        }
        actionBar.setSubtitle(charSeq);
    }

}


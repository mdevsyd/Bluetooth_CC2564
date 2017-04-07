package com.mdevsolutions.cc2564;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

/**
 * Created by Michi on 7/04/2017.
 */

public class BluetoothSPPService {

    // fields
    private final int mState;
    private final int mNewState;
    private BluetoothAdapter mBtAdapter;
    private final Handler mHandler;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public BluetoothSPPService(Context context, Handler handler){
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mNewState = mState;
        mHandler = handler;
    }



    public void stop() {
    }

    /**
     * Returns the current state of the connection
     * @return
     */
    public int getState() {
        return mState;
    }

    public void start() {
    }

    public void write(byte[] msgToSend) {
    }

    public void connect(BluetoothDevice device) {
        Log.d(Constants.DEBUG_TAG,"BluetoothChatService connectDevice ");
    }

}

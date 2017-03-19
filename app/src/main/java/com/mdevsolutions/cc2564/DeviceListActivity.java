package com.mdevsolutions.cc2564;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {

    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter mPairedDevicesArrayAdapter;
    private ArrayAdapter mNewDevicesArrayAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(Constants.DEBUG_TAG,"DeviceListActivity onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Button scanBtn = (Button)findViewById(R.id.button_scan);

        setResult(Activity.RESULT_CANCELED);

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDiscovery();
                // make the current button disappear
                v.setVisibility(View.GONE);
            }
        });
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBtCompatible();
        //enableBt();
        // Setup up the two array adapters one for each type of device list. (paried and discovered).
        mPairedDevicesArrayAdapter = new ArrayAdapter(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter(this, R.layout.device_name);


        //obtain currently paired device list, if any, add them to the ArrayAdapter
        ListView pairedListView = (ListView)findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        ListView newDevicesListView = (ListView)findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        //register to receive broadcasts when a device is found
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // register to receive broadcast when the discovery is finished
        filter = new IntentFilter((BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        this.registerReceiver(mReceiver,filter);

        createDiscoverabilityFilter();

        getPairedDevices();


    }

    /**
     * Create filter and register to receive notification of discoverabilty changes
     * (from http://stackoverflow.com/questions/30222409/android-broadcast-receiver-bluetooth-
     * events-catching/30292660#30292660)
     */
    private void createDiscoverabilityFilter() {
        IntentFilter discoverabilityFilter = new IntentFilter();
        discoverabilityFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        discoverabilityFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        discoverabilityFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mReceiver2, discoverabilityFilter);
    }

    private Set<BluetoothDevice> getPairedDevices() {
        Log.d(Constants.DEBUG_TAG, "DeviceListActivity, getPairedDevices() method call");
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        Log.d(Constants.DEBUG_TAG," pairedDevice LIst length = "+pairedDevices.size());
        if(pairedDevices.size()>0){
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices){
                mPairedDevicesArrayAdapter.add(device.getName()+ "\n" +device.getAddress());
            }
        }else{
            String noDev = getResources().getText(R.string.no_paired_devices).toString();
            mPairedDevicesArrayAdapter.add(noDev);
        }
        return pairedDevices;
    }

    private void enableBt() {
        Log.d(Constants.DEBUG_TAG, "DeviceListActivity, enableBt() method call");
        if (!mBtAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
        }
    }

    private void checkBtCompatible() {
        Log.d(Constants.DEBUG_TAG, "DeviceListActivity, checkBtCompatible() method call");
        //check if BT is supported on host device
        if (mBtAdapter == null) {
            Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void startDiscovery() {
        Log.d(Constants.DEBUG_TAG, "DeviceListActivity, startDiscovery() method call");
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        //TODO Progress bar of some sort
        //cancel any current discoveries if any exist
        if (mBtAdapter.isDiscovering()){
            mBtAdapter.cancelDiscovery();
        }
        mBtAdapter.startDiscovery();
    }

    /**
     * On click listener for the paired and new devices in lists
     */
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            //need to cancel the discovery as it is resource heavy
            mBtAdapter.cancelDiscovery();

            // MAC hardware address of the device is the last 17 chars of the view
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length()-17);
            String name = info.substring(0, info.length()-17);
            Log.d(Constants.DEBUG_TAG, "item was clicked " + address);

            Toast.makeText(DeviceListActivity.this, name+", "+address,Toast.LENGTH_SHORT).show();

            //enable local device discoverability
            enableHostDiscoverability();

            //create intent including the hardware address

//            Intent viewDeviceIntent = new Intent(DeviceListActivity.this, SelectedDeviceActivity.class);
//            viewDeviceIntent.putExtra(Constants.EXTRA_DEVICE_ADDRESS, address);
//            viewDeviceIntent.putExtra(Constants.EXTRA_DEVICE_NAME, name);
//            viewDeviceIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(viewDeviceIntent);

        }
    };

    /**
     * Makes the host device discoverable to other BT devices for 2 mins (120 seconds)
     */
    private void enableHostDiscoverability() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,120);
        startActivityForResult(discoverableIntent, Constants.REQUEST_HOST_DISCOVERABILITY);

    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //get the action to check if device was found
            String action = intent.getAction();

            switch(action){
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //check if the device has already paired
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED){
                        mNewDevicesArrayAdapter.add(device.getName()+ "\n" +device.getAddress());
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    if (mNewDevicesArrayAdapter.getCount()==0){
                        //change display to show no devices were found
                        String noDev = getResources().getText(R.string.no_discovered_device).toString();
                        mNewDevicesArrayAdapter.add(noDev);
                        mBtAdapter.cancelDiscovery();
                    }
                    Button scanBtn = (Button)findViewById(R.id.button_scan);
                    scanBtn.setVisibility(View.VISIBLE);
                    mBtAdapter.cancelDiscovery();
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    if(mBtAdapter.getState() == BluetoothAdapter.STATE_OFF){
                        enableBt();
                    }
                    break;
            }
        }
    };

    private BroadcastReceiver mReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                //int previous = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE, BluetoothAdapter.ERROR);
                Log.d(Constants.DEBUG_TAG, "ScanMode has changed! ");
                int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                switch (scanMode){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(Constants.DEBUG_TAG, "scanMode: CONNECTABLE");
                        Toast.makeText(DeviceListActivity.this,R.string.connectable, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(Constants.DEBUG_TAG, "scanMode: CONNECTABLE_DISCOVERABLE");
                        Toast.makeText(DeviceListActivity.this,R.string.connectable_discoverable, Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(Constants.DEBUG_TAG, "scanMode: NONE");
                        Toast.makeText(DeviceListActivity.this,R.string.discovery_error, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };

    @Override
    protected void onPause() {
        Log.d(Constants.DEBUG_TAG, "DeviceListActivity,  onPause()");
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        Log.d(Constants.DEBUG_TAG, "DeviceListActivity, onResume() method call");
        super.onResume();
        //enable BT on device
        enableBt();
        //getPairedDevices();
    }

    @Override
    protected void onDestroy() {
        Log.d(Constants.DEBUG_TAG, "DeviceListActivity, onResume() method call");
        super.onDestroy();
        // always cancel the discovery
        if (mBtAdapter !=null) {
            mBtAdapter.cancelDiscovery();
        }
        //unregister listeners to the broadcasts
        this.unregisterReceiver(mReceiver);
        this.unregisterReceiver(mReceiver2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //check requestCode to know what requested the result
        switch (requestCode) {
            case Constants.REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "Bluetooth successfully enabled.", Toast.LENGTH_LONG).show();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "Bluetooth enabling failed. Exiting.", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case Constants.REQUEST_HOST_DISCOVERABILITY:
                if (resultCode == RESULT_CANCELED){
                    Toast.makeText(DeviceListActivity.this, R.string.discovery_error, Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }
}

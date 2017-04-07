package com.mdevsolutions.cc2564;

import java.util.UUID;

/**
 * Various project Constants
 */

public class Constants {

    public static final String EXTRA_DEVICE_ADDRESS = "device_address";
    public static final String EXTRA_DEVICE_NAME = "device_name";
    public static final String LOCAL_DEVICE_NAME = "local_device_name";
    public static final UUID MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final UUID SECURE_UUID = UUID.fromString("1f940d0e-017e-11e7-93ae-92361f002671");
    public static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    public static final String DEBUG_TAG = "debug";

    public static final int REQUEST_ENABLE_BT = 100;
    public static final int REQUEST_CONNECT_DEVICE_SECURE = 200;
    public static final int REQUEST_CONNECT_DEVICE_INSECURE = 300;
    public static final int REQUEST_HOST_DISCOVERABILITY = 400;

    // Messages sent from BT chat service handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;


}

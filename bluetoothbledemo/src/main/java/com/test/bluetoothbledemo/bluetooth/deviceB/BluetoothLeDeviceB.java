package com.test.bluetoothbledemo.bluetooth.deviceB;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import com.test.bluetoothbledemo.bluetooth.BluetoothLeDeviceBase;

public class BluetoothLeDeviceB extends BluetoothLeDeviceBase {

    private static final String TAG = "BluetoothLeDeviceB";

    public BluetoothLeDeviceB(Context context) {
        super(context);
    }


    @Override
    public void parseData(BluetoothGattCharacteristic characteristic) {
        Log.e(TAG, "BluetoothLeDeviceB-parseData: -------解析数据");
    }
}

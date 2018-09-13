package com.test.bluetoothbledemo.bluetooth.deviceA;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import com.test.bluetoothbledemo.bluetooth.BluetoothLeDeviceBase;

public class BluetoothLeDeviceA extends BluetoothLeDeviceBase {

    private static final String TAG = "BluetoothLeDeviceA";

    public BluetoothLeDeviceA(Context context) {
        super(context);
    }


    @Override
    public void parseData(BluetoothGattCharacteristic characteristic) {
        Log.e(TAG, "BluetoothLeDeviceA-parseData: -------解析数据");
    }
}

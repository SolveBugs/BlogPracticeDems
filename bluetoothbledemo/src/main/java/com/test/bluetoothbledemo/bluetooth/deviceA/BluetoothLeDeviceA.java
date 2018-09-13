package com.test.bluetoothbledemo.bluetooth.deviceA;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import com.test.bluetoothbledemo.bluetooth.BluetoothLeDeviceBase;

public class BluetoothLeDeviceA extends BluetoothLeDeviceBase {

    private static final String TAG = "BluetoothLeDeviceA";
    //根据具体硬件进行设置
    public static String DEVICEA_UUID_SERVICE = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static String DEVICEA_UUID_CHARACTERISTIC = "0000fff4-0000-1000-8000-00805f9b34fb";

    //一般不用修改
    public static String DEVICEA_UUID_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    public BluetoothLeDeviceA(Context context) {
        super(context);
        UUID_SERVICE = DEVICEA_UUID_SERVICE;
        UUID_CHARACTERISTIC = DEVICEA_UUID_CHARACTERISTIC;
        UUID_DESCRIPTOR = DEVICEA_UUID_DESCRIPTOR;
    }


    @Override
    public void parseData(BluetoothGattCharacteristic characteristic) {
        Log.e(TAG, "BluetoothLeDeviceA-parseData: -------解析数据");
    }
}

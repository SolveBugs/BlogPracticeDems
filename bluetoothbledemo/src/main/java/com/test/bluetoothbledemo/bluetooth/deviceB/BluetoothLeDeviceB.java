package com.test.bluetoothbledemo.bluetooth.deviceB;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import com.test.bluetoothbledemo.bluetooth.BluetoothLeDeviceBase;

public class BluetoothLeDeviceB extends BluetoothLeDeviceBase {

    private static final String TAG = "BluetoothLeDeviceB";

    //根据具体硬件进行设置
    public static String DEVICEB_UUID_SERVICE = "0000ffff-0000-1000-8000-00805f9b34fb";
    public static String DEVICEB_UUID_CHARACTERISTIC = "0000ffff-0000-1000-8000-00805f9b34fb";

    //一般不用修改
    public static String DEVICEB_UUID_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    public BluetoothLeDeviceB(Context context) {
        super(context);
        UUID_SERVICE = DEVICEB_UUID_SERVICE;
        UUID_CHARACTERISTIC = DEVICEB_UUID_CHARACTERISTIC;
        UUID_DESCRIPTOR = DEVICEB_UUID_DESCRIPTOR;
    }


    @Override
    public void parseData(BluetoothGattCharacteristic characteristic) {
        Log.e(TAG, "BluetoothLeDeviceB-parseData: -------解析数据");
    }
}

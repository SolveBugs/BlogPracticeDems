package com.test.bluetoothbledemo.bluetooth.callback;

/**
 * 蓝牙设备连接发生变化回调
 */
public interface OnDeviceConnectChangedListener {
    void onConnected();

    void onDisconnected();
}

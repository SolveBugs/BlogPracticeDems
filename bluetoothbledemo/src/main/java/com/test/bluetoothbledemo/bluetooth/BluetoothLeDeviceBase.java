/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.test.bluetoothbledemo.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;


import com.test.bluetoothbledemo.bluetooth.callback.BleDevceScanCallback;
import com.test.bluetoothbledemo.bluetooth.callback.OnDeviceConnectChangedListener;
import com.test.bluetoothbledemo.bluetooth.callback.OnScanCallback;
import com.test.bluetoothbledemo.bluetooth.callback.OnWriteCallback;
import com.test.bluetoothbledemo.bluetooth.utils.HexUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public abstract class BluetoothLeDeviceBase {
    private final static String TAG = BluetoothLeDeviceBase.class.getSimpleName();

    private Context context;

    //默认扫描时间：10s
    private static final int SCAN_TIME = 10000;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;

    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private HashMap<String, Map<String, BluetoothGattCharacteristic>> servicesMap = new HashMap<>();
    private BluetoothGattCharacteristic mBleGattCharacteristic;
    private BluetoothGattCharacteristic mNotifyCharacteristic1;

    protected String UUID_SERVICE = "";
    protected String UUID_CHARACTERISTIC = "";
    protected String UUID_DESCRIPTOR = "";

    private Handler handler = new Handler();
    private boolean mScanning;
    private BleDevceScanCallback bleDeviceScanCallback;
    private OnDeviceConnectChangedListener connectChangedListener;

    public BluetoothLeDeviceBase(Context context) {
        this.context = context;
        initialize();
    }


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (connectChangedListener != null) {
                    connectChangedListener.onConnected();
                }
                mConnectionState = STATE_CONNECTED;
                mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (connectChangedListener != null) {
                    connectChangedListener.onDisconnected();
                }
                mConnectionState = STATE_DISCONNECTED;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                List<BluetoothGattService> services = mBluetoothGatt.getServices();
                for (int i = 0; i < services.size(); i++) {
                    HashMap<String, BluetoothGattCharacteristic> charMap = new HashMap<>();
                    BluetoothGattService bluetoothGattService = services.get(i);
                    String serviceUuid = bluetoothGattService.getUuid().toString();
                    List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
                    for (int j = 0; j < characteristics.size(); j++) {
                        charMap.put(characteristics.get(j).getUuid().toString(), characteristics.get(j));
                    }
                    servicesMap.put(serviceUuid, charMap);
                }
                BluetoothGattCharacteristic bluetoothGattCharacteristic = getBluetoothGattCharacteristic(UUID_SERVICE, UUID_CHARACTERISTIC);
                if (bluetoothGattCharacteristic == null)
                    return;
                enableGattServicesNotification(bluetoothGattCharacteristic);

            } else {
                Log.w(TAG, " --------- onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead: status---=" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {

            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged: -----------=");
            parseData(characteristic);
        }
    };


    public abstract void parseData(BluetoothGattCharacteristic characteristic);


    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public void initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, " --------- Unable to initialize BluetoothManager. --------- ");
                return;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, " --------- Unable to obtain a BluetoothAdapter. --------- ");
        }
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, " --------- BluetoothAdapter not initialized or unspecified address. --------- ");
            return false;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, " --------- Device not found.  Unable to connect. --------- ");
            return false;
        }


        mBluetoothGatt = device.connectGatt(context, true, mGattCallback);
        Log.d(TAG, " --------- Trying to create a new connection. --------- ");
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, " --------- BluetoothAdapter not initialized --------- ");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, " --------- BluetoothAdapter not initialized --------- ");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, " --------- BluetoothAdapter not initialized --------- ");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        if (UUID_CHARACTERISTIC.equals(characteristic.getUuid().toString())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(UUID_DESCRIPTOR));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
            Log.d(TAG, " --------- Connect setCharacteristicNotification --------- " + characteristic.getUuid());
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }


    /**
     * 当前蓝牙是否打开
     */
    private boolean isEnable() {
        if (null != mBluetoothAdapter) {
            return mBluetoothAdapter.isEnabled();
        }
        return false;
    }


    /**
     * @param enable
     * @param scanCallback
     */
    public void scanBleDevice(final boolean enable, final OnScanCallback scanCallback) {
        scanBleDevice(SCAN_TIME, enable, scanCallback, null);
    }

    /**
     * @param enable
     * @param scanCallback
     * @param specificUUids 扫描指定service uuid的设备
     */
    public void scanBleDevice(final boolean enable, final OnScanCallback scanCallback, UUID[] specificUUids) {
        scanBleDevice(SCAN_TIME, enable, scanCallback, specificUUids);
    }

    /**
     * @param time          扫描时长
     * @param enable
     * @param scanCallback
     * @param specificUUids
     */
    public void scanBleDevice(int time, final boolean enable, final OnScanCallback scanCallback, UUID[] specificUUids) {
        if (!isEnable()) {
            mBluetoothAdapter.enable();
            Log.e(TAG, "Bluetooth is not open!");
        }
        if (null != mBluetoothGatt) {
            mBluetoothGatt.close();
        }
        if (bleDeviceScanCallback == null) {
            bleDeviceScanCallback = new BleDevceScanCallback(scanCallback);
        }
        if (enable) {
            if (mScanning) return;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    //time后停止扫描
                    mBluetoothAdapter.stopLeScan(bleDeviceScanCallback);
                    scanCallback.onFinish();
                }
            }, time <= 0 ? SCAN_TIME : time);
            mScanning = true;
            if (specificUUids != null) {
                mBluetoothAdapter.startLeScan(specificUUids, bleDeviceScanCallback);
            } else {
                mBluetoothAdapter.startLeScan(bleDeviceScanCallback);
            }
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(bleDeviceScanCallback);
        }
    }


    public void stopScan() {
        mScanning = false;
        mBluetoothAdapter.stopLeScan(bleDeviceScanCallback);
    }


    public void writeBuffer(byte[] value, OnWriteCallback writeCallback) {
        writeBuffer(HexUtil.bytesToHexString(value), writeCallback);
    }

    /**
     * 发送数据
     *
     * @param value         指令
     * @param writeCallback 发送回调
     */
    public void writeBuffer(String value, OnWriteCallback writeCallback) {
        if (!isEnable()) {
            if (writeCallback != null) {
                writeCallback.onFailed(OnWriteCallback.FAILED_BLUETOOTH_DISABLE);
            }
            Log.e(TAG, "FAILED_BLUETOOTH_DISABLE");
            return;
        }

        if (mBleGattCharacteristic == null) {
            mBleGattCharacteristic = getBluetoothGattCharacteristic(UUID_SERVICE, UUID_CHARACTERISTIC);
        }

        if (null == mBleGattCharacteristic) {
            if (writeCallback != null) {
                writeCallback.onFailed(OnWriteCallback.FAILED_INVALID_CHARACTER);
            }
            Log.e(TAG, "FAILED_INVALID_CHARACTER");
            return;
        }

        //设置数组进去
        mBleGattCharacteristic.setValue(HexUtil.hexStringToBytes(value));
        //发送

        boolean b = mBluetoothGatt.writeCharacteristic(mBleGattCharacteristic);

        if (b) {
            if (writeCallback != null) {
                writeCallback.onSuccess();
            }
        }
        Log.e(TAG, "send:" + b + "data：" + value);
    }

    /**
     * 根据服务UUID和特征UUID,获取一个特征{@link BluetoothGattCharacteristic}
     *
     * @param serviceUUID   服务UUID
     * @param characterUUID 特征UUID
     */
    private BluetoothGattCharacteristic getBluetoothGattCharacteristic(String serviceUUID, String characterUUID) {
        if (!isEnable()) {
            throw new IllegalArgumentException(" Bluetooth is no enable please call BluetoothAdapter.enable()");
        }
        if (null == mBluetoothGatt) {
            Log.e(TAG, "mBluetoothGatt is null");
            return null;
        }

        //找服务
        Map<String, BluetoothGattCharacteristic> bluetoothGattCharacteristicMap = servicesMap.get(serviceUUID);
        if (null == bluetoothGattCharacteristicMap) {
            Log.e(TAG, "Not found the serviceUUID!");
            return null;
        }

        //找特征
        Set<Map.Entry<String, BluetoothGattCharacteristic>> entries = bluetoothGattCharacteristicMap.entrySet();
        BluetoothGattCharacteristic gattCharacteristic = null;
        for (Map.Entry<String, BluetoothGattCharacteristic> entry : entries) {
            if (characterUUID.equals(entry.getKey())) {
                gattCharacteristic = entry.getValue();
                break;
            }
        }
        return gattCharacteristic;
    }


    private void enableGattServicesNotification(BluetoothGattCharacteristic gattCharacteristic) {
        if (gattCharacteristic == null) return;
        setNotify(gattCharacteristic);
    }

    private void setNotify(BluetoothGattCharacteristic characteristic) {

        final int charaProp = characteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            if (mNotifyCharacteristic1 != null) {
                setCharacteristicNotification(
                        mNotifyCharacteristic1, false);
                mNotifyCharacteristic1 = null;
            }
            readCharacteristic(characteristic);
        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic1 = characteristic;
            setCharacteristicNotification(
                    characteristic, true);
        }
    }


    public BluetoothAdapter isDeviceSupport() {
        //需要设备支持ble
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return null;
        }

        //需要有BluetoothAdapter
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        if (adapter == null) {
            return null;
        }
        return adapter;
    }

    public void setConnectChangedListener(OnDeviceConnectChangedListener connectChangedListener) {
        this.connectChangedListener = connectChangedListener;
    }
}

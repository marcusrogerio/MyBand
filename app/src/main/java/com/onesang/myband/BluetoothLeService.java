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

package com.onesang.myband;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.view.KeyEvent;

import com.onesang.myband.sdk.ActionCallback;
import com.onesang.myband.sdk.MiBand;
import com.onesang.myband.sdk.listeners.HeartRateNotifyListener;
import com.onesang.myband.sdk.listeners.NotifyListener;
import com.onesang.myband.sdk.listeners.model.AlertMode;
import com.onesang.myband.sdk.listeners.model.BandLocation;
import com.onesang.myband.sdk.listeners.model.Profile;
import com.onesang.myband.sdk.listeners.model.TimeFormat;

import java.util.List;
import java.util.UUID;

import static com.onesang.myband.DeviceScanActivity.log;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private MiBand miband;

    private final IBinder mBinder = new LocalBinder();

    private ProgressDialog pd;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        miband = new MiBand(this);
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address, final Activity activity) {
        pd = new ProgressDialog(activity);
        pd.setMessage("connecting...");
        pd.setCancelable(false);
        pd.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    disconnect();
                    dialog.dismiss();
                }
                return true;
            }
        });
//        pd.show();

        return miband.connect(address, new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                pd.dismiss();
                log('d', TAG, "connected!!!");

                miband.setDisconnectedListener(new NotifyListener() {
                    @Override
                    public void onNotify(byte[] data) {
                        log('d', TAG, "disconnected!!!");
                    }
                });
            }
            @Override
            public void onFail(int errorCode, String msg) {
                pd.dismiss();
                log('d', TAG, "connect fail, code:" + errorCode + ",mgs:" + msg);
            }
        });
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        miband.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        miband.close();
    }

    public void startAlert(AlertMode mode) {
        miband.startAlert(mode);
    }

    public void stopAlert() {
        miband.stopAlert();
    }

    public void setHeartRateScanListener(HeartRateNotifyListener listener) {
        miband.setHeartRateScanListener(listener);
    }

    public void startHeartRateScan(ActionCallback callback) {
        miband.startHeartRateScan(callback);
    }

    public void stoptHeartRateScan(ActionCallback callback) {
        miband.stoptHeartRateScan(callback);
    }

    public void getBatteryInfo(UUID characteristicUUID, ActionCallback callback) {
        miband.getBatteryInfo(characteristicUUID, callback);
    }
    public void readRssi(ActionCallback callback) {
        miband.readRssi(callback);
    }

    public void setNotifyListener(UUID characteristicId, NotifyListener listener) {
        miband.setNotifyListener(characteristicId, listener);
    }
    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        miband.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        miband.setCharacteristicNotification(characteristic, enabled);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        return miband.getSupportedGattServices();

    }

    public void dismiss() {
        pd.dismiss();
    }

    public void setBandLocation(BandLocation bandLocation) {
        miband.setBandLocation(bandLocation);
    }

    public void setTimeFormat(TimeFormat timeFormat) {
        miband.setTimeFormat(timeFormat);
    }

    public void setLiftWrist(boolean enabled) {
        miband.setLiftWrist(enabled);
    }

    public void setHeartRateSleepAssistant(boolean enabled) {
        miband.setHeartRateSleepAssistant(enabled);
    }

    public void setDiscoverable(boolean enabled) {
        miband.setDiscoverable(enabled);
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

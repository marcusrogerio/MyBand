package com.onesang.myband.sdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;

import com.onesang.myband.sdk.listeners.HeartRateNotifyListener;
import com.onesang.myband.sdk.listeners.NotifyListener;
import com.onesang.myband.sdk.listeners.RealtimeStepsNotifyListener;
import com.onesang.myband.sdk.listeners.model.AlertMode;
import com.onesang.myband.sdk.listeners.model.BatteryInfo;
import com.onesang.myband.sdk.listeners.model.LedColor;
import com.onesang.myband.sdk.listeners.model.Profile;
import com.onesang.myband.sdk.listeners.model.Protocol;
import com.onesang.myband.sdk.listeners.model.UserInfo;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.onesang.myband.DeviceScanActivity.log;

public class MiBand {

    private static final String TAG = MiBand.class.getSimpleName();

    private BluetoothIO io;

    public MiBand(Context context) {
        this.io = new BluetoothIO(context);
    }

    public static void startScan(ScanCallback callback) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (null == adapter) {
            log('e', TAG, "BluetoothAdapter is null");
            return;
        }
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (null == scanner) {
            log('e', TAG, "BluetoothLeScanner is null");
            return;
        }
        log('d', TAG, "startScan...");
        scanner.startScan(callback);
    }

    public static void stopScan(ScanCallback callback) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (null == adapter) {
            log('e', TAG, "BluetoothAdapter is null");
            return;
        }
        BluetoothLeScanner scanner = adapter.getBluetoothLeScanner();
        if (null == scanner) {
            log('e', TAG, "BluetoothLeScanner is null");
            return;
        }
        log('d', TAG, "stopScan...");
        scanner.stopScan(callback);
    }

    /**
     * 连接指定的手环
     *
     * @param callback
     */
    public boolean connect(String address, final ActionCallback callback) {
        return this.io.connect(address, callback);
    }

    public void setDisconnectedListener(NotifyListener disconnectedListener) {
        this.io.setDisconnectedListener(disconnectedListener);
    }

    /**
     * 和手环配对, 实际用途未知, 不配对也可以做其他的操作
     *
     * @return data = null
     */
    public void pair(final ActionCallback callback) {
        ActionCallback ioCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object data) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                log('d', TAG, "pair result " + Arrays.toString(characteristic.getValue()));
                if (characteristic.getValue().length == 1 && characteristic.getValue()[0] == 2) {
                    callback.onSuccess(null);
                } else {
                    callback.onFail(-1, "respone values no succ!");
                }
            }

            @Override
            public void onFail(int errorCode, String msg) {
                callback.onFail(errorCode, msg);
            }
        };
//        this.io.writeAndRead(Profile.UUID_CHAR_PAIR, Protocol.PAIR, ioCallback);
    }

    public BluetoothDevice getDevice() {
        return this.io.getDevice();
    }

    /**
     * 读取和连接设备的信号强度RSSI值
     *
     * @param callback
     * @return data : int, rssi值
     */
    public void readRssi(ActionCallback callback) {
        this.io.readRssi(callback);
    }

    /**
     * 读取手环电池信息
     *
     * @return {@link BatteryInfo}
     */
    public void getBatteryInfo(UUID characteristicUUID, final ActionCallback callback) {
        ActionCallback ioCallback = new ActionCallback() {

            @Override
            public void onSuccess(Object data) {
                BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                log('d', TAG, "getBatteryInfo result " + Arrays.toString(characteristic.getValue()));
                if (characteristic.getValue().length == 11) {
                    BatteryInfo info = BatteryInfo.fromByteData(characteristic.getValue());
                    callback.onSuccess(info);
                } else {
                    callback.onFail(-1, "result format wrong!");
                }
            }

            @Override
            public void onFail(int errorCode, String msg) {
                callback.onFail(errorCode, msg);
            }
        };
        this.io.readCharacteristic(characteristicUUID, ioCallback);
    }

    /**
     * 让手环震动
     */
    public void startVibration(AlertMode mode) {
        byte[] protocal;
        switch (mode) {
            case ALERT_MESSAGE:
                protocal = Protocol.ALERT_MESSAGE;
                break;
            case ALERT_CALL:
                protocal = Protocol.ALERT_CALL;
                break;
            case ALERT_NORMAL:
                protocal = Protocol.ALERT_NORMAL;
                break;
            default:
                return;
        }
        this.io.writeCharacteristic(Profile.UUID_SERVICE_ALERT, Profile.UUID_CHAR_ALERT, protocal, null);
    }

    /**
     * 停止以模式Protocol.ALERT_CALL 开始的震动
     */
    public void stopVibration() {
        this.io.writeCharacteristic(Profile.UUID_SERVICE_ALERT, Profile.UUID_CHAR_ALERT, Protocol.STOP_VIBRATION, null);
    }

    public void setNotifyListener(UUID characteristicId, NotifyListener listener) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MIBAND, characteristicId, listener);
    }

    public void setNormalNotifyListener(NotifyListener listener) {
//        this.io.setNotifyListener(Profile.UUID_SERVICE_MIBAND, Profile.UUID_CHAR_NOTIFICATION, listener);
    }

    /**
     * 重力感应器数据通知监听, 设置完之后需要另外使用 {@link MiBand#enableRealtimeStepsNotify} 开启 和
     * {@link MiBand##disableRealtimeStepsNotify} 关闭通知
     *
     * @param listener
     */
    public void setSensorDataNotifyListener(final NotifyListener listener) {
//        this.io.setNotifyListener(Profile.UUID_SERVICE_MIBAND, Profile.UUID_CHAR_SENSOR_DATA, new NotifyListener() {
//
//            @Override
//            public void onNotify(byte[] data) {
//                listener.onNotify(data);
//            }
//        });
    }

    /**
     * 开启重力感应器数据通知
     */
    public void enableSensorDataNotify() {
//        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.ENABLE_SENSOR_DATA_NOTIFY, null);
    }

    /**
     * 关闭重力感应器数据通知
     */
    public void disableSensorDataNotify() {
//        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.DISABLE_SENSOR_DATA_NOTIFY, null);
    }

    /**
     * 实时步数通知监听器, 设置完之后需要另外使用 {@link MiBand#enableRealtimeStepsNotify} 开启 和
     * {@link MiBand##disableRealtimeStepsNotify} 关闭通知
     *
     * @param listener
     */
    public void setRealtimeStepsNotifyListener(final RealtimeStepsNotifyListener listener) {
//        this.io.setNotifyListener(Profile.UUID_SERVICE_MIBAND, Profile.UUID_CHAR_REALTIME_STEPS, new NotifyListener() {
//
//            @Override
//            public void onNotify(byte[] data) {
//                log('d', TAG, Arrays.toString(data));
//                if (data.length == 4) {
//                    int steps = data[3] << 24 | (data[2] & 0xFF) << 16 | (data[1] & 0xFF) << 8 | (data[0] & 0xFF);
//                    listener.onNotify(steps);
//                }
//            }
//        });
    }

    /**
     * 开启实时步数通知
     */
    public void enableRealtimeStepsNotify() {
//        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.ENABLE_REALTIME_STEPS_NOTIFY, null);
    }

    /**
     * 关闭实时步数通知
     */
    public void disableRealtimeStepsNotify() {
//        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, Protocol.DISABLE_REALTIME_STEPS_NOTIFY, null);
    }

    /**
     * 设置led灯颜色
     */
    public void setLedColor(LedColor color) {
        byte[] protocal;
        switch (color) {
            case RED:
                protocal = Protocol.SET_COLOR_RED;
                break;
            case BLUE:
                protocal = Protocol.SET_COLOR_BLUE;
                break;
            case GREEN:
                protocal = Protocol.SET_COLOR_GREEN;
                break;
            case ORANGE:
                protocal = Protocol.SET_COLOR_ORANGE;
                break;
            default:
                return;
        }
//        this.io.writeCharacteristic(Profile.UUID_CHAR_CONTROL_POINT, protocal, null);
    }

    /**
     * 设置用户信息
     *
     * @param userInfo
     */
    public void setUserInfo(UserInfo userInfo) {
        BluetoothDevice device = this.io.getDevice();
        byte[] data = userInfo.getBytes(device.getAddress());
//        this.io.writeCharacteristic(Profile.UUID_CHAR_USER_INFO, data, null);
    }

    public void showServicesAndCharacteristics() {
        for (BluetoothGattService service : this.io.mBluetoothGatt.getServices()) {
            log('d', TAG, "onServicesDiscovered:" + service.getUuid());

            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                log('d', TAG, "  char:" + characteristic.getUuid());

                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    log('d', TAG, "    descriptor:" + descriptor.getUuid());
                }
            }
        }
    }

    public void setHeartRateScanListener(final HeartRateNotifyListener listener) {
        this.io.setNotifyListener(Profile.UUID_SERVICE_HEARTRATE, Profile.UUID_CHAR_HEART_RATE_NOTIFICATION, new NotifyListener() {
            @Override
            public void onNotify(byte[] data) {
                log('d', TAG, Arrays.toString(data));
                if (data.length == 2 && data[0] == 0) {
                    int heartRate = data[1] & 0xFF;
                    listener.onNotify(heartRate);
                }
            }
        });
    }

    public void startHeartRateScan(ActionCallback callback) {

        this.io.writeCharacteristic(Profile.UUID_SERVICE_HEARTRATE, Profile.UUID_CHAR_HEART_RATE_CONTROL_POINT, Protocol.START_HEARTRATE_SCAN, callback);
    }

    public void stoptHeartRateScan(ActionCallback callback) {

        this.io.writeCharacteristic(Profile.UUID_SERVICE_HEARTRATE, Profile.UUID_CHAR_HEART_RATE_CONTROL_POINT, Protocol.STOP_HEARTRATE_SCAN, callback);
    }


    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.io.readCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        this.io.setCharacteristicNotification(characteristic, enabled);
    }


    public List getSupportedGattServices() {
        return this.io.getSupportedGattServices();
    }

    public void disconnect() {
        this.io.disconnect();
    }

    public void close() {
        this.io.close();
    }
}

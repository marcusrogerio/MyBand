package com.onesang.myband;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.onesang.myband.sdk.ActionCallback;
import com.onesang.myband.sdk.BluetoothIO;
import com.onesang.myband.sdk.listeners.HeartRateNotifyListener;
import com.onesang.myband.sdk.listeners.NotifyListener;
import com.onesang.myband.sdk.listeners.model.BatteryInfo;
import com.onesang.myband.sdk.listeners.model.Profile;
import com.onesang.myband.sdk.listeners.model.AlertMode;

import java.util.Arrays;
import java.util.UUID;

import static com.onesang.myband.DeviceScanActivity.log;


/**
 * Created by mutecsoft on 2016-10-17.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String PRFFS_ADDRESS = "prefs_address";
    public static final String PRFFS_ADDRESS_KEY = "prefs_address_key";

    SharedPreferences mSpAddress;
    EditText mEtAddress;
    Button mBtnSetAddr, mBtnConnect, mBtnDisConnect, mBtnReadRssi, mBtnStartVibration, mBtnStopVibration, mBtnSetHeartRateListener, mBtnStartHeartRateScan, mBtnSetNotifyListener, mBtnGetBatteryInfo;
    TextView mTvState, mTvPing, mTvReadRssi, mTvHeartRate, mTvBatteryInfo;

    private BluetoothLeService mBluetoothLeService;
    private String mDeviceAddress;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {

            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                log('e', TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress, MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothIO.ACTION_GATT_CONNECTED.equals(action)) {
//                mConnected = true;
                updateUIState(mTvState, R.string.state, R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothIO.ACTION_GATT_DISCONNECTED.equals(action)) {
//                mConnected = false;
                updateUIState(mTvState, R.string.state, R.string.disconnected);
//                invalidateOptionsMenu();
//                clearUI();
                mBluetoothLeService.dismiss();
            } else if (BluetoothIO.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothIO.ACTION_DATA_AVAILABLE.equals(action)) {
//                displayData(intent.getStringExtra(BluetoothIO.EXTRA_DATA));
                log('d', TAG, "ACTION_DATA_AVAILABLE : "+intent.getStringExtra(BluetoothIO.EXTRA_DATA));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSpAddress = getSharedPreferences(PRFFS_ADDRESS, MODE_PRIVATE);
        mEtAddress = (EditText) findViewById(R.id.etAddress);
        mBtnSetAddr = (Button) findViewById(R.id.btnSetAddress);
        mBtnConnect = (Button) findViewById(R.id.btnConnect);
        mBtnDisConnect = (Button) findViewById(R.id.btnDisconnect);
        mBtnReadRssi = (Button) findViewById(R.id.btnReadRssi);
        mBtnStartVibration = (Button) findViewById(R.id.btnStartVibration);
        mBtnStopVibration = (Button) findViewById(R.id.btnStopVibration);
        mBtnSetHeartRateListener = (Button) findViewById(R.id.btnSetHeartRateListener);
        mBtnStartHeartRateScan = (Button) findViewById(R.id.btnStartHeartRateScan);
        mBtnSetNotifyListener = (Button) findViewById(R.id.btnSetNotifyListener);
        mBtnGetBatteryInfo = (Button) findViewById(R.id.btnGetBatteryInfo);

        mTvState = (TextView) findViewById(R.id.tvState);
        mTvPing = (TextView) findViewById(R.id.tvPing);
        mTvReadRssi = (TextView) findViewById(R.id.tvReadRssi);
        mTvHeartRate = (TextView) findViewById(R.id.tvHeartRate);
        mTvBatteryInfo = (TextView) findViewById(R.id.tvBatteryInfo);
        String str = getResources().getString(R.string.rssi) + " " + "--";
        mTvReadRssi.setText(str);

        mBtnSetAddr.setOnClickListener(this);
        mBtnConnect.setOnClickListener(this);
        mBtnDisConnect.setOnClickListener(this);
        mBtnReadRssi.setOnClickListener(this);
        mBtnStartVibration.setOnClickListener(this);
        mBtnStopVibration.setOnClickListener(this);
        mBtnSetHeartRateListener.setOnClickListener(this);
        mBtnStartHeartRateScan.setOnClickListener(this);
        mBtnSetNotifyListener.setOnClickListener(this);
        mBtnGetBatteryInfo.setOnClickListener(this);
        updateUIState(mTvState, R.string.state, R.string.disconnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        String address = mSpAddress.getString(PRFFS_ADDRESS_KEY, null);
        if (address != null) {
            mDeviceAddress = address;
        }
        mEtAddress.setText(mDeviceAddress);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        if (mBluetoothLeService != null)
            mBluetoothLeService.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothLeService != null) {
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
        }
    }
    UUID uuid = Profile.UUID_CHAR_DEVICE_NAME;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSetAddress:
                mDeviceAddress = mEtAddress.getText().toString();
                if (mSpAddress.edit().putString(PRFFS_ADDRESS_KEY, mDeviceAddress).commit())
                    Toast.makeText(MainActivity.this, R.string.address_has_been_updated, Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnConnect:
                if (mBluetoothLeService != null) {
                    mBluetoothLeService.connect(mDeviceAddress, MainActivity.this);
                } else {
                    Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
                    bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                }
                updateUIState(mTvState, R.string.state, R.string.connecting);
                break;
            case R.id.btnDisconnect:
                if (mBluetoothLeService != null) {
                    mBluetoothLeService.disconnect();
                }
                break;
            case R.id.btnReadRssi:
                if (mBluetoothLeService != null) {
                    mBluetoothLeService.readRssi(new ActionCallback() {
                        @Override
                        public void onSuccess(Object data) {
                            updateUIState(mTvReadRssi, R.string.rssi, data.toString());
                            log('d', TAG, "readRssi success");
                        }

                        @Override
                        public void onFail(int errorCode, String msg) {
                            log('d', TAG, "readRssi fail");
                        }
                    });
                }
                break;
            case R.id.btnStartVibration:
                if (mBluetoothLeService != null) {
                    new AlertDialog.Builder(MainActivity.this).setSingleChoiceItems(new String[]{"ALERT_MESSAGE", "ALERT_CALL", "ALERT_NORMAL"}, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AlertMode mode = AlertMode.ALERT_MESSAGE;
                            switch (which) {
                                case 0:
                                    mode = AlertMode.ALERT_MESSAGE;
                                    break;
                                case 1:
                                    mode = AlertMode.ALERT_CALL;
                                    break;
                                case 2:
                                    mode = AlertMode.ALERT_NORMAL;
                                    break;
                            }
                            mBluetoothLeService.startAlert(mode);
                            dialog.dismiss();
                        }
                    }).show();

                }
                break;
            case R.id.btnStopVibration:
                if (mBluetoothLeService != null) {
                    mBluetoothLeService.stopAlert();
                }
                break;
            case R.id.btnSetHeartRateListener:
                if (mBluetoothLeService != null) {
                    mBluetoothLeService.setHeartRateScanListener(new HeartRateNotifyListener() {
                        @Override
                        public void onNotify(int heartRate) {
                            updateUIState(mTvHeartRate, R.string.heartrate, " " + heartRate);
                        }
                    });
                }
                break;
            case R.id.btnStartHeartRateScan:
                if (mBluetoothLeService != null) {
                    mBluetoothLeService.startHeartRateScan(new ActionCallback() {
                        @Override
                        public void onSuccess(Object data) {
                            BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) data;
                            log('d', TAG, Arrays.toString(characteristic.getValue()));
                        }

                        @Override
                        public void onFail(int errorCode, String msg) {
                            log('e', TAG, "errorCode : "+errorCode+", msg : "+msg);
                            updateUIState(mTvHeartRate, R.string.heartrate, "Can't get info");
                        }
                    });
                }
                break;
            case R.id.btnSetNotifyListener:
                if (mBluetoothLeService != null) {
                    new AlertDialog.Builder(MainActivity.this).setSingleChoiceItems(new String[]{"UNKNOWN_1", "UNKNOWN_2", "UNKNOWN_3"}, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            switch (which) {
                                case 0:
                                    uuid = Profile.UUID_CHAR_DEVICE_NAME;
                                    break;
                                case 1:
                                    uuid = Profile.UUID_CHAR_APPEARANCE;
                                    break;
                                case 2:
                                    uuid = Profile.UUID_CHAR_PERIPHERAL_PREFERRED_CONNECTION_PRAMETERS;
                                    break;
                            }
                            mBluetoothLeService.setNotifyListener(uuid, new NotifyListener() {
                                @Override
                                public void onNotify(byte[] data) {
                                    log('d', TAG, "current uuid : "+SampleGattAttributes.lookup(uuid.toString(), "defaultName"));
                                }
                            });
                            dialog.dismiss();
                        }
                    }).show();
                }
                break;
            case R.id.btnGetBatteryInfo:
                if (mBluetoothLeService != null) {
                    mBluetoothLeService.getBatteryInfo(Profile.UUID_CHAR_CURRENT_TIME, new ActionCallback() {
                        @Override
                        public void onSuccess(Object data) {
                            BatteryInfo batteryInfo = (BatteryInfo) data;
                            int level = batteryInfo.getLevel();
                            updateUIState(mTvBatteryInfo, R.string.battery_info, level);
                        }

                        @Override
                        public void onFail(int errorCode, String msg) {
                            log('e', TAG, "errorCode : "+errorCode+", msg : "+msg);
                            updateUIState(mTvBatteryInfo, R.string.battery_info, "Can't get info");
                        }
                    });
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                startActivity(new Intent(getApplicationContext(), DeviceScanActivity.class));
                break;
            case R.id.menu_developer:
                startActivity(new Intent(getApplicationContext(), DeveloperModeActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateUIState(final TextView tv, final int rowResourceId, final int resourceId) {
        String addStr = getResources().getString(resourceId);
        updateUIState(tv, rowResourceId, addStr);
    }

    private void updateUIState(final TextView tv, final int rowResourceId, final String addStr) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String str = getResources().getString(rowResourceId) + " " + addStr;
                tv.setText(str);
            }
        });
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothIO.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothIO.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothIO.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothIO.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}

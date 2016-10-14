package com.onesang.myband;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.onesang.myband.sdk.MiBand;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MyBand";
    private static final int REQUEST_ENABLE_BT = 999;

    private MiBand miBand;

    private BluetoothAdapter mBluetoothAdapter;

    HashMap<String, BluetoothDevice> devices = new HashMap<>();

    private static final int REQUEST_CODE = 0x11;
    String[] permissions = {"android.permission.ACCESS_COARSE_LOCATION"};

    TextView tv;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(getApplicationContext(), permissions[0] + "PERMISSION_DENIED", Toast.LENGTH_SHORT).show();
                finish();
            }

//            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//
//            } else {
//                Toast.makeText(getApplicationContext(), permissions[1]+"PERMISSION_DENIED", Toast.LENGTH_SHORT).show();
//                finish();
//            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);

        setContentView(R.layout.activity_main);

        // BLE 기능이 지원되는지 확인
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }



        miBand = new MiBand(this);

        final ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.item, new ArrayList<String>());

        final ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                show("found device!!");
                BluetoothDevice device = result.getDevice();
                log("device name:" + device.getName() + ",uuid:"
                        + device.getUuids() + ",add:"
                        + device.getAddress() + ",type:"
                        + device.getType() + ",bondState:"
                        + device.getBondState() + ",rssi:" + result.getRssi());

                String item = device.getName() + "|" + device.getAddress();
                if (!devices.containsKey(item)) {
                    devices.put(item, device);
                    adapter.add(item);
                }
            }
        };

        tv = (TextView) findViewById(R.id.tv);

        ((Button) findViewById(R.id.starScanButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log("startScan...");
                MiBand.startScan(scanCallback, tv);
            }
        });

        ((Button) findViewById(R.id.stopScanButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log("stopScan...");
                MiBand.stopScan(scanCallback, tv);
            }
        });


        ListView lv = (ListView) findViewById(R.id.listView);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView) view).getText().toString();
                if (devices.containsKey(item)) {

                    log("startActivity...");
                    MiBand.stopScan(scanCallback, tv);

                    BluetoothDevice device = devices.get(item);
                    Intent intent = new Intent();
                    intent.putExtra("device", device);
                    intent.setClass(MainActivity.this, DeviceControlActivity.class);
                    MainActivity.this.startActivity(intent);
                    MainActivity.this.finish();
                }
            }
        });

    }

    private void show(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    public static void log(String msg) {
        Log.d(TAG, msg);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (!mBluetoothAdapter.isEnabled()) {
                finish();
            }
        }
    }
}

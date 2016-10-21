package com.onesang.myband;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.onesang.myband.sdk.MiBand;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by K on 2016-03-29.
 */
public class DeviceScanActivity extends ListActivity {
    private final static String TAG = DeviceScanActivity.class.getSimpleName();

    private LeDeviceListAdapter mLeDeviceListAdapter; // 검색된 디바이스들과 리스트 뷰를 담을 변수
    private BluetoothAdapter mBluetoothAdapter; // BLE 디바이스를 검색할 블루투스 어댑터
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // 검색할 시간을 담는 변수
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 15000;

    // 스캔된 디바이스에 거리를 담는 변수
    private HashMap<String, Double> mDistances;

    private MiBand miBand;

    String mPath;
    File file;

    final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            String deviceAddress = device.getAddress();
            int rssi = result.getRssi();
            double distance = computeAccuracy(rssi, -74);

            log("device name:" + device.getName() +
                    ",uuid:" + device.getUuids() +
                    ",add:" + device.getAddress() +
                    ",type:" + device.getType() +
                    ",bondState:" + device.getBondState() +
                    ",rssi:" + rssi);

//            Log.d(TAG, "" + device.getName());
//            Log.d(TAG, "" + rssi);
//            Log.d(TAG, "" + distance);

            if(rssi < 0) {

                if(!mDistances.containsKey(deviceAddress)) {
                    mDistances.put(deviceAddress, distance);
                }else{
                    mDistances.remove(deviceAddress);
                    mDistances.put(deviceAddress, distance);
                }
                write(device.getName(), mDistances.get(deviceAddress));
            }


            mLeDeviceListAdapter.addDevice(device);
            mLeDeviceListAdapter.notifyDataSetChanged();
            log("device name:" + device.getName() + ",uuid:"
                    + device.getUuids() + ",add:"
                    + device.getAddress() + ",type:"
                    + device.getType() + ",bondState:"
                    + device.getBondState() + ",rssi:" + rssi);

//            String item = device.getName() + "|" + device.getAddress();
//            if (!devices.containsKey(item)) {
//                devices.put(item, device);
//                adapter.add(item);
//            }
        }
    };

    // BLE 디바이스가 스캔되었을떄 호출 될 스캔콜백
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String deviceAddress = device.getAddress();
                            double distance = computeAccuracy(rssi, -74);

                            Log.d(TAG, "" + device.getName());
                            Log.d(TAG, "" + rssi);
                            Log.d(TAG, "" + distance);

                            if(rssi < 0) {

                                if(!mDistances.containsKey(deviceAddress)) {
                                    mDistances.put(deviceAddress, distance);
                                }else{
                                    mDistances.remove(deviceAddress);
                                    mDistances.put(deviceAddress, distance);
                                }
                                write(device.getName(), mDistances.get(deviceAddress));
                            }


                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();

                        }
                    });
                }
            };

    private static final int REQUEST_PERMISSIONS = 0x11;
    String[] permissions = {"android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN", "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION",
    "android.permission.READ_PHONE_STATE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSIONS) {
            for (int i=0; i<permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(getApplicationContext(), permissions[i] + "PERMISSION_DENIED", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_devices);
        }

        mHandler = new Handler();
        mPath = Environment.getExternalStorageDirectory().getPath();
        file = new File(mPath +"/test.txt");
        mDistances = new HashMap<String, Double>();

        miBand = new MiBand(this);

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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 블루투스 기능이 활성화 되어있는지 체크
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // 리스트 뷰 어댑터 초기화
        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(mLeDeviceListAdapter);
        scanLeDevice(true); // BLE 디바이스 스캔 시작
    }



    @Override
    protected void onPause() {
        super.onPause();
        // 액티비티가 Pause 상태가 되면 스캔 중지 디바이스리스트어댑터 비움
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
        if (device == null) return;
        final Intent intent = new Intent(this, DeviceControlActivity.class);
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
        intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }
        startActivity(intent);
    }

    // BLE 디바이스 스캔 메서드
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // SCAN_PERIOD 동안 스캔한 후에 스캔을 중단
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    MiBand.stopScan(mScanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            MiBand.startScan(mScanCallback);
        } else {
            // 스캔 중단
            mScanning = false;
            MiBand.stopScan(mScanCallback);
        }
        invalidateOptionsMenu();
    }

    private void write(String deviceName, double distance){
        String str = String.format(deviceName+" %.2fm", distance);
        str = getCurrentTime() +" "+str+"\n";
        FileOutputStream fos = null;
        try {

            fos = new FileOutputStream(file, true);
            if(fos != null){
                fos.write(str.getBytes());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        }finally {
            try {
                if(fos != null){
                    fos.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    private static String getCurrentTime(){
        Calendar calendar = Calendar.getInstance();
        String strTime = String.format("%d-%d-%d %d:%d:%d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
        return  strTime;
    }



    // 비콘의 거리 측정 메서드
    private static double computeAccuracy(int rssi, int measuredPower) {
        if(rssi == 0) {
            return -1.0D;
        } else {
            double ratio = (double)rssi / (double)measuredPower;
            double rssiCorrection = 0.96D + Math.pow((double)Math.abs(rssi), 3.0D) % 10.0D / 150.0D;
            return ratio <= 1.0D?Math.pow(ratio, 9.98D) * rssiCorrection:(0.103D + 0.89978D * Math.pow(ratio, 7.71D)) * rssiCorrection;
        }
    }


    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceDistance;
    }

    // 검색된 디바이스들과 리스트 뷰를 담을 어댑터
    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices; // 블루트스 디바이스를 담을 ArrayList 변수
        private LayoutInflater mInflator; // layout을 가져올 인플레이터 변수




        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
            mDistances = new HashMap<String, Double>();
        }

        // BLE 디바이스 추가
        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                viewHolder.deviceDistance = (TextView) view.findViewById(R.id.device_distance);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.deviceName.setText(deviceName);
                viewHolder.deviceDistance.setText(String.format("distance: (%.2fm)", mDistances.get(device.getAddress())));
            }
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}

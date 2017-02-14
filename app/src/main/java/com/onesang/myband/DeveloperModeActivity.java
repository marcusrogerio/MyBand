package com.onesang.myband;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.widget.Toast;

import com.onesang.myband.sdk.listeners.model.AlertMode;
import com.onesang.myband.sdk.listeners.model.BandLocation;
import com.onesang.myband.sdk.listeners.model.TimeFormat;

import static com.onesang.myband.DeviceScanActivity.log;

/**
 * Created by mutecsoft on 2016-10-31.
 */

public class DeveloperModeActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    private static final String TAG = DeveloperModeActivity.class.getSimpleName();

    private static final String KEY_DISCOVERABLE = "discoverable";
    private static final String KEY_FIND_BAND = "find_band";
    private static final String KEY_BAND_LOCATION = "band_location";
    private static final String KEY_BAND_DISPLAY_SETTINGS = "mi_band_display_settings";
    private static final String KEY_TIME_FORMAT = "time_format";
    private static final String KEY_LIFT_WRIST = "lift_wrist_to_view_info";
    private static final String KEY_ROTATE_WRIST = "rotate_your_wrist_to_switch_info_on_mi_band";
    private static final String KEY_HEART_RATE_SLEEP_ASSISTANT = "heart_rate_sleep_assistant";

    private SwitchPreference mDiscoverable;
    private Preference mFindBand;
    private ListPreference mBandLocation;
    private MultiSelectListPreference mBandDisplaySettings;
    private ListPreference mTimeFormat;
    private SwitchPreference mLiftWrist;
    private SwitchPreference mRotateWrist;
    private SwitchPreference mHeartRateSleepAssistant;

    private BluetoothLeService mBluetoothLeService;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {

            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                log('e', TAG, "Unable to initialize Bluetooth");
                finish();
            }

            String address = getSharedPreferences(MainActivity.PRFFS_ADDRESS, MODE_PRIVATE).getString(MainActivity.PRFFS_ADDRESS_KEY, null);
            if (address != null) {
                // Automatically connects to the device upon successful start-up initialization.
                mBluetoothLeService.connect(address, DeveloperModeActivity.this);
            } else {
                Toast.makeText(DeveloperModeActivity.this, "device address is null !!", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_developer_mode);

        mDiscoverable = (SwitchPreference) findPreference(KEY_DISCOVERABLE);
        mFindBand = findPreference(KEY_FIND_BAND);
        mBandLocation = (ListPreference) findPreference(KEY_BAND_LOCATION);
        mBandDisplaySettings = (MultiSelectListPreference) findPreference(KEY_BAND_DISPLAY_SETTINGS);
        mTimeFormat = (ListPreference) findPreference(KEY_TIME_FORMAT);
        mLiftWrist = (SwitchPreference) findPreference(KEY_LIFT_WRIST);
        mRotateWrist = (SwitchPreference) findPreference(KEY_ROTATE_WRIST);
        mHeartRateSleepAssistant = (SwitchPreference) findPreference(KEY_HEART_RATE_SLEEP_ASSISTANT);

        mDiscoverable.setOnPreferenceChangeListener(this);
        mFindBand.setOnPreferenceClickListener(this);
        mBandLocation.setOnPreferenceChangeListener(this);
        mBandDisplaySettings.setOnPreferenceChangeListener(this);
        mTimeFormat.setOnPreferenceChangeListener(this);
        mLiftWrist.setOnPreferenceChangeListener(this);
        mRotateWrist.setOnPreferenceChangeListener(this);
        mHeartRateSleepAssistant.setOnPreferenceChangeListener(this);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mFindBand) {
            mBluetoothLeService.startAlert(AlertMode.ALERT_NORMAL);
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = 0;
        boolean flag = false;
        if (newValue instanceof String) {
            value = Integer.parseInt((String) newValue);
        } else {
            flag = (boolean) newValue;
        }

        if (preference == mDiscoverable) {
            if (flag) {
                mBluetoothLeService.setDiscoverable(true);
            } else {
                mBluetoothLeService.setDiscoverable(false);
            }
        } else if (preference == mBandLocation) {
            if (value == 0) {
                mBluetoothLeService.setBandLocation(BandLocation.BAND_LEFT);
                mBandLocation.setSummary(R.string.left_hand);
            } else {
                mBluetoothLeService.setBandLocation(BandLocation.BAND_RIGHT);
                mBandLocation.setSummary(R.string.right_hand);
            }
        } else if (preference == mBandDisplaySettings) {

        } else if (preference == mTimeFormat) {
            if (value == 0) {
                mBluetoothLeService.setTimeFormat(TimeFormat.TIME_ONLY);
            } else {
                mBluetoothLeService.setTimeFormat(TimeFormat.TIME_DATE);
            }
        } else if (preference == mLiftWrist) {
            if (flag) {
                mBluetoothLeService.setLiftWrist(true);
            } else {
                mBluetoothLeService.setLiftWrist(false);
            }
        } else if (preference == mRotateWrist) {

        } else if (preference == mHeartRateSleepAssistant) {
            if (flag) {
                mBluetoothLeService.setHeartRateSleepAssistant(true);
            } else {
                mBluetoothLeService.setHeartRateSleepAssistant(false);
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothLeService != null) {
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
        }
    }


}

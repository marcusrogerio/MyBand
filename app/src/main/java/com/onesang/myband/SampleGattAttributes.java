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

import com.onesang.myband.sdk.listeners.model.Profile;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap<String, String>();

    static {
        // Sample Services.
        attributes.put(Profile.UUID_SERVICE_GENERIC_ACCESS.toString(), "Generic Access Service");
        attributes.put(Profile.UUID_SERVICE_GENERIC_ATTRIBUTE.toString(), "Generic Attribute Service");
        attributes.put(Profile.UUID_SERVICE_DEVICE_INFORMATION.toString(), "Device Information Service");
        attributes.put(Profile.UUID_SERVICE_UNKNOWN3.toString(), "Uuid_service_unknown3");
        attributes.put(Profile.UUID_SERVICE_ALERT_NOTIFICATION.toString(), "Alert Notification Service");
        attributes.put(Profile.UUID_SERVICE_ALERT.toString(), "Alert Service");
        attributes.put(Profile.UUID_SERVICE_HEARTRATE.toString(), "Heart Rate Service");
        attributes.put(Profile.UUID_SERVICE_MIBAND.toString(), "Miband Service");
        attributes.put(Profile.UUID_SERVICE_MIBAND2.toString(), "Miband2 Service");

        // Sample Descriptor.
        attributes.put(Profile.UUID_DESCRIPTOR_UPDATE_NOTIFICATION.toString(), "Update Notification Descriptor");

        // Sample Characteristics.
        attributes.put(Profile.UUID_CHAR_DEVICE_NAME.toString(), "Device Name Characteristic");
        attributes.put(Profile.UUID_CHAR_APPEARANCE.toString(), "Appearance Characteristic");
        attributes.put(Profile.UUID_CHAR_PERIPHERAL_PREFERRED_CONNECTION_PRAMETERS.toString(), "Peripheral Preferred Connection Parameters");

        attributes.put(Profile.UUID_CHAR_SERVICE_CHANGED.toString(), "Service Changed Characteristic");

        attributes.put(Profile.UUID_CHAR_SERIAL_NUMBER_STRING.toString(), "Serial Number String Characteristic");
        attributes.put(Profile.UUID_CHAR_HARDWARE_REVISION_STRING.toString(), "Hardware Revision String Characteristic");
        attributes.put(Profile.UUID_CHAR_SOFTWARE_REVISION_STRING.toString(), "Software Revision String Characteristic");
        attributes.put(Profile.UUID_CHAR_SYSTEM_ID.toString(), "System Id Characteristic");
        attributes.put(Profile.UUID_CHAR_PNP_ID.toString(), "Pnp Id Characteristic");

        attributes.put(Profile.UUID_CHAR_UNKNOWN10.toString(), "uuid_char_unknown10");
        attributes.put(Profile.UUID_CHAR_UNKNOWN11.toString(), "uuid_char_unknown11");

        attributes.put(Profile.UUID_CHAR_NEW_ALERT.toString(), "New Alert Characteristic");
        attributes.put(Profile.UUID_CHAR_ALERT_NOTIFICATION_CONTROL_POINT.toString(), "Alert Notification Control Point Characteristic");

        attributes.put(Profile.UUID_CHAR_ALERT.toString(), "Alert Characteristic");

        attributes.put(Profile.UUID_CHAR_HEART_RATE_NOTIFICATION.toString(), "Heart rate Notification Characteristic");
        attributes.put(Profile.UUID_CHAR_HEART_RATE_CONTROL_POINT.toString(), "Heart rate Control Point Characteristic");

        attributes.put(Profile.UUID_CHAR_CURRENT_TIME.toString(), "Current Time Characteristic");
        attributes.put(Profile.UUID_CHAR_UNKNOWN15.toString(), "uuid_char_unknown15");
        attributes.put(Profile.UUID_CHAR_UNKNOWN16.toString(), "uuid_char_unknown16");
        attributes.put(Profile.UUID_CHAR_DISPLAY_SETTINGS.toString(), "Display settings Characteristic");
        attributes.put(Profile.UUID_CHAR_UNKNOWN18.toString(), "uuid_char_unknown18");
        attributes.put(Profile.UUID_CHAR_UNKNOWN19.toString(), "uuid_char_unknown19");
        attributes.put(Profile.UUID_CHAR_UNKNOWN20.toString(), "uuid_char_unknown20");
        attributes.put(Profile.UUID_CHAR_UNKNOWN21.toString(), "uuid_char_unknown21");
        attributes.put(Profile.UUID_CHAR_BAND_LOCATION.toString(), "Band Location Characteristic");
        attributes.put(Profile.UUID_CHAR_UNKNOWN23.toString(), "uuid_char_unknown23");

        attributes.put(Profile.UUID_CHAR_UNKNOWN24.toString(), "uuid_char_unknown24");
        attributes.put(Profile.UUID_CHAR_UNKNOWN25.toString(), "uuid_char_unknown25");
        attributes.put(Profile.UUID_CHAR_UNKNOWN26.toString(), "uuid_char_unknown26");
        attributes.put(Profile.UUID_CHAR_UNKNOWN27.toString(), "uuid_char_unknown27");
        attributes.put(Profile.UUID_CHAR_UNKNOWN28.toString(), "uuid_char_unknown28");
        attributes.put(Profile.UUID_CHAR_UNKNOWN29.toString(), "uuid_char_unknown29");
        attributes.put(Profile.UUID_CHAR_UNKNOWN30.toString(), "uuid_char_unknown30");
        attributes.put(Profile.UUID_CHAR_UNKNOWN31.toString(), "uuid_char_unknown31");
        attributes.put(Profile.UUID_CHAR_UNKNOWN32.toString(), "uuid_char_unknown32");

    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}

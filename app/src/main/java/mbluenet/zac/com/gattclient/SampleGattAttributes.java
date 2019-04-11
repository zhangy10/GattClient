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

package mbluenet.zac.com.gattclient;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
//    private static HashMap<String, String> attributes = new HashMap();
//    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
//    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
//
//    static {
//        // Sample Services.
//        attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
//        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
//        // Sample Characteristics.
//        attributes.put(HEART_RATE_MEASUREMENT, "Heart Rate Measurement");
//        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
//    }
//
//    public static String lookup(String uuid, String defaultName) {
//        String name = attributes.get(uuid);
//        return name == null ? defaultName : name;
//    }



    private static HashMap<String, String> attributes = new HashMap();

    /* Current Time Service UUID */
    public static UUID TIME_SERVICE = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
    /* Mandatory Current Time Information Characteristic */
    public static UUID CURRENT_TIME    = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb");
    /* Optional Local Time Information Characteristic */
    public static UUID LOCAL_TIME_INFO = UUID.fromString("00002a0f-0000-1000-8000-00805f9b34fb");
    /* Mandatory Client Characteristic Config Descriptor */
    public static UUID CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    static {
        // Sample Services.
        attributes.put(TIME_SERVICE.toString(), "Time Service");
//        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        // Sample Characteristics.
        attributes.put(CURRENT_TIME.toString(), "Current Time");
        attributes.put(LOCAL_TIME_INFO.toString(), "Local Time");
        attributes.put(CLIENT_CONFIG.toString(), "Current Time Config");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}

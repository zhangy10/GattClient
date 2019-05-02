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

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import mbluenet.zac.com.gattclient.ble.GattClient;
import mbluenet.zac.com.gattclient.ble.GattClientListener;
import mbluenet.zac.com.gattclient.utils.Log;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";


    private static Log log = Log.getInstance();
    private final IBinder mBinder = new LocalBinder();

    private GattClient connectOp;
    private GattClientListener listener = new GattClientListener() {
        @Override
        public void broadcastUpdate(String action, BluetoothGattCharacteristic characteristic) {

            final Intent intent = new Intent(action);

            if (characteristic != null) {
                // This is special handling for the Heart Rate Measurement profile.  Data parsing is
                // carried out as per profile specifications:
                // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
                if (SampleGattAttributes.MDL_SERVICE.equals(characteristic.getUuid())) {
                    int flag = characteristic.getProperties();
                    int format = -1;
                    if ((flag & 0x01) != 0) {
                        format = BluetoothGattCharacteristic.FORMAT_UINT16;
                        log.d("Heart rate format UINT16.");
                    } else {
                        format = BluetoothGattCharacteristic.FORMAT_UINT8;
                        log.d("Heart rate format UINT8.");
                    }
                    final int heartRate = characteristic.getIntValue(format, 1);
                    log.d(String.format("--------------Received heart rate: %d", heartRate));
                    intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
                } else {
                    // For all other profiles, writes the data formatted in HEX.
                    final byte[] data = characteristic.getValue();

                    if (data != null && data.length > 0) {

                        // original data show
                        final StringBuilder stringBuilder = new StringBuilder(data.length);
                        for (byte byteChar : data)
                            stringBuilder.append(String.format("%02X ", byteChar));

                        String extraData = new String(data) ;
//                                + "\n" + stringBuilder.toString();

                        log.d("-----------ExtraData: " + extraData + " byte length: " + data.length);
                        intent.putExtra(EXTRA_DATA, extraData);
                    }
                }
            }

            // update UI
            sendBroadcast(intent);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        if (connectOp != null) {
            connectOp.close();
        }
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public void initialize(String address) {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        log.d("[Connect ready...]");
        connectOp = new GattClient(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address), listener);
    }

    public GattClient getConnection() {
        return connectOp;
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

}

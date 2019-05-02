package mbluenet.zac.com.gattclient.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.List;

import mbluenet.zac.com.gattclient.BluetoothLeService;
import mbluenet.zac.com.gattclient.SampleGattAttributes;
import mbluenet.zac.com.gattclient.utils.Log;

public class GattClient implements GattRequest {

//        extends BaseTask {

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    private static Log log = Log.getInstance();
    private int state = STATE_DISCONNECTED;
    private GattClientListener listener;
    private BluetoothGatt mBluetoothGatt;
    /**
     * Client Gatt callback
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                state = STATE_CONNECTED;
                log.d("onConnectionStateChange: Connected!!");

                // Attempts to discover services after successful connection.
                boolean service = mBluetoothGatt.discoverServices();
                log.d("Get service list..." + service);

                if (listener != null) {
                    listener.broadcastUpdate(BluetoothLeService.ACTION_GATT_CONNECTED, null);
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                log.d("onConnectionStateChange: Disconnected from GATT server, and close current one!");
                close();
            } else {
                log.d("unknow: op...");
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                log.d(String.format("ServicesDiscovered"));
                if (listener != null) {
                    listener.broadcastUpdate(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED, null);
                }
            } else {
                log.d("onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                log.d(String.format("Read Data: "));
                if (listener != null) {
                    listener.broadcastUpdate(BluetoothLeService.ACTION_DATA_AVAILABLE, characteristic);
                }

                characteristic.setValue("This zac yes....");
                sendData(characteristic);

//                setNotice(characteristic, true);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                log.d(String.format("[write finish, turn off notice]"));
                byte[] data = characteristic.getValue();
                if (data != null) {
                    log.d("[Message from server]: " + new String(data));
                }

//                setNotice(characteristic, false);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            /**
             * sever to client message
             *
             */
            log.d(String.format("Get Notice: "));

            if (listener != null) {
                listener.broadcastUpdate(BluetoothLeService.ACTION_DATA_AVAILABLE, characteristic);
            }
        }


        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            log.d(String.format("onDescriptorWrite status: " + status));

        }
    };
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice device;

    public GattClient(BluetoothDevice device, GattClientListener listener) {
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.device = device;
        this.listener = listener;
    }

//    @Override
//    public boolean runTask() throws Exception {
//
//
//        return true;
//    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect() {
        if (device == null) {
            log.d("Device not found.  Unable to connect.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
//        if (mBluetoothGatt != null) {
//            log.d("use an existing mBluetoothGatt for connection.");
//            if (mBluetoothGatt.connect()) {
//                state = STATE_CONNECTING;
//                return true;
//            } else {
//                return false;
//            }
//        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(null, false, mGattCallback);

        boolean refresh = refreshDeviceCache(mBluetoothGatt);
        log.d("create a new connection. refresh: " + refresh);

//        log.d("create a new connection.");
        state = STATE_CONNECTING;
        return true;
    }

    public int getState() {
        return state;
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
        state = STATE_DISCONNECTED;
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
        mBluetoothGatt = null;

        if (listener != null) {
            listener.broadcastUpdate(BluetoothLeService.ACTION_GATT_DISCONNECTED, null);
        }
        log.d("Gatt disconnect and close....");
    }


    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    @Override
    public void readData(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt == null) {
            log.d("Client is not initialized!!");
            return;
        }

        log.d("readData data: " + characteristic.getValue());
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    @Override
    public void sendData(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothGatt == null) {
            log.d("Client is not initialized!!");
            return;
        }

        boolean result = mBluetoothGatt.writeCharacteristic(characteristic);
        log.d("[send data: ]" + new String(characteristic.getValue()) + " result: " + result);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    @Override
    public void setNotice(BluetoothGattCharacteristic characteristic,
                          boolean enabled) {
        if (mBluetoothGatt == null) {
            log.d("Client is not initialized");
            return;
        }

        byte[] data = characteristic.getValue();
        log.d("setNotice data: " + (data == null ? data : new String(data)));
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        /**
         * listening data change
         *
         */
        if (SampleGattAttributes.RES_INFO.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    SampleGattAttributes.RES_NOTICE);
            descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);

            log.d("writeDescriptor data: " + enabled);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    @Override
    public List<BluetoothGattService> getService() {
        if (mBluetoothGatt == null) {
            log.d("Client is not initialized");
            return null;
        }

        return mBluetoothGatt.getServices();
    }

    private boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        } catch (Exception localException) {
            log.e("An exception occured while refreshing device");
        }
        return false;
    }
}

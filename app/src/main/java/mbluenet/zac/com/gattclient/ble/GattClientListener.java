package mbluenet.zac.com.gattclient.ble;

import android.bluetooth.BluetoothGattCharacteristic;

public interface GattClientListener {

    public void broadcastUpdate(final String action,
                                final BluetoothGattCharacteristic characteristic);

}

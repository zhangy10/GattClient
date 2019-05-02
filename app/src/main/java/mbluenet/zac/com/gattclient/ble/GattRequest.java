package mbluenet.zac.com.gattclient.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.List;

public interface GattRequest {

    public void readData(BluetoothGattCharacteristic characteristic);
    public void setNotice(BluetoothGattCharacteristic characteristic,
                          boolean enabled);
    public List<BluetoothGattService> getService();

    public void sendData(BluetoothGattCharacteristic characteristic);
}

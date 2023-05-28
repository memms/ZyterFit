package MiBandbluetooth.Bluetooth.Actions;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import androidx.annotation.NonNull;

import MiBandbluetooth.Device.MiBandService;

import java.util.Date;
import java.util.UUID;

public abstract class BtLEAction {
    static final UUID UUID_DESCRIPTOR_GATT_CLIENT_CHARACTERISTIC_CONFIGURATION = UUID.fromString((String.format(MiBandService.BASE_UUID, "2902")));

    private final BluetoothGattCharacteristic characteristic;
    private final long creationTimestamp;

    BtLEAction(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
        creationTimestamp = System.currentTimeMillis();
    }

    public abstract boolean expectsResult();


    public abstract boolean run(BluetoothGatt gatt);

    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }

    private String getCreationTime() {
        return new Date(creationTimestamp).toString();
    }

    @NonNull
    public String toString() {
        BluetoothGattCharacteristic characteristic = getCharacteristic();
        String uuid = characteristic == null ? "(null)" : characteristic.getUuid().toString();
        return getCreationTime() + ": " + getClass().getSimpleName() + " on characteristic: " + uuid;
    }
}



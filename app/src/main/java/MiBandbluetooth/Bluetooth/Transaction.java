package MiBandbluetooth.Bluetooth;

import android.bluetooth.BluetoothGattCallback;

import androidx.annotation.Nullable;



import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import MiBandbluetooth.Bluetooth.Actions.BtLEAction;

class Transaction {
    private final List<BtLEAction> mActions = new ArrayList<>(4);
    private
    @Nullable
    BluetoothGattCallback gattCallback;

    void add(BtLEAction action) {
        mActions.add(action);
    }

    List<BtLEAction> getActions() {
        return Collections.unmodifiableList(mActions);
    }

    boolean hasElements() {
        return !mActions.isEmpty();
    }

    void setGattCallback(@Nullable BluetoothGattCallback callback) {
        gattCallback = callback;
    }


    @Nullable
    BluetoothGattCallback getGattCallback() {
        return gattCallback;
    }

}

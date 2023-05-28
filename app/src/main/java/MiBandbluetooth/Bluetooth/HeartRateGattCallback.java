package MiBandbluetooth.Bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import MiBandbluetooth.Activities.DeviceControlActivity;
import MiBandbluetooth.DataStructures.HeartRate;
import MiBandbluetooth.Device.MiBandDevice;
import MiBandbluetooth.Device.MiBandService;
import MiBandbluetooth.Device.MiBandSupport;

@RequiresApi(api = Build.VERSION_CODES.M)
public class HeartRateGattCallback extends BluetoothGattCallback {

    public static String TAG = "MiBand: HeartRateGattCallback";

    private static final byte[] stopHeartMeasurementManual = new byte[]{0x15, MiBandService.COMMAND_SET_HR_MANUAL, 0};
    private static final byte[] startHeartMeasurementContinuous = new byte[]{0x15, MiBandService.COMMAND_SET__HR_CONTINUOUS, 1};
    private static final byte[] stopHeartMeasurementContinuous = new byte[]{0x15, MiBandService.COMMAND_SET__HR_CONTINUOUS, 0};

    private MiBandSupport mSupport;
    private Context mContext;

    private boolean heartRateNotifyEnabled;

    public HeartRateGattCallback(MiBandSupport support, Context context){
        mSupport = support;
        mContext = context;
    }

    MiBandDevice getDevice(){
        return mSupport.getDevice();
    }

    private BluetoothQueue getQueue(){
        return mSupport.getQueue();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private TransactionBuilder performInitialized() throws IOException {
        TransactionBuilder builder = mSupport.performInitialized();
        builder.setGattCallback(this);
        return builder;
    }

    private BluetoothGattCharacteristic getCharacteristic(UUID uuid) {
        return mSupport.getCharacteristic(uuid);
    }

    private void enableNotifyHeartRateMeasurements(boolean enable, TransactionBuilder builder) {
        if (heartRateNotifyEnabled != enable) {
            BluetoothGattCharacteristic heartrateCharacteristic = getCharacteristic(MiBandService.UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT);
            if (heartrateCharacteristic != null) {
                builder.notify(heartrateCharacteristic, enable);
                heartRateNotifyEnabled = enable;
            }
        }
    }

    public void enableRealtimeHeartRateMeasurement(boolean enable) {
        BluetoothGattCharacteristic characteristicHRControlPoint = getCharacteristic(MiBandService.UUID_CHARACTERISTIC_HEART_RATE_CONTROL_POINT);
        if (characteristicHRControlPoint == null) {
            Log.i("TESTTTTWESJLHNWFKJLHFWF", "Hkwjbdajkjhbdawliudha");
            return;
        }
        try {
            TransactionBuilder builder = performInitialized();
            enableNotifyHeartRateMeasurements(enable, builder);
            if (enable== true) {
                builder.write(characteristicHRControlPoint, stopHeartMeasurementManual);
                builder.write(characteristicHRControlPoint, startHeartMeasurementContinuous);
            } else {
                Log.i("THE STOPHEARTMEASUREMENT RAN", "THE STOPHEARTMEASUREMENT RAN");
                builder.write(characteristicHRControlPoint, stopHeartMeasurementContinuous);
            }
            builder.queue(getQueue());
        } catch (IOException ex) {
            Log.d(HeartRateGattCallback.TAG, "Unable to enable realtime heart rate measurement", ex);
        }
    }

    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {

        Log.d(HeartRateGattCallback.TAG, "On characteristic changed");

        UUID characteristicUUID = characteristic.getUuid();
        if (MiBandService.UUID_CHARACTERISTIC_HEART_RATE_MEASUREMENT.equals(characteristicUUID)) {
            Log.d(MiBandSupport.TAG, "Heart rate characteristic captured");
            handleHeartRate(characteristic.getValue());
        }else {
            mSupport.onCharacteristicChanged(gatt, characteristic);
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    private void handleHeartRate(byte[] value) {
        if (value.length == 2 && value[0] == 0) {
            int hrValue = (value[1] & 0xff);

            Log.d(HeartRateGattCallback.TAG, "heart rate: " + hrValue);

            DeviceControlActivity activity = (DeviceControlActivity) mContext;

            if (hrValue > 0) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                HeartRate heartRate = new HeartRate(hrValue, Calendar.getInstance().getTime());
//                activity.addEntry(heartRate);
                SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putInt("HeartRateValue", heartRate.getValue());
                editor.commit();
//                Log.d(HeartRateGattCallback.TAG, ""+heartRate.getValue());

            }
        }
    }
}

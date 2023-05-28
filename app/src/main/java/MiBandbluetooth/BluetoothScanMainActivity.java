package MiBandbluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import MiBandbluetooth.Activities.DiscoveryActivity;
import MiBandbluetooth.Device.MiBandSupport;
import com.example.zyterfitlayout.R;


public class BluetoothScanMainActivity extends AppCompatActivity {

    public static final String TAG = "MiBand: BluetoothScanMainActivity";

    @SuppressLint("StaticFieldLeak")
    private static BluetoothScanMainActivity context;
    @SuppressLint("StaticFieldLeak")
    private static MiBandSupport miBandSupport;

    BluetoothAdapter bluetoothAdapter;
    public static Activity BLEScanMainActivity;



    public BluetoothScanMainActivity() {
        context = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVisible(false);
        BLEScanMainActivity = this;
//        setContentView(R.layout.activity_bluetooth_scan);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        startActivity(new Intent(BluetoothScanMainActivity.getContext(), DiscoveryActivity.class));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant access");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                    }
                });
                builder.show();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "coarse location permission granted");
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Functionality limited");
                builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                });
                builder.show();
            }
        }
    }



    public static Context getContext() {
        return context;
    }

    public static void setMiBandSupport(MiBandSupport miBandSupport) {
        BluetoothScanMainActivity.miBandSupport = miBandSupport;
    }

    public static MiBandSupport getMiBandSupport(){
        return BluetoothScanMainActivity.miBandSupport;
    }
}

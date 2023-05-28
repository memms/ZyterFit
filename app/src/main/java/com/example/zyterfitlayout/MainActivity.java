package com.example.zyterfitlayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.PreferenceManager;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.api.services.people.v1.model.BraggingRights;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.prefs.PreferenceChangeListener;

import MiBandbluetooth.Device.DeviceCandidateAdapter;
import MiBandbluetooth.Device.DeviceService;
import MiBandbluetooth.Device.MiBandDevice;

import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;


public class MainActivity extends AppCompatActivity {
    public static MiBandDevice device;
    public static final ArrayList<MiBandDevice> deviceList = new ArrayList<>();
    public static DeviceCandidateAdapter devicesListAdapter;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getBundleExtra("MiBandDevice");
            device = bundle.getParcelable(MiBandDevice.EXTRA_DEVICE);
            int index = deviceList.indexOf(device);
            if (index >= 0) {
                // replace existing device candidate
                deviceList.set(index, device);
            } else {
                deviceList.add(device);
            }
            devicesListAdapter.notifyDataSetChanged();
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        IntentFilter deviceIntent = new IntentFilter();
        deviceIntent.addAction("testingmiband");
        registerReceiver(broadcastReceiver,deviceIntent);

//      Bottom Navigation Bar start
        BottomNavigationView bottomNav = findViewById(R.id.BottomNav);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
//      To set Dashboard as start screen
        getSupportFragmentManager().beginTransaction().replace(R.id.FragmentContainer,
                new DashboardFragment()).commit();

        devicesListAdapter = new DeviceCandidateAdapter(this, deviceList);


     }

    /**  Action that takes place when clicked on the Bottom Nav Bar */
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFrag = null;
                    switch (item.getItemId()){
                        case R.id.Dashboard:
                            selectedFrag = new DashboardFragment();
                            break;
                        case R.id.Heart:
                            selectedFrag = new HeartFragment();
                            break;
                        case R.id.Metrics:
                            selectedFrag = new MetricsFragment();
                            break;
                        case R.id.Settings:
                            selectedFrag = new SettingsFragment();
                            break;
                        case R.id.Profile:
                            selectedFrag = new ProfileFragment();
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.FragmentContainer,
                            selectedFrag).commit();
                    return true;
                }
            };

//    public void refreshMetricsFragment(){
//
//        MetricsFragment metricsFragment = new MetricsFragment();
//        getSupportFragmentManager().beginTransaction().replace(R.id.FragmentContainer,
//                metricsFragment).commit();
//    }

}
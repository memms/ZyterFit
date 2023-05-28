package MiBandbluetooth.Activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;


import com.example.zyterfitlayout.MainActivity;
import com.example.zyterfitlayout.R;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import MiBandbluetooth.Bluetooth.HeartRateGattCallback;
import MiBandbluetooth.BluetoothScanMainActivity;
import MiBandbluetooth.Device.MiBandDevice;
import MiBandbluetooth.Utils.AndroidUtils;

public class DeviceControlActivity extends AppCompatActivity {
    public static String TAG = "MiBand: DeviceControlActivity";

    HeartRateGattCallback heartrateGattCallback;
    public static ScheduledExecutorService service;

    Button clickBtn;
    MiBandDevice mDevice;
    RadioButton radioButton;
    RadioGroup radioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_control);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        mDevice = bundle.getParcelable(MiBandDevice.EXTRA_DEVICE);

        Intent mainActIntent = new Intent();
        mainActIntent.setAction("testingmiband");
        mainActIntent.putExtra("MiBandDevice", bundle);
        sendBroadcast(mainActIntent);

        clickBtn = findViewById(R.id.onBtn);
        clickBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                int radioID = radioGroup.getCheckedRadioButtonId();
                radioButton = findViewById(radioID);

                Log.i("TAGGG", ""+radioID);

                startHeartRateMeasurement();
//                moveTaskToBack(true);
                finish();
                BluetoothScanMainActivity.BLEScanMainActivity.finish();
            }
        });

        radioGroup = findViewById(R.id.radio_group);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startHeartRateMeasurement(){
        AndroidUtils.toast(DeviceControlActivity.this, "Heart Rate Recording has started", Toast.LENGTH_SHORT);
        final MainActivity mainActivity = new MainActivity();
        heartrateGattCallback = new HeartRateGattCallback(BluetoothScanMainActivity.getMiBandSupport(), DeviceControlActivity.this);
        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                FragmentActivity activity = mainActivity;
                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            heartrateGattCallback.enableRealtimeHeartRateMeasurement(true);
                        }
                    });
                }
            }
        }, 0, 4000, TimeUnit.MILLISECONDS);
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void checkButton(View view) {
        int radioID = radioGroup.getCheckedRadioButtonId();
        radioButton = findViewById(radioID);
        Toast.makeText(this, "Data will be tracked every: " +radioButton.getText(), Toast.LENGTH_SHORT).show();
    }

}

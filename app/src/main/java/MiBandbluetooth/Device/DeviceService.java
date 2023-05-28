package MiBandbluetooth.Device;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.example.zyterfitlayout.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataUpdateRequest;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import MiBandbluetooth.Activities.DeviceControlActivity;
import MiBandbluetooth.BluetoothScanMainActivity;
import MiBandbluetooth.Utils.AndroidUtils;

public class DeviceService extends Service {
    public static String TAG = "MiBand: DeviceService";

    public static final String CHANNEL_ID = "MyNotificationChannelID";
    int hrValue;
    private boolean mStarted;
    public static MiBandSupport mMiBandSupport;
    public MiBandDevice mDevice;

    public static boolean mainActivityisPaused = true;

    final String PREFIX = "com.example.miband";

    SharedPreferences.OnSharedPreferenceChangeListener listener;

    final String ACTION_START = PREFIX + ".action.start";
    final String ACTION_CONNECT = PREFIX + ".action.connect";
    String ACTION_DISCONNECT = PREFIX + ".action.disconnect";
    String EXTRA_CONNECT_FIRST_TIME = "connect_first_time";

    @Deprecated
    String EXTRA_REALTIME_STEPS = "realtime_steps";

    @Deprecated
    String EXTRA_HEART_RATE_VALUE = "hr_value";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            Log.d(DeviceService.TAG, "no intent");
            return START_NOT_STICKY;
        }

        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "ZyterFit", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(notificationChannel);
        Intent notificationIntent = new Intent(this, DeviceControlActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(DeviceService.this);

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals("HeartRateValue")) {
                    hrValue = mPreferences.getInt("HeartRateValue", 0);
                    Notification notification = new NotificationCompat.Builder(DeviceService.this, CHANNEL_ID)
                            .setContentTitle("Currently Tracking Heart Rate")
                            .setContentText("Last Heart Rate: " + hrValue + " BPM")
                            .setSmallIcon(R.drawable.ic_icon)
                            .build();
                    Log.i("hrVlueDeviceService", "" + hrValue);
                    if (mainActivityisPaused) {
                        setGoogleHeartRate();
                    }
                    startForeground(2, notification);
                }
            }
        };

            Notification notification = new NotificationCompat.Builder(DeviceService.this, CHANNEL_ID)
                    .setContentTitle("Currently Tracking HeartRate")
                    .setContentText("Last Heart Rate: " + 0 + " BPM")
                    .setSmallIcon(R.drawable.ic_icon)
                    .build();

            startForeground(2,notification);

            mPreferences.registerOnSharedPreferenceChangeListener(listener);




        String action = intent.getAction();
        boolean firstTime = intent.getBooleanExtra(EXTRA_CONNECT_FIRST_TIME, true);

        if (action == null) {
            Log.d(DeviceService.TAG, "no action");
            return START_NOT_STICKY;
        }

        Log.d(DeviceService.TAG, "Service startcommand: " + action);

        if (!action.equals(ACTION_START) && !action.equals(ACTION_CONNECT)) {
            if (!mStarted) {
                Log.d(DeviceService.TAG, "Must start service with " + ACTION_START + " or " + ACTION_CONNECT + " before using it: " + action);
                return START_NOT_STICKY;
            }
        }

        switch (action) {
            case ACTION_START:
                start();
                break;
            case ACTION_CONNECT:
                start();
                MiBandDevice device = intent.getParcelableExtra(MiBandDevice.EXTRA_DEVICE);

                if (!device.isConnecting() && !device.isConnected()) {
                    setDeviceSupport(null);
                    try {
                        MiBandSupport miBandSupport = new MiBandSupport();
                        miBandSupport.setContext(device, BluetoothAdapter.getDefaultAdapter(), this);

                        setDeviceSupport(miBandSupport);
                        if (firstTime) {
                            miBandSupport.connectFirstTime();
                        } else {
                            miBandSupport.connect();
                        }
                    } catch (Exception e) {
                        Log.d(DeviceService.TAG, e.getMessage());
                        AndroidUtils.toast(this, "Cannot connect:" + e.getMessage(), Toast.LENGTH_SHORT);
                        setDeviceSupport(null);
                    }
                } else {
                    device.sendDeviceUpdateIntent(this);
                }
                break;
            default:
                Log.d(DeviceService.TAG, "Unable to recognize action: " + action);

                break;
        }
        return START_NOT_STICKY;
    }
    private void setGoogleHeartRate(){
        Log.i("Device Service", "Service setGoogleHeartRate is set off.");
        Calendar cal = Calendar.getInstance();
        Date date = new Date();
        cal.setTime(date);
        long endTime = cal.getTimeInMillis();
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(this)
                .setDataType(DataType.TYPE_HEART_RATE_BPM)
                .setType(DataSource.TYPE_RAW)
                .build();
        float BPM = (float) hrValue;
        DataPoint dataPoint =
                DataPoint.builder(dataSource)
                        .setTimeInterval(endTime, endTime, TimeUnit.MILLISECONDS)
                        .setField(Field.FIELD_BPM, BPM)
                        .build();
        DataSet dataSet = DataSet.builder(dataSource).add(dataPoint).build();
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this)).insertData(dataSet);

    }


    private void start() {
        if (!mStarted) {
            mStarted = true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mStarted){
            mStarted = false;
        }
    }

    private void setDeviceSupport(@Nullable MiBandSupport deviceSupport) {
        if (deviceSupport != mMiBandSupport && mMiBandSupport != null) {
            mMiBandSupport.dispose();
            mMiBandSupport = null;
        }
        mMiBandSupport = deviceSupport;

        if (mMiBandSupport != null) {
            BluetoothScanMainActivity.setMiBandSupport(mMiBandSupport);
        }

        mDevice = mMiBandSupport != null ? mMiBandSupport.getDevice() : null;
    }
}

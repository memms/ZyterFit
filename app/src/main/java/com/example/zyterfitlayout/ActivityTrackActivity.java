package com.example.zyterfitlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.zyterfitlayout.DashboardFragment.TAG;
import static com.google.android.gms.fitness.data.Field.FIELD_CALORIES;
import static java.text.DateFormat.getTimeInstance;

public class ActivityTrackActivity extends AppCompatActivity  {

    private Button bActivityType, bstartTracking, bstopTracking;
    private String setActivityType, sessionName, sessionDesc;
    private EditText eActivityName, eActivityDescription;
    private TextView tname, tstep_num, tdistance, tcalories , tnotes, theart_points, tspeed;
    public Chronometer chronometer;
    private RelativeLayout startTrackingLayout, startTrackHeader, stopTrackingLayout;
    private long serviceelapsedtime;
    private int calsint, stepcount, heartpoints;
    private Handler trackinghandler = new Handler();
    boolean getSessionbool = false;
    private Runnable runnable;
    private String sessionID;
    long startTimetest;
    private TrackingService trackingService;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setTracking(true, ActivityTrackActivity.this);
            sessionID = intent.getStringExtra("SessionID");
            serviceelapsedtime = intent.getLongExtra("time", 0);
            startTimetest = intent.getLongExtra("startT", 0);
            startStopWatch();
            if(sessionID != null) {
                getTrackingSessionInfo(sessionID);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_activity);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("ID");
        intentFilter.addAction("time");
        registerReceiver(broadcastReceiver, intentFilter);

        bActivityType = findViewById(R.id.track_activity_type);
        bstartTracking = findViewById(R.id.track_activity_start);
        bstopTracking = findViewById(R.id.stop_tracking_button);

        eActivityName = findViewById(R.id.track_activity_title);
        eActivityDescription = findViewById(R.id.track_activity_desc);

        startTrackingLayout = findViewById(R.id.start_tracking_layout);
        startTrackHeader = findViewById(R.id.track_activity_header);
        stopTrackingLayout = findViewById(R.id.stop_tracking_layout);

        tname = findViewById(R.id.activity_name_show);
        tdistance = findViewById(R.id.miles_track);
        tstep_num = findViewById(R.id.steps_track);
        tcalories = findViewById(R.id.cals_track);
        theart_points = findViewById(R.id.heart_points_track);
        tspeed = findViewById(R.id.pace_track);

        chronometer = findViewById(R.id.tracking_stopwatch);

        Log.i("ZyterFit", "Tracking?" + getTracking(this));

        bstartTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countDown();
            }
        });
        bstopTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService();
            }
        });

        if(getTracking(this) ==true){
            starter();
        }

    }
    @Override
    protected void onResume() {
        if(getSessionbool == true) {
            trackinghandler.postDelayed(runnable, 10);
            Log.i("Runnanble", "runnable executed");
        }
        if(getTracking(this) == true) {
            startService();
        }
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Runnanble", "runnable was paused");
        trackinghandler.removeCallbacks(runnable);

    }

    @Override
    protected void onDestroy() {
        try{
            unregisterReceiver(broadcastReceiver);
        }catch(IllegalArgumentException e){
        }
        super.onDestroy();
    }

    public void setTracking(Boolean value, Context context){
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean("tracking", value);
        editor.commit();
    }
    public boolean getTracking(Context context){
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return mPreferences.getBoolean("tracking", false);
    }

    public void getActivityType(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Activity");

        String[] activities = {"Walking", "Running", "Sleep"};
        builder.setItems(activities, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int pos) {
                switch (pos) {
                    case 0:
                        setActivityType = FitnessActivities.WALKING;
                        bActivityType.setText("Walking");
                        break;
                    case 1:
                        setActivityType = FitnessActivities.RUNNING;
                        bActivityType.setText("Running");
                        break;
                    case 2:
                        setActivityType = FitnessActivities.SLEEP;
                        bActivityType.setText("Sleep");
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void getTrackingSessionInfo(String ID){

        calsint =0;
        stepcount = 0;
        heartpoints = 0;

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.add(Calendar.DATE, +1);
        long endTime = cal.getTimeInMillis();
        long startTime = serviceelapsedtime;

        final SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setSessionId(ID)
                .setTimeInterval(startTime,endTime,TimeUnit.MILLISECONDS)
                .read(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .read(DataType.AGGREGATE_DISTANCE_DELTA)
                .read(DataType.AGGREGATE_CALORIES_EXPENDED)
                .read(DataType.AGGREGATE_HEART_POINTS)
                .read(DataType.TYPE_SPEED)
                .build();

                Fitness.getSessionsClient(ActivityTrackActivity.this, GoogleSignIn.getLastSignedInAccount(ActivityTrackActivity.this))
                        .readSession(readRequest)
                        .addOnSuccessListener(new OnSuccessListener<SessionReadResponse>() {
                            @Override
                            public void onSuccess(final SessionReadResponse sessionReadResponse) {
                                final List<Session> sessions = sessionReadResponse.getSessions();
                                getSessionbool = true;
                                runnable = new Runnable() {
                                    public void run() {
                                        Log.i("Runnanble", "runnable is running");
                                        Log.i(TAG, "Session read was successful. Number of returned sessions is: "
                                                + sessions.size());
                                        trackinghandler.postDelayed(runnable, 2000);
                                        setActivitySession(sessions.get(0));
                                        List<DataSet> dataSets = sessionReadResponse.getDataSet(sessions.get(0));

                                        setActivityDataSet();



                                    }
                                };
                                trackinghandler.postDelayed(runnable, 10);

                            }

                        });
    }

    private void setActivitySession(Session session) {

        tname.setText(session.getName());
//        tnotes.setText(session.getDescription());

    }
    private void setActivityDataSet() {


        final DataReadRequest req = new DataReadRequest.Builder()
                .bucketByTime(1, TimeUnit.DAYS)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .aggregate(DataType.TYPE_HEART_POINTS, DataType.AGGREGATE_HEART_POINTS)
                .aggregate(DataType.TYPE_SPEED,DataType.AGGREGATE_SPEED_SUMMARY)
                .setTimeRange(startTimetest, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(req)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                Log.i("Success:", "readData Successful");
                                if (dataReadResponse.getBuckets().size() > 0) {
                                    for (Bucket bucket : dataReadResponse.getBuckets()) {
                                        List<DataSet> dataSets = bucket.getDataSets();
                                        for (DataSet dataSet : dataSets) {
                                            for (DataPoint dp : dataSet.getDataPoints()) {
                                                for (Field field : dp.getDataType().getFields()) {
                                                    if (field.getName().contains("step")) {
                                                        Log.i("tracking steps: ", "" + dp.getValue(field).asInt());
                                                        stepcount = dp.getValue(field).asInt();
                                                        tstep_num.setText("" + stepcount);
                                                    }
                                                    if (dp.getDataType().getName().contains("distance")) {
                                                        Float meters = Float.valueOf(String.valueOf(dp.getValue(field)));
                                                        Double dmeters = (double) meters;
                                                        dmeters /= 1609.0;
                                                        dmeters = Math.round(dmeters * 100.0) / 100.0;
                                                        tdistance.setText(dmeters.toString());
                                                    }
                                                    if (dp.getDataType().getName().contains("heart_minutes")) {
                                                        Float fheartpoints = dp.getValue(field).asFloat();
                                                        heartpoints = fheartpoints.hashCode();
                                                        theart_points.setText("" + heartpoints);
                                                    }
                                                    if (field.getName().contains("average")){
                                                        Float fspeed = dp.getValue(field).asFloat();
                                                        Double speed = (double) fspeed*2.2369;
                                                        speed = Math.round(speed*100)/100.00;
                                                        tspeed.setText(""+speed);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });


    }

    public void startStopWatch(){
        chronometer.setBase(serviceelapsedtime);
        chronometer.start();
    }

    /** Working **/
    private void countDown(){
        TextView actstarttitle = findViewById(R.id.track_activity_start_text);
        bstartTracking.setVisibility(View.GONE);
        actstarttitle.setVisibility(View.GONE);
        final Handler handler = new Handler();
        final TextView textView = findViewById(R.id.track_activity_timer);
        textView.setVisibility(View.VISIBLE);
        final java.util.concurrent.atomic.AtomicInteger n = new AtomicInteger(3);

        final Runnable counter =  new Runnable() {
            @Override
            public void run() {
                textView.setText(Integer.toString(n.get()));
                if(n.getAndDecrement() >= 1 )
                    handler.postDelayed(this, 1000);
                else {
                    textView.setVisibility(View.GONE);
                    starter();
                }
            }
        };
        handler.postDelayed(counter, 0);

    }

    public void startService(){
        sessionName = eActivityName.getText().toString();
        sessionDesc = eActivityDescription.getText().toString();

        Intent serviceintent = new Intent(this, TrackingService.class);
        serviceintent.putExtra("activitytype", "buffer_activity");
        serviceintent.putExtra("sessionName", sessionName);
        serviceintent.putExtra("sessionDesc", sessionDesc);
        serviceintent.putExtra("setActivityType", setActivityType);

        trackingService = new TrackingService();
        startService(serviceintent);

    }

    public void stopService(){
        stopTracking();
        Intent serviceintent = new Intent(this, TrackingService.class);
        stopService(serviceintent);
        chronometer.stop();
        trackinghandler.removeCallbacks(runnable);
        try{
            unregisterReceiver(broadcastReceiver);
        }catch(IllegalArgumentException e){
        }
    }
    private void stopTracking(){
        Task<List<Session>> response = Fitness.getSessionsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .stopSession(sessionID);

        response.addOnSuccessListener(new OnSuccessListener<List<Session>>() {
            @Override
            public void onSuccess(List<Session> sessions) {
                setTracking(false, ActivityTrackActivity.this);

            }
        });

    }

    public void starter() {
        startTrackingLayout.setVisibility(View.GONE);
        startTrackHeader.setVisibility(View.GONE);
        stopTrackingLayout.setVisibility(View.VISIBLE);

        if (!getTracking(this) ==true) {
            startService();
        }
    }

}
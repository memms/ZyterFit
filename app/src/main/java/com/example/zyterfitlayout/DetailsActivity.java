package com.example.zyterfitlayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.example.zyterfitlayout.DashboardFragment.TAG;
import static com.google.android.gms.fitness.data.Field.FIELD_CALORIES;
import static com.google.android.gms.fitness.data.Field.FIELD_DISTANCE;
import static com.google.android.gms.fitness.data.Field.FIELD_DURATION;
import static com.google.android.gms.fitness.data.Field.FIELD_STEPS;
import static java.text.DateFormat.getDateTimeInstance;
import static java.text.DateFormat.getTimeInstance;

public class DetailsActivity extends AppCompatActivity {
    Button Bcancel, Bdelete, bTestButton;
    private TextView Tname, Tdatetime, Tactive_time, Tdistance, Tcalories,Tmove_mins, Tstep_num, Tnotes;
    private Integer position;
    private int moveminstest, stepcount,calsint;
    private String activityName;
    private Session dsessionnum;
    private Context context;
    long startTime, endTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        context = this;


        Tdatetime = findViewById(R.id.Ddate_time);
        Tname = findViewById(R.id.DactivityName);
        Tactive_time = findViewById(R.id.Dactive_time_val);
        Tdistance = findViewById(R.id.Ddistance_val);
        Tcalories = findViewById(R.id.Dcalories_val);
        Tmove_mins = findViewById(R.id.Dmove_mins_val);
        Tstep_num = findViewById(R.id.Dstep_num);
        Tnotes = findViewById(R.id.Dnotes_text_val);
        Bcancel = findViewById(R.id.Dclose);
        Bdelete = findViewById(R.id.Ddelete);
        bTestButton = findViewById(R.id.test_button);
        Intent intent = getIntent();
        position = intent.getIntExtra("sessionPosition", 0);
        activityName = intent.getStringExtra("activityName");

        Tname.setText("" + activityName);
        getActivityList();

        Bcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MetricsFragment metricsFragment = new MetricsFragment();
                finish();
            }
        });

    }

    private void getActivityList() {

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.add(Calendar.DATE, +1);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.YEAR, -1);
        long startTime = cal.getTimeInMillis();

        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .readSessionsFromAllApps()
                .readSessionsFromAllApps()
                .enableServerQueries()
                .build();

        Fitness.getSessionsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readSession(readRequest)
                .addOnSuccessListener(new OnSuccessListener<SessionReadResponse>() {
                    @Override
                    public void onSuccess(SessionReadResponse sessionReadResponse) {
                        List<Session> sessions = sessionReadResponse.getSessions();
                        Log.i("Test", ""+sessions.size());

                        int sessionPos = (sessions.size()-1)-position;
                        dsessionnum = sessions.get(sessionPos);
                        setSession(sessions.get(sessionPos));
                        }

                });
    }
    private void setSession(Session session){


        if (session.getEndTime(TimeUnit.MILLISECONDS)>0) {
            endTime = session.getEndTime(TimeUnit.MILLISECONDS);
        } else {
            endTime = session.getStartTime(TimeUnit.MILLISECONDS)+1000000;
        }
        startTime = session.getStartTime(TimeUnit.MILLISECONDS);


//        Log.i("buffer_activity", "" + startTime + "\n " + endTime);
        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .readSessionsFromAllApps()
                .read(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .read(DataType.TYPE_MOVE_MINUTES)
                .read(DataType.AGGREGATE_MOVE_MINUTES)
                .read(DataType.AGGREGATE_DISTANCE_DELTA)
                .read(DataType.AGGREGATE_CALORIES_EXPENDED)
                .build();

        Fitness.getSessionsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readSession(readRequest)
                .addOnSuccessListener(new OnSuccessListener<SessionReadResponse>() {
                    @Override
                    public void onSuccess(SessionReadResponse sessionReadResponse) {
                        List<Session> sessions = sessionReadResponse.getSessions();
                        Log.i(TAG, "Session read was successful. Number of returned sessions is: "
                                + sessions.size());

                        setActivitySession(sessions.get(0));
                        deleteSession(sessions.get(0));

                        stopTracking(sessions.get(0));
                        List<DataSet> dataSets = sessionReadResponse.getDataSet(sessions.get(0));
                        calsint =0;
                        moveminstest = 0;
                        stepcount = 0;
                        for (DataSet dataSet : dataSets) {
                            setActivityDataSet(dataSet);
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "Failed to read session");
            }
        });
    }
    private void setActivitySession(Session session) {
        Log.i("Sessionnametest" , ""+session.getName());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, hh:mm", Locale.getDefault());
        DateFormat dateFormat1 = getTimeInstance(DateFormat.SHORT);

        Tdatetime.setText(simpleDateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS)) + " - " + dateFormat1.format(session.getEndTime(TimeUnit.MILLISECONDS)));

        long diff = session.getEndTime(TimeUnit.MILLISECONDS) - session.getStartTime(TimeUnit.MILLISECONDS);
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        String activetime = String.valueOf(hours) + "h " + String.valueOf(minutes - (hours * 60)) + "m " + String.valueOf(seconds-(minutes*60) + "s");
        Tactive_time.setText(activetime);
        Tnotes.setText(session.getDescription());
    }
    private void setActivityDataSet(DataSet dataSet) {
        for (DataPoint dp : dataSet.getDataPoints()) {
            if(dp.getStartTime(TimeUnit.MILLISECONDS) >= (startTime-500) && dp.getStartTime(TimeUnit.MILLISECONDS) <= (endTime+500)) {
                if (dp.getDataType().getName().contains("distance")) {
                    Float meters = Float.valueOf(String.valueOf(dp.getValue(FIELD_DISTANCE)));
                    Double dmeters = (double) meters;
                    dmeters /= 1609.0;
                    dmeters = Math.round(dmeters * 100.0) / 100.0;
                    Tdistance.setText("" + dmeters + " miles");
                }
                if (dp.getDataType().getName().contains("calories")) {
                    Float kcal = Float.valueOf(String.valueOf(dp.getValue(FIELD_CALORIES)));
                    calsint += kcal.intValue();
                    Tcalories.setText("" + calsint);
                }
                if (dp.getDataType().getName().contains("com.google.active_minutes")) {
                    moveminstest += (dp.getValue(FIELD_DURATION)).asInt();
                    Tmove_mins.setText("" + moveminstest);
                }
                if (dp.getDataType().getName().contains("step_count.delta")) {
                    Log.i("stepcounttest", dp.getDataType().getFields() + " " + String.valueOf(dp.getValue(FIELD_STEPS)) + " " + dp.getStartTime(TimeUnit.MILLISECONDS) + " " + dp.getEndTime(TimeUnit.MILLISECONDS));
                    stepcount += dp.getValue(FIELD_STEPS).asInt();
                    Tstep_num.setText("" + stepcount);
                }
            }
        }
    }
    private void deleteSession (Session session){

        if(session.getAppPackageName().contains("zyterfit")){
            Bdelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DataDeleteRequest request = new DataDeleteRequest.Builder()
                            .setTimeInterval(dsessionnum.getStartTime(TimeUnit.MILLISECONDS), dsessionnum.getEndTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
                            .deleteAllData()
                            .addSession(dsessionnum)
                            .build();
                    Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context))
                            .deleteData(request)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    PreferenceManager.getDefaultSharedPreferences(context)
                                            .edit()
                                            .putBoolean("list_changed", true)
                                            .apply();
                                    finish();
                                    Toast.makeText(DetailsActivity.this, "Activity: " + activityName + " was successfully deleted!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(DetailsActivity.this, "Activity: " + activityName + " failed to be deleted!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        } else {
            Bdelete.setVisibility(View.GONE);
        }
    }
    private void stopTracking (final Session session){
        bTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Task<List<Session>> response = Fitness.getSessionsClient(DetailsActivity.this, GoogleSignIn.getLastSignedInAccount(DetailsActivity.this))
                        .stopSession(session.getIdentifier());
                ActivityTrackActivity activityTrackActivity = new ActivityTrackActivity();
                activityTrackActivity.setTracking(false, DetailsActivity.this);
            }
        });
    }

}
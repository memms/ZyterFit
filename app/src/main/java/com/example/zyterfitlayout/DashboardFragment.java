package com.example.zyterfitlayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.example.zyterfitlayout.datasetdetails.DataSetDetails;
import com.example.zyterfitlayout.datasetdetails.DataSetDetailsActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Calendar;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import MiBandbluetooth.Device.DeviceService;

import static com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA;

public class DashboardFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "ZyterFit";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 1;
    private boolean authgranted = false;
    private int stepsc;
    private TextView textviewcalender,steps,cals,stepsupdated, distance_count, movemins, weight,height,heightunits,weightunit,distanceunit, energyunit, profileName, heartRateText;
    boolean trackerval;
    private ImageView profilePic;
    String heightunitm,weightunitm,distanceunitm,energyunitm;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    ProfileFragment profileFragment;
    SharedPreferences prefs, mPreferences;
    CardView cardHeartRate, cardRestRate, cardStepCount, cardCaloriesCount, cardDistance, cardMoveMinutes, cardWeight;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);


        textviewcalender = (TextView) v.findViewById(R.id.datetime);
        steps = v.findViewById(R.id.stepscount);
        cals = v.findViewById(R.id.energy_count);
        stepsupdated = v.findViewById(R.id.steps_updated);
        distance_count = v.findViewById(R.id.mile_count);
        movemins = v.findViewById(R.id.move_minutes);
        weight = v.findViewById(R.id.weight);
        height = v.findViewById(R.id.height);
        heightunits = v.findViewById(R.id.height_unit);
        weightunit = v.findViewById(R.id.weight_unit);
        distanceunit = v.findViewById(R.id.distance_unit);
        energyunit = v.findViewById(R.id.energy_unit);
        profileName = v.findViewById(R.id.DprofileName);
        profilePic = v.findViewById(R.id.DprofilePic);
        heartRateText = v.findViewById(R.id.heart_rate_num);

        profileFragment = new ProfileFragment();

        cardHeartRate = v.findViewById(R.id.heart_rate_card);
        cardDistance = v.findViewById(R.id.distance_count_card);
        cardMoveMinutes = v.findViewById(R.id.move_mins_card);
        cardWeight = v.findViewById(R.id.weight_card);
        cardRestRate = v.findViewById(R.id.rest_rate_card);
        cardStepCount = v.findViewById(R.id.step_count_card);
        cardCaloriesCount = v.findViewById(R.id.calories_count_card);

        cardStepCount.setOnClickListener(this);
        cardHeartRate.setOnClickListener(this);
        cardRestRate.setOnClickListener(this);
        cardWeight.setOnClickListener(this);
        cardDistance.setOnClickListener(this);
        cardMoveMinutes.setOnClickListener(this);
        cardCaloriesCount.setOnClickListener(this);



        /** Google auth begins */
        getDateTime();
        getGoogleSignIn();
        HeartRateChangeListener();

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        trackerval = prefs.getBoolean("activities_track", true);
        checkSettings();
        heightunitm = prefs.getString("height_unit", "Feet &amp; Inches");
        weightunitm = prefs.getString("weight_unit", "Kilograms");
        distanceunitm = prefs.getString("distance_unit", "Miles");
        energyunitm = prefs.getString("energy_unit", "Calories");

        mPreferences.registerOnSharedPreferenceChangeListener(listener);

        if(authgranted == true){
            dailySteps();
            dailyCal();
            totalDistance();
            totalMoveMinutes();
            weightread();
            heightRead();
            getGoogleHeartRateBPM();
        }
        else {
            getGoogleSignIn();
        }
        setProfile();
        return v;

    }
    private void setProfile(){
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (!mPreferences.getString("personGivenName", "").equals("")) {
            String SprofileName = mPreferences.getString("personGivenName", "");
            profileName.setText("Welcome, " + SprofileName);
        }
        if (!mPreferences.getString("personImage", "").equals("")) {
            Uri UprofilePic = Uri.parse(mPreferences.getString("personImage", ""));
            Glide.with(this)
                    .load(UprofilePic)
                    .into(profilePic);
        }
    }
    private void getDateTime(){
        Date current = Calendar.getInstance().getTime();
        String formattedDate = DateFormat.getDateInstance().format(current);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat timeformat = new SimpleDateFormat("hh:mm a");
        String formtime = timeformat.format(cal.getTime());
        formattedDate += " " + formtime;
        textviewcalender.setText(formattedDate);
    }
    private void checkSettings(){
        if(trackerval==true){
            recordingsubscribe();
        }
        else if(trackerval==false){
            cancelSubscription();
        }
    }
    public void cancelSubscription() {
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .unsubscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully unsubscribed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem unsubscribing.");
                    }
                });
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .unsubscribe(DataType.TYPE_HEART_RATE_BPM)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully unsubscribed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem unsubscribing.");
                    }
                });
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .unsubscribe(DataType.TYPE_ACTIVITY_SEGMENT)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully unsubscribed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem unsubscribing.");
                    }
                });
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .unsubscribe(DataType.TYPE_MOVE_MINUTES)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully unsubscribed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem unsubscribing.");
                    }
                });
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .unsubscribe(DataType.TYPE_SPEED)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully unsubscribe!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem unsubscribe.");
                    }
                });
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .unsubscribe(DataType.TYPE_WORKOUT_EXERCISE)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully unsubscribe!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem unsubscribe.");
                    }
                });
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .unsubscribe(DataType.TYPE_HEART_POINTS)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully unsubscribe!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem unsubscribe.");
                    }
                });
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .unsubscribe(DataType.TYPE_DISTANCE_DELTA)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully unsubscribe!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem unsubscribe.");
                    }
                });

    }
    private void getGoogleSignIn() {
        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                        .addDataType(TYPE_STEP_COUNT_DELTA)
                        .addDataType(DataType.TYPE_HEART_RATE_BPM)
                        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .addDataType(DataType.TYPE_HEIGHT)
                        .addDataType(DataType.TYPE_WEIGHT)
                        .addDataType(DataType.TYPE_MOVE_MINUTES)
                        .addDataType(DataType.TYPE_SPEED)
                        .addDataType(DataType.TYPE_WORKOUT_EXERCISE)
                        .addDataType(DataType.TYPE_HEART_POINTS)
                        .addDataType(DataType.TYPE_DISTANCE_DELTA)
                        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
                        .addDataType(TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_WORKOUT_EXERCISE, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_HEART_POINTS, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_WRITE)
                        .build();
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(getActivity()), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(getActivity()),
                    fitnessOptions);
        }
        else {
            authgranted = true;
            dailySteps();
            dailyCal();
            totalDistance();
            totalMoveMinutes();
            weightread();
            heightRead();
        }
    }
    public void recordingsubscribe(){
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully subscribed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem subscribing.");
                    }
                });
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .subscribe(DataType.TYPE_HEART_RATE_BPM)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully subscribed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem subscribing.");
                    }
                });
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .subscribe(DataType.TYPE_ACTIVITY_SEGMENT)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully subscribed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem subscribing.");
                    }
                });
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .subscribe(DataType.TYPE_MOVE_MINUTES)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully subscribed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem subscribing.");
                    }
                });
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .subscribe(DataType.TYPE_SPEED)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully subscribed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem subscribing.");
                    }
                });
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .subscribe(DataType.TYPE_WORKOUT_EXERCISE)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully subscribed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem subscribing.");
                    }
                });
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .subscribe(DataType.TYPE_HEART_POINTS)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully subscribed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem subscribing.");
                    }
                });
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .subscribe(DataType.TYPE_DISTANCE_DELTA)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully subscribed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "There was a problem subscribing.");
                    }
                });
    }
    public void heightRead(){

        Calendar cal = Calendar.getInstance();
        Date date = new Date();
        cal.setTime(date);
        long endTime = cal.getTimeInMillis();

        final DataReadRequest req = new DataReadRequest.Builder()
                .read(DataType.TYPE_HEIGHT)
                .setTimeRange(1, endTime, TimeUnit.MILLISECONDS)
                .setLimit(1)
                .build();


                Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(req)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                if (dataReadResponse.getBuckets().size() > 0) {
                                    for (Bucket bucket : dataReadResponse.getBuckets()) {
                                        List<DataSet> dataSets = bucket.getDataSets();
                                        for (DataSet dataSet : dataSets) {
                                            for (DataPoint dp : dataSet.getDataPoints()) {
                                                for (Field field : dp.getDataType().getFields()) {
                                                    Float heightfloat = dp.getValue(field).asFloat();
                                                    if(heightunitm.equals("Feet & Inches")) {
                                                        Double heightdouble = (double) heightfloat * 3.2808;
                                                        BigDecimal bigDecimal = new BigDecimal(String.valueOf(heightdouble));
                                                        int intvalue = bigDecimal.intValue();
                                                        double doublevalue = ((bigDecimal.subtract(new BigDecimal(intvalue))).doubleValue()) * 12;
                                                        int inches = (int) (Math.round(doublevalue));
                                                        height.setText("" + intvalue + "'" + inches + "\"");
                                                        heightunits.setText("Feet");
                                                    }
                                                    else {
                                                        int heightint = (int) (heightfloat *100);
                                                        height.setText("" + heightint);
                                                        heightunits.setText("cm");
                                                    }
                                                    Log.i(TAG, "heightc=" + dp.getValue(field));
                                                }
                                            }
                                        }
                                    }
                                } else if (dataReadResponse.getDataSets().size() > 0) {
                                    for (DataSet dataSet : dataReadResponse.getDataSets()) {
                                        for (DataPoint dp : dataSet.getDataPoints()) {
                                            for (Field field : dp.getDataType().getFields()) {
                                                Float heightfloat = dp.getValue(field).asFloat();
                                                if(heightunitm.equals("Feet & Inches")) {
                                                    Double heightdouble = (double) heightfloat * 3.2808;
                                                    BigDecimal bigDecimal = new BigDecimal(String.valueOf(heightdouble));
                                                    int intvalue = bigDecimal.intValue();
                                                    double doublevalue = ((bigDecimal.subtract(new BigDecimal(intvalue))).doubleValue()) * 12;
                                                    int inches = (int) (Math.round(doublevalue));
                                                    height.setText("" + intvalue + "'" + inches + "\"");
                                                    heightunits.setText("Feet");
                                                }
                                                else {
                                                    Double heightdouble =(double) (Math.round((double)(heightfloat *100)));
                                                    height.setText("" + (heightdouble.intValue()));
                                                    heightunits.setText("cm");
                                                }
                                                Log.i(TAG, "heightc=" + dp.getValue(field));
                                            }
                                        }
                                    }
                                }
                            }
                        });

    }
    public void weightread(){
        Calendar cal = Calendar.getInstance();
        Date date = new Date();
        cal.setTime(date);
        long endTime = cal.getTimeInMillis();

        final DataReadRequest req = new DataReadRequest.Builder()
                .read(DataType.TYPE_WEIGHT)
                .setTimeRange(1, endTime, TimeUnit.MILLISECONDS)
                .setLimit(1)
                .build();

        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(req)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                if (dataReadResponse.getBuckets().size() > 0) {
                                    Log.i(
                                            TAG, "Number of returned buckets of DataSets for weight is: " + dataReadResponse.getBuckets().size());
                                    for (Bucket bucket : dataReadResponse.getBuckets()) {
                                        List<DataSet> dataSets = bucket.getDataSets();
                                        for (DataSet dataSet : dataSets) {
                                            for (DataPoint dp : dataSet.getDataPoints()) {
                                                for (Field field : dp.getDataType().getFields()) {
                                                    Float test = dp.getValue(field).asFloat();
                                                    if(weightunitm.equals("Kilograms")) {
                                                        Double weightdouble = (double) test;
                                                        weightdouble = Math.round(weightdouble * 100.0)/100.0;
                                                        weight.setText("" + weightdouble);
                                                    }
                                                    else if(weightunitm.equals("Kilograms")) {

                                                    }
                                                    Log.i(TAG, "weightc=" + dp.getValue(field));

                                                }
                                            }
                                        }
                                    }
                                } else if (dataReadResponse.getDataSets().size() > 0) {
                                    Log.i(
                                            TAG, "Number of returned DataSets for weight is: " + dataReadResponse.getDataSets().size());
                                    for (DataSet dataSet : dataReadResponse.getDataSets()) {
                                        for (DataPoint dp : dataSet.getDataPoints()) {
                                            for (Field field : dp.getDataType().getFields()) {
                                                Float test = dp.getValue(field).asFloat();
                                                Double weightdouble = (double) test;
                                                if(weightunitm.equals("Kilograms")) {
                                                    weightdouble = Math.round(weightdouble * 10.0)/10.0;
                                                    weightunit.setText("Kilograms");
                                                }
                                                else if(weightunitm.equals("Pounds")) {
                                                    weightdouble = Math.round((weightdouble*2.205)*10)/10.0;
                                                    weightunit.setText("Pounds");
                                                }
                                                else if (weightunitm.equals("Stones")){
                                                    weightdouble = Math.round((weightdouble/6.35)*10)/10.0;
                                                    weightunit.setText("Stones");
                                                }
                                                weight.setText("" + weightdouble);
                                                Log.i(TAG, "weightc=" + dp.getValue(field));
                                            }
                                        }
                                    }
                                }
                            }
                        });

    }
    private void dailySteps() {

        Calendar cal = Calendar.getInstance();

        ZonedDateTime nowZoned = ZonedDateTime.now();
        Instant midnight = nowZoned.toLocalDate().atStartOfDay(nowZoned.getZone()).toInstant();
        Date start = Date.from(midnight);
        cal.setTime(start);
        long startTime = cal.getTimeInMillis();
        Date end = Date.from(Instant.now());
        cal.setTime(end);
        long endTime = cal.getTimeInMillis();

        final DataReadRequest req = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(req)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                if (dataReadResponse.getBuckets().size() > 0) {
                                    Log.i(
                                            TAG, "Number of returned buckets of DataSets for steps is: " + dataReadResponse.getBuckets().size());
                                    for (Bucket bucket : dataReadResponse.getBuckets()) {
                                        List<DataSet> dataSets = bucket.getDataSets();
                                        for (DataSet dataSet : dataSets) {
                                            for (DataPoint dp : dataSet.getDataPoints()) {
                                                for (Field field : dp.getDataType().getFields()) {

                                                    stepsc = dp.getValue(field).asInt();
                                                    Log.i(TAG, "stepsc=" + dp.getValue(field));
                                                    steps.setText(""+stepsc);
                                                }
                                            }
                                        }
                                    }
                                } else if (dataReadResponse.getDataSets().size() > 0) {
                                    Log.i(
                                            TAG, "Number of returned DataSets is: " + dataReadResponse.getDataSets().size());
                                    for (DataSet dataSet : dataReadResponse.getDataSets()) {
                                        for (DataPoint dp : dataSet.getDataPoints()) {
                                            for (Field field : dp.getDataType().getFields()) {
                                                stepsc = dp.getValue(field).asInt();
                                                Log.i(TAG, "stepsc=" + dp.getValue(field));
                                                steps.setText(""+stepsc);
                                            }
                                        }
                                    }
                                }
                            }
                        });
    }
    private void dailyCal() {
        Calendar cal = Calendar.getInstance();

        ZonedDateTime nowZoned = ZonedDateTime.now();
        Instant midnight = nowZoned.toLocalDate().atStartOfDay(nowZoned.getZone()).toInstant();
        Date start = Date.from(midnight);
        cal.setTime(start);
        long startTime = cal.getTimeInMillis();
        Date end = Date.from(Instant.now());
        cal.setTime(end);
        long endTime = cal.getTimeInMillis();


        final DataReadRequest req = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(req)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                if (dataReadResponse.getBuckets().size() > 0) {
                                    for (Bucket bucket : dataReadResponse.getBuckets()) {
                                        List<DataSet> dataSets = bucket.getDataSets();
                                        for (DataSet dataSet : dataSets) {
                                            for (DataPoint dp : dataSet.getDataPoints()) {
                                                for (Field field : dp.getDataType().getFields()) {
                                                    float test = dp.getValue(field).asFloat();
                                                    Double calsdouble = (double) test;
                                                    int calsint = 0;
                                                    if(energyunitm.equals("Calories")) {
                                                        calsint = calsdouble.intValue();
                                                        energyunit.setText("kCal");
                                                    }
                                                    else if (energyunitm.equals("Kilojoules")){
                                                        calsdouble *= 4.184;
                                                        calsint = calsdouble.intValue();
                                                        energyunit.setText("kJ");
                                                    }
                                                    cals.setText("" + calsint);
                                                    Log.i(TAG, "calsc=" + dp.getValue(field));
                                                }
                                            }
                                        }
                                    }
                                } else if (dataReadResponse.getDataSets().size() > 0) {
                                    for (DataSet dataSet : dataReadResponse.getDataSets()) {
                                        for (DataPoint dp : dataSet.getDataPoints()) {
                                            for (Field field : dp.getDataType().getFields()) {
                                                float test = dp.getValue(field).asFloat();
                                                Double calsdouble = (double)test;
                                                int calsint = calsdouble.intValue();
                                                cals.setText("" + calsint);
                                                Log.i(TAG, "calsc=" + dp.getValue(field));
                                            }
                                        }
                                    }
                                }
                            }
                        });
    }
    private void totalDistance() {
        Calendar cal = Calendar.getInstance();

        ZonedDateTime nowZoned = ZonedDateTime.now();
        Instant midnight = nowZoned.toLocalDate().atStartOfDay(nowZoned.getZone()).toInstant();
        Date start = Date.from(midnight);
        cal.setTime(start);
        long startTime = cal.getTimeInMillis();
        Date end = Date.from(Instant.now());
        cal.setTime(end);
        long endTime = cal.getTimeInMillis();

        final DataReadRequest req = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(req)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                if (dataReadResponse.getBuckets().size() > 0) {
                                    Log.i(
                                            TAG, "Number of returned buckets of DataSets is: " + dataReadResponse.getBuckets().size());
                                    for (Bucket bucket : dataReadResponse.getBuckets()) {
                                        List<DataSet> dataSets = bucket.getDataSets();
                                        for (DataSet dataSet : dataSets) {
                                            for (DataPoint dp : dataSet.getDataPoints()) {
                                                for (Field field : dp.getDataType().getFields()) {
                                                    float test = dp.getValue(field).asFloat();
                                                    Double test1 = (double) test;
                                                    if(distanceunitm.equals("Miles")) {
                                                        test1 = (double) test / 1609;
                                                        test1 = Math.round(test1 * 100.0) / 100.0;
                                                        distanceunit.setText("mi");
                                                    }
                                                    else if (distanceunitm.equals("Kilometers")){
                                                        test1 = (double)test1/1000;
                                                        test1 = Math.round(test1 * 100) / 100.0;
                                                        distanceunit.setText("km");
                                                    }
                                                    distance_count.setText("" + test1);
                                                    Log.i(TAG, "mile_count=" + dp.getValue(field));
                                                }
                                            }
                                        }
                                    }
                                } else if (dataReadResponse.getDataSets().size() > 0) {
                                    Log.i(
                                            TAG, "Number of returned DataSets is: " + dataReadResponse.getDataSets().size());
                                    for (DataSet dataSet : dataReadResponse.getDataSets()) {
                                        for (DataPoint dp : dataSet.getDataPoints()) {
                                            for (Field field : dp.getDataType().getFields()) {
                                                float test = dp.getValue(field).asFloat();
                                                Double test1 = (double) test;
                                                if(distanceunitm.equals("Miles")) {
                                                    test1 = (double) test / 1609;
                                                    test1 = Math.round(test1 * 100.0) / 100.0;
                                                    distanceunit.setText("Mi");
                                                }
                                                else if (distanceunitm.equals("Kilometers")){
                                                    test1 = (double)test1/1000;
                                                    test1 = Math.round(test1 * 100) / 100.0;
                                                    distanceunit.setText("Km");
                                                }
                                                distance_count.setText("" + test1);
                                                Log.i(TAG, "mile_count=" + dp.getValue(field));
                                            }
                                        }
                                    }
                                }
                            }
                        });
    }
    private void totalMoveMinutes() {
        Calendar cal = Calendar.getInstance();

        ZonedDateTime nowZoned = ZonedDateTime.now();
        Instant midnight = nowZoned.toLocalDate().atStartOfDay(nowZoned.getZone()).toInstant();
        Date start = Date.from(midnight);
        cal.setTime(start);
        long startTime = cal.getTimeInMillis();
        Date end = Date.from(Instant.now());
        cal.setTime(end);
        long endTime = cal.getTimeInMillis();


        final DataReadRequest req = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_MOVE_MINUTES, DataType.AGGREGATE_MOVE_MINUTES)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(req)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                if (dataReadResponse.getBuckets().size() > 0) {
                                    Log.i(
                                            TAG, "Number of returned buckets of DataSets is: " + dataReadResponse.getBuckets().size());
                                    for (Bucket bucket : dataReadResponse.getBuckets()) {
                                        List<DataSet> dataSets = bucket.getDataSets();
                                        for (DataSet dataSet : dataSets) {
                                            for (DataPoint dp : dataSet.getDataPoints()) {
                                                for (Field field : dp.getDataType().getFields()) {
                                                    movemins.setText(""+dp.getValue(field));
                                                    Log.i(TAG, "move_mins=" + dp.getValue(field));
                                                    Log.i(TAG, "move_field " + field.toString());
                                                }
                                            }
                                        }
                                    }
                                } else if (dataReadResponse.getDataSets().size() > 0) {
                                    Log.i(
                                            TAG, "Number of returned DataSets is: " + dataReadResponse.getDataSets().size());
                                    for (DataSet dataSet : dataReadResponse.getDataSets()) {
                                        for (DataPoint dp : dataSet.getDataPoints()) {
                                            for (Field field : dp.getDataType().getFields()) {
                                                movemins.setText(""+dp.getValue(field));
                                                Log.i(TAG, "movemins=" + dp.getValue(field));
                                            }
                                        }
                                    }
                                }
                            }
                        });
    }
    private void getGoogleHeartRateBPM(){
        Calendar cal = Calendar.getInstance();
        Date date = new Date();
        cal.setTime(date);
        long endTime = cal.getTimeInMillis();

        final DataReadRequest req = new DataReadRequest.Builder()
                .read(DataType.TYPE_HEART_RATE_BPM)
                .setTimeRange(1, endTime, TimeUnit.MILLISECONDS)
                .setLimit(1)
                .build();

        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(req)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                for (DataSet dataSet : dataReadResponse.getDataSets()) {
                                    for (DataPoint dp : dataSet.getDataPoints()) {
                                        for (Field field : dp.getDataType().getFields()) {
                                            heartRateText.setText(""+(int)dp.getValue(field).asFloat());
                                        }
                                    }
                                }
                            }
                        });
    }
    public void HeartRateChangeListener(){
        if (!DeviceService.mainActivityisPaused) {
            listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals("HeartRateValue")) {
                        Log.i("Testing", "Testing");
                    }
                }
            };
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.heart_rate_card:
                startDetailsFragment(1);
                break;
            case R.id.calories_count_card:
                startDetailsFragment(2);
                break;
            case R.id.distance_count_card:
                startDetailsFragment(3);
                break;
            case R.id.height_card:
                startDetailsFragment(4);
                break;
            case R.id.step_count_card:
                startDetailsFragment(5);
                break;
            case R.id.weight_card:
                startDetailsFragment(6);
                break;
            case R.id.move_mins_card:
                startDetailsFragment(7);
                break;
        }
    }



    public void startDetailsFragment(int cardclicked){
        Intent dataSet = new Intent(getActivity(), DataSetDetailsActivity.class);
        dataSet.putExtra("cardClicked", cardclicked);
        startActivity(dataSet);
    }

}
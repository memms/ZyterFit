package com.example.zyterfitlayout;

import android.app.Notification;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataUpdateRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnSuccessListener;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import MiBandbluetooth.Device.DeviceService;

public class HeartFragment extends Fragment {

    private TextView HeartRateNumber, minimumRate, maximumRate, averageRate;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    SharedPreferences mPreferences;
    int heartRate;
    public ScheduledExecutorService heartservice;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_heart, container, false);

        HeartRateNumber = v.findViewById(R.id.heart_rate_num);
        minimumRate = v.findViewById(R.id.minimum_rate);
        maximumRate = v.findViewById(R.id.maximum_rate);
        averageRate = v.findViewById(R.id.average_rate);
        DeviceService.mainActivityisPaused = false;
        getGoogleHeartRateBPM();
        HeartRateChangeListener();
        getDailyHeartRateDetails();

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPreferences.registerOnSharedPreferenceChangeListener(listener);
        return v;
    }
    public void HeartRateChangeListener(){
        if (!DeviceService.mainActivityisPaused) {
            listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (key.equals("HeartRateValue")) {
                        heartRate = mPreferences.getInt("HeartRateValue", 0);
                        setGoogleHeartRate();
                    }
                }
            };
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DeviceService.mainActivityisPaused = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        DeviceService.mainActivityisPaused = true;
    }

    private void setGoogleHeartRate(){
        if(isAdded()) {
            Calendar cal = Calendar.getInstance();
            Date date = new Date();
            cal.setTime(date);
            long endTime = cal.getTimeInMillis();
            DataSource dataSource = new DataSource.Builder()
                    .setAppPackageName(requireActivity())
                    .setDataType(DataType.TYPE_HEART_RATE_BPM)
                    .setType(DataSource.TYPE_RAW)
                    .build();
            float BPM = (float) heartRate;
            DataPoint dataPoint =
                    DataPoint.builder(dataSource)
                            .setTimeInterval(endTime, endTime, TimeUnit.MILLISECONDS)
                            .setField(Field.FIELD_BPM, BPM)
                            .build();
            DataSet dataSet = DataSet.builder(dataSource).add(dataPoint).build();
            Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity())).insertData(dataSet);

            getGoogleHeartRateBPM();
        }
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
                                            Log.i("HeartRateGoogle",""+dp.getValue(field).asFloat());

                                            HeartRateNumber.setText(""+((int)dp.getValue(field).asFloat()));
                                        }
                                    }
                                }
                            }
                        });
    }

    private void getDailyHeartRateDetails(){
        Calendar cal = Calendar.getInstance();
        ZonedDateTime nowZoned = ZonedDateTime.now();
        Instant midnight = nowZoned.toLocalDate().atStartOfDay(nowZoned.getZone()).toInstant();
        Date start = Date.from(midnight);
        cal.setTime(start);
        long startTime = cal.getTimeInMillis();
        Date date = new Date();
        cal.setTime(date);
        long endTime = cal.getTimeInMillis();

        DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                .bucketByTime(1, TimeUnit.DAYS)
                .aggregate(DataType.TYPE_HEART_RATE_BPM, DataType.AGGREGATE_HEART_RATE_SUMMARY)
                .setTimeRange(startTime,endTime,TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();

        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(dataReadRequest)
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

                                                    if (field.getName().contains("average")) {
                                                        Log.i("average: ", "" + dp.getValue(field).asFloat());
                                                        averageRate.setText(""+((int)dp.getValue(field).asFloat()));
                                                    }
                                                    if (field.getName().contains("max")) {
                                                        Log.i("max: ", "" + dp.getValue(field).asFloat());
                                                        maximumRate.setText(""+((int)dp.getValue(field).asFloat()));
                                                    }
                                                    if (field.getName().contains("min")) {
                                                        Log.i("min: ", "" + dp.getValue(field).asFloat());
                                                        minimumRate.setText(""+((int)dp.getValue(field).asFloat()));

                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });

    }


}

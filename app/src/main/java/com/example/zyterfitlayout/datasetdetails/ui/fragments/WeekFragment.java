package com.example.zyterfitlayout.datasetdetails.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.zyterfitlayout.R;
import com.example.zyterfitlayout.datasetdetails.DataSetDetails;
import com.example.zyterfitlayout.datasetdetails.DataSetDetailsActivity;
import com.example.zyterfitlayout.datasetdetails.dataSetArrayAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class WeekFragment extends Fragment {

    DataSetDetailsActivity dataSetDetailsActivity;
    private LineChart lineChart;
    ArrayList<Entry> values = new ArrayList<>();
    ArrayList<String> labels;
    ListView listView;
    ArrayList<DataSetDetails> dataSetDetailsList = new ArrayList<>();
    ArrayAdapter<DataSetDetails> adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_day, container, false);
        dataSetDetailsActivity = new DataSetDetailsActivity();
        lineChart = v.findViewById(R.id.daychart);
        listView = v.findViewById(R.id.DataSetListView);
        getsetData();


        return v;
    }

    private class DayFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return getDay((int) value);
        }

        String getDay(int day) {
            switch (day) {
                case 1:
                    return "Mon";
                case 2:
                    return "Tue";
                case 3:
                    return "Wed";
                case 4:
                    return "Thu";
                case 5:
                    return "Fri";
                case 6:
                    return "Sat";
                case 7:
                    return "Sun";
                default:
                    return "";
            }
        }}

    private void chartGeneration(){
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);


        XAxis xAxis = lineChart.getXAxis();
        xAxis.setLabelCount(values.size());
        xAxis.setValueFormatter(new DayFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
//        xAxis.setLabelCount(7, true);
        if(lineChart.getLineData()!=null) {
            xAxis.setAxisMinimum(lineChart.getLineData().getXMin() - 0.5f);
            xAxis.setAxisMaximum(lineChart.getLineData().getXMax() + 0.5f);
        }

        xAxis.setGranularity(1);
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setValueFormatter(new YValueFormatter());
        leftAxis.setLabelCount(5, false);
        leftAxis.setAxisMinimum(0f);



//        leftAxis.setDrawGridLines(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);



        LineDataSet set;
        if (lineChart.getData() != null &&
                lineChart.getData().getDataSetCount() > 0) {
            set = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            set.setValues(values);
            set.notifyDataSetChanged();
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
            Log.i("LINEDATASET Week", ""+set);
        } else {
            set = new LineDataSet(values, "Steps");
            Log.i("LINEDATASET Week", ""+set);
        }
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);
        LineData data = new LineData(dataSets);

        lineChart.setData(data);
        lineChart.animateX(750);

    }
    private class YValueFormatter implements IAxisValueFormatter {
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            String result = value + "";
            if (value ==0) {
                result ="";
            }
            return result;
        }
    }


    private void getsetData(){

        if(values.size()!=0){
            adapter.clear();
        }
        if (values.size() == 0) {
            for (int i = 1; i <= 7; i++) {
                values.add(new Entry(i, 0));
            }
        }

        long endTime = dataSetDetailsActivity.startofweek(Calendar.getInstance())+604800000;

        DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                .aggregate(DataSetDetailsActivity.dataType1(), DataSetDetailsActivity.dataType2())
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(dataSetDetailsActivity.startofweek(Calendar.getInstance()), endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(dataReadRequest)
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
                                                    Float test;
                                                    try {
                                                        test = (float) dp.getValue(field).asFloat();
                                                    } catch (Exception e){
                                                        int itest = dp.getValue(field).asInt();
                                                        test = (float)itest;
                                                    }
                                                    Calendar cal = Calendar.getInstance();
                                                    cal.setTimeInMillis(dp.getEndTime(TimeUnit.MILLISECONDS));
                                                    String dayLongName = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
                                                    Log.i("ENDTIME", ""+ dayLongName);
                                                    Log.i("Value", "" + test);
                                                    int temp =0;
                                                    if (dayLongName.contains("Mon")){
                                                        temp=1;
                                                    } else if(dayLongName.contains("Tues")){
                                                        temp=2;
                                                    } else if(dayLongName.contains("Wed")){
                                                        temp=3;
                                                    } else if(dayLongName.contains("Thurs")){
                                                        temp=4;
                                                    } else if(dayLongName.contains("Fri")){
                                                        temp=5;
                                                    } else if(dayLongName.contains("Sat")){
                                                        temp=6;
                                                    } else if(dayLongName.contains("Sun")){
                                                        temp=7;
                                                    }
                                                    addDataSetList(dp.getEndTime(TimeUnit.MILLISECONDS),test);
                                                    values.set(temp-1, new Entry(temp,test));
                                                    Log.i("test", "");
                                                    if (values.isEmpty()==false){
                                                        chartGeneration();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });

    }


    private void addDataSetList(long timeinMili, float value){
        SimpleDateFormat dateformat = new SimpleDateFormat("EEEE, MMMM d");

        String formattedDate = dateformat.format(timeinMili);


        if(isAdded()) {
            adapter = new dataSetArrayAdapter(requireActivity(), 0, dataSetDetailsList);
            listView.setAdapter(adapter);
            dataSetDetailsList.add(
                    new DataSetDetails(formattedDate, ""+((int)value))
            );
        }
    }



}
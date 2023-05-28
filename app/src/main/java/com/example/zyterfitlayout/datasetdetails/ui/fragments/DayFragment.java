package com.example.zyterfitlayout.datasetdetails.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.zyterfitlayout.Activity;
import com.example.zyterfitlayout.datasetdetails.DataSetDetails;
import com.example.zyterfitlayout.R;
import com.example.zyterfitlayout.activityArrayAdapter;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DayFragment extends Fragment {

    DataSetDetailsActivity dataSetDetailsActivity;
    private LineChart lineChart;
    ArrayList<Entry>values = new ArrayList<>();
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
            return getHour((int) value);
        }

        String getHour(int hour) {
            if(hour==0){
                return "12\nAM";
            }else if(hour==4 || hour == 8 ){
                return ""+hour+"\nAM";
            }else if(hour==12){
                return "12\nPM";
            }else if(hour==16||hour==20){
                return ""+(hour-12)+"\nPM";
            }else if(hour==24){
                return "12\nAM";
            }else{
                return "";
            }
        }
    }



    private void chartGeneration(){
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setLabelCount(values.size());
        xAxis.setValueFormatter(new DayFormatter());
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        if(lineChart.getLineData()!=null) {
            xAxis.setAxisMinimum(lineChart.getLineData().getXMin() - 0.5f);
            xAxis.setAxisMaximum(lineChart.getLineData().getXMax() + 0.5f);
        }

        xAxis.setGranularity(1);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setValueFormatter(new YValueFormatter());
        leftAxis.setLabelCount(5, false);
        leftAxis.setAxisMinimum(0f);



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
            Log.i("LINEDATASET Day", ""+set);
        } else {
            set = new LineDataSet(values, "Steps");
            Log.i("LINEDATASET Day", ""+set);
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
            for (int i = 0; i <= 24; i++) {
                values.add(new Entry(i, 0));
            }
        }
        //todo change to 86400000
        long endTime = dataSetDetailsActivity.startofday()+86400000;

        final DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                .aggregate(DataSetDetailsActivity.dataType1(), DataSetDetailsActivity.dataType2())
                .bucketByTime(1, TimeUnit.HOURS)
                //todo for testing purposes a special date is chosen, revert back
                .setTimeRange(dataSetDetailsActivity.startofday(), endTime, TimeUnit.MILLISECONDS)
//               .setTimeRange(1598587200000L, 1598673600000L, TimeUnit.MILLISECONDS)
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
                                                    SimpleDateFormat dateformat = new SimpleDateFormat("H");
                                                    Integer hour = Integer.parseInt(dateformat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
                                                    Log.i("ENDTIME", ""+ hour);
                                                    Log.i("Value", "" + test);
                                                    Log.i("Dataset time period", ""+dataReadRequest.getStartTime(TimeUnit.MILLISECONDS)+" - " + dataReadRequest.getEndTime(TimeUnit.MILLISECONDS));
                                                    addDataSetList(dp.getEndTime(TimeUnit.MILLISECONDS), test);
                                                    values.set(hour, new Entry(hour,test));
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
        SimpleDateFormat dateformat = new SimpleDateFormat("h:mm a");

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
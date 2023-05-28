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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MonthFragment extends Fragment {

    DataSetDetailsActivity dataSetDetailsActivity;
    private LineChart lineChart;
    ArrayList<Entry> values = new ArrayList<>();
    ListView listView;
    ArrayList<DataSetDetails> dataSetDetailsList = new ArrayList<>();
    ArrayAdapter<DataSetDetails> adapter;
    int daysinMonth;


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
            if(day==1){
                return ""+day;
            }else if(day == 7 ){
                return ""+day;
            }else if(day==14){
                return ""+day;
            }else if(day==21){
                return ""+day;
            }else if(day==28){
                return ""+day;
            }else if(day==daysinMonth) {
                return "" + day;
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
            xAxis.setAxisMinimum(lineChart.getLineData().getXMin() - 1f);
            xAxis.setAxisMaximum(lineChart.getLineData().getXMax() + 1f);
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
            Log.i("LINEDATASET Month", ""+set);
        } else {
            set = new LineDataSet(values, "Steps");
            Log.i("LINEDATASET Month", ""+set);
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



    private void getsetData() {

        if(values.size()!=0){
            adapter.clear();
        }


        Log.i("MONTH", "MONTH FRAGMENT");

        YearMonth yearMonth = YearMonth.of(dataSetDetailsActivity.getYear(), dataSetDetailsActivity.getMonth());
        daysinMonth = yearMonth.lengthOfMonth();
        Log.i("YEARMONTH", "" + dataSetDetailsActivity.getYear() + " " + dataSetDetailsActivity.getMonth());
        Log.i("DAYSINMONTH", "" + daysinMonth);
        if (values.size() == 0) {
            for (int i = 1; i <= daysinMonth; i++) {
                values.add(new Entry(i, 0));
            }
        }
        //todo change to 86400000
        long endTime = dataSetDetailsActivity.startofmonth()+2592000000L;

        final DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                .aggregate(DataSetDetailsActivity.dataType1(), DataSetDetailsActivity.dataType2())
                .bucketByTime(1, TimeUnit.DAYS)
                //todo for testing purposes a special date is chosen, revert back
                .setTimeRange(dataSetDetailsActivity.startofmonth(), endTime, TimeUnit.MILLISECONDS)
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
                                                    SimpleDateFormat dateformat = new SimpleDateFormat("d");
                                                    Integer day = Integer.parseInt(dateformat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));

                                                    Log.i("ENDTIME Month", ""+ day);
                                                    Log.i("Value Month", "" + test);
                                                    Log.i("Da ta se t time period Month", ""+dataReadRequest.getStartTime(TimeUnit.MILLISECONDS)+" - " + dataReadRequest.getEndTime(TimeUnit.MILLISECONDS));
                                                    addDataSetList(dp.getEndTime(TimeUnit.MILLISECONDS),test);
                                                    values.set(day-1, new Entry(day,test));
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
    @Override
    public void onPause() {
        super.onPause();
    }

    private void addDataSetList(long timeinMili, float value){
        SimpleDateFormat dateformat = new SimpleDateFormat("EEE, MMM d");

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
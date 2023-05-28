package com.example.zyterfitlayout.datasetdetails.ui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

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
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class YearFragment extends Fragment {

    DataSetDetailsActivity dataSetDetailsActivity;
    private LineChart lineChart;
    ArrayList<Entry> values = new ArrayList<>();
    ListView listView;
    ArrayList<DataSetDetails> dataSetDetailsList = new ArrayList<>();
    ArrayAdapter<DataSetDetails> adapter;
    float tempdata = 0;
    Integer tempmonth;
    String weightunitm;
    SharedPreferences prefs, mPreferences;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_day, container, false);
        dataSetDetailsActivity = new DataSetDetailsActivity();
        lineChart = v.findViewById(R.id.daychart);
        listView = v.findViewById(R.id.DataSetListView);
        getsetData();

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        return v;
    }

    private class DayFormatter implements IAxisValueFormatter {

        @Override
        public String getFormattedValue(float value, AxisBase axis) { return getMonth( value); }

        String getMonth(float month) {
            if(month==1){
                return "Jan";
            } else if(month==2){
                return "Feb";
            }else if(month==3){
                return "Mar";
            }else if(month==4){
                return "Apr";
            }else if(month==5){
                return "May";
            }else if(month==6){
                return "Jun";
            }else if(month==7){
                return "Jul";
            }else if(month==8){
                return "Aug";
            }else if(month==9){
                return "Sep";
            }else if(month==10){
                return "Oct";
            }else if(month==11){
                return "Nov";
            }else if(month==12){
                return "Dec";
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
            Log.i("LINEDATASET Year", ""+set);
        } else {
            set = new LineDataSet(values, "Steps");
            Log.i("LINEDATASET Year", ""+set);
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
        final SimpleDateFormat dateformat = new SimpleDateFormat("M");

        final int[] t = {0};
        final long[] tempfinaltime = {0};

        if (values.size() == 0) {
            for (int i = 1; i <= 12; i++) {
                values.add(new Entry(i, 0));
            }
        }
        long endTime = dataSetDetailsActivity.startofyear()+31556952000L;
        final DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                .aggregate(DataSetDetailsActivity.dataType1(), DataSetDetailsActivity.dataType2())
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(dataSetDetailsActivity.startofyear(), endTime, TimeUnit.MILLISECONDS)
                .build();

        final Task<DataReadResponse> response = Fitness
                .getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(dataReadRequest);

        response.addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
            @Override
            public void onSuccess(DataReadResponse dataReadResponse) {
                final Long templong = response.getResult().getBuckets().get(0).getEndTime(TimeUnit.MILLISECONDS);
                tempmonth = Integer.parseInt(dateformat.format(templong));


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
                                                            } catch (Exception e) {
                                                                int itest = dp.getValue(field).asInt();
                                                                test = (float) itest;
                                                            }

                                                            Integer month = Integer.parseInt(dateformat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
                                                            Log.i("MONTH YEAR", "TempMonth: " + tempmonth + " Month: " + month + field);

                                                            if (field.equals(Field.FIELD_AVERAGE) && DataSetDetailsActivity.dataType1() == DataType.TYPE_WEIGHT) {
                                                                weightunitm = prefs.getString("weight_unit", "Kilograms");
                                                                double weightdouble = (double) test;
                                                                Log.i("YEAR MONTH WEIGHT", " weight double " + weightdouble);
                                                                if (weightunitm.equals("Kilograms")) {
                                                                    weightdouble = Math.round(weightdouble * 10.0) / 10.0;
                                                                } else if (weightunitm.equals("Pounds")) {
                                                                    weightdouble = Math.round((weightdouble * 2.205) * 10) / 10.0;
                                                                } else if (weightunitm.equals("Stones")) {
                                                                    weightdouble = Math.round((weightdouble / 6.35) * 10) / 10.0;
                                                                }
                                                                tempdata = (float) weightdouble;
                                                                Log.i("YEAR MONTH WEIGHT", "tempdate weightdouble" + tempdata);
                                                                addDataSetList(dp.getEndTime(TimeUnit.MILLISECONDS), tempdata);
                                                                values.set(month - 1, new Entry(month, tempdata));
                                                                Log.i("MONTH YEAR TEMP weight", "TEST: " + test);
                                                                tempfinaltime[0] = dp.getEndTime(TimeUnit.MILLISECONDS);
                                                            }
                                                            else if (field.equals(Field.FIELD_MIN)){}
                                                            else if (field.equals(Field.FIELD_MAX)){}
                                                            else if(field.equals(Field.FIELD_AVERAGE) && DataSetDetailsActivity.dataType1() == DataType.TYPE_HEART_RATE_BPM){
                                                                tempdata = (float) test;
                                                                Log.i("YEAR MONTH WEIGHT", "tempdate weightdouble" + tempdata);
                                                                addDataSetList(dp.getEndTime(TimeUnit.MILLISECONDS), tempdata);
                                                                values.set(month - 1, new Entry(month, tempdata));
                                                                Log.i("MONTH YEAR TEMP weight", "TEST: " + test);
                                                                tempfinaltime[0] = dp.getEndTime(TimeUnit.MILLISECONDS);
                                                            }
                                                            else {
                                                                if (tempmonth == month) {
                                                                    tempdata += test;
                                                                    Log.i("MONTH YEAR", "Test: " + test + " tempdata: " + tempdata);
                                                                    t[0]++;
                                                                } else {
                                                                    if (t[0] != 0) {
                                                                        addDataSetList(dp.getEndTime(TimeUnit.MILLISECONDS), tempdata);
                                                                        values.set(tempmonth - 1, new Entry(tempmonth, tempdata));
                                                                    }
                                                                    tempmonth = month;
                                                                    tempdata = test;

                                                                    Log.i("MONTH YEAR TEMP", "TEST: " + test);
                                                                }
                                                                if (t[0] != 0) {
                                                                    values.set(tempmonth - 1, new Entry(tempmonth, tempdata));
                                                                    tempfinaltime[0] = dp.getEndTime(TimeUnit.MILLISECONDS);
                                                                }
                                                            }
                                                            Log.i("ENDTIME", "" + month);
                                                            Log.i("Value", "" + test);
                                                            Log.i("tempfinaltime", "" + tempfinaltime[0]);
                                                            Log.i("Dataset time period", "" + dataReadRequest.getStartTime(TimeUnit.MILLISECONDS) + " - " + dataReadRequest.getEndTime(TimeUnit.MILLISECONDS));
                                                            if (values.isEmpty() == false) {
                                                                chartGeneration();
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            if(t[0]!=0)
                                                addDataSetList(tempfinaltime[0], tempdata);
                                        }
                                    }

                                });
            }
        });

    }


    private void addDataSetList(long timeinMili, float value){
        SimpleDateFormat dateformat;
        if(DataSetDetailsActivity.dataType1() == DataType.TYPE_HEART_RATE_BPM || DataSetDetailsActivity.dataType1() == DataType.TYPE_WEIGHT){
            dateformat = new SimpleDateFormat("MMMM d, yyyy");
        } else {
            dateformat = new SimpleDateFormat("MMMM");
        }
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
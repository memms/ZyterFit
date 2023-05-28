package com.example.zyterfitlayout.datasetdetails;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.MonthDisplayHelper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.zyterfitlayout.R;
import com.example.zyterfitlayout.datasetdetails.ui.fragments.DayFragment;
import com.example.zyterfitlayout.datasetdetails.ui.fragments.MonthFragment;
import com.example.zyterfitlayout.datasetdetails.ui.fragments.WeekFragment;
import com.example.zyterfitlayout.datasetdetails.ui.fragments.YearFragment;
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
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.Year;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DataSetDetailsActivity extends AppCompatActivity {

    private static Integer cardClicked;
    ViewPager viewPager;
    TabLayout tabLayout;
    TextView title;
    DayFragment dayFragment;
    MonthFragment monthFragment;
    WeekFragment weekFragment;
    YearFragment yearFragment;
    Intent intent;
    Button close;

//    public static ArrayList<Integer>xValue;
//    public static ArrayList<Integer>yValue;

    Calendar cal = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_data_set_details);

        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        dayFragment = new DayFragment();
        weekFragment = new WeekFragment();
        monthFragment = new MonthFragment();
        yearFragment = new YearFragment();

        title = findViewById(R.id.dataSettitle);
        close = findViewById(R.id.dataSetClose);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), 1);
        viewPagerAdapter.addFragment(dayFragment, "Day");
        viewPagerAdapter.addFragment(weekFragment, "Week");
        viewPagerAdapter.addFragment(monthFragment, "Month");
        viewPagerAdapter.addFragment(yearFragment, "Year");
        viewPager.setAdapter(viewPagerAdapter);

        intent = getIntent();
        cardClicked = intent.getIntExtra("cardClicked", 0);

        title.setText(DataSetDetailsActivity.name());

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public static String name(){
        String name = "";
        switch(cardClicked){
            case 1:
                name= "Heart Rate";
                break;
            case 2:
                name= "Calories";
                break;
            case 3:
                name= "Distance";
                break;
            case 4:
                name= "Height";
                break;
            case 5:
                name= "Step Count";
                break;
            case 6:
                name= "Weight";
                break;
            case 7:
                name= "Move Minutes";
                break;
        }
        return name;
    }

    public static DataType dataType1(){
        DataType dataType = null;
        switch(cardClicked){
            case 1:
                dataType= DataType.TYPE_HEART_RATE_BPM;
                break;
            case 2:
                dataType= DataType.TYPE_CALORIES_EXPENDED;
                break;
            case 3:
                dataType= DataType.TYPE_DISTANCE_DELTA;
                break;
            case 4:
                dataType= DataType.TYPE_HEIGHT;
                break;
            case 5:
                dataType= DataType.TYPE_STEP_COUNT_DELTA;
                break;
            case 6:
                dataType= DataType.TYPE_WEIGHT;
                break;
            case 7:
                dataType= DataType.TYPE_MOVE_MINUTES;
                break;
        }
        return dataType;
    }

    public static DataType dataType2(){
        DataType dataType = null;
        switch(cardClicked){
            case 1:
                dataType= DataType.AGGREGATE_HEART_RATE_SUMMARY;
                break;
            case 2:
                dataType= DataType.AGGREGATE_CALORIES_EXPENDED;
                break;
            case 3:
                dataType= DataType.AGGREGATE_DISTANCE_DELTA;
                break;
            case 4:
                dataType= DataType.AGGREGATE_HEIGHT_SUMMARY;
                break;
            case 5:
                dataType= DataType.AGGREGATE_STEP_COUNT_DELTA;
                break;
            case 6:
                dataType= DataType.AGGREGATE_WEIGHT_SUMMARY;
                break;
            case 7:
                dataType= DataType.AGGREGATE_MOVE_MINUTES;
                break;
        }
        return dataType;
    }



    public long startofweek(Calendar calender){
        cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        long miliseconds = cal.getTimeInMillis();

        return miliseconds;
    }


    public long startofmonth(){
        cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        long miliseconds = cal.getTimeInMillis();

        return miliseconds;
    }
    public long startofyear(){
        cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(Calendar.DAY_OF_YEAR, cal.getActualMinimum(Calendar.DAY_OF_YEAR));
        long miliseconds = cal.getTimeInMillis();

        return miliseconds;
    }

    public int getYear(){
//        cal = Calendar.getInstance();

        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy");
        Integer year = Integer.parseInt(dateformat.format(cal.getTimeInMillis()));
        return year;
    }

    public int getMonth(){
//        cal = Calendar.getInstance();

        SimpleDateFormat dateformat = new SimpleDateFormat("M");
        Integer month = Integer.parseInt(dateformat.format(cal.getTimeInMillis()));
        return month;
    }

    public long startofday(){
        Calendar cal = Calendar.getInstance();
        ZonedDateTime nowZoned = ZonedDateTime.now();
        Instant midnight = nowZoned.toLocalDate().atStartOfDay(nowZoned.getZone()).toInstant();
        Date start = Date.from(midnight);
        cal.setTime(start);
        long startTime = cal.getTimeInMillis();

        return startTime;
    }
//TODO Optimize this
//    public void getData(long startTime, long endTime){
//
//        DataReadRequest dataReadRequest = new DataReadRequest.Builder()
//                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
//                .bucketByTime(1, TimeUnit.DAYS)
//                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
//                .build();
//
//        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
//                .readData(dataReadRequest)
//                .addOnSuccessListener(
//                        new OnSuccessListener<DataReadResponse>() {
//                            @Override
//                            public void onSuccess(DataReadResponse dataReadResponse) {
//                                if (dataReadResponse.getBuckets().size() > 0) {
//                                    for (Bucket bucket : dataReadResponse.getBuckets()) {
//                                        List<DataSet> dataSets = bucket.getDataSets();
//                                        for (DataSet dataSet : dataSets) {
//                                            for (DataPoint dp : dataSet.getDataPoints()) {
//                                                for (Field field : dp.getDataType().getFields()) {
//                                                    float test = dp.getValue(field).asFloat();
//
//                                                }
//                                            }
//                                        }
//                                    }
//                                } else if (dataReadResponse.getDataSets().size() > 0) {
//                                    for (DataSet dataSet : dataReadResponse.getDataSets()) {
//                                        for (DataPoint dp : dataSet.getDataPoints()) {
//                                            for (Field field : dp.getDataType().getFields()) {
//                                                float test = dp.getValue(field).asFloat();
//
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        });
//
//
//    }



    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments = new ArrayList<>();
        private List<String> fragmentTitle = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            fragmentTitle.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitle.get(position);
        }
    }

}
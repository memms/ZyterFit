package com.example.zyterfitlayout;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.service.autofill.Dataset;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.GoalsClient;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Goal;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataUpdateRequest;
import com.google.android.gms.fitness.request.SessionInsertRequest;
import com.google.android.gms.fitness.request.SessionReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.SessionReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.example.zyterfitlayout.DashboardFragment.TAG;
import static com.google.android.gms.fitness.data.Field.FIELD_CALORIES;
import static com.google.android.gms.fitness.data.Field.FIELD_DISTANCE;
import static com.google.android.gms.fitness.data.Field.FIELD_STEPS;
import static java.text.DateFormat.getDateTimeInstance;
import static java.text.DateFormat.getTimeInstance;

public class MetricsFragment extends Fragment {

    private FloatingActionButton maddfab, mactivityfab, mweightfab, mtrackactfab;
    private TextView mactivitytext, mweighttext, mtrackacttext,activity_date, activity_start_time, activity_end_time, weight_date, weight_time;
    private Button activity_type, activity_close, activity_save, weight_close, weight_save;
    private EditText activity_title, activity_notes, activity_calories, activity_steps, activity_miles;
    private ArrayList<Activity> activities = new ArrayList<>();
    private ListView listView;
    private String aicon_start, atime, aname, adetail, aicon_end, setActivityType;
    private View v;
    private Calendar setStartTime, setEndTime;
    private ArrayAdapter<Activity> adapter;
    private int weight_whole, weight_decimal;
    private double weight,weighttemp;

    private String setName, setDetails;
    private float setSteps = 0, setCalories = 0, setMiles = 0;

    private boolean isOpen;
    OvershootInterpolator interpolator = new OvershootInterpolator();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_metrics, container, false);
//      ArrayAdapter<Activity> adapter = new activityArrayAdapter(this.getActivity(), 0, activities);


//        listView = v.findViewById(R.id.customListView);
//        listView.setAdapter(adapter);
//        Order: String icon_start, String time, String name, String details,String icon_end
//        activities.add(
//                new Activity(String icon_start, String time, String name, String details,String icon_end)
//        );
        getActivityList(getActivity());
        getClickListener();
        mactivityfab = v.findViewById(R.id.activity_fab);
        maddfab = v.findViewById(R.id.add_fab);
        mweightfab = v.findViewById(R.id.weight_fab);
        mtrackactfab = v.findViewById(R.id.track_activity_fab);

        mactivitytext = v.findViewById(R.id.activity_text);
        mweighttext = v.findViewById(R.id.weight_text);
        mtrackacttext = v.findViewById(R.id.track_activity_text);

        isOpen = false;
        clickListener();
        onClick();
        setStartTime = Calendar.getInstance();
        setEndTime = Calendar.getInstance();
        return v;
    }


    private void getClickListener() {
        if (isAdded()) {
            adapter = new activityArrayAdapter(requireActivity(), 0, activities);
            listView = v.findViewById(R.id.customListView);
            listView.setAdapter(adapter);
            AdapterView.OnItemClickListener adapterViewListener = new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Activity activity = activities.get(position);

                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
                    intent.putExtra("sessionPosition", position);
                    intent.putExtra("activityName", activity.getName());
                    startActivity(intent);

                }
            };
            listView.setOnItemClickListener(adapterViewListener);
        }
    }

    private void clickListener() {
        maddfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOpen == true) {
                    maddfab.animate().setInterpolator(interpolator).rotationBy(135f).setDuration(500).start();
                    mactivityfab.animate().translationY(100f).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
                    mweightfab.animate().translationY(100f).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
                    mtrackactfab.animate().translationY(100f).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
                    mtrackacttext.animate().translationY(100f).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
                    mactivitytext.animate().translationY(100f).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
                    mweighttext.animate().translationY(100f).alpha(0f).setInterpolator(interpolator).setDuration(300).start();
                    isOpen = false;
                } else {
                    maddfab.animate().setInterpolator(interpolator).rotationBy(225f).setDuration(500).start();
                    mactivityfab.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
                    mweightfab.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
                    mtrackactfab.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
                    mtrackacttext.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
                    mactivitytext.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
                    mweighttext.animate().translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start();
                    isOpen = true;
                }
            }
        });


    }

    public void onClick() {
        mactivityfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityDialog();
            }
        });
        mweightfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWeight();
                //will launch weight dialog after weight has been fetched
            }
        });
        mtrackactfab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ActivityTrackActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getWeight(){
        Calendar cal = Calendar.getInstance();
        Date date = new Date();
        cal.setTime(date);
        long endTime = cal.getTimeInMillis();

        final DataReadRequest req = new DataReadRequest.Builder()
                .read(DataType.TYPE_WEIGHT)
                .setTimeRange(1, endTime, TimeUnit.MILLISECONDS)
                /** to get latest weight only **/
                .setLimit(1)
                .build();

        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(req)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                for (DataSet dataSet : dataReadResponse.getDataSets()) {
//                                        Log.i("Set Weight dataset Size ", ""+dataSet.getDataPoints().size());
                                    for (DataPoint dp : dataSet.getDataPoints()) {
                                        for (Field field : dp.getDataType().getFields()) {
                                            Float test = dp.getValue(field).asFloat();
                                            Double weightdouble = (double) test;
                                            weight = Math.round((weightdouble*2.205)*10)/10.0;
                                            Log.i(TAG, String.valueOf(weight));
                                            weight_whole = (int)weight;
                                            BigDecimal bigDecimal = new BigDecimal(String.valueOf(weight));
                                            int convert = (int)(bigDecimal.doubleValue()*10);
                                            weight_decimal = convert%10;
                                            Log.i(TAG, "Weight whole: " + weight_whole);
                                            Log.i(TAG, "Weight decimal: " + weight_decimal);
                                        }
                                    }
                                }
                                weightDialog();
                            }
                        });

    }

    private void weightDialog() {

        View mview = getLayoutInflater().inflate(R.layout.weight_add, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity(), R.style.DialogFullScreen);
        weight_date = mview.findViewById(R.id.weight_date);
        weight_time = mview.findViewById(R.id.weight_time);
        weight_close = mview.findViewById(R.id.weight_close);
        weight_save = mview.findViewById(R.id.weight_save);

        final NumberPicker weight_int = mview.findViewById(R.id.weight_int_picker);
        weight_int.setMinValue(1);
        weight_int.setMaxValue(1000);
        weight_int.setValue(weight_whole);
        final NumberPicker weight_double = mview.findViewById(R.id.weight_double_picker);
        weight_double.setMinValue(0);
        weight_double.setMaxValue(9);
        weight_double.setValue(weight_decimal);

        builder.setView(mview);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogSlideUp;

        setWeightDate();

        weight_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        weight_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                weight_whole = weight_int.getValue();
                weight_decimal = weight_double.getValue();
                weighttemp = Double.parseDouble(weight_whole+"."+weight_decimal);
                Toast.makeText(getActivity(), String.valueOf(weighttemp)+" lbs", Toast.LENGTH_SHORT).show();
                weighttemp = weighttemp/2.205;
                dialog.dismiss();

                setWeight();
            }
        });

        dialog.show();
    }
    private void setWeightDate(){
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        weight_date.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        Log.i(TAG, "test: " + String.valueOf(year));
                        setStartTime.set(Calendar.YEAR, year);
                        setStartTime.set(Calendar.MONTH, month);
                        setStartTime.set(Calendar.DAY_OF_MONTH, day);
                        setEndTime.set(Calendar.YEAR, year);
                        setEndTime.set(Calendar.MONTH, month);
                        setEndTime.set(Calendar.DAY_OF_MONTH, day);
                        weight_date.setText(String.format("%1$tBxx %1$td", setStartTime));
                        setWeightTime();
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });
    }
    private void setWeightTime(){
        final Calendar c = Calendar.getInstance();
        final int hr = c.get(Calendar.HOUR_OF_DAY);
        final int min = c.get(Calendar.MINUTE);

        weight_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        setStartTime.set(Calendar.HOUR_OF_DAY, hour);
                        setStartTime.set(Calendar.MINUTE, minute);
                        weight_time.setText(String.format("%1$tI:%1$tM %1$Tp", setStartTime));
                    }
                }, hr, min, false);
                timePickerDialog.show();
            }
        });
    }

    private void setWeight(){

        long endTime = setStartTime.getTimeInMillis();

        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(getActivity())
                .setDataType(DataType.TYPE_WEIGHT)
                .setType(DataSource.TYPE_RAW)
                .build();

        float weightadd = (float) weighttemp;
        DataPoint dataPoint =
                DataPoint.builder(dataSource)
                        .setTimeInterval(1, endTime, TimeUnit.MILLISECONDS)
                        .setField(Field.FIELD_WEIGHT, weightadd)
                        .build();

        DataSet dataSet = DataSet.builder(dataSource).add(dataPoint).build();

        DataUpdateRequest request = new DataUpdateRequest.Builder()
                .setDataSet(dataSet)
                .setTimeInterval(1, endTime, TimeUnit.MILLISECONDS)
                .build();
        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity())).updateData(request);

    }

    // working
    private void activityDialog() {

        View mview = getLayoutInflater().inflate(R.layout.activity_add, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity(), R.style.DialogFullScreen);
        activity_date = mview.findViewById(R.id.activity_date);
        activity_start_time = mview.findViewById(R.id.activity_start_time);
        activity_end_time = mview.findViewById(R.id.activity_end_time);
        activity_title = mview.findViewById(R.id.activity_title);
        activity_type = mview.findViewById(R.id.activity_type);
        activity_notes = mview.findViewById(R.id.activity_notes);
        activity_calories = mview.findViewById(R.id.activity_calories);
        activity_steps = mview.findViewById(R.id.activity_steps);
        activity_miles = mview.findViewById(R.id.activity_miles);
        activity_close = mview.findViewById(R.id.activity_close);
        activity_save = mview.findViewById(R.id.activity_save);

        builder.setView(mview);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogSlideUp;

        setActivity_date();

        activity_type.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivityType();
            }
        });

        activity_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                setStartTime = Calendar.getInstance();
                setEndTime = Calendar.getInstance();
            }
        });
        activity_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setName = activity_title.getText().toString();
                setDetails = activity_notes.getText().toString();
                setSteps = Float.parseFloat(activity_steps.getText().toString());
                setCalories = Float.parseFloat(activity_calories.getText().toString());
                setMiles = Float.parseFloat(activity_miles.getText().toString());
                dialog.dismiss();

                Log.i(TAG, "Start_Time" + String.format("%1$tA, %1$tb %1$td %1$tY at %1$tI:%1$tM %1$Tp", setStartTime));
                Log.i(TAG, "Start_Time" + String.format("%1$tA, %1$tb %1$td %1$tY at %1$tI:%1$tM %1$Tp", setEndTime));
                Log.i(TAG, "Name: " + setName);
                Log.i(TAG, "Details: " + setDetails);
                Log.i(TAG, "Steps: " + setSteps);
                Log.i(TAG, "Calories: " + setCalories);
                Log.i(TAG, "Miles: " + setMiles);

                addActivity();
            }
        });

        dialog.show();
    }

    // working
    private void getActivityType() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Activity");

        String[] activities = {"Walking", "Running", "Sleep"};
        builder.setItems(activities, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int pos) {
                switch (pos) {
                    case 0:
                        setActivityType = FitnessActivities.WALKING;
                        activity_type.setText("Walking");
                        break;
                    case 1:
                        setActivityType = FitnessActivities.RUNNING;
                        activity_type.setText("Running");
                        break;
                    case 2:
                        setActivityType = FitnessActivities.SLEEP;
                        activity_type.setText("Sleep");
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // working
    private void setActivity_date() {

        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        activity_date.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        Log.i(TAG, "test: " + String.valueOf(year));
                        setStartTime.set(Calendar.YEAR, year);
                        setStartTime.set(Calendar.MONTH, month);
                        setStartTime.set(Calendar.DAY_OF_MONTH, day);
                        setEndTime.set(Calendar.YEAR, year);
                        setEndTime.set(Calendar.MONTH, month);
                        setEndTime.set(Calendar.DAY_OF_MONTH, day);
                        activity_date.setText(String.format("%1$tA, %1$tb %1$td %1$tY", setStartTime));
                        setActivity_start_time();
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });
    }

    // put an hour back
    private void setActivity_start_time() {
        final Calendar c = Calendar.getInstance();
        final int hr = c.get(Calendar.HOUR_OF_DAY);
        final int min = c.get(Calendar.MINUTE);

        activity_start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        setStartTime.set(Calendar.HOUR_OF_DAY, hour);
                        setStartTime.set(Calendar.MINUTE, minute);
                        activity_start_time.setText(String.format("%1$tI:%1$tM %1$Tp", setStartTime));
                        setActivity_end_time();
                    }
                }, hr, min, false);
                timePickerDialog.show();
            }
        });
    }

    // working
    private void setActivity_end_time() {
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, setStartTime.get(Calendar.HOUR) + 1);
        c.set(Calendar.MINUTE, setStartTime.get(Calendar.MINUTE));
        final int hr = c.get(Calendar.HOUR_OF_DAY);
        final int min = c.get(Calendar.MINUTE);

        activity_end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        setEndTime.set(Calendar.HOUR_OF_DAY, hour);
                        setEndTime.set(Calendar.MINUTE, minute);
                        activity_end_time.setText(String.format("%1$tI:%1$tM %1$Tp", setEndTime));
                    }
                }, hr, min, false);
                timePickerDialog.show();
            }
        });
    }

    // working
    private void addActivity() {
        long startTime = setStartTime.getTimeInMillis();
        long endTime = setEndTime.getTimeInMillis();
        setMiles = setMiles*1609;

        DataSource stepDataSource = new DataSource.Builder()
                .setAppPackageName((getActivity().getPackageName()))
                .setDataType(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_RAW)
                .build();
        DataSource calsDataSource = new DataSource.Builder()
                .setAppPackageName((getActivity().getPackageName()))
                .setDataType(DataType.TYPE_CALORIES_EXPENDED)
                .setType(DataSource.TYPE_RAW)
                .build();
        DataSource distanceDataSource = new DataSource.Builder()
                .setAppPackageName((getActivity().getPackageName()))
                .setDataType(DataType.AGGREGATE_DISTANCE_DELTA)
                .setType(DataSource.TYPE_RAW)
                .build();

        DataSet stepDataSet = DataSet.create(stepDataSource);
        DataSet calsDataSet = DataSet.create(calsDataSource);
        DataSet distanceDataSet = DataSet.create(distanceDataSource);

//        DataPoint.builder(activityDataSource)
//                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
//                .setField(Field.FIELD_DISTANCE, setMiles)
//                .build();
//        DataPoint.builder(activityDataSource)
//                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
//                .setField(Field.FIELD_CALORIES, setCalories)
//                .build();
//
//        DataPoint.builder(activityDataSource)
//                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
//                .setField(Field.FIELD_STEPS, Integer.valueOf((int) setSteps))
//                .build();

        DataPoint firstRunSpeed = stepDataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        firstRunSpeed.getValue(FIELD_STEPS).setInt(Integer.valueOf((int) setSteps));
        stepDataSet.add(firstRunSpeed);

        DataPoint firstRunSpeed1 = distanceDataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        firstRunSpeed1.getValue(Field.FIELD_DISTANCE).setFloat(setMiles);
        distanceDataSet.add(firstRunSpeed1);

        DataPoint firstRunSpeed2 = calsDataSet.createDataPoint()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
        firstRunSpeed2.getValue(Field.FIELD_CALORIES).setFloat(setCalories);
        calsDataSet.add(firstRunSpeed2);

        Session session = new Session.Builder()
                .setName(setName)
                .setDescription(setDetails)
                .setActivity(setActivityType)
                .setStartTime(startTime, TimeUnit.MILLISECONDS)
                .setEndTime(endTime, TimeUnit.MILLISECONDS)
                .build();

        SessionInsertRequest insertRequest = new SessionInsertRequest.Builder()
                .setSession(session)
                .addDataSet(stepDataSet)
                .addDataSet(calsDataSet)
                .addDataSet(distanceDataSet)
                .build();

        Fitness.getSessionsClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .insertSession(insertRequest)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // At this point, the session has been inserted and can be read.
                        Toast.makeText(getActivity(), "Activity Successfully Added!", Toast.LENGTH_SHORT).show();
                        refreshFragment();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        refreshFragment();
                        Toast.makeText(getActivity(), "Activity wasn't able to be added! Error: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    // working
    public void getActivityList(Context context) {

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.add(Calendar.DATE, +1);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.YEAR, -1);
        long startTime = cal.getTimeInMillis();

        Log.i("Test", "This test ran successfully");

        // Build a session read request
        SessionReadRequest readRequest = new SessionReadRequest.Builder()
                .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .readSessionsFromAllApps()
                .read(DataType.TYPE_MOVE_MINUTES)
                .read(DataType.TYPE_STEP_COUNT_DELTA)
                .enableServerQueries()
                .build();

        Fitness.getSessionsClient(context, GoogleSignIn.getLastSignedInAccount(context))
                .readSession(readRequest)
                .addOnSuccessListener(new OnSuccessListener<SessionReadResponse>() {
                    @Override
                    public void onSuccess(SessionReadResponse sessionReadResponse) {
                        // Get a list of the sessions that match the criteria to check the result.
                        List<Session> sessions = sessionReadResponse.getSessions();
                        Log.i(TAG, "Session read was successful. Number of returned sessions is: "
                                + sessions.size());
                        for (Session session : sessions) {
                            // Process the session
                            setActivitySession(session);

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Failed to read session");
                    }
                });
    }

    // working
    private void setActivitySession(Session session) {
        DateFormat dateFormat = getTimeInstance(DateFormat.SHORT);
        //
        if (session.getName() == null) {
            aname = checkTimeBetween(session) + formatName(session);
        } else {
            aname = session.getName();
        }
        //
        atime = dateFormat.format(session.getStartTime(TimeUnit.MILLISECONDS)) + " - " + dateFormat.format(session.getEndTime(TimeUnit.MILLISECONDS));
        //
        long diff = session.getEndTime(TimeUnit.MILLISECONDS) - session.getStartTime(TimeUnit.MILLISECONDS);
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        adetail = String.valueOf(hours) + " hr " + String.valueOf(minutes - (hours * 60)) + " min";
        setIcons(session);
    }

    // working
    private String formatName(Session session) {

        if (session.getActivity().contains("running")) {
            return "Run";
        } else if (session.getActivity().contains("walking")) {
            return "Walk";
        } else if (session.getActivity().contains("sleep")) {
            return "Sleep";
        }
        return session.getActivity().substring(0, 1).toUpperCase() + session.getActivity().substring(1).toLowerCase();
    }

    // working
    private String checkTimeBetween(Session session) {
        String checkTime = (String) android.text.format.DateFormat.format("HH:mm:ss", session.getStartTime(TimeUnit.MILLISECONDS));
//        3-6:59 am Early morning
//        7 -10:59 am morning
//        11 am-12:59 pm Lunch
//        1 pm - 5:59 pm Afternoon
//        6 pm - 9:59 pm Evening
//        10pm - 2:59 am Late night
//            {
//                em1 = "03:00:00";
//                em2 = "06:59:59";

//                m1 = "07:00:00";
//                m2 = "10:59:59";

//                l1 = "11:00:00";
//                l2 = "12:59:59";

//                a1 = "13:00:00";
//                a2 = "17:59:59";

//                e1 = "18:00:00";
//                e2 = "21:59:59";

//                ln1 = "22:00:00";
//                ln2 = "2:59:59";
//            }
        int hourTime = Integer.parseInt(checkTime.substring(0, 2));
        if (hourTime >= 3 && hourTime <= 6) {
            return "Early Morning ";
        } else if (hourTime >= 7 && hourTime <= 10) {
            return "Morning ";
        } else if (hourTime >= 11 && hourTime <= 12) {
            return "Lunch ";
        } else if (hourTime >= 13 && hourTime <= 17) {
            return "Afternoon ";
        } else if (hourTime >= 18 && hourTime <= 21) {
            return "Evening ";
        } else if (hourTime >= 22 && hourTime <= 23) {
            return "Late Night ";
        } else if (hourTime >= 0 && hourTime <= 2) {
            return "Late Night ";
        }
        return "";
    }

    // working
    private void setIcons(Session session) {
        if (session.getActivity().contains("running")) {
            aicon_start = "ic_running";
            aicon_end = "ic_running_circle";
        } else if (session.getActivity().contains("walking")) {
            aicon_start = "ic_walking";
            aicon_end = "ic_walking_circle";
        } else if (session.getActivity().contains("sleep")) {
            aicon_start = "ic_sleep";
            aicon_end = "ic_sleep_circle";
        } else {
            aicon_start = "ic_warning";
            aicon_end = "ic_warning_circle";
        }
        setActivityList();
    }

    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (prefs.getBoolean("list_changed", false)) {
            prefs.edit().remove("list_changed").apply();
            refreshFragment();
        }
    }

    private void refreshFragment(){
        activities.clear();
        Fragment currentFragment = getActivity().getSupportFragmentManager().findFragmentById(R.id.FragmentContainer);
        if (currentFragment instanceof MetricsFragment) {
            FragmentTransaction fragTransaction = (getActivity()).getSupportFragmentManager().beginTransaction();
            fragTransaction.detach(currentFragment);
            fragTransaction.attach(currentFragment);
            fragTransaction.commit();
        }
    }


    // working
    private void setActivityList() {
        Log.i("setActivityList", "This activity ran");
        if(isAdded()) {
            adapter = new activityArrayAdapter(requireActivity(), 0, activities);
            listView = v.findViewById(R.id.customListView);
            listView.setAdapter(adapter);
            activities.add(0,
                    new Activity(aicon_start, atime, aname, adetail, aicon_end)
            );
        }
    }


}

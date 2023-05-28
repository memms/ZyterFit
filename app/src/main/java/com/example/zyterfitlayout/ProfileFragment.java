package com.example.zyterfitlayout;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.BaseKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.GoalsClient;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Goal;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataUpdateRequest;
import com.google.android.gms.fitness.request.GoalsReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.GoalsResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import MiBandbluetooth.Activities.DiscoveryActivity;
import MiBandbluetooth.Bluetooth.BluetoothQueue;
import MiBandbluetooth.Bluetooth.HeartRateGattCallback;
import MiBandbluetooth.Bluetooth.TransactionBuilder;
import MiBandbluetooth.BluetoothScanMainActivity;
import MiBandbluetooth.Device.DeviceCandidateAdapter;
import MiBandbluetooth.Device.DeviceService;
import MiBandbluetooth.Device.MiBandDevice;
import MiBandbluetooth.Device.MiBandService;
import MiBandbluetooth.Device.MiBandSupport;
import MiBandbluetooth.Utils.AndroidUtils;

import static com.example.zyterfitlayout.R.layout.specific_device_info;
import static com.example.zyterfitlayout.R.layout.weight_numberpicker_dialog;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ZF";
    private int stepgoals, weight_whole, weight_decimal, feet, inches,heartgoals;
    private double weight,weighttemp,meterstemp,testing;
    private TextView stepgoal, unit_first, unit_end, heartgoal;
    private Button stepminus,stepplus, weight_button, height_button;
    private DashboardFragment dashboardFragment;
    private String Tag = "ZF";
    private RelativeLayout manageDevicesTab, addDevicesTab;
    private DeviceCandidateAdapter devicesListAdapter;


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        stepgoal = v.findViewById(R.id.stepgoal);
        stepminus = v.findViewById(R.id.stepminus);
        stepplus = v.findViewById(R.id.stepplus);
        weight_button = v.findViewById(R.id.weight_button);
        height_button = v.findViewById(R.id.height_button);
        heartgoal = v.findViewById(R.id.heartGoal);

        manageDevicesTab = v.findViewById(R.id.manage_devices_tab);
        addDevicesTab = v.findViewById(R.id.add_devices_tab);

        stepminus.setOnClickListener(this);
        stepplus.setOnClickListener(this);
        weight_button.setOnClickListener(this);
        height_button.setOnClickListener(this);
        manageDevicesTab.setOnClickListener(this);
        addDevicesTab.setOnClickListener(this);

        dashboardFragment = new DashboardFragment();
        try {
            getWeight();
            getHeight();
            getStepGoal();
            getHeartGoal();
        } catch (Exception e){
        }


        setText();

        return v;
    }


    private void setText(){
        stepgoal.setText(""+stepgoals);
        heartgoal.setText(""+heartgoals);
        weight_button.setText(""+weight + " lbs");
        height_button.setText("" + feet + "'" + inches + "\"");
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.stepminus:
                break;
            case R.id.stepplus:
                break;
            case R.id.weight_button:
                weightAlertBuilder();
                break;
            case R.id.height_button:
                heightAlertBuilder();
                break;
            case R.id.manage_devices_tab:
                test();
                break;
            case R.id.add_devices_tab:
                Intent intent = new Intent(getActivity(), BluetoothScanMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;

        }
    }
    private void test(){
//        BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
//        BluetoothDevice bluetoothDevice = ;
//        MiBandDevice device = new MiBandDevice(bluetoothDevice.getBondState());
        View Mainview = getLayoutInflater().inflate(R.layout.fragment_manage_devices, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity(), R.style.DialogFullScreen);
        ListView MDListView = Mainview.findViewById(R.id.MDevicesListView);
        devicesListAdapter = new DeviceCandidateAdapter(getActivity(), MainActivity.deviceList);
        MDListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final MiBandDevice deviceCandidate = MainActivity.deviceList.get(position);
                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog( getActivity());
                bottomSheetDialog.setContentView(specific_device_info);

                TextView name = bottomSheetDialog.findViewById(R.id.specific_device);
                Button unpair = bottomSheetDialog.findViewById(R.id.device_unpair);
                Button stopTracking = bottomSheetDialog.findViewById(R.id.device_stop_tracking);

                TransactionBuilder builder = new TransactionBuilder();
//                builder.add();

                name.setText(""+deviceCandidate.getName());
                stopTracking.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        HeartRateGattCallback heartRateGattCallback = new HeartRateGattCallback(DeviceService.mMiBandSupport,getActivity());
                        heartRateGattCallback.enableRealtimeHeartRateMeasurement(false);
                    }
                });
                unpair.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MiBandService miBandService = new MiBandService(getActivity());
                        miBandService.disconnect();
                        MiBandSupport miBandSupport = new MiBandSupport();
                        BluetoothQueue bluetoothQueue = miBandSupport.getQueue();
                        bluetoothQueue.disconnect();
                        BluetoothDevice device = MiBandDevice.mBluetoothDevice;
                        try {
                            Method m = device.getClass()
                                    .getMethod("removeBond", (Class[]) null);
                            m.invoke(device, (Object[]) null);
                            BluetoothQueue.mTransactions.clear();
//                            BluetoothQueue.mTransactions.add(null);
                            BluetoothQueue.mAbortTransaction = true;
                            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            bluetoothAdapter.disable();
                            MiBandDevice miBandDevice = new MiBandDevice(device, device.getAddress());
                            miBandDevice.setState(MiBandDevice.State.NOT_CONNECTED);

                            HeartRateGattCallback heartRateGattCallback = new HeartRateGattCallback(DeviceService.mMiBandSupport,getActivity());
                            heartRateGattCallback.enableRealtimeHeartRateMeasurement(false);
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                        Intent serviceintent = new Intent(getActivity(), DeviceService.class);
                        getActivity().stopService(serviceintent);
                    }
                });
                bottomSheetDialog.show();
            }
        });
        MDListView.setAdapter(devicesListAdapter);
        builder.setView(Mainview);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogSlideUp;

        dialog.show();
    }

    private void weightAlertBuilder() {
        AlertDialog.Builder mBuilder= new AlertDialog.Builder(getActivity());
        View mView = getLayoutInflater().inflate(weight_numberpicker_dialog,null);
        mBuilder.setTitle("Weight");
        final NumberPicker weight_int = mView.findViewById(R.id.weight_int_picker);
        weight_int.setMinValue(1);
        weight_int.setMaxValue(1000);
        weight_int.setValue(weight_whole);
        final NumberPicker weight_double = mView.findViewById(R.id.weight_double_picker);
        weight_double.setMinValue(0);
        weight_double.setMaxValue(9);
        weight_double.setValue(weight_decimal);

        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                weight_whole = weight_int.getValue();
                weight_decimal = weight_double.getValue();
                weighttemp = Double.parseDouble(weight_whole+"."+weight_decimal);
                Toast.makeText(getActivity(), String.valueOf(weighttemp)+" lbs", Toast.LENGTH_SHORT).show();
                weighttemp = weighttemp/2.205;
                dialogInterface.dismiss();
                setWeight();
            }
        });
        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }
    public void getWeight(){
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
                                                setText();
                                            }
                                        }
                                    }
                                }
                        });

    }
    public void setWeight(){
        Calendar cal = Calendar.getInstance();
        Date date = new Date();
        cal.setTime(date);
        long endTime = cal.getTimeInMillis();

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
        getWeight();

    }
    private void heightAlertBuilder(){
        AlertDialog.Builder mBuilder= new AlertDialog.Builder(getActivity());
        View mView = getLayoutInflater().inflate(weight_numberpicker_dialog,null);
        mBuilder.setTitle("Height");
        unit_first = mView.findViewById(R.id.decimal);
        unit_end = mView.findViewById(R.id.units_end);
        unit_first.setText("ft");
        unit_end.setText("in");
        final NumberPicker feet_int = mView.findViewById(R.id.weight_int_picker);
        feet_int.setMinValue(1);
        feet_int.setMaxValue(8);
        feet_int.setValue(feet);
        final NumberPicker inch_int = mView.findViewById(R.id.weight_double_picker);
        inch_int.setMinValue(0);
        inch_int.setMaxValue(11);
        inch_int.setValue(inches);

        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                feet = feet_int.getValue();
                inches = inch_int.getValue();
                double inch_decimal = inches/12.0;
                double feet_temp = feet+inch_decimal;
                meterstemp = Math.round((feet_temp/3.281)*10000.0)/10000.0;
                Toast.makeText(getActivity(), String.valueOf(feet)+"'"+String.valueOf(inches) + "\"", Toast.LENGTH_SHORT).show();
                dialogInterface.dismiss();
                setHeight();
            }
        });
        mBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();
    }
    public void getHeight(){
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
                                for (DataSet dataSet : dataReadResponse.getDataSets()) {
                                    for (DataPoint dp : dataSet.getDataPoints()) {
                                        for (Field field : dp.getDataType().getFields()) {
                                            Float test = dp.getValue(field).asFloat();
                                            Double heightdouble = (double) test * 3.2808;
                                            BigDecimal bigDecimal = new BigDecimal(String.valueOf(heightdouble));
                                            feet = bigDecimal.intValue();
                                            double doublevalue = ((bigDecimal.subtract(new BigDecimal(feet))).doubleValue()) * 12;
                                            inches = (int) (Math.round(doublevalue));
                                            Log.i(TAG, "" + feet + "'" + inches + "\"");
                                            setText();
                                        }
                                    }
                                }
                            }
                        });
    }
    public void setHeight(){
        Calendar cal = Calendar.getInstance();
        Date date = new Date();
        cal.setTime(date);
        long endTime = cal.getTimeInMillis();

        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(getActivity())
                .setDataType(DataType.TYPE_HEIGHT)
                .setType(DataSource.TYPE_RAW)
                .build();
        float heightadd = (float) meterstemp;
        DataPoint dataPoint =
                DataPoint.builder(dataSource)
                        .setTimeInterval(1, endTime, TimeUnit.MILLISECONDS)
                        .setField(Field.FIELD_HEIGHT, heightadd)
                        .build();
        DataSet dataSet = DataSet.builder(dataSource).add(dataPoint).build();
        DataUpdateRequest request = new DataUpdateRequest.Builder()
                .setDataSet(dataSet)
                .setTimeInterval(1, endTime, TimeUnit.MILLISECONDS)
                .build();
        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity())).updateData(request);
        getHeight();
    }
    public void getStepGoal()  {

        GoogleSignInOptionsExtension fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .build();
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(getActivity()), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    1,
                    GoogleSignIn.getLastSignedInAccount(getActivity()),
                    fitnessOptions);
        } else {
            try {
                GoalsReadRequest req = new GoalsReadRequest.Builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .build();

                Fitness.getGoalsClient(getActivity(), GoogleSignIn.getAccountForExtension(getActivity(), fitnessOptions))
                        .readCurrentGoals(req)
                        .addOnCompleteListener(new OnCompleteListener<List<Goal>>() {
                            @Override
                            public void onComplete(@NonNull Task<List<Goal>> task) {
                                if (task.getResult().size()!=0) {
                                    double t = task.getResult().get(0).getMetricObjective().getValue();
                                    stepgoals = (int) t;
                                    setText();
                                }
                            }
                        });
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public void getHeartGoal() {

        GoogleSignInOptionsExtension fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_HEART_POINTS, FitnessOptions.ACCESS_READ)
                        .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(getActivity()), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    1,
                    GoogleSignIn.getLastSignedInAccount(getActivity()),
                    fitnessOptions);
        } else {

            try {
                GoalsReadRequest req = new GoalsReadRequest.Builder()
                        .addDataType(DataType.TYPE_HEART_POINTS)
                        .build();


                Fitness.getGoalsClient(getActivity(), GoogleSignIn.getAccountForExtension(getActivity(), fitnessOptions))
                        .readCurrentGoals(req)
                        .addOnCompleteListener(new OnCompleteListener<List<Goal>>() {
                            @Override
                            public void onComplete(@NonNull Task<List<Goal>> task) {
                                if (task.getResult().size()!=0) {
                                    double t = task.getResult().get(0).getMetricObjective().getValue();
                                    task.getResult();
                                    heartgoals = (int) t;
                                    setText();
                                }
                            }
                        });
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }


}

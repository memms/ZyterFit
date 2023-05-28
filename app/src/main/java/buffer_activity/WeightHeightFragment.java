package buffer_activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.zyterfitlayout.MainActivity;
import com.example.zyterfitlayout.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
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
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class WeightHeightFragment extends Fragment implements View.OnClickListener {

    private ImageView IprofilePic;
    private TextView TemailID;
    private Button weight_button, height_button, next_button;
    private TextView unit_first, unit_end;
    private double weight,weighttemp,meterstemp,testing;
    private int weight_whole, weight_decimal, feet, inches;
    private static final String TAG = "ZF";
    private Boolean weightset =false, heightset =false;
    private BufferActivity bufferActivity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_weight_height, container, false);
        bufferActivity = new BufferActivity();
        IprofilePic = v.findViewById(R.id.G_profile_pic);
        TemailID = v.findViewById(R.id.G_profile_email);

        weight_button = v.findViewById(R.id.Sweight_button);
        height_button = v.findViewById(R.id.Sheight_button);
        next_button = v.findViewById(R.id.Snextbutton);

        weight_button.setOnClickListener(this);
        height_button.setOnClickListener(this);

        setProfile();

        getWeight();
        getHeight();

        next_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(weightset == true || heightset == true){
                    SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putInt("FirstStart", 1);
                    editor.commit();
                    startActivity(new Intent(getActivity(), MainActivity.class));
                    getActivity().finish();
                } else {
                    if(weightset == false || heightset == true) {
                        Toast.makeText(getActivity(), "Please set your appropriate weight.", Toast.LENGTH_SHORT).show();
                    } else if(weightset == true || heightset == false){
                        Toast.makeText(getActivity(), "Please set your appropriate height.", Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(getActivity(), "Please set your appropriate weight and height.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return v;
    }
    private void setProfile(){
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (!mPreferences.getString("personEmail", "").equals("")) {
            String SprofileName = mPreferences.getString("personEmail", "");
            TemailID.setText(""+SprofileName);
        }
        if (!mPreferences.getString("personImage", "").equals("")) {
            Uri UprofilePic = Uri.parse(mPreferences.getString("personImage", ""));
            Glide.with(this)
                    .load(UprofilePic)
                    .into(IprofilePic);
        }
    }

    private void setText(){
        weight_button.setText(""+weight + " lbs");
        height_button.setText("" + feet + "'" + inches + "\"");
    }
    private void setButtonFalse(){
        next_button.setAlpha(.5f);
        next_button.setClickable(false);
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.stepminus:
                break;
            case R.id.stepplus:
                break;
            case R.id.Sweight_button:
                weightAlertBuilder();
                break;
            case R.id.Sheight_button:
                heightAlertBuilder();
                break;

        }
    }
    private void weightAlertBuilder() {
        Log.i("weightAlertBuilder", "weightAlertBuilder");
        AlertDialog.Builder mBuilder= new AlertDialog.Builder(getActivity());
        View mView = getLayoutInflater().inflate(R.layout.weight_numberpicker_dialog,null);
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
    private void getWeight(){
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
                                for (DataSet dataSet : dataReadResponse.getDataSets()) {
                                    for (DataPoint dp : dataSet.getDataPoints()) {
                                        for (Field field : dp.getDataType().getFields()) {
                                            if(dp.getValue(field)!= null && dp.getDataType().getFields()!=null) {
                                                Float test = dp.getValue(field).asFloat();
                                                Double weightdouble = (double) test;
                                                weight = Math.round((weightdouble * 2.205) * 10) / 10.0;
                                                Log.i(TAG, String.valueOf(weight));
                                                weight_whole = (int) weight;
                                                BigDecimal bigDecimal = new BigDecimal(String.valueOf(weight));
                                                int convert = (int) (bigDecimal.doubleValue() * 10);
                                                weight_decimal = convert % 10;
                                                Log.i(TAG, "Weight whole: " + weight_whole);
                                                Log.i(TAG, "Weight decimal: " + weight_decimal);
                                                setText();
                                                weightset = true;
                                            } else {
                                                weightset = false;
                                                setButtonFalse();
                                            }
                                        }
                                    }
                                }
                            }
                        });

    }
    private void setWeight(){
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
        View mView = getLayoutInflater().inflate(R.layout.weight_numberpicker_dialog,null);
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
    private void getHeight(){
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
//                                if (dataReadResponse.getDataSets().size()==0)
                                for (DataSet dataSet : dataReadResponse.getDataSets()) {
                                    for (DataPoint dp : dataSet.getDataPoints()) {
                                        for (Field field : dp.getDataType().getFields()) {
                                            if(dp.getValue(field)!=null&&dp.getDataType().getFields()!=null) {
                                                Float test = dp.getValue(field).asFloat();
                                                Double heightdouble = (double) test * 3.2808;
                                                BigDecimal bigDecimal = new BigDecimal(String.valueOf(heightdouble));
                                                feet = bigDecimal.intValue();
                                                double doublevalue = ((bigDecimal.subtract(new BigDecimal(feet))).doubleValue()) * 12;
                                                inches = (int) (Math.round(doublevalue));
                                                Log.i(TAG, "" + feet + "'" + inches + "\"");
                                                setText();
                                                heightset = true;
                                            }
                                            else{
                                                heightset = false;
                                                setButtonFalse();
                                            }
                                        }
                                    }
                                }
                            }
                        });
    }
    private void setHeight(){
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


}
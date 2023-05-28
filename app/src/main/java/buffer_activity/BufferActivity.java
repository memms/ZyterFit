package buffer_activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;

import com.example.zyterfitlayout.LoginFragment;
import com.example.zyterfitlayout.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;

import static com.google.android.gms.fitness.data.DataType.TYPE_STEP_COUNT_DELTA;

public class BufferActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buffer);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        GoogleLoginFragment googleLoginFragment = new GoogleLoginFragment();
        FragmentManager glf = getSupportFragmentManager();
        glf.beginTransaction().add(R.id.bufferact,googleLoginFragment).commit();

    }
    public FitnessOptions fitnessOptions(){
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
                        .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED)
                        .addDataType(DataType.AGGREGATE_HEART_POINTS)
                        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_HEART_POINTS, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_HEART_POINTS, FitnessOptions.ACCESS_WRITE)
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
        return fitnessOptions;
    }
    public boolean checkGooglePermissions(Context context){
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(context), fitnessOptions())) {
            return false;
        } else{
            return true;
        }
    }
    public boolean checkAndroidPermissions(Context context){
        int permissionState = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionState1 = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.BODY_SENSORS);
        int permissionState2 = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.FOREGROUND_SERVICE);
        int permissionState3 = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACTIVITY_RECOGNITION);
        int permissionState4 = ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        Log.i("permissionscheck",  String.valueOf(permissionState == PackageManager.PERMISSION_GRANTED));
        Log.i("permissionscheck1",  String.valueOf(permissionState1 == PackageManager.PERMISSION_GRANTED));
        Log.i("permissionscheck2",  String.valueOf(permissionState2 == PackageManager.PERMISSION_GRANTED));
        Log.i("permissionscheck3",  String.valueOf(permissionState3));

        /** permissionState 3 is not checked due to android never asking for the permision,
         * hence value never being Permission Granted **/
        return permissionState == PackageManager.PERMISSION_GRANTED && permissionState1 == PackageManager.PERMISSION_GRANTED && permissionState2 == PackageManager.PERMISSION_GRANTED && permissionState4 == PackageManager.PERMISSION_GRANTED;
    }

}
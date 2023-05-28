package buffer_activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.view.DragAndDropPermissionsCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.zyterfitlayout.MainActivity;
import com.example.zyterfitlayout.R;

import java.security.Permission;

public class AndroidPermisionsFragment extends Fragment {
    private Button Bcontinue;
    private BufferActivity bufferActivity;
    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_android_permisions, container, false);

        Bcontinue = v.findViewById(R.id.Apermissionsbutton);
        bufferActivity = new BufferActivity();

        onclick();
        return v;
    }

    private void requestAndroidPermission(){
        requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BODY_SENSORS,Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.FOREGROUND_SERVICE},
                1);

        runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(runnable, 4000);
                if (bufferActivity.checkAndroidPermissions(getActivity())== true){
                    Bcontinue.setText("Continue");
                }
                Log.i("runnable", "Runnable for checking button is running");
            }
        }; handler.postDelayed(runnable, 10);
    }
    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 10);

    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    private void onclick(){
        Bcontinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                if(bufferActivity.checkAndroidPermissions(getActivity())==false){
                    requestAndroidPermission();
                } else if (mPreferences.getInt("FirstStart",0)==0) {
                    WeightHeightFragment weightHeightFragment = new WeightHeightFragment();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.bufferact, weightHeightFragment).commit();
                } else{
                    startActivity(new Intent(getActivity(), MainActivity.class));
                    getActivity().finish();
                }
            }
        });
    }

}
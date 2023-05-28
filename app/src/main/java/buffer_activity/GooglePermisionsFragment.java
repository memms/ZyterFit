package buffer_activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.FitnessOptions;

public class GooglePermisionsFragment extends Fragment {

    private Button Bcontinue;
    private BufferActivity bufferActivity;
    private FitnessOptions fitnessOptions;
    private Handler handler = new Handler();
    private Runnable runnable;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_google_permisions, container, false);
        Bcontinue = v.findViewById(R.id.Gpermissionsbutton);
        bufferActivity = new BufferActivity();
        fitnessOptions = bufferActivity.fitnessOptions();
        onclick();
        return v;
    }
    private void requestPerms(){
        GoogleSignIn.requestPermissions(
                    this,
                    1,
                    GoogleSignIn.getLastSignedInAccount(getActivity()),
                    fitnessOptions);
        runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(runnable, 4000);
                if (bufferActivity.checkGooglePermissions(getActivity())== true){
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
                if (bufferActivity.checkGooglePermissions(getActivity())==false){
                    requestPerms();
                } else if(bufferActivity.checkAndroidPermissions(getActivity()) == false){
                    AndroidPermisionsFragment androidPermisionsFragment = new AndroidPermisionsFragment();
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.bufferact, androidPermisionsFragment).commit();
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
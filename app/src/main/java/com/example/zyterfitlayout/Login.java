package com.example.zyterfitlayout;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class Login extends AppCompatActivity {
    Button register1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_first);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        {

            LoginFragment loginFragment = new LoginFragment();
            FragmentManager lf = getSupportFragmentManager();
            lf.beginTransaction().add(R.id.loginact,loginFragment).commit();

            ActivityTrackActivity activityTrackActivity = new ActivityTrackActivity();

        }

    }
}

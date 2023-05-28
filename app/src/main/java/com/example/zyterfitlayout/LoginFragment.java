package com.example.zyterfitlayout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.material.snackbar.Snackbar;

import buffer_activity.BufferActivity;

public class LoginFragment extends Fragment {
    Button register1;
    Button register2;
    Button login;
    View v;

    private static final int REQUEST_OAUTH_REQUEST_CODE = 1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_login, container, false);

        register1 = v.findViewById(R.id.button_register_bottom);
        register2 = v.findViewById(R.id.button_register_top);
        login = v.findViewById(R.id.button_login);
        Login();
        Register();

//        if (!checkPermissions()) {
//            requestPermissions();
//        } else {
//            googlesignin();
//        }
//        googlesignin();

        return v;
    }


    private void Login(){
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//              add if statement to make query with sql for credentials = true
                startActivity(new Intent(getActivity(), BufferActivity.class));
                getActivity().finish();
            }
        });
    }

    private void Register(){
        register1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterFragment registerFragment = new RegisterFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.loginact,registerFragment);
                transaction.commit();
            }
        });
        register2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterFragment registerFragment = new RegisterFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.loginact,registerFragment);
                transaction.commit();
            }
        });
    }

}
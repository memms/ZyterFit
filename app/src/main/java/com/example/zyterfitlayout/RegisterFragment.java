package com.example.zyterfitlayout;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import buffer_activity.BufferActivity;

public class RegisterFragment extends Fragment {
    private Button login1;
    private Button login2;
    private Button register;
    private Spinner gender;
    private int count;
    private TextView bday;
    private Calendar calendar = Calendar.getInstance();
    private final int year = calendar.get(Calendar.YEAR);
    private final int month = calendar.get(Calendar.MONTH);
    private final int day = calendar.get(Calendar.DAY_OF_MONTH);


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_register, container, false);
        login1 = v.findViewById(R.id.button_login_top);
        login2 = v.findViewById(R.id.button_login_bottom);
        register = v.findViewById(R.id.button_register);
        gender =  v.findViewById(R.id.gender);
        bday = v.findViewById(R.id.bday);
        bday();
        login();
        register();
        gender();
        return v;
    }
    private void bday(){
        bday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        month += 1;
                        String date = day+"/"+month+"/"+year;

                        bday.setText(date);
                        bday.setTextColor(Color.BLACK);
                    }
                },year,month,day);
                datePickerDialog.show();
            }
        });
    }
    private void gender(){
        count = 0;

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.Gender,android.R.layout.simple_list_item_1);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(adapter);

        gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(adapterView.getItemAtPosition(i).equals("Gender")){
                    if(count!=0)
                        Toast.makeText(gender.getContext(), "Error: Select an apropriate Gender" , Toast.LENGTH_SHORT).show();
                    count++;
                }
                else {
                    String item = gender.getItemAtPosition(i).toString();
                    Toast.makeText(gender.getContext(), "Selected: " + item, Toast.LENGTH_SHORT).show();
                    count++;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }
    private void register(){
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//              add if statement to make query with sql for confirmed account creation
                startActivity(new Intent(getActivity(), BufferActivity.class));
                getActivity().finish();
            }
        });
    }

    private void login(){
        login1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginFragment loginFragment = new LoginFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.loginact,loginFragment);
                transaction.commit();
            }
        });
        login2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginFragment loginFragment = new LoginFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.loginact, loginFragment);
                transaction.commit();
            }
        });
    }
}
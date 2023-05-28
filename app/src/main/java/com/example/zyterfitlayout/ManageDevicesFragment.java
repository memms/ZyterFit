package com.example.zyterfitlayout;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import MiBandbluetooth.Device.DeviceCandidateAdapter;

public class ManageDevicesFragment extends Fragment {
    private DeviceCandidateAdapter candidateListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_manage_devices, container, false);

//        ListView MDListView = v.findViewById(R.id.MDevicesListView);
//        candidateListAdapter = new DeviceCandidateAdapter(getActivity(), MainActivity.deviceList);
//        MDListView.setAdapter(candidateListAdapter);


        return v;
    }
}
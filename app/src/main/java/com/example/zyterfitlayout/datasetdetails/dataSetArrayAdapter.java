package com.example.zyterfitlayout.datasetdetails;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.zyterfitlayout.R;

import java.util.ArrayList;
import java.util.List;

public class dataSetArrayAdapter extends ArrayAdapter<DataSetDetails> {
    private Context context;
    private List<DataSetDetails> dataSetDetailsArrayList;

    public dataSetArrayAdapter(@NonNull Context context, int resource, @NonNull ArrayList<DataSetDetails> list) {
        super(context, resource, list);
        this.context = context;
        this.dataSetDetailsArrayList = list;
    }



    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        DataSetDetails dataSetDetails = dataSetDetailsArrayList.get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.title_detail_adapterview,parent,false);

        TextView title = view.findViewById(R.id.title);
        TextView datapoint = view.findViewById(R.id.datapoint);

        title.setText(dataSetDetails.getTime());
        datapoint.setText(dataSetDetails.getDatapoint());

        return view;
    }



}

package com.example.zyterfitlayout;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class activityArrayAdapter extends ArrayAdapter<Activity> {
    private Context context;
    private List<Activity> activityList;
    public activityArrayAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Activity> objects) {
        super(context, resource, objects);

        this.context = context;
        this.activityList = objects;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        //get activity being displayed
        Activity activity = activityList.get(position);
        //inflating the xml
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.fitness_data, null);
        // setting views
        ImageView icon_start = view.findViewById(R.id.icon_start);
        TextView time = view.findViewById(R.id.time);
        TextView name = view.findViewById(R.id.name);
        TextView details = view.findViewById(R.id.details);
        ImageView icon_end = view.findViewById(R.id.icon_end);
        //texts
        time.setText(activity.getTime());
        name.setText(activity.getName());
        details.setText(activity.getDetails());

        //icons
        //start
        int imageID = context.getResources().getIdentifier(activity.getIcon_start(), "drawable", context.getPackageName());
        icon_start.setImageResource(imageID);
        //end
        imageID = context.getResources().getIdentifier(activity.getIcon_end(), "drawable", context.getPackageName());
        icon_end.setImageResource(imageID);

        return view;
    }
}

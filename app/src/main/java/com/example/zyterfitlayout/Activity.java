package com.example.zyterfitlayout;

public class Activity {
    private String icon_start;
    private String time;
    private String name;
    private String details;
    private String icon_end;

    public Activity(String icon_start, String time, String name, String details,String icon_end){
        this.icon_start = icon_start;
        this.time=time;
        this.name=name;
        this.details=details;
        this.icon_end = icon_end;
    }
    public String getIcon_start(){return icon_start;}
    public String getTime(){return time;}
    public String getName(){return name;}
    public String getDetails(){return details;}
    public String getIcon_end(){return icon_end;}

}

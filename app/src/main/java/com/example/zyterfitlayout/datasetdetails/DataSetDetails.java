package com.example.zyterfitlayout.datasetdetails;

public class DataSetDetails {
    private String time;
    private String datapoint;

    public DataSetDetails(String time, String datapoint) {
        this.time = time;
        this.datapoint = datapoint;
    }

    public String getTime() {
        return time;
    }
    public String getDatapoint() {
        return datapoint;
    }
    public void setTime(String time) {
        this.time = time;
    }

    public void setDatapoint(String datapoint) {
        this.datapoint = datapoint;
    }


}

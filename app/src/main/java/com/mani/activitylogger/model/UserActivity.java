package com.mani.activitylogger.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by manikandan.selvaraju on 10/3/14.
 */
public class UserActivity {

    long id;

    long startTime;

    long endTime;

    int headerId;

    String headerTxt;

    ActivityLocation startLocation;

    ActivityLocation endLocation;

    DetectedActivity activity;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ActivityLocation getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(ActivityLocation startLocation) {
        this.startLocation = startLocation;
    }

    public ActivityLocation getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(ActivityLocation endLocation) {
        this.endLocation = endLocation;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public DetectedActivity getActivity() {
        return activity;
    }

    public void setActivity(DetectedActivity activity) {
        this.activity = activity;
    }

    public int getHeaderId() {
        return headerId;
    }

    public void setHeaderId(int headerId) {
        this.headerId = headerId;
    }

    public String getHeaderTxt() {
        return headerTxt;
    }

    public void setHeaderTxt(String headerTxt) {
        this.headerTxt = headerTxt;
    }

    public String toString() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime * 1000);
        String TIME_FORMAT = "hh:mm aa";
        SimpleDateFormat TimeFormat = new SimpleDateFormat(TIME_FORMAT);

        String startTimeTxt = TimeFormat.format(calendar.getTime());

        calendar.setTimeInMillis(startTime * 1000);
        String endTimeTxt = TimeFormat.format(calendar.getTime());

        StringBuilder builder = new StringBuilder();
        builder.append(startTimeTxt);
        builder.append("\n");
        builder.append(startLocation.toString());
        builder.append(endTimeTxt);
        builder.append("\n");
        builder.append(endLocation.toString());
        builder.append("\n");
        return builder.toString();
    }
}

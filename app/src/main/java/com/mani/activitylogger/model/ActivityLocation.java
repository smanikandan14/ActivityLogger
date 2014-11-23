package com.mani.activitylogger.model;

/**
 * Created by manikandan.selvaraju on 10/3/14.
 */
public class ActivityLocation {

    long id;

    double latitude;

    double longitude;

    String address;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(latitude);
        builder.append(",");
        builder.append(longitude);
        builder.append(", ");
        builder.append(address);
        builder.append("\n");
        return builder.toString();
    }
}

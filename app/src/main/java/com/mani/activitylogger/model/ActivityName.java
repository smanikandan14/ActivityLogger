package com.mani.activitylogger.model;

/**
 * Created by manikandan.selvaraju on 10/3/14.
 */
public enum ActivityName {

    WALK ("walk"),
    BICYCLE ("bicycle"),
    VEHICLE ("vehicle"),
    UNKNOWN ("unknown");
    String name;

    ActivityName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ActivityName getActivity(String name) {

        if (name.equals(WALK.getName())) {
            return WALK;
        } else if (name.equals(BICYCLE.getName())) {
            return BICYCLE;
        } else if (name.equals(VEHICLE.getName())) {
            return VEHICLE;
        }

        return UNKNOWN;
    }
}

package com.mani.activitylogger.database;

/**
 * Created by manikandan.selvaraju on 10/3/14.
 */
public class ActivitiesConstants {
    public static final String ACTIVITY_TABLE = "activities";
    public static final String LOCATION_TABLE = "location";
    public static final String ACTIVITY_NAME_TABLE = "activitiesName";

    public static class ACTIVITY {
        public static final String ACTIVITY_ID = "activity_id";
        public static final String START_LOCATION = "start_location";
        public static final String END_LOCATION = "end_location";
        public static final String START_TIME = "start_time";
        public static final String END_TIME = "end_time";
        public static final String ACTIVITY_TYPE = "activity_type";

        public static final String START_LATITUDE = "start_latitude";
        public static final String START_LONGITUDE = "start_longitude";
        public static final String START_ADDRESS = "start_address";
        public static final String END_LATITUDE = "end_latitude";
        public static final String END_LONGITUDE = "end_longitude";
        public static final String END_ADDRESS = "end_address";
    }

    public static class LOCATION {
        public static final String LOCATION_ID = "location_id";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String ADDRESS = "address";
    }

    public static class ACTIVITY_NAME {
        public static final String ACTIVITY_ID = "activity_id";
        public static final String ACTIVITY_NAME = "activity";

    }

}

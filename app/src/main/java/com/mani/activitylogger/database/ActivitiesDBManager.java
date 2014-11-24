package com.mani.activitylogger.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.mani.activitylogger.app.ActivitiesLoggerApplication;
import com.mani.activitylogger.model.ActivityLocation;
import com.mani.activitylogger.model.ActivityName;
import com.mani.activitylogger.model.UserActivity;
import com.mani.activitylogger.util.DateTimeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by manikandan.selvaraju on 10/3/14.
 */
public class ActivitiesDBManager {

    // SQLite DB handle for activities.db
    private static SQLiteDatabase activitiesDB;

    private static ActivitiesLoggerDatabase activitiesDBCreator;

    // Singleton instance
    private static ActivitiesDBManager instance;

    // Dictionary of activity table ids and ActivityNames
    private HashMap<Integer, ActivityName> activityIds = new HashMap<Integer, ActivityName>();

    public static ActivitiesDBManager getInstance() {
        // Double locking pattern in multi threading scenario
        if( instance == null ) {
            synchronized (ActivitiesDBManager.class) {
                if (instance == null) {
                    instance = new ActivitiesDBManager();
                }
            }
        }
        return instance;
    }

    public ActivitiesDBManager() {
        activitiesDBCreator = new ActivitiesLoggerDatabase(ActivitiesLoggerApplication.getContext());
        open();
    }

    public void open() throws SQLException {
        activitiesDB = activitiesDBCreator.getWritableDatabase();
        fillActivityTable();
    }

    public static void release() {
        close();
    }

    public static void close() {
        if( activitiesDB != null) {
            activitiesDB.close();
            activitiesDB = null;
        }
        if(activitiesDBCreator != null) {
            activitiesDBCreator.close();
            activitiesDBCreator = null;
        }
    }

    private void fillActivityTable() {
        Cursor cursor = activitiesDB.query(ActivitiesConstants.ACTIVITY_NAME_TABLE,
                null,null,null, null, null, null);
        if(cursor != null && cursor.moveToFirst() == false ) {
            ContentValues values = new ContentValues();
            for(ActivityName activity: ActivityName.values()) {
                values.put(ActivitiesConstants.ACTIVITY_NAME.ACTIVITY_NAME, activity.getName());
                long id = activitiesDB.insert(ActivitiesConstants.ACTIVITY_NAME_TABLE, null, values);
                // Fill the dictionary
                activityIds.put((int) id, activity);
            }
        } else if(cursor.getCount() > 0) {
            //Fill the activityIds dictionary.
            activityIds.clear();
            do {
                String activity = cursor.getString(cursor.getColumnIndex(ActivitiesConstants.ACTIVITY_NAME.ACTIVITY_NAME));
                long id  = cursor.getLong(cursor.getColumnIndex(ActivitiesConstants.ACTIVITY_NAME.ACTIVITY_ID));
                activityIds.put((int) id, ActivityName.getActivity(activity));
            } while (cursor.moveToNext());
        }
    }

    private int getActivityId(ActivityName activity) {
        Set<Integer> keys = activityIds.keySet();
        int unknownId = 0;
        for(Integer key: keys) {
            ActivityName value = activityIds.get(key);
            if (activity == value) {
                return key.intValue();
            } else if ( activity == ActivityName.UNKNOWN) {
                //If there is not match set the unknown.
                unknownId = key.intValue();
            }
        }
        return unknownId;
    }

    public long addActivity(ActivityName activity) throws SQLException {
        long insertId = 0;
        ContentValues values = new ContentValues();
        values.put(ActivitiesConstants.ACTIVITY.START_TIME, System.currentTimeMillis()/1000);
        values.put(ActivitiesConstants.ACTIVITY.ACTIVITY_TYPE, getActivityId(activity));

        try {
            insertId = activitiesDB.insertOrThrow(ActivitiesConstants.ACTIVITY_TABLE, null,values);
        } catch (SQLException ex) {
            throw ex;
        }
        return insertId;
    }

    public long endActivity(long activityId, long endTime) throws SQLException {
        String WHERE = ActivitiesConstants.ACTIVITY.ACTIVITY_ID+"=?";
        String args[] = { Long.toString(activityId)};

        long insertId = 0;
        ContentValues values = new ContentValues();
        values.put(ActivitiesConstants.ACTIVITY.END_TIME,endTime);

        try {
            insertId = activitiesDB.update(ActivitiesConstants.ACTIVITY_TABLE, values, WHERE, args);
        } catch (SQLException ex) {
            throw ex;
        }

        return insertId;
    }

    public long updateTripStartLocation(long activityId, long startLocationId) throws SQLException {
        String WHERE = ActivitiesConstants.ACTIVITY.ACTIVITY_ID+"=?";
        String args[] = { Long.toString(activityId)};

        long insertId = 0;
        ContentValues values = new ContentValues();
        values.put(ActivitiesConstants.ACTIVITY.START_LOCATION, startLocationId);

        try {
            insertId = activitiesDB.update(ActivitiesConstants.ACTIVITY_TABLE, values, WHERE, args);
        } catch (SQLException ex) {
            throw ex;
        }

        return insertId;
    }

    public long updateTripEndLocation(long activityId, long endLocationId) throws SQLException {
        String WHERE = ActivitiesConstants.ACTIVITY.ACTIVITY_ID+"=?";
        String args[] = { Long.toString(activityId)};

        long insertId = 0;
        ContentValues values = new ContentValues();
        values.put(ActivitiesConstants.ACTIVITY.END_LOCATION, endLocationId);

        try {
            insertId = activitiesDB.update(ActivitiesConstants.ACTIVITY_TABLE, values, WHERE, args);
        } catch (SQLException ex) {
            throw ex;
        }

        return insertId;
    }

    public boolean isActivityStartLocationSet(long activityId) {
        String WHERE = ActivitiesConstants.ACTIVITY.ACTIVITY_ID + "=?";
        String args[] = {Long.toString(activityId)};

        Cursor cursor = activitiesDB.query(ActivitiesConstants.ACTIVITY_TABLE,
                null, WHERE, args, null, null, null);

        boolean startLocationSet = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                long activityStartLocationId = cursor.getLong(cursor.getColumnIndex(ActivitiesConstants.ACTIVITY.START_LOCATION));
                startLocationSet = (activityStartLocationId <= 0) ? false: true;
            }
        }

        return startLocationSet;
    }

    // Deleting a activity also deletes on references table.
    public long deleteActivity(long activityId) throws SQLException {

        String WHERE = ActivitiesConstants.ACTIVITY.ACTIVITY_ID + "=?";
        String args[] = {Long.toString(activityId)};

        long insertId = 0;
        try{
            insertId = activitiesDB.delete(ActivitiesConstants.ACTIVITY_TABLE, WHERE, args);

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw ex;
        }
        return insertId;
    }

    public ActivityName getActivity(long activityId) {
        String WHERE = ActivitiesConstants.ACTIVITY.ACTIVITY_ID + "=?";
        String args[] = { Long.toString(activityId)};

        Cursor cursor = activitiesDB.query(ActivitiesConstants.ACTIVITY_TABLE,
                null, WHERE, args, null, null, null);

        ActivityName activity = ActivityName.UNKNOWN;
        if (cursor != null ) {
            if (cursor.moveToFirst()) {
                activity = activityIds.get(cursor.getInt(
                        cursor.getColumnIndex(ActivitiesConstants.ACTIVITY.ACTIVITY_TYPE)));
            }
        }

        if ( cursor != null) {
            cursor.close();
        }
        return activity;
    }

    /*
     * Add a new location with ( lat,long).
     */
    public long addLocation(double latitude, double longitude) throws SQLException {
        long insertId = 0;
        ContentValues values = new ContentValues();
        values.put(ActivitiesConstants.LOCATION.LATITUDE, latitude);
        values.put(ActivitiesConstants.LOCATION.LONGITUDE, longitude);

        try {
            insertId = activitiesDB.insertOrThrow(ActivitiesConstants.LOCATION_TABLE, null,values);
        } catch (SQLException ex) {
            throw ex;
        }
        return insertId;
    }

    /**
     * Update the location address.
     * @param locationId
     * @param address
     * @return
     * @throws SQLException
     */
    public long updateLocationAddress(long locationId, String address) throws SQLException {
        long insertId = 0;

        String WHERE = ActivitiesConstants.LOCATION.LOCATION_ID+"=?";
        String args[] = { Long.toString(locationId)};

        ContentValues values = new ContentValues();
        values.put(ActivitiesConstants.LOCATION.ADDRESS, address);

        try {
            insertId = activitiesDB.update(ActivitiesConstants.LOCATION_TABLE, values, WHERE, args);
        } catch (SQLException ex) {
            throw ex;
        }
        return insertId;
    }

    public List<UserActivity> getActivities() {
        return getActivities(0, System.currentTimeMillis()/1000);
    }

    /**
     * Get all activity between start time and end time.
     * Uses left join on location table to get start location ( lat,long, address) and
     * end location information.
     *
     * @param startTime
     * @param endTime
     * @return
     *
     * Actual query for reference:
     *
     * select t.activity_id, t.start_time, t.end_time, t.activity_type, l1.latitude as start_latitude,
     * l1.longitude as start_longitude, l1.address as start_address, l2.latitude as end_latitude,
     * l2.longitude as end_longitude, l2.address as end_address from activities as t
     * left join location as l1 on t.start_location = l1.location_id
     * left join location as l2 on t.end_location = l2.location_id
     * where t.start_time between 1412459824 and 0 and t.end_time > 0
     */
    public List<UserActivity> getActivities(long startTime, long endTime) {
        List<UserActivity> activitiesList = new ArrayList<UserActivity>();
        Map<String,Integer> headerMap = new HashMap<String,Integer>();
        int headerId = 0;

        String rawQuery = "select t."+ ActivitiesConstants.ACTIVITY.ACTIVITY_ID+", "+
                "t."+ ActivitiesConstants.ACTIVITY.START_TIME+", "+
                "t."+ ActivitiesConstants.ACTIVITY.END_TIME+", "+
                "t."+ ActivitiesConstants.ACTIVITY.ACTIVITY_TYPE+", "+

                "l1."+ ActivitiesConstants.LOCATION.LATITUDE+" as "+
                    ActivitiesConstants.ACTIVITY.START_LATITUDE+", "+
                "l1."+ ActivitiesConstants.LOCATION.LONGITUDE+" as "+
                    ActivitiesConstants.ACTIVITY.START_LONGITUDE+", "+
                "l1."+ ActivitiesConstants.LOCATION.ADDRESS+" as "+
                    ActivitiesConstants.ACTIVITY.START_ADDRESS+", "+

                "l2."+ ActivitiesConstants.LOCATION.LATITUDE+" as "+
                    ActivitiesConstants.ACTIVITY.END_LATITUDE+", "+
                "l2."+ ActivitiesConstants.LOCATION.LONGITUDE+" as "+
                    ActivitiesConstants.ACTIVITY.END_LONGITUDE+", "+
                "l2."+ ActivitiesConstants.LOCATION.ADDRESS+" as "+
                    ActivitiesConstants.ACTIVITY.END_ADDRESS+

                " from "+ ActivitiesConstants.ACTIVITY_TABLE +" as t " +

                "left join "+ ActivitiesConstants.LOCATION_TABLE +" as l1 on t."+
                ActivitiesConstants.ACTIVITY.START_LOCATION+"=l1."+ ActivitiesConstants.LOCATION.LOCATION_ID +

                " left join "+ ActivitiesConstants.LOCATION_TABLE +" as l2 on t."+
                ActivitiesConstants.ACTIVITY.END_LOCATION+"=l2."+ ActivitiesConstants.LOCATION.LOCATION_ID +

                " where t."+ ActivitiesConstants.ACTIVITY.START_TIME+ " > "+ startTime+" and "+
                "t."+ ActivitiesConstants.ACTIVITY.START_TIME+" < "+ endTime +
                " and t."+ ActivitiesConstants.ACTIVITY.END_TIME+" > 0";

        Cursor cursor = activitiesDB.rawQuery(rawQuery, null);

        if (cursor != null ) {
            if  (cursor.moveToFirst()) {
                do {

                    UserActivity userActivity = new UserActivity();
                    long id = cursor.getLong(cursor.getColumnIndex(ActivitiesConstants.ACTIVITY.ACTIVITY_ID));
                    long activityStartTime = cursor.getLong(cursor.getColumnIndex(ActivitiesConstants.ACTIVITY.START_TIME));
                    long activityEndTime = cursor.getLong(cursor.getColumnIndex(ActivitiesConstants.ACTIVITY.END_TIME));
                    userActivity.setId(id);
                    userActivity.setStartTime(activityStartTime);
                    userActivity.setEndTime(activityEndTime);

                    ActivityLocation startLocation = new ActivityLocation();
                    String address = cursor.getString(cursor.getColumnIndex(ActivitiesConstants.ACTIVITY.START_ADDRESS));
                    double latitude = cursor.getDouble(cursor.getColumnIndex(ActivitiesConstants.ACTIVITY.START_LATITUDE));
                    double longitude = cursor.getDouble(cursor.getColumnIndex(ActivitiesConstants.ACTIVITY.START_LONGITUDE));
                    startLocation.setAddress(address);
                    startLocation.setLatitude(latitude);
                    startLocation.setLongitude(longitude);
                    userActivity.setStartLocation(startLocation);

                    ActivityLocation endLocation = new ActivityLocation();
                    address = cursor.getString(cursor.getColumnIndex(ActivitiesConstants.ACTIVITY.END_ADDRESS));
                    latitude = cursor.getDouble(cursor.getColumnIndex(ActivitiesConstants.ACTIVITY.END_LATITUDE));
                    longitude = cursor.getDouble(cursor.getColumnIndex(ActivitiesConstants.ACTIVITY.END_LONGITUDE));
                    endLocation.setAddress(address);
                    endLocation.setLatitude(latitude);
                    endLocation.setLongitude(longitude);
                    userActivity.setEndLocation(endLocation);

                    int activity = cursor.getInt(cursor.getColumnIndex(ActivitiesConstants.ACTIVITY.ACTIVITY_TYPE));
                    userActivity.setActivity(activityIds.get(activity));

                    //Set the header for this activity.
                    userActivity.setHeaderTxt(DateTimeUtil.getActivityHeaderText(userActivity.getStartTime()));
                    if( !headerMap.containsKey(userActivity.getHeaderTxt())) {
                        headerMap.put(userActivity.getHeaderTxt(), Integer.valueOf(headerId));
                        userActivity.setHeaderId(headerId);
                        headerId++;
                    } else {
                        Integer headerIdInteger = headerMap.get(userActivity.getHeaderTxt());
                        userActivity.setHeaderId(headerIdInteger.intValue());
                    }

                    activitiesList.add(userActivity);
                } while (cursor.moveToNext());
            }
        }

        if(cursor != null)
            cursor.close();


        return activitiesList;
    }

}

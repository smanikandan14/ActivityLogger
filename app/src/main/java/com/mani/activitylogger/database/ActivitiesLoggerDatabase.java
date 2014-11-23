package com.mani.activitylogger.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by manikandan.selvaraju on 10/3/14.
 */
public class ActivitiesLoggerDatabase extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "trips.db";

    public ActivitiesLoggerDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table if not exists " + ActivitiesConstants.TRIP_TABLE +
                "(" + ActivitiesConstants.TRIP.TRIP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ActivitiesConstants.TRIP.START_LOCATION + " INTEGER," +
                ActivitiesConstants.TRIP.END_LOCATION + " INTEGER," +
                ActivitiesConstants.TRIP.START_TIME + " INTEGER," +
                ActivitiesConstants.TRIP.END_TIME + " INTEGER, " +
                ActivitiesConstants.TRIP.ACTIVITY_TYPE + " INTEGER, " +
                "FOREIGN KEY ("+ ActivitiesConstants.TRIP.ACTIVITY_TYPE+") REFERENCES "+
                    ActivitiesConstants.ACTIVITY_TABLE +"("+ ActivitiesConstants.ACTIVITY.ACTIVITY_ID +") ON DELETE CASCADE,"+
                "FOREIGN KEY ("+ ActivitiesConstants.TRIP.START_LOCATION+") REFERENCES "+
                    ActivitiesConstants.LOCATION_TABLE +"("+ ActivitiesConstants.LOCATION.LOCATION_ID +") ON DELETE CASCADE,"+
                "FOREIGN KEY ("+ ActivitiesConstants.TRIP.END_LOCATION+") REFERENCES "+
                    ActivitiesConstants.LOCATION_TABLE +"("+ ActivitiesConstants.LOCATION.LOCATION_ID +") ON DELETE CASCADE "+
                ");");

        db.execSQL("Create table if not exists " + ActivitiesConstants.LOCATION_TABLE +
                "(" + ActivitiesConstants.LOCATION.LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ActivitiesConstants.LOCATION.LATITUDE + " FLOAT NOT NULL," +
                ActivitiesConstants.LOCATION.LONGITUDE + " FLOAT NOT NULL," +
                ActivitiesConstants.LOCATION.ADDRESS + " TEXT" +
                ");");

        db.execSQL("Create table if not exists " + ActivitiesConstants.ACTIVITY_TABLE +
                "(" + ActivitiesConstants.ACTIVITY.ACTIVITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ActivitiesConstants.ACTIVITY.ACTIVITY_NAME + " TEXT" +
                ");");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // [NOT TAKEN CARE for the DEMO]
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + ActivitiesConstants.TRIP_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ActivitiesConstants.LOCATION_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ActivitiesConstants.ACTIVITY_TABLE);
    }

}

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
    private static final String DATABASE_NAME = "activities.db";

    public ActivitiesLoggerDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table if not exists " + ActivitiesConstants.ACTIVITY_TABLE +
                "(" + ActivitiesConstants.ACTIVITY.ACTIVITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ActivitiesConstants.ACTIVITY.START_LOCATION + " INTEGER," +
                ActivitiesConstants.ACTIVITY.END_LOCATION + " INTEGER," +
                ActivitiesConstants.ACTIVITY.START_TIME + " INTEGER," +
                ActivitiesConstants.ACTIVITY.END_TIME + " INTEGER, " +
                ActivitiesConstants.ACTIVITY.ACTIVITY_TYPE + " INTEGER, " +
                "FOREIGN KEY ("+ ActivitiesConstants.ACTIVITY.ACTIVITY_TYPE+") REFERENCES "+
                    ActivitiesConstants.ACTIVITY_TABLE +"("+ ActivitiesConstants.ACTIVITY_NAME.ACTIVITY_ID +") ON DELETE CASCADE,"+
                "FOREIGN KEY ("+ ActivitiesConstants.ACTIVITY.START_LOCATION+") REFERENCES "+
                    ActivitiesConstants.LOCATION_TABLE +"("+ ActivitiesConstants.LOCATION.LOCATION_ID +") ON DELETE CASCADE,"+
                "FOREIGN KEY ("+ ActivitiesConstants.ACTIVITY.END_LOCATION+") REFERENCES "+
                    ActivitiesConstants.LOCATION_TABLE +"("+ ActivitiesConstants.LOCATION.LOCATION_ID +") ON DELETE CASCADE "+
                ");");

        db.execSQL("Create table if not exists " + ActivitiesConstants.LOCATION_TABLE +
                "(" + ActivitiesConstants.LOCATION.LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ActivitiesConstants.LOCATION.LATITUDE + " FLOAT NOT NULL," +
                ActivitiesConstants.LOCATION.LONGITUDE + " FLOAT NOT NULL," +
                ActivitiesConstants.LOCATION.ADDRESS + " TEXT" +
                ");");

        db.execSQL("Create table if not exists " + ActivitiesConstants.ACTIVITY_NAME_TABLE +
                "(" + ActivitiesConstants.ACTIVITY_NAME.ACTIVITY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ActivitiesConstants.ACTIVITY_NAME.ACTIVITY_NAME + " TEXT" +
                ");");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // [NOT TAKEN CARE for the DEMO]
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + ActivitiesConstants.ACTIVITY_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ActivitiesConstants.LOCATION_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ActivitiesConstants.ACTIVITY_TABLE);
    }

}

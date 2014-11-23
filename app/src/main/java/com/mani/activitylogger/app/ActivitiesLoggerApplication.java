package com.mani.activitylogger.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mani.activitylogger.util.ActivityLoggerPreferenceManager;
import com.mani.activitylogger.util.MessageUtil;

/**
 * Created by manikandan.selvaraju on 10/3/14.
 */
public class ActivitiesLoggerApplication extends Application {

    private static Context sApplicationContext;

    public void onCreate() {
        super.onCreate();
        sApplicationContext = this.getApplicationContext();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(sApplicationContext);
        ActivityLoggerPreferenceManager.initializePreferenceManager(sharedPreferences);
        MessageUtil.initializeMessageUtil(sApplicationContext);
    }

    public static Context getContext() {
        return sApplicationContext;
    }


}

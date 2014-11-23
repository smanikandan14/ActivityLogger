package com.mani.activitylogger.util;

import android.content.SharedPreferences;

/**
 * Created by manikandan.selvaraju on 10/3/14.
 */
public class ActivityLoggerPreferenceManager {

    private static SharedPreferences sharedPreferences;

    public static void initializePreferenceManager(SharedPreferences preferences) {
        sharedPreferences = preferences;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public static void setBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static Long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    public static void setLong(String key, long value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }
}

package com.mani.activitylogger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.mani.activitylogger.loader.ActivitiesLoader;

/**
 * Created by maniselvaraj on 4/10/14.
 */
public class NewActivityReceiver extends BroadcastReceiver {

    public static final String ACTION_NEW_TRIP = "action.NEW_TRIP";
    private ActivitiesLoader mTripsLoader;

    public NewActivityReceiver(ActivitiesLoader loader) {
        mTripsLoader = loader;
        IntentFilter filter = new IntentFilter(ACTION_NEW_TRIP);
        LocalBroadcastManager.getInstance(mTripsLoader.getContext()).registerReceiver(this, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mTripsLoader.onContentChanged();
    }

}

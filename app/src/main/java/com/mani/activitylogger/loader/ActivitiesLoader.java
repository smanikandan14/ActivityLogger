package com.mani.activitylogger.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.mani.activitylogger.database.ActivitiesDBManager;
import com.mani.activitylogger.model.UserActivity;
import com.mani.activitylogger.receiver.NewActivityReceiver;

import java.util.List;

/**
 * Load activities from database asynchronously in a background thread.
 * Helps avoid UI from being blocked while loading data from database.
 *
 * Created by manikandan.selvaraju on 9/30/14.
 */
public class ActivitiesLoader extends AsyncTaskLoader<List<UserActivity>> {

    /** List of Trips **/
    List<UserActivity> mUserActivities;

    // The observer to notify the Loader when there is a new activity detected.
    private NewActivityReceiver mNewActivityReceiver;

    private final String TAG = "ActivitesLoader";

    public ActivitiesLoader(Context context) {
        super(context);
    }

    @Override
    public List<UserActivity> loadInBackground() {
        Log.d(TAG, "loadInBackground()");
        mUserActivities = ActivitiesDBManager.getInstance().getActivities();
        return mUserActivities;
    }

    @Override
    public void forceLoad() {
        super.forceLoad();
        Log.d(TAG, "forceLoad()");
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        Log.d(TAG, "onStartLoading()");

        if(mUserActivities != null) {
            deliverResult(mUserActivities);
        }

        if (mNewActivityReceiver == null) {
            mNewActivityReceiver = new NewActivityReceiver(this);
        }

        if (takeContentChanged() || mUserActivities == null) {
            // When the new activity receiver receives a broadcast, calls
            // onContentChanged() on the Loader, which will cause the next call to
            // takeContentChanged() to return true. So force a new load.
            Log.d(TAG, "A content change has been detected, so force load!");
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        Log.d(TAG, "onStopLoading()");
        cancelLoad();
    }

    @Override
    protected void onReset() {
        Log.d(TAG, "onReset()");
        onStopLoading();

        // The Loader is being reset, so we should stop listening for new activity broadcast.
        if (mNewActivityReceiver != null) {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mNewActivityReceiver);
            mNewActivityReceiver = null;
        }
    }

    @Override
    public void onCanceled(List<UserActivity> apps) {
        // Attempt to cancel the current asynchronous load.
        super.onCanceled(apps);
        Log.d(TAG, "onCanceled()");
    }
}

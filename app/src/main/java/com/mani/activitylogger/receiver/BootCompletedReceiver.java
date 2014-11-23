package com.mani.activitylogger.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mani.activitylogger.service.ActivitiesLoggerService;

/**
 * Created by manikandan.selvaraju on 10/3/14.
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //Start the service.
        context.startService(new Intent(context, ActivitiesLoggerService.class));
    }
}

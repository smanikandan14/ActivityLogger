package com.mani.activitylogger.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.mani.activitylogger.R;


public class MainActivity extends ActionBarActivity {

    public final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private final static String FRAGMENT_TAG = "trip_logger_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // If fragment is not saved by system, then create a one. Else re-use.
        if (getFragmentManager().findFragmentByTag(FRAGMENT_TAG) == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ActivitiesLoggerFragment(), FRAGMENT_TAG)
                    .commit();
        }

        // Set the toolbar as ActionBar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
    }
}

package com.mani.activitylogger.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.mani.activitylogger.R;
import com.mani.activitylogger.adapter.ActivitiesListAdapter;
import com.mani.activitylogger.app.ActivitiesLoggerApplication;
import com.mani.activitylogger.loader.ActivitiesLoader;
import com.mani.activitylogger.model.UserActivity;
import com.mani.activitylogger.service.ActivitiesLoggerService;
import com.mani.activitylogger.util.ActivityLoggerPreferenceManager;
import com.mani.activitylogger.util.FontProvider;
import com.mani.activitylogger.util.PreferenceKeys;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by manikandan.selvaraju on 10/3/14.
 */
public class ActivitiesLoggerFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<UserActivity>> {

    // id specific to the ListFragment's LoaderManager
    private static final int LOADER_ID = 1;

    // ListView Adapter
    private ActivitiesListAdapter mAdapter;

    // List view to show trips
    private StickyListHeadersListView mTripsListView;

    // Switch to turn on/off activity tracking
    private Switch mTripLoggingSwitch;

    // Tells if play services and location services are enabled for
    // the app to function.
    boolean mIsPlayServicesAndLocationProvidersEnabled = false;

    AlertDialog mEnableLocationServicesDialog = null;

    private final String TAG = "TripLoggerFragment";

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        private Dialog mDialog;

        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mAdapter = new ActivitiesListAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activities_logger,container,false);

        ((TextView) view.findViewById(R.id.tripLoggingTxt)).
                setTypeface(FontProvider.getMediumFont());

        mTripsListView = (StickyListHeadersListView) view.findViewById(R.id.tripsList);

        //Set up empty view
        setUpTripListView(view);

        //Switch
        mTripLoggingSwitch = (Switch) view.findViewById(R.id.tripLogSwitch);
        mTripLoggingSwitch.setSwitchTextAppearance(getActivity(), R.style.SwitchTextAppearance);

        mTripLoggingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                ActivityLoggerPreferenceManager.setBoolean(
                        PreferenceKeys.TRIP_LOG_SWITCH_CHANGED_BY_USER, true);

                if( mIsPlayServicesAndLocationProvidersEnabled ) {
                    if (isChecked) {
                        ActivityLoggerPreferenceManager.setBoolean(
                                PreferenceKeys.TRIP_LOG_SWITCH_STATE, true);

                        //Start the service.
                        ActivitiesLoggerFragment.this.getActivity().
                                startService(new Intent(ActivitiesLoggerFragment.this.getActivity(), ActivitiesLoggerService.class));

                    } else {
                        ActivityLoggerPreferenceManager.setBoolean(
                                PreferenceKeys.TRIP_LOG_SWITCH_STATE, false);
                        //Stop the service.
                        ActivitiesLoggerFragment.this.getActivity().
                                stopService(new Intent(ActivitiesLoggerFragment.this.getActivity(), ActivitiesLoggerService.class));
                    }
                }
            }
        });

        //Initialize a Loader with id '1'.
        getLoaderManager().initLoader(LOADER_ID, null, this);

        return view;
    }

    public void onResume() {
        super.onResume();
        boolean playStoreAvailable = servicesConnected();

        //Check for GPS or network location provider enabled.
        LocationManager locationManager = (LocationManager)
                ActivitiesLoggerApplication.getContext().getSystemService(Context.LOCATION_SERVICE);

        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if( playStoreAvailable && !gpsEnabled && !networkProviderEnabled ) {
            getEnableLocationServicesAlertDialog().dismiss();
            getEnableLocationServicesAlertDialog().show();

            // Disable the switch
            mTripLoggingSwitch.setEnabled(false);

            //Indicate trip logging is switched off
            mTripLoggingSwitch.setChecked(false);

            // Stop the service.
            ActivitiesLoggerFragment.this.getActivity().
                    startService(new Intent(ActivitiesLoggerFragment.this.getActivity(), ActivitiesLoggerService.class));


        } else {

            mTripLoggingSwitch.setEnabled(true);

            mIsPlayServicesAndLocationProvidersEnabled = true;
            boolean isSwitchChangedByUser = ActivityLoggerPreferenceManager.getBoolean(
                    PreferenceKeys.TRIP_LOG_SWITCH_CHANGED_BY_USER, false);

            boolean switchState = true;

            if(isSwitchChangedByUser) {
                switchState = ActivityLoggerPreferenceManager.getBoolean(
                        PreferenceKeys.TRIP_LOG_SWITCH_STATE, true);
            }

            if (switchState) {
                //Start the service.
                ActivitiesLoggerFragment.this.getActivity().
                        startService(new Intent(ActivitiesLoggerFragment.this.getActivity(), ActivitiesLoggerService.class));

            }

            mTripLoggingSwitch.setChecked(switchState);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private AlertDialog getEnableLocationServicesAlertDialog() {
        if ( mEnableLocationServicesDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
            builder.setTitle(getResources().getString(R.string.alert_location_services_not_active));
            builder.setMessage(getResources().getString(R.string.alert_location_services_message));
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            mEnableLocationServicesDialog = builder.create();
            mEnableLocationServicesDialog.setCanceledOnTouchOutside(true);
        }
        return mEnableLocationServicesDialog;
    }

    private void setUpTripListView(View view) {
        TextView emptyListView = (TextView) view.findViewById(R.id.emptyListView);
        emptyListView.setTypeface(FontProvider.getNormalFont());
        mTripsListView.setEmptyView(emptyListView);
        mTripsListView.setAdapter(mAdapter);
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this.getActivity());
        // If Google Play services is available
        if ((ConnectionResult.SUCCESS == resultCode)) {
            // In debug mode, log the status
            Log.d(TAG, "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason.
            // resultCode holds the error code.
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this.getActivity(),
                    MainActivity.CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getFragmentManager(),
                        "TripLogger");
            }
            return false;
        }
    }

    @Override
    public Loader<List<UserActivity>> onCreateLoader(int i, Bundle bundle) {
        return new ActivitiesLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<UserActivity>> listLoader, List<UserActivity> userActivityList) {
        //Loading progress will be shown until at least one trip is obtained.
        if( userActivityList != null && userActivityList.size() > 0) {
            mAdapter.setTrips(userActivityList);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<UserActivity>> listLoader) {
        mAdapter.setTrips(null);
    }

}

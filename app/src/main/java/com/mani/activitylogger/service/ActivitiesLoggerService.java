package com.mani.activitylogger.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.mani.activitylogger.database.ActivitiesDBManager;
import com.mani.activitylogger.model.DetectedActivity;
import com.mani.activitylogger.receiver.NewActivityReceiver;
import com.mani.activitylogger.util.ActivityLoggerPreferenceManager;
import com.mani.activitylogger.util.PreferenceKeys;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Service responsible for listening to user's activity in background using ActivityRecognitionClient.
 * This is a non-dying service ( i.e sticky service). Even if the service is killed by OS
 * to reclaim memory, the service would be restarted by system again since it is sticky.
 *
 * - Broadcasts new trip available message for UI to refresh whenever a trip is ended.
 *
 * - There is one handler thread created and is available as long as this service is running.
 *   It is primarily used for reverse geo coding purpose.
 *
 * - Updates the current trip Id into preference whenever a trip is started and sets it to 0
 *   when trip is ended.
 *
 * - If the service is killed in between a trip, services reads from preference when starting and
 *   continue recording the trip.
 *
 * - A 30 Sec CountDownTimer is used to identify if the user is in STILL state. The timer is started
 *   when STILL activity is identified during a trip.
 *
 * Created by manikandan.selvaraju on 10/3/14.
 */
public class ActivitiesLoggerService extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    // Activity recognition interval in milli seconds
    private final static long ACTIVITY_RECOGNITION_DETECTION_INTERVAL
            = 5 * DateUtils.SECOND_IN_MILLIS; //5sec

    // Update frequency in milliseconds
    private static final long LOCATION_UPDATE_INTERVAL = 1 * DateUtils.SECOND_IN_MILLIS; //5sec

    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = 1 * DateUtils.SECOND_IN_MILLIS ; // 1sec

    // Still state interval in seconds
    public static final long STILL_STATE_INTERVAL = 30 * DateUtils.SECOND_IN_MILLIS; // 30 Sec

    // In vehicle still state interval in seconds
    public static final long IN_VEHICLE_STILL_STATE_INTERVAL =
            2 * 60 * DateUtils.SECOND_IN_MILLIS; // 2 min

    // Minimum accuracy distance required to accept a location
    private int MINIMUM_ACCURACY_DISTANCE = 100; //In meters

    // Defines the minimum age of last location obtained.
    private final int TIME_SINCE_LAST_LOCATION = 5 * 60 * 1000; // 15 minutes

    private final String TAG = "TripLoggerService";

    // Stores the instantiation of the location client
    private LocationClient mLocationClient;

    // Stores the instantiation of the activity recognition client
    private ActivityRecognitionClient mActivityRecognitionClient;

    // Stores the instantiation of the location request
    private LocationRequest mLocationRequest;

    // Tells if play services available
    private boolean mPlayServicesAvailable = false;

    // Indicates if STILL_STATE timer is running
    private boolean mTimerStarted = false;

    // Holds the activity of the current trip.
    private DetectedActivity mCurrentDetectedActivity;

    // Holds the current trip Id
    private long mCurrentTripId = 0;

    // Timer used to identify STILL_STATE of user
    private StillStateTimer mStillStateTimer;

    // Timer used to identify IN Vehicle STILL_STATE of user
    private StillStateTimer mInVehicleStillStateTimer;

    // Pending intent used to register with activity recognition client
    private PendingIntent mActivityRecognitionPendingIntent;

    // Worker thread used to carry out heavy lifting operation
    private static HandlerThread mHandlerThread;

    // Handler for the worker thread
    private static Handler mHandler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        mHandlerThread = new HandlerThread("TripLoggerServiceWorkerThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create();

        mPlayServicesAvailable = servicesConnected();

        mLocationClient = new LocationClient(this, this, this);

        mActivityRecognitionClient = new ActivityRecognitionClient(this, this, this);

        mStillStateTimer = new StillStateTimer(STILL_STATE_INTERVAL, 1000L);

        mInVehicleStillStateTimer = new StillStateTimer(STILL_STATE_INTERVAL, 1000L);

        mCurrentDetectedActivity = DetectedActivity.UNKNOWN;

        checkForTripIdFromPreference();
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if there was a previous trip user was carrying out when the services is killed and
     * restarted.
     */
    private void checkForTripIdFromPreference() {
        mCurrentTripId = ActivityLoggerPreferenceManager.getLong(PreferenceKeys.CURRENT_TRIP_ID, 0);
        if ( mCurrentTripId > 0) {
            boolean isStartLocationSet = ActivitiesDBManager.getInstance().
                    isTripStartLocationSet(mCurrentTripId);

            if(isStartLocationSet == false) {
                //If start location is not set, then delete the trip.
                ActivitiesDBManager.getInstance().deleteTrip(mCurrentTripId);
                mCurrentTripId = 0;
            }  else {
                //Set the previous current activity when service died.
                mCurrentDetectedActivity = ActivitiesDBManager.getInstance().getTripActivity(mCurrentTripId);
            }
        }
    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // Handle if any activity recognition is sent.
        if( mLocationClient.isConnected() == true && ActivityRecognitionResult.hasResult(intent)) {
            final ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            // Run the activity detection logic in worker thread.
            mHandler.post( new Runnable() {
                public void run() {
                    handleActivityRecognition(result);
                }
            });
        }

        // If play services is not available ( or)
        // Both locationClient and ActivityRecognitionClient is connected
        // then just return.
        if( mPlayServicesAvailable == false ||
                ( mLocationClient.isConnected() &&
                        mActivityRecognitionClient.isConnected()) ) {
            return START_STICKY;
        }

        // Make sure mActivityRecognitionClient is not connected or
        // there is no other mActivityRecognitionClient in connecting
        if( mActivityRecognitionClient.isConnected() == false ||
                mActivityRecognitionClient.isConnecting() == false) {
            mActivityRecognitionClient.connect();
        }

        // Make sure locationClient is not connected or
        // there is no other mActivityRecognitionClient in connecting
        if( mLocationClient.isConnected() == false ||
                mLocationClient.isConnecting() == false) {
            mLocationClient.connect();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service destroyed");
        // Remove the activity recognition updates.
        if (mPlayServicesAvailable && mActivityRecognitionClient !=null){
            mActivityRecognitionClient.removeActivityUpdates(mActivityRecognitionPendingIntent);
            mActivityRecognitionClient.disconnect();
        }

        if (mPlayServicesAvailable && mLocationClient !=null){
            mLocationClient.disconnect();
        }
        super.onDestroy();
    }

    /**
     * Request for activity recognition updates with pending intent set to
     * this service. So that this service receives periodical activity
     * through 'onStartCommand(Intent intent)'
     */
    public void registerActivityUpdates() {
        Intent intent = new Intent(this, ActivitiesLoggerService.class);
        mActivityRecognitionPendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mActivityRecognitionClient.requestActivityUpdates(ACTIVITY_RECOGNITION_DETECTION_INTERVAL,
                mActivityRecognitionPendingIntent);
    }

    /**
     * Converts Activity of user to TripActivity.
     * @param type
     * @return
     */
    private DetectedActivity getActivity(int type) {
         if(type == com.google.android.gms.location.DetectedActivity.IN_VEHICLE)
            return DetectedActivity.VEHICLE;
        else if(type == com.google.android.gms.location.DetectedActivity.ON_BICYCLE)
            return DetectedActivity.BICYCLE;
        else if(type == com.google.android.gms.location.DetectedActivity.ON_FOOT)
            return DetectedActivity.WALK;
        else
             return DetectedActivity.UNKNOWN;
    }

    /**
     * Used for Logging purpose.
     * DetectedActivity to text.
     */
    private String getNameForActivity(int type){
        if(type == com.google.android.gms.location.DetectedActivity.UNKNOWN)
            return "Unknown";
        else if(type == com.google.android.gms.location.DetectedActivity.IN_VEHICLE)
            return "In Vehicle";
        else if(type == com.google.android.gms.location.DetectedActivity.ON_BICYCLE)
            return "On Bicycle";
        else if(type == com.google.android.gms.location.DetectedActivity.ON_FOOT)
            return "On Foot";
        else if(type == com.google.android.gms.location.DetectedActivity.STILL)
            return "Still";
        else if(type == com.google.android.gms.location.DetectedActivity.TILTING)
            return "Tilting";
        else
            return "";
    }

    /**
     *
     * @param result
     */
    private void handleActivityRecognition(ActivityRecognitionResult result) {

        int detectedActivity = result.getMostProbableActivity().getType();
        int detectedActivityConfidence = result.getMostProbableActivity().getConfidence();

        Log.d(TAG, "handleActivityRecognition Activity Detected "+
                getNameForActivity(detectedActivity)+", Confidence "+detectedActivityConfidence);

        List<com.google.android.gms.location.DetectedActivity> activities = result.getProbableActivities();

        int footConfidence = 0;
        int bicycleConfidence = 0;
        int unknownConfidence = 0;
        int vehicleConfidence = 0;

        //For logging
        StringBuffer probableActivitiesString = new StringBuffer();
        for (com.google.android.gms.location.DetectedActivity activity : activities) {
            if (activity.getType() == com.google.android.gms.location.DetectedActivity.ON_BICYCLE) {
                bicycleConfidence = activity.getConfidence();
                probableActivitiesString.append("ON_BICYCLE "+bicycleConfidence+" ");

            } else if (activity.getType() == com.google.android.gms.location.DetectedActivity.ON_FOOT) {
                footConfidence = activity.getConfidence();
                probableActivitiesString.append("ON_FOOT "+footConfidence+" ");

            } else if (activity.getType() == com.google.android.gms.location.DetectedActivity.UNKNOWN) {
                unknownConfidence = activity.getConfidence();
                probableActivitiesString.append("UNKNOWN "+unknownConfidence+" ");

            } else if (activity.getType() == com.google.android.gms.location.DetectedActivity.IN_VEHICLE) {
                vehicleConfidence = activity.getConfidence();
                probableActivitiesString.append("IN_VEHICLE "+vehicleConfidence+" ");
            }
        }
        Log.d(TAG, "Probable Activities Detected - "+probableActivitiesString);

        if ( detectedActivity == com.google.android.gms.location.DetectedActivity.ON_FOOT || detectedActivity == com.google.android.gms.location.DetectedActivity.ON_BICYCLE
                    || detectedActivity == com.google.android.gms.location.DetectedActivity.IN_VEHICLE ) {

            // No matter what the probable activity's confidences are
            // Cancel if any STILL_STATE timer running.
            stopTimer();

            // Decide if the detected activity is good enough based on probable detected activities
            // confidences.

            // Rejected: ON_FOOT 42  Probable - [UNKNOWN 25 ON_BICYCLE 20 IN_VEHICLE 12]
            if( detectedActivity == com.google.android.gms.location.DetectedActivity.ON_FOOT) {
                if( detectedActivityConfidence < 50 || unknownConfidence > 30 ||
                        bicycleConfidence > 2 || vehicleConfidence > 10) {
                    //Mostly likely not reliable to assume ON_FOOT
                    Log.d(TAG, " Ignoring the ON_FOOT activity as it has not reliable " );
                    return;
                }
            }
            // IN_VEHICLE & ON_BICYCLE activity needs more strict check on probable activities
            // confidences as well.
            else if( detectedActivity == com.google.android.gms.location.DetectedActivity.ON_BICYCLE ) {
                // Accepted: ON_BICYCLE 46 - Probable [UNKNOWN 29 ON_FOOT 2]
                if( detectedActivityConfidence < 80 || unknownConfidence > 30 ||
                        footConfidence > 2 || vehicleConfidence > 10) {
                    //Mostly likely not reliable to assume ON_BICYCLE
                    Log.d(TAG, " Ignoring the ON_BICYCLE activity as it has not reliable " );
                    return;
                }

            } else if( detectedActivity == com.google.android.gms.location.DetectedActivity.IN_VEHICLE ) {
                // Accepted: IN_VEHICLE 46 - Probable [UNKNOWN 29 ON_FOOT 2]
                // Rejected: IN_VEHICLE 54 - Sometimes no probable activities obtained.
                if( detectedActivityConfidence < 80 || unknownConfidence > 30 ||
                        footConfidence > 2 || bicycleConfidence > 10) {
                    //Mostly likely not reliable to assume IN_VEHICLE
                    Log.d(TAG, " Ignoring the IN_VEHICLE activity as it has not reliable " );
                    return;
                }
            }

            DetectedActivity userActivity = getActivity(detectedActivity);

            if(mCurrentTripId == 0) {
                // No current trip found. Start a trip.
                Log.d(TAG, " Starting a trip " );
                mCurrentDetectedActivity = userActivity;
                startTrip();
            } else {
                if (userActivity != mCurrentDetectedActivity) {
                    // A different activity is observed from current activity. So end the current trip
                    // and consider this as new trip. This could happen for example
                    // if user [ WALKS -- DRIVES -- WALKS ]  or [ WALKS - BICYCLES - WALKS ].
                    //Then we need record each transition in activity as new trip.
                    mCurrentDetectedActivity = userActivity;
                    Log.d(TAG, " New user activity identified. Finishing current trip - "+userActivity );
                    //End the trip.
                    endTrip(true);

                    Log.d(TAG, " New user activity identified. Starting new trip - "+ mCurrentDetectedActivity);
                    //Start new trip.
                    startTrip();
                } else {
                   //Same activity is obtained. Just continue to wait for STILL state to occur.
                }
            }
        } else {
            //Ignore UNKNOWN, TILTING activity
            if( detectedActivity == com.google.android.gms.location.DetectedActivity.STILL ) {
                //Ignore if other probable activity confidences are found.
                // Accept UNKNOWN if its confidence is less than 30.
                if( bicycleConfidence > 0 || vehicleConfidence > 0 || footConfidence > 0 ||
                        unknownConfidence > 30) {
                    Log.d(TAG, " Ignoring the STILL activity as it has not reliable " );
                    return;
                }

                // If there is trip detected only then start identifying if user in still state.
                if (mCurrentTripId != 0) {
                    // We need a 2 minutes timer if user is IN_VEHICLE state.
                    if (mCurrentDetectedActivity == DetectedActivity.VEHICLE) {
                        startInVehicleTimer();
                    } else {
                        // Otherwise start a 30sec timer.
                        startStillStateTimer();
                    }
                }
            }
        }
    }
        @Override
    public void onConnected(Bundle bundle) {
        if (mActivityRecognitionClient.isConnected() ) {
            registerActivityUpdates();
        }
    }

    @Override
    public void onDisconnected() {
        //Do nothing. As we continue to get activity updates from location services.
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Log the error. Since this situation shouldn't happen as Applications main
        // Activity does a play services available and takes corrective action unless
        // play services are fully functional. This shouldn't be hit ideally.
    }

    private void startTrip() {
        if(mCurrentTripId == 0) {
            mCurrentTripId = ActivitiesDBManager.getInstance().addTrip(mCurrentDetectedActivity);
        }
        ActivityLoggerPreferenceManager.setLong(PreferenceKeys.CURRENT_TRIP_ID, mCurrentTripId);
        getCurrentLocation();
    }

    private void endTrip(boolean isDueToOtherActivityChange) {

        if(isDueToOtherActivityChange) {
            ActivitiesDBManager.getInstance().endTrip(mCurrentTripId, System.currentTimeMillis() / 1000);
        } else {
            // User is in STILL state more than 30 secs.
            ActivitiesDBManager.getInstance().endTrip(mCurrentTripId,
                    (System.currentTimeMillis() - STILL_STATE_INTERVAL) / 1000);
        }

        getCurrentLocation();
        mCurrentTripId = 0;
        ActivityLoggerPreferenceManager.setLong(PreferenceKeys.CURRENT_TRIP_ID, mCurrentTripId);
    }

    public void getCurrentLocation() {
        //Check for last location's accuracy and age.
        Location lastLocation = mLocationClient.getLastLocation();

        if (lastLocation != null && lastLocation.getAccuracy() < MINIMUM_ACCURACY_DISTANCE &&
            System.currentTimeMillis() - lastLocation.getTime() < TIME_SINCE_LAST_LOCATION ) {

            getAddress(lastLocation, mCurrentTripId);

        } else {
            //Fall back to get current location through location updates.

            // Use Balanced power accuracy. Could be improved by using HIGH_POWER_ACCURACY using GPS.
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            mLocationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationClient.requestLocationUpdates(mLocationRequest,
                    new TripLocationListener(mCurrentTripId));
        }
    }

    private void startStillStateTimer() {
        if (mTimerStarted == false) {
            Log.d(TAG, " Starting Still State Timer " );
            mTimerStarted = true;
            mStillStateTimer.start();
        }
    }

    private void startInVehicleTimer() {
        if (mTimerStarted == false) {
            Log.d(TAG, " Starting IN Vehicle Still State Timer " );
            mTimerStarted = true;
            mInVehicleStillStateTimer.start();
        }
    }

    private void stopTimer() {
        mTimerStarted = false;
        if (mStillStateTimer != null) {
            mStillStateTimer.cancel();
        }
    }

    private class StillStateTimer extends CountDownTimer {
        public StillStateTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        public void onFinish() {
            endTrip(false);
        }

        public void onTick(long millisUntilFinished) { }
    }

    private void sendNewTripBroadcast() {
        Log.d(TAG, "New Trip available message is Broad casted " );
        Intent broadCastIntent = new Intent(NewActivityReceiver.ACTION_NEW_TRIP);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadCastIntent);
    }

    private void getAddress(final Location currentLocation,final long tripId) {
        mHandler.post(new ReverseGeoCodeRunnable(tripId, currentLocation));
    }

    /**
     * Location listener for a trip. The location request can be triggered for
     * either to find trip's start or end location. Until a reasonable good location
     * is obtained, it waits to get a location.
     */
    class TripLocationListener implements LocationListener {

        long tripId;

        TripLocationListener(long tripId) {
            this.tripId = tripId;
        }

        @Override
        public void onLocationChanged(Location location) {
            long ageOfLocation = System.currentTimeMillis() - location.getTime();
            if (location != null && location.getAccuracy() < MINIMUM_ACCURACY_DISTANCE &&
                    ageOfLocation < TIME_SINCE_LAST_LOCATION ) {
                if (mLocationClient.isConnected()) {
                    mLocationClient.removeLocationUpdates(this);
                }
                //Once a healthy location is obtained, do reverse geo coding.
                getAddress(location, tripId);
            } else {
                //Continue to wait for a accurate location.
            }
        }
    }

    /**
     * Given a tripId and a location, this runnable does reverse geocoding of the location
     * and update the trip's either start or end location.
     *
     */
    class ReverseGeoCodeRunnable implements Runnable {

        long tripId;
        Location currentLocation;

        public ReverseGeoCodeRunnable(long tripId, Location location) {
            this.tripId = tripId;
            this.currentLocation = location;
        }

        @Override
        public void run() {
            boolean isStartLocationSet = ActivitiesDBManager.getInstance().isTripStartLocationSet(tripId);
            long locationId = ActivitiesDBManager.getInstance().addLocation(
                    currentLocation.getLatitude(), currentLocation.getLongitude());
            if (isStartLocationSet == false) {
                ActivitiesDBManager.getInstance().
                        updateTripStartLocation(tripId, locationId);

            } else {
                ActivitiesDBManager.getInstance().
                        updateTripEndLocation(tripId, locationId);
            }

            // Get the address and update the corresponding location id.
            String addressText = reverseGeoCode(currentLocation);
            ActivitiesDBManager.getInstance().updateLocationAddress(locationId, addressText);

            if (isStartLocationSet) {
                //Once the address is obtained. We can send the broadcast.
                sendNewTripBroadcast();
            }
        }
    }

    private String reverseGeoCode(Location location) {
        Geocoder geocoder =
                new Geocoder(this, Locale.getDefault());

        // Create a list to contain the result address
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(),
                    location.getLongitude(), 1);
        } catch (IOException e1) {
            Log.e(TAG, "IO Exception in getFromLocation()");
            e1.printStackTrace();
        } catch (IllegalArgumentException e2) {
            // Error message to post in the log
            String errorString = "Illegal arguments " +
                    Double.toString(location.getLatitude()) +
                    " , " +
                    Double.toString(location.getLongitude()) +
                    " passed to address service";
            Log.e(TAG, errorString);
            e2.printStackTrace();
        }

        String addressText = null;
        // If the reverse geocode returned an address
        if (addresses != null && addresses.size() > 0) {
            // Get the first address
            Address address = addresses.get(0);

            if( address.getMaxAddressLineIndex() > 0 ) {
                addressText = address.getAddressLine(0);
            }

            addressText = (addressText != null) ? addressText : address.getLocality();

            // Get neighbourhood if address cannot be found.
            if (addressText == null) {
                addressText = address.getSubLocality();
            }

            // Get Sub-administrative area if address cannot be found.
            if (addressText == null) {
                addressText = address.getSubAdminArea();
            }

            // Get thorough name of area if address cannot be found.
            if (addressText == null) {
                addressText = address.getThoroughfare();
            }

            // Get Sub-thorough name of area if address cannot be found.
            if (addressText == null) {
                addressText = address.getSubThoroughfare();
            }

            // Give up. Set the address to UnKnown.
            if (addressText == null) {
                addressText = "Unknown";
            }
        } else {
            addressText = "Not Found";
        }
        return addressText;
    }

}



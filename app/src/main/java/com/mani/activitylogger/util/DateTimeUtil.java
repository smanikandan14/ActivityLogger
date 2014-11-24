package com.mani.activitylogger.util;

import android.content.res.Resources;

import com.mani.activitylogger.R;
import com.mani.activitylogger.app.ActivitiesLoggerApplication;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by maniselvaraj on 4/10/14.
 */
public class DateTimeUtil {

    private static final String ACTIVITY_DISPLAY_DATE_FORMAT = "h:mmaa";
    private static final String ACTIVITY_HEADER_DATE_FORMAT = "MMMM dd, yyyy";

    private static SimpleDateFormat sTripHeaderDateFormat =
            new SimpleDateFormat(ACTIVITY_HEADER_DATE_FORMAT);

    public static String getActivityDisplayTime(long startTime, long endTime) {

        SimpleDateFormat timeFormat = new SimpleDateFormat(ACTIVITY_DISPLAY_DATE_FORMAT);
        //Replace uppercase AM/PM to lower case am/pm
        DateFormatSymbols symbols = new DateFormatSymbols();
        symbols.setAmPmStrings(new String[] { "am", "pm" });
        timeFormat.setDateFormatSymbols(symbols);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime * 1000);
        String startTimeText = timeFormat.format(calendar.getTime());

        calendar.setTimeInMillis(endTime * 1000);
        String endTimeText = timeFormat.format(calendar.getTime());

        Resources resources = ActivitiesLoggerApplication.getContext().getResources();
        return String.format(
                resources.getString(R.string.trip_time_format),
                startTimeText, endTimeText, getActivityTime(startTime, endTime) );
    }

    private static String getActivityTime(long startTime, long endTime) {
        long differenceInSeconds = endTime - startTime;

        //Check for seconds
        if ( differenceInSeconds < 60) {
            return differenceInSeconds+"sec";
        } else {
            //Check for minutes & hours
            long minutes = differenceInSeconds/60;
            if ( minutes < 60 ) {
                return minutes+"min";
            } else {
                long hours = minutes/60;
                minutes = minutes - ( hours * 60); // Could be zero.
                return hours+"hr "+ ( minutes > 0 ? minutes+"min": "");
            }
        }
    }

    public static String getActivityHeaderText(long startTime) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime * 1000);
        return sTripHeaderDateFormat.format(calendar.getTime());

    }
}

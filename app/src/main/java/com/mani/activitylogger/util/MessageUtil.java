package com.mani.activitylogger.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Log helper class.
 * logMessage helps to log the debug logs in sdcard. Easy for debugging.
 */
public class MessageUtil {
	private static Context context;
	private static final boolean LOG_ENABLED = true;

	public static void initializeMessageUtil(Context applicationContext) {
		context = applicationContext;
	}

    private static String currentDateTime() {
		SimpleDateFormat sFmt = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss Z");
		return sFmt.format(new java.util.Date());
	}

	public static void logMessage(String message) {
        System.out.println(message);

		if (!LOG_ENABLED) {
            return;
        }
		File root = null;
		File logfile = null;
		FileWriter writer = null;
		boolean externalStorageAvailable = false;
		boolean externalStorageWriteable = false;
		try {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				externalStorageAvailable = externalStorageWriteable = true;
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
				externalStorageAvailable = true;
				externalStorageWriteable = false;
			} else {
				externalStorageAvailable = externalStorageWriteable = false;
			}

			if (externalStorageAvailable && externalStorageWriteable) {
				root = new File(Environment.getExternalStorageDirectory(), "TripLogger");
				if (!root.exists()) {
					root.mkdirs();
				}
				logfile = new File(root, "trips.log");
				writer = new FileWriter(logfile, true);
				StringBuffer buf = new StringBuffer();
				buf.append(currentDateTime());
				buf.append(' ');
				buf.append(message);
				buf.append('\n');
				writer.append(buf.toString());
				writer.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//  ############################################################################
}

package com.mani.activitylogger.util;

import android.graphics.Typeface;

import com.mani.activitylogger.app.ActivitiesLoggerApplication;

/**
 * Created by maniselvaraj on 4/10/14.
 */
public class FontProvider {

    private final static String ROBOTO_BOLD = "RobotoBold.ttf";
    private final static String ROBOTO_MEDIUM = "RobotoMedium.ttf";
    private final static String ROBOTO_NORMAL = "RobotoRegular.ttf";
    private final static String ROBOTO_LIGHT_ITALIC = "RobotoMediumItalic.ttf";

    static Typeface sHelveticaBoldFont;
    static Typeface sHelveticaNormalFont;
    static Typeface sHelveticaVeryLightFont;

    public static Typeface getBold() {
        if (sHelveticaBoldFont == null) {
            sHelveticaBoldFont = Typeface.createFromAsset(ActivitiesLoggerApplication.getContext().getAssets(),ROBOTO_BOLD );
        }
        return sHelveticaBoldFont;
    }

    public static Typeface getMediumFont() {
        if (sHelveticaNormalFont == null) {
            sHelveticaNormalFont = Typeface.createFromAsset(ActivitiesLoggerApplication.getContext().getAssets(), ROBOTO_MEDIUM );
        }
        return sHelveticaNormalFont;
    }

    public static Typeface getNormalFont() {
        if (sHelveticaNormalFont == null) {
            sHelveticaNormalFont = Typeface.createFromAsset(ActivitiesLoggerApplication.getContext().getAssets(),ROBOTO_NORMAL );
        }
        return sHelveticaNormalFont;
    }

    public static Typeface getItalicFont() {
        if (sHelveticaVeryLightFont == null) {
            sHelveticaVeryLightFont = Typeface.createFromAsset(ActivitiesLoggerApplication.getContext().getAssets(),ROBOTO_LIGHT_ITALIC );
        }
        return sHelveticaVeryLightFont;
    }
}

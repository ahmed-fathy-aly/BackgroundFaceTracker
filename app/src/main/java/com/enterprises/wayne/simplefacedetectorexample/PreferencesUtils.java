package com.enterprises.wayne.simplefacedetectorexample;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ahmed on 7/21/2016.
 * used to save if the service is running or not,
 * so if the user restarts the service we can check if the service was running before or not
 */
public class PreferencesUtils
{
    private static final String KEY_PREFERENCES_NAME = "backgroundFaceTrackerPreferences";

    public static void setServiceStarted(Context context, boolean started)
    {
        SharedPreferences pref = context.getSharedPreferences(KEY_PREFERENCES_NAME, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean("serviceStarted", started);

        editor.commit();
    }


    public static boolean getServiceStarted(Context context)
    {
        SharedPreferences pref = context.getSharedPreferences(KEY_PREFERENCES_NAME, context.MODE_PRIVATE);
        return pref.getBoolean("serviceStarted", false);
    }
}

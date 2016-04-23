package com.mahii.Global;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class AppMethodSharedPref {

    public static final String PREFS_NAME = "daynightmode";

    // string preference
    public static boolean setStringPreference(Activity activity, String key, String value) {
        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public static String getStringPreference(Activity activity, String key) {
        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String value = settings.getString(key, "");
        return value;
    }

    // int preference
    public static boolean setIntegerPreference(Activity activity, String key, int value) {
        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public static int getIntegerPreference(Activity activity, String key) {
        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int value = settings.getInt(key, -1);
        return value;
    }

    // boolean preference
    public static boolean setBooleanPreference(Activity activity, String key, Boolean value) {
        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public static Boolean getBooleanPreference(Activity activity, String key) {
        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Boolean value = settings.getBoolean(key, false);
        return value;
    }

}

package com.mobile.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

public class SuggestionCacheManager  {
    private static final String PREF_NAME = "rcm_suggestions";
    private static final String PREF_CLICK_COUNT = "rcm_click_count";

    public static void saveSuggestions(Context context, String uid, List<String> productIds) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String joined = TextUtils.join(",", productIds);
        prefs.edit().putString(uid, joined).apply();
    }

    public static List<String> loadSuggestions(Context context, String uid) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String joined = prefs.getString(uid, null);
        if (joined != null) {
            return Arrays.asList(joined.split(","));
        }
        return null;
    }

    public static void clearSuggestions(Context context, String uid) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(uid).apply();
    }
    public static void incrementClickCount(Context context, String uid) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int count = prefs.getInt(uid + "_click_count", 0);
        prefs.edit().putInt(uid + "_click_count", count + 1).apply();
    }
    public static int getClickCount(Context context, String uid) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(uid + "_click_count", 0);
    }
    public static void resetClickCount(Context context, String uid) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(uid + "_click_count", 0).apply();
    }

}

package com.mobile.evocasa;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.facebook.appevents.AppEventsLogger;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_ONBOARDING = "hasShownOnboarding";

    @Override
    public void onCreate() {
        super.onCreate();

        // Khởi tạo SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Khởi tạo ngôn ngữ mặc định
        if (!prefs.contains(KEY_LANGUAGE)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_LANGUAGE, "en");
            editor.apply();
            Log.d(TAG, "Default language set to: en");
        } else {
            Log.d(TAG, "Language already set: " + prefs.getString(KEY_LANGUAGE, "en"));
        }

        // Khởi tạo trạng thái onboarding
        if (!prefs.contains(KEY_ONBOARDING)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_ONBOARDING, false);
            editor.apply();
            Log.d(TAG, "Onboarding not shown yet");
        }

        // Khởi tạo Facebook SDK
        AppEventsLogger.activateApp(this);
    }
}
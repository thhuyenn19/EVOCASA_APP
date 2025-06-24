package com.mobile.evocasa;

import android.app.Application;
import android.content.SharedPreferences;

import com.facebook.appevents.AppEventsLogger;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Khởi tạo ngôn ngữ mặc định nếu chưa có
        SharedPreferences prefs = getSharedPreferences("LanguagePrefs", MODE_PRIVATE);
        if (!prefs.contains("language")) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("language", "en");
            editor.apply();
        }

        // Không cần gọi sdkInitialize() nữa
        AppEventsLogger.activateApp(this);
    }
}
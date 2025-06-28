package com.mobile.evocasa;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.appevents.AppEventsLogger;
import com.mobile.utils.GlobalNetworkManager;
import com.mobile.utils.NetworkOverlayManager;

public class MyApplication extends Application implements GlobalNetworkManager.NetworkListener {
    private static final String TAG = "MyApplication";

    // SharedPreferences constants
    private static final String PREFS_NAME = "AppSettings";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_ONBOARDING = "hasShownOnboarding";

    // Network and activity management
    private Handler mainHandler;
    private Activity currentActivity;
    private boolean isAppInForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize SharedPreferences and default settings
        initializeSharedPreferences();

        // Initialize Facebook SDK
        AppEventsLogger.activateApp(this);

        // Initialize network management
        mainHandler = new Handler(Looper.getMainLooper());
        GlobalNetworkManager.getInstance(this);
        GlobalNetworkManager.getInstance().addListener(this);

        // Register activity lifecycle callbacks
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
                Log.d(TAG, "Activity created: " + activity.getClass().getSimpleName());
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                Log.d(TAG, "Activity started: " + activity.getClass().getSimpleName());
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                currentActivity = activity;
                isAppInForeground = true;
                NetworkOverlayManager.setCurrentActivity(activity);

                Log.d(TAG, "Activity resumed: " + activity.getClass().getSimpleName());

                // Check network state when activity is resumed
                // Don't show overlay if we're in NoInternetActivity
                if (!(activity instanceof NoInternetActivity)) {
                    checkAndShowNoInternetOverlay(activity);
                }
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
                Log.d(TAG, "Activity paused: " + activity.getClass().getSimpleName());
                NetworkOverlayManager.clearCurrentActivity(activity);
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                Log.d(TAG, "Activity stopped: " + activity.getClass().getSimpleName());
                if (currentActivity == activity) {
                    isAppInForeground = false;
                }
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
                // No implementation needed
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                Log.d(TAG, "Activity destroyed: " + activity.getClass().getSimpleName());
                if (currentActivity == activity) {
                    currentActivity = null;
                }
            }
        });

        Log.d(TAG, "MyApplication initialized");
    }

    private void initializeSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize default language
        if (!prefs.contains(KEY_LANGUAGE)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_LANGUAGE, "en");
            editor.apply();
            Log.d(TAG, "Default language set to: en");
        } else {
            Log.d(TAG, "Language already set: " + prefs.getString(KEY_LANGUAGE, "en"));
        }

        // Initialize onboarding status
        if (!prefs.contains(KEY_ONBOARDING)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(KEY_ONBOARDING, false);
            editor.apply();
            Log.d(TAG, "Onboarding not shown yet");
        }
    }

    @Override
    public void onNetworkChanged(boolean isConnected) {
        Log.d(TAG, "Network state changed: " + (isConnected ? "Connected" : "Disconnected"));

        mainHandler.post(() -> {
            if (isConnected) {
                // Network is available
                NetworkOverlayManager.hideNoInternetOverlay();
            } else {
                // Network is not available
                if (isAppInForeground && currentActivity != null && !(currentActivity instanceof NoInternetActivity)) {
                    checkAndShowNoInternetOverlay(currentActivity);
                }
            }
        });
    }

    private void checkAndShowNoInternetOverlay(Activity activity) {
        if (!GlobalNetworkManager.getInstance().isConnected()) {
            Log.d(TAG, "No internet connection detected, showing overlay");
            NetworkOverlayManager.showNoInternetOverlayIfNeeded(activity);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Clean up resources
        if (GlobalNetworkManager.getInstance() != null) {
            GlobalNetworkManager.getInstance().removeListener(this);
            GlobalNetworkManager.getInstance().cleanup();
        }
        NetworkOverlayManager.reset();
        Log.d(TAG, "MyApplication terminated");
    }
}
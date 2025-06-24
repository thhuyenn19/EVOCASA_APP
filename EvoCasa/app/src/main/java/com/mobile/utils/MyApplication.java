package com.mobile.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mobile.evocasa.NoInternetActivity;
import com.mobile.utils.GlobalNetworkManager;
import com.mobile.utils.NetworkOverlayManager;

public class MyApplication extends Application implements GlobalNetworkManager.NetworkListener {
    private static final String TAG = "MyApplication";
    private Handler mainHandler;
    private Activity currentActivity;
    private boolean isAppInForeground = false;

    @Override
    public void onCreate() {
        super.onCreate();

        mainHandler = new Handler(Looper.getMainLooper());

        // Initialize GlobalNetworkManager and add listener
        GlobalNetworkManager.getInstance(this);
        GlobalNetworkManager.getInstance().addListener(this);

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
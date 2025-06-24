package com.mobile.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mobile.evocasa.NoInternetActivity;

public class NetworkOverlayManager {
    private static final String TAG = "NetworkOverlayManager";
    private static boolean isOverlayShowing = false;
    private static Activity currentActivity = null;

    public static synchronized void showNoInternetOverlayIfNeeded(Context context) {
        if (isOverlayShowing) {
            Log.d(TAG, "Overlay already showing, skipping");
            return;
        }

        // Don't show overlay if we're already in NoInternetActivity
        if (context instanceof NoInternetActivity) {
            Log.d(TAG, "Already in NoInternetActivity, skipping overlay");
            return;
        }

        try {
            isOverlayShowing = true;
            Intent intent = new Intent(context, NoInternetActivity.class);

            if (!(context instanceof Activity)) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }

            context.startActivity(intent);
            Log.d(TAG, "No internet overlay started");
        } catch (Exception e) {
            Log.e(TAG, "Failed to show no internet overlay", e);
            isOverlayShowing = false;
        }
    }

    public static synchronized void hideNoInternetOverlay() {
        if (isOverlayShowing) {
            isOverlayShowing = false;
            Log.d(TAG, "No internet overlay hidden");
        }
    }

    public static synchronized void setOverlayShown(boolean shown) {
        isOverlayShowing = shown;
        Log.d(TAG, "Overlay state set to: " + shown);
    }

    public static synchronized boolean isOverlayShowing() {
        return isOverlayShowing;
    }

    public static synchronized void setCurrentActivity(Activity activity) {
        currentActivity = activity;
    }

    public static synchronized Activity getCurrentActivity() {
        return currentActivity;
    }

    public static synchronized void clearCurrentActivity(Activity activity) {
        if (currentActivity == activity) {
            currentActivity = null;
        }
    }

    public static synchronized void reset() {
        isOverlayShowing = false;
        currentActivity = null;
        Log.d(TAG, "NetworkOverlayManager reset");
    }
}
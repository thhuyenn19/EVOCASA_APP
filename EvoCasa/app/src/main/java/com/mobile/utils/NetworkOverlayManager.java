package com.mobile.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import com.mobile.evocasa.R;

public class NetworkOverlayManager implements Application.ActivityLifecycleCallbacks,
        GlobalNetworkManager.NetworkStatusCallback {

    private static final String TAG = "NetworkOverlayManager";
    private static NetworkOverlayManager instance;

    private Application application;
    private WindowManager windowManager;
    private View overlayView;
    private GlobalNetworkManager networkManager;
    private Activity currentActivity;
    private boolean isOverlayShowing = false;
    private int activeActivities = 0;

    private NetworkOverlayManager(Application application) {
        this.application = application;
        this.windowManager = (WindowManager) application.getSystemService(Context.WINDOW_SERVICE);
        this.networkManager = GlobalNetworkManager.getInstance(application);

        // Đăng ký lifecycle callbacks
        application.registerActivityLifecycleCallbacks(this);

        // Đăng ký network callbacks
        networkManager.registerCallback(this);

        createOverlayView();
    }

    public static synchronized NetworkOverlayManager getInstance(Application application) {
        if (instance == null) {
            instance = new NetworkOverlayManager(application);
        }
        return instance;
    }

    private void createOverlayView() {
        try {
            LayoutInflater inflater = LayoutInflater.from(application);

            // Inflate layout từ XML
            overlayView = inflater.inflate(R.layout.layout_no_internet, null);

            // Tìm retry button và set click listener
            Button retryButton = overlayView.findViewById(R.id.btnRetry);
            if (retryButton != null) {
                retryButton.setOnClickListener(v -> {
                    Log.d(TAG, "Retry button clicked");
                    // Kiểm tra lại network
                    if (networkManager.isNetworkConnected()) {
                        hideOverlay();
                    } else {
                        // Có thể thêm animation hoặc feedback khác
                        Log.d(TAG, "Still no internet connection");
                        // Có thể hiển thị toast hoặc animation
                    }
                });
            }

            // Tìm settings button và set click listener
            View settingsButton = overlayView.findViewById(R.id.tvSettings);
            if (settingsButton != null) {
                settingsButton.setOnClickListener(v -> {
                    Log.d(TAG, "Settings button clicked");
                    openNetworkSettings();
                });
            }

            Log.d(TAG, "Overlay view created successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error creating overlay view", e);
        }
    }

    @Override
    public void onNetworkConnected() {
        Log.d(TAG, "Network connected - hiding overlay");
        hideOverlay();
    }

    public void onNetworkDisconnected() {
        Log.d(TAG, "Network disconnected - activeActivities: " + activeActivities);
        if (activeActivities > 0) {
            Log.d(TAG, "Showing overlay due to network loss");
            showOverlay();
        } else {
            Log.d(TAG, "No active activities, skipping overlay");
        }
    }

    private void showOverlay() {
        if (isOverlayShowing || overlayView == null || currentActivity == null) {
            Log.w(TAG, "Cannot show overlay: isOverlayShowing=" + isOverlayShowing + ", overlayView=" + (overlayView == null) + ", currentActivity=" + (currentActivity == null));
            return;
        }
        try {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                params.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            params.format = PixelFormat.TRANSLUCENT;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            params.gravity = Gravity.TOP | Gravity.LEFT;

            windowManager.addView(overlayView, params);
            isOverlayShowing = true;
            Log.d(TAG, "Overlay displayed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error showing overlay", e);
            createFallbackOverlay();
        }
    }
    private void createFallbackOverlay() {
        // Fallback nếu không load được XML
        Log.d(TAG, "Creating fallback overlay");
        // Có thể tạo overlay đơn giản bằng code ở đây
    }

    private void hideOverlay() {
        if (!isOverlayShowing || overlayView == null) {
            return;
        }

        try {
            windowManager.removeView(overlayView);
            isOverlayShowing = false;
            Log.d(TAG, "Overlay hidden");
        } catch (Exception e) {
            Log.e(TAG, "Error hiding overlay", e);
        }
    }

    private void openNetworkSettings() {
        try {
            android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            application.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening network settings", e);
        }
    }

    // Activity Lifecycle Callbacks
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.d(TAG, "Activity created: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStarted(Activity activity) {
        activeActivities++;
        currentActivity = activity;
        Log.d(TAG, "Activity started: " + activity.getClass().getSimpleName() +
                ", Active activities: " + activeActivities);

        // Kiểm tra network khi activity start
        if (!networkManager.isNetworkConnected()) {
            Log.d(TAG, "No internet, attempting to show overlay");
            showOverlay();
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
        Log.d(TAG, "Activity resumed: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d(TAG, "Activity paused: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        activeActivities--;
        Log.d(TAG, "Activity stopped: " + activity.getClass().getSimpleName() +
                ", Active activities: " + activeActivities);

        if (activeActivities <= 0) {
            hideOverlay(); // Ẩn overlay khi không có activity nào active
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.d(TAG, "Activity destroyed: " + activity.getClass().getSimpleName());
    }

    public void destroy() {
        Log.d(TAG, "Destroying NetworkOverlayManager");

        if (application != null) {
            application.unregisterActivityLifecycleCallbacks(this);
        }

        if (networkManager != null) {
            networkManager.unregisterCallback(this);
        }

        hideOverlay();
        instance = null;
    }

    // Kiểm tra permission cho overlay (Android 6.0+)
    public boolean canDrawOverlays() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(application);
        }
        return true;
    }

    // Phương thức để force check network (có thể gọi từ bất kỳ đâu)
    public void forceCheckNetwork() {
        if (!networkManager.isNetworkConnected()) {
            if (activeActivities > 0) {
                showOverlay();
            }
        } else {
            hideOverlay();
        }
    }
}
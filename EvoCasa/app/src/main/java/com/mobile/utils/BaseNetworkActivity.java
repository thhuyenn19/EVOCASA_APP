package com.mobile.utils;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mobile.evocasa.NoInternetActivity;

public abstract class BaseNetworkActivity extends AppCompatActivity {

    private static final String TAG = "BaseNetworkActivity";
    protected static final int REQUEST_NO_INTERNET = 1001;

    private Handler mainHandler;
    private boolean isActivityActive = false;
    private boolean hasShownNoInternetDialog = false;

    private final GlobalNetworkManager.NetworkListener networkListener = isConnected -> {
        if (isActivityActive) {
            mainHandler.post(() -> {
                if (!isConnected && !hasShownNoInternetDialog) {
                    showNoInternetActivity();
                } else if (isConnected) {
                    hasShownNoInternetDialog = false;
                    onNetworkRestored();
                }
            });
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainHandler = new Handler(Looper.getMainLooper());

        Log.d(TAG, "BaseNetworkActivity created: " + this.getClass().getSimpleName());

        // Add network listener
        GlobalNetworkManager.getInstance(this).addListener(networkListener);

        // Check initial network state
        if (!GlobalNetworkManager.getInstance(this).isConnected()) {
            Log.d(TAG, "No internet connection detected on create");
            showNoInternetActivity();
        } else {
            Log.d(TAG, "Internet connection available, proceeding normally");
            proceedNormally();
        }
    }

    private void showNoInternetActivity() {
        if (!hasShownNoInternetDialog && !NetworkOverlayManager.isOverlayShowing()) {
            hasShownNoInternetDialog = true;
            Log.d(TAG, "Showing NoInternetActivity");

            Intent intent = new Intent(this, NoInternetActivity.class);
            startActivityForResult(intent, REQUEST_NO_INTERNET);
        }
    }

    /**
     * Override this method to implement what should happen when the activity starts normally
     * (i.e., when there is internet connection)
     */
    protected abstract void proceedNormally();

    /**
     * Override this method to implement what should happen when network is restored
     * Default implementation calls proceedNormally()
     */
    protected void onNetworkRestored() {
        Log.d(TAG, "Network restored in " + this.getClass().getSimpleName());
        proceedNormally();
    }

    /**
     * Override this method to implement what should happen when network is lost
     * Default implementation shows the NoInternetActivity
     */
    protected void onNetworkLost() {
        Log.d(TAG, "Network lost in " + this.getClass().getSimpleName());
        showNoInternetActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityActive = true;
        Log.d(TAG, "BaseNetworkActivity resumed: " + this.getClass().getSimpleName());
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityActive = false;
        Log.d(TAG, "BaseNetworkActivity paused: " + this.getClass().getSimpleName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityActive = false;

        // Remove network listener
        try {
            GlobalNetworkManager.getInstance().removeListener(networkListener);
        } catch (Exception e) {
            Log.e(TAG, "Error removing network listener", e);
        }

        Log.d(TAG, "BaseNetworkActivity destroyed: " + this.getClass().getSimpleName());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_NO_INTERNET) {
            hasShownNoInternetDialog = false;

            if (resultCode == RESULT_OK) {
                if (GlobalNetworkManager.getInstance().isConnected()) {
                    Log.d(TAG, "Network restored, proceeding normally");
                    proceedNormally();
                } else {
                    Log.d(TAG, "Still no network connection");
                    Toast.makeText(this, "Vẫn chưa có mạng!", Toast.LENGTH_SHORT).show();
                    // Optionally show the no internet activity again
                    showNoInternetActivity();
                }
            } else {
                // User closed the no internet activity without restoring connection
                if (!GlobalNetworkManager.getInstance().isConnected()) {
                    Log.d(TAG, "No internet activity closed without network restoration");
                    // You can choose to finish this activity or handle it differently
                    // finish();
                }
            }
        }
    }

    /**
     * Utility method to check if network is currently available
     * @return true if network is available, false otherwise
     */
    protected boolean isNetworkAvailable() {
        return GlobalNetworkManager.getInstance().isConnected();
    }

    /**
     * Force check network status and show no internet activity if needed
     */
    protected void checkNetworkAndShowOverlayIfNeeded() {
        if (!isNetworkAvailable() && !hasShownNoInternetDialog) {
            showNoInternetActivity();
        }
    }
}
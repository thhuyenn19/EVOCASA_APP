package com.mobile.evocasa;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.mobile.utils.GlobalNetworkManager;
import com.mobile.utils.NetworkOverlayManager;

public class NoInternetActivity extends AppCompatActivity {

    private static final String TAG = "NoInternetActivity";
    private MaterialButton btnRetry;
    private TextView tvSettings;
    private Handler mainHandler;
    private boolean isActivityActive = false;

    private final GlobalNetworkManager.NetworkListener networkListener = isConnected -> {
        if (isActivityActive && isConnected) {
            Log.d(TAG, "Network restored, finishing activity");
            mainHandler.post(() -> {
                NetworkOverlayManager.setOverlayShown(false);
                setResult(RESULT_OK);
                finish();
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);

        mainHandler = new Handler(Looper.getMainLooper());

        Log.d(TAG, "NoInternetActivity created");

        // Mark overlay as showing
        NetworkOverlayManager.setOverlayShown(true);

        // Add network listener
        GlobalNetworkManager.getInstance(getApplicationContext()).addListener(networkListener);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        btnRetry = findViewById(R.id.btnRetry);
        tvSettings = findViewById(R.id.tvSettings);
    }

    private void setupClickListeners() {
        btnRetry.setOnClickListener(v -> {
            Log.d(TAG, "Retry button clicked");

            // Check network status
            boolean isConnected = GlobalNetworkManager.getInstance().isConnected();
            Log.d(TAG, "Current network status: " + isConnected);

            if (isConnected) {
                NetworkOverlayManager.setOverlayShown(false);
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
            }
        });

        tvSettings.setOnClickListener(v -> {
            Log.d(TAG, "Settings clicked, opening WiFi settings");
            try {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            } catch (Exception e) {
                Log.e(TAG, "Failed to open WiFi settings", e);
                Toast.makeText(this, "Unable to open WiFi settings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityActive = true;
        Log.d(TAG, "NoInternetActivity resumed");

        // Check if network is available when resuming
        if (GlobalNetworkManager.getInstance().isConnected()) {
            Log.d(TAG, "Network available on resume, finishing activity");
            NetworkOverlayManager.setOverlayShown(false);
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityActive = false;
        Log.d(TAG, "NoInternetActivity paused");
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

        // Reset overlay state
        NetworkOverlayManager.setOverlayShown(false);

        Log.d(TAG, "NoInternetActivity destroyed");
    }

    @Override
    public void onBackPressed() {
        // Allow back button to close the activity
        Log.d(TAG, "Back button pressed");
        NetworkOverlayManager.setOverlayShown(false);
        super.onBackPressed();
    }
}
package com.mobile.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.CopyOnWriteArrayList;

public class GlobalNetworkManager {

    private static final String TAG = "GlobalNetworkManager";
    private static GlobalNetworkManager instance;
    private static Context appContext;

    private boolean isConnected = false;
    private ConnectivityManager connectivityManager;
    private Handler mainHandler;

    public interface NetworkListener {
        void onNetworkChanged(boolean isConnected);
    }

    private final CopyOnWriteArrayList<NetworkListener> listeners = new CopyOnWriteArrayList<>();
    private ConnectivityManager.NetworkCallback networkCallback;

    private GlobalNetworkManager(Context context) {
        appContext = context.getApplicationContext();
        mainHandler = new Handler(Looper.getMainLooper());
        connectivityManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Initialize current connection state
        isConnected = checkCurrentConnection();

        // Register network callback
        registerNetworkCallback();

        Log.d(TAG, "GlobalNetworkManager initialized. Initial connection state: " + isConnected);
    }

    // Gọi lần đầu với context
    public static synchronized GlobalNetworkManager getInstance(Context context) {
        if (instance == null) {
            instance = new GlobalNetworkManager(context);
        }
        return instance;
    }

    // Các lần sau không cần context nữa
    public static synchronized GlobalNetworkManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("You must call getInstance(Context) at least once before using getInstance()");
        }
        return instance;
    }

    private boolean checkCurrentConnection() {
        if (connectivityManager == null) {
            return false;
        }

        try {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) {
                return false;
            }

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            if (capabilities == null) {
                return false;
            }

            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } catch (Exception e) {
            Log.e(TAG, "Error checking network connection", e);
            return false;
        }
    }

    private void registerNetworkCallback() {
        if (connectivityManager == null) {
            Log.e(TAG, "ConnectivityManager is null, cannot register network callback");
            return;
        }

        try {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    .build();

            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    Log.d(TAG, "Network available: " + network);
                    updateConnectionState(true);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    Log.d(TAG, "Network lost: " + network);
                    updateConnectionState(false);
                }

                @Override
                public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                    boolean hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                    Log.d(TAG, "Network capabilities changed. Has internet: " + hasInternet);
                    updateConnectionState(hasInternet);
                }
            };

            connectivityManager.registerNetworkCallback(request, networkCallback);
            Log.d(TAG, "Network callback registered successfully");
        } catch (Exception e) {
            Log.e(TAG, "Failed to register network callback", e);
        }
    }

    private void updateConnectionState(boolean connected) {
        if (isConnected != connected) {
            isConnected = connected;
            Log.d(TAG, "Connection state changed to: " + connected);

            // Notify listeners on main thread
            mainHandler.post(() -> {
                for (NetworkListener listener : listeners) {
                    try {
                        listener.onNetworkChanged(connected);
                    } catch (Exception e) {
                        Log.e(TAG, "Error notifying network listener", e);
                    }
                }
            });
        }
    }

    public boolean isConnected() {
        // Double-check current state
        boolean currentState = checkCurrentConnection();
        if (currentState != isConnected) {
            isConnected = currentState;
            Log.d(TAG, "Connection state updated during check: " + isConnected);
        }
        return isConnected;
    }

    public void addListener(NetworkListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            Log.d(TAG, "Added network listener. Total listeners: " + listeners.size());
        }
    }

    public void removeListener(NetworkListener listener) {
        if (listener != null) {
            boolean removed = listeners.remove(listener);
            if (removed) {
                Log.d(TAG, "Removed network listener. Total listeners: " + listeners.size());
            }
        }
    }

    public void cleanup() {
        try {
            if (networkCallback != null && connectivityManager != null) {
                connectivityManager.unregisterNetworkCallback(networkCallback);
                Log.d(TAG, "Network callback unregistered");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during cleanup", e);
        }

        listeners.clear();
        networkCallback = null;
    }
}
package com.mobile.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.util.Log;

public class NetworkMonitor {

    private final ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private NetworkStatusListener listener;

    public interface NetworkStatusListener {
        void onNetworkAvailable();
        void onNetworkLost();
        void onNetworkChanged(boolean connected);
    }

    public NetworkMonitor(Context context) {
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (this.connectivityManager == null) {
            Log.e("NetworkMonitor", "ConnectivityManager is null, check context!");
        }
    }

    public void startMonitoring(NetworkStatusListener listener) {
        this.listener = listener;
        if (connectivityManager == null) {
            Log.e("NetworkMonitor", "ConnectivityManager is null, cannot start monitoring!");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    Log.d("NetworkMonitor", "Network available");
                    if (listener != null) listener.onNetworkAvailable();
                    if (listener != null) listener.onNetworkChanged(true);
                }

                @Override
                public void onLost(Network network) {
                    Log.d("NetworkMonitor", "Network lost");
                    if (listener != null) listener.onNetworkLost();
                    if (listener != null) listener.onNetworkChanged(false);
                }
            };
            try {
                connectivityManager.registerDefaultNetworkCallback(networkCallback);
                Log.d("NetworkMonitor", "Registered default network callback");
            } catch (SecurityException e) {
                Log.e("NetworkMonitor", "Permission error", e);
            }
        } else {
            NetworkRequest request = new NetworkRequest.Builder().build();
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    if (listener != null) listener.onNetworkAvailable();
                    if (listener != null) listener.onNetworkChanged(true);
                }

                @Override
                public void onLost(Network network) {
                    if (listener != null) listener.onNetworkLost();
                    if (listener != null) listener.onNetworkChanged(false);
                }
            };
            try {
                connectivityManager.registerNetworkCallback(request, networkCallback);
                Log.d("NetworkMonitor", "Registered network callback for Android < 7");
            } catch (SecurityException e) {
                Log.e("NetworkMonitor", "Permission error", e);
            }
        }
    }

    public void stopMonitoring() {
        if (networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

    public boolean isNetworkAvailable() {
        if (connectivityManager == null) {
            Log.e("NetworkMonitor", "ConnectivityManager is null");
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                Log.d("NetworkMonitor", "No active network");
                return false;
            }
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if (capabilities == null) {
                Log.d("NetworkMonitor", "No network capabilities");
                return false;
            }
            boolean result = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
            Log.d("NetworkMonitor", "Network available: " + result);
            return result;
        } else {
            android.net.NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            boolean result = info != null && info.isConnected();
            Log.d("NetworkMonitor", "Network available (legacy): " + result);
            return result;
        }
    }
}
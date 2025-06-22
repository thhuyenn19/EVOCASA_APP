package com.mobile.utils;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class GlobalNetworkManager implements NetworkMonitor.NetworkStatusListener {

    private static GlobalNetworkManager instance;
    private NetworkMonitor networkMonitor;
    private boolean isConnected = true;
    private List<NetworkStatusCallback> callbacks;
    private Context applicationContext;

    public interface NetworkStatusCallback {
        void onNetworkConnected();
        void onNetworkDisconnected();
    }

    private GlobalNetworkManager(Context context) {
        this.applicationContext = context.getApplicationContext();
        this.callbacks = new ArrayList<>();
        this.networkMonitor = new NetworkMonitor(applicationContext);

        // Bắt đầu monitor ngay khi khởi tạo
        networkMonitor.startMonitoring(this);

        // Kiểm tra trạng thái ban đầu
        isConnected = networkMonitor.isNetworkAvailable();
        Log.d("GlobalNetworkManager", "Initial network status: " + isConnected);
    }

    public static synchronized GlobalNetworkManager getInstance(Context context) {
        if (instance == null) {
            instance = new GlobalNetworkManager(context);
        }
        return instance;
    }

    public void registerCallback(NetworkStatusCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    public void unregisterCallback(NetworkStatusCallback callback) {
        callbacks.remove(callback);
    }

    public boolean isNetworkConnected() {
        return isConnected;
    }

    @Override
    public void onNetworkAvailable() {
        Log.d("GlobalNetworkManager", "Network available - switching from offline to online");
        if (!isConnected) {
            isConnected = true;
            notifyNetworkConnected();
        }
    }

    @Override
    public void onNetworkLost() {
        Log.d("GlobalNetworkManager", "Network lost - switching to offline mode");
        if (isConnected) {
            isConnected = false;
            notifyNetworkDisconnected();
        }
    }

    @Override
    public void onNetworkChanged(boolean connected) {
        if (isConnected != connected) {
            isConnected = connected;
            if (connected) {
                notifyNetworkConnected();
            } else {
                notifyNetworkDisconnected();
            }
        }
    }

    private void notifyNetworkConnected() {
        Log.d("GlobalNetworkManager", "Notifying " + callbacks.size() + " callbacks: Network connected");
        for (NetworkStatusCallback callback : new ArrayList<>(callbacks)) {
            try {
                callback.onNetworkConnected();
            } catch (Exception e) {
                Log.e("GlobalNetworkManager", "Error notifying callback", e);
            }
        }
    }

    private void notifyNetworkDisconnected() {
        Log.d("GlobalNetworkManager", "Notifying " + callbacks.size() + " callbacks: Network disconnected");
        for (NetworkStatusCallback callback : new ArrayList<>(callbacks)) {
            try {
                callback.onNetworkDisconnected();
            } catch (Exception e) {
                Log.e("GlobalNetworkManager", "Error notifying callback", e);
            }
        }
    }

    public void destroy() {
        if (networkMonitor != null) {
            networkMonitor.stopMonitoring();
        }
        callbacks.clear();
        instance = null;
    }
}
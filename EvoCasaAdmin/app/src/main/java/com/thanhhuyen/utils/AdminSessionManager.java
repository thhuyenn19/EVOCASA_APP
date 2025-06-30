package com.thanhhuyen.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class AdminSessionManager {
    private static final String PREF_NAME = "AdminSession";
    private static final String KEY_NAME = "FullName";
    private static final String KEY_IMAGE = "Image";
    private static final String KEY_ID = "employeeid";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public AdminSessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveAdmin(String id, String name) {
        editor.putString(KEY_ID, id);
        editor.putString(KEY_NAME, name);
        editor.apply();
    }

    public String getFullName() {
        return prefs.getString(KEY_NAME, "");
    }

    public String getImageUrl() {
        return prefs.getString(KEY_IMAGE, "");
    }

    public String getEmployeeId() {
        return prefs.getString(KEY_ID, "");
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
    public String getShortName() {
        String fullName = getFullName();
        if (fullName == null || fullName.trim().isEmpty()) return "";
        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1]; // Lấy từ cuối cùng
    }
}


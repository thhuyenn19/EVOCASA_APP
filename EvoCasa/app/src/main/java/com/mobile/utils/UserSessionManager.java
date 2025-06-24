package com.mobile.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_UID = "user_uid";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public UserSessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Lưu UID
    public void saveUid(String uid) {
        editor.putString(KEY_UID, uid);
        editor.apply();
    }

    // Lấy UID
    public String getUid() {
        return prefs.getString(KEY_UID, null);
    }

    // Kiểm tra đã đăng nhập chưa
    public boolean isLoggedIn() {
        return getUid() != null;
    }

    // Xóa session (đăng xuất)
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
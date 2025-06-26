package com.mobile.evocasa.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SearchHistoryManager {
    private static final String PREF_NAME = "search_history";
    private static final String KEY_PREFIX = "history_";
    private static final int MAX_HISTORY_SIZE = 50;

    // 🔸 Lưu lịch sử tìm kiếm
    public static void saveSearch(Context context, String userId, String keyword) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String key = KEY_PREFIX + userId;

        Gson gson = new Gson();
        String json = prefs.getString(key, "");
        List<String> history = new ArrayList<>();

        if (!json.isEmpty()) {
            Type type = new TypeToken<List<String>>() {}.getType();
            history = gson.fromJson(json, type);
        }

        history.remove(keyword);           // Xoá nếu đã có (tránh trùng)
        history.add(0, keyword);           // Thêm lên đầu

        // Giới hạn số lượng
        while (history.size() > MAX_HISTORY_SIZE) {
            history.remove(history.size() - 1);
        }

        // Lưu lại
        String updatedJson = gson.toJson(history);
        prefs.edit().putString(key, updatedJson).apply();

        Log.d("SearchHistory", "✅ Đã lưu lịch sử: " + updatedJson);
    }

    // 🔸 Lấy danh sách lịch sử
    public static List<String> getHistory(Context context, String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String key = KEY_PREFIX + userId;
        String json = prefs.getString(key, "");

        if (!json.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> history = gson.fromJson(json, type);
            Log.d("SearchHistory", "📄 Lấy lịch sử: " + history);
            return history;
        }

        Log.d("SearchHistory", "📄 Lịch sử trống");
        return new ArrayList<>();
    }

    // 🔸 Xoá toàn bộ lịch sử
    public static void clearHistory(Context context, String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_PREFIX + userId).apply();
        Log.d("SearchHistory", "🗑️ Đã xoá lịch sử cho user: " + userId);
    }
}

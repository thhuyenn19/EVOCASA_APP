package com.mobile.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;


import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MetadataHelper {
    private static Map<String,Integer> prodToIdx;
    private static Map<Integer,String> idxToProd;
    private static Map<String,Integer> custToIdx;

    public static void init(Context ctx) {
        try (InputStream is = ctx.getAssets().open("id_mappings.json");
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {

            // 1) Đọc toàn bộ JSON thành JsonObject
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            Gson gson = new Gson();
            Type stringIntMapType = new TypeToken<Map<String,Integer>>(){}.getType();

            // 2) product_id_mapping: String->Integer
            prodToIdx = gson.fromJson(root.getAsJsonObject("product_id_mapping"), stringIntMapType);

            // 3) customer_id_mapping (nếu bạn đã lưu)
            custToIdx = gson.fromJson(root.getAsJsonObject("customer_id_mapping"), stringIntMapType);

            // 4) reverse_product_mapping: String->String, sau đó parse key thành Integer
            Type stringStringMapType = new TypeToken<Map<String,String>>(){}.getType();
            Map<String,String> revRaw = gson.fromJson(
                    root.getAsJsonObject("reverse_product_mapping"),
                    stringStringMapType
            );

            idxToProd = new HashMap<>();
            for (Map.Entry<String,String> e : revRaw.entrySet()) {
                idxToProd.put(Integer.parseInt(e.getKey()), e.getValue());
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load id_mappings.json", e);
        }
    }


    /** Trả về index trong model cho productId, hoặc -1 nếu không tìm thấy */
    public static int getProductIndex(String productId) {
        if (prodToIdx == null) {
            Log.e("MetadataHelper", "prodToIdx is null. Did you call MetadataHelper.init()?");
            return -1;
        }
        Integer idx = prodToIdx.get(productId);
        if (idx == null) {
            Log.e("MetadataHelper", "ProductId not found: " + productId);
            return -1;
        }
        return idx;
    }

    /** Trả về productId tương ứng với index, hoặc null nếu out-of-range */
    public static String getProductId(int index) {
        return idxToProd.get(index);
    }

    /** Trả về mảng tất cả product indices [0..N-1] */
    public static int[] getAllProductIndices() {
        int n = idxToProd.size();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) arr[i] = i;
        return arr;
    }

    /** Trả về index trong model cho userId, hoặc -1 nếu không tìm thấy */
    public static int getUserIndex(String userId) {
        Integer idx = custToIdx.get(userId);
        return idx != null ? idx : -1;
    }
    // In MetadataHelper.java, just after init(…)
    public static int getUserCount() {
        return custToIdx == null ? 0 : custToIdx.size();
    }
    public static int getProductCount() {
        return prodToIdx == null ? 0 : prodToIdx.size();
    }

}



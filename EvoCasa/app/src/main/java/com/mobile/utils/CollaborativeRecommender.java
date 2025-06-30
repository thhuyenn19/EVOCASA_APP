package com.mobile.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class CollaborativeRecommender {
    private Interpreter tflite;

    private HashMap<String, Integer> customerIdMapping;
    private HashMap<Integer, String> reverseProductMapping;

    public CollaborativeRecommender(Context context) throws IOException, JSONException {
        // Load TFLite model
        MappedByteBuffer buffer = FileUtil.loadMappedFile(context, "collaborative_model.tflite");
        tflite = new Interpreter(buffer);

        // Load id_mappings.json
        String jsonStr = loadJSONFromAsset(context.getAssets(), "id_mappings.json");
        JSONObject jsonObject = new JSONObject(jsonStr);

        customerIdMapping = new HashMap<>();
        reverseProductMapping = new HashMap<>();

        JSONObject customerObj = jsonObject.getJSONObject("customer_id_mapping");
        JSONObject reverseProdObj = jsonObject.getJSONObject("reverse_product_mapping");

        // Parse customer_id_mapping
        for (int i = 0; i < customerObj.names().length(); i++) {
            String key = customerObj.names().getString(i);
            int value = customerObj.getInt(key);
            customerIdMapping.put(key, value);
        }

        // Parse reverse_product_mapping
        for (int i = 0; i < reverseProdObj.names().length(); i++) {
            String key = reverseProdObj.names().getString(i);
            String value = reverseProdObj.getString(key);
            reverseProductMapping.put(Integer.parseInt(key), value);
        }
    }

    public List<String> recommend(String uid, int topK) {
        List<String> topProducts = new ArrayList<>();

        if (uid == null || !customerIdMapping.containsKey(uid)) {
            Log.e("RCM_DEBUG", "❌ UID not found in mapping: " + uid);
            return topProducts;
        }

        int userIdx = customerIdMapping.get(uid);
        int numProducts = reverseProductMapping.size();

        Log.d("RCM_DEBUG", "✅ Recommend called for userIdx=" + userIdx + ", numProducts=" + numProducts);

        List<Pair<String, Float>> productScores = new ArrayList<>();
        int numItems = Collections.max(reverseProductMapping.keySet()) + 1;
        for (int prodIdx = 0; prodIdx <  numItems; prodIdx++) {
            if (!reverseProductMapping.containsKey(prodIdx)) {
                Log.w("RCM_DEBUG", "⚠ Skipping product index not in mapping: " + prodIdx);
                continue;
            }

            try {
                int[][] userInput = new int[][]{{userIdx}};
                int[][] itemInput = new int[][]{{prodIdx}};
                float[][] output = new float[1][1];

                Object[] inputs = {userInput, itemInput};
                HashMap<Integer, Object> outputs = new HashMap<>();
                outputs.put(0, output);

                Log.d("RCM_DEBUG", "➡ Predicting for userIdx=" + userIdx + ", prodIdx=" + prodIdx);
                tflite.runForMultipleInputsOutputs(inputs, outputs);

                float score = output[0][0];
                String productId = reverseProductMapping.get(prodIdx);
                productScores.add(new Pair<>(productId, score));

            } catch (Exception e) {
                Log.e("RCM_DEBUG", "❌ Error predicting for userIdx=" + userIdx + ", prodIdx=" + prodIdx, e);
                // Optionally: skip this product, or stop
            }
        }

        Collections.sort(productScores, (a, b) -> Float.compare(b.second, a.second));

        for (int i = 0; i < Math.min(topK, productScores.size()); i++) {
            topProducts.add(productScores.get(i).first);
        }

        Log.d("RCM_DEBUG", "✅ Top recommended products: " + topProducts);

        return topProducts;
    }

    private String loadJSONFromAsset(AssetManager assetManager, String filename) throws IOException {
        InputStream is = assetManager.open(filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    public void close() {
        if (tflite != null) {
            tflite.close();
        }
    }
}

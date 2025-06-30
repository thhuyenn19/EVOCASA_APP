package com.mobile.utils;

import android.content.Context;
import android.content.res.AssetManager;
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
        if (uid == null || !customerIdMapping.containsKey(uid)) {
            return new ArrayList<>();  // fallback handled outside
        }


        int userIdx = customerIdMapping.get(uid);
        int numProducts = reverseProductMapping.size();

        List<Pair<String, Float>> productScores = new ArrayList<>();

        // Predict for each product
        for (int prodIdx = 0; prodIdx < numProducts; prodIdx++) {
            int[][] userInput = new int[][]{{userIdx}};
            int[][] itemInput = new int[][]{{prodIdx}};
            float[][] output = new float[1][1];

            Object[] inputs = {userInput, itemInput};
            HashMap<Integer, Object> outputs = new HashMap<>();
            outputs.put(0, output);

            tflite.runForMultipleInputsOutputs(inputs, outputs);

            float score = output[0][0];
            String productId = reverseProductMapping.get(prodIdx);
            productScores.add(new Pair<>(productId, score));
        }

        // Sort by score descending
        Collections.sort(productScores, new Comparator<Pair<String, Float>>() {
            @Override
            public int compare(Pair<String, Float> o1, Pair<String, Float> o2) {
                return Float.compare(o2.second, o1.second);
            }
        });

        // Get topK
        List<String> topProducts = new ArrayList<>();
        for (int i = 0; i < Math.min(topK, productScores.size()); i++) {
            topProducts.add(productScores.get(i).first);
        }

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

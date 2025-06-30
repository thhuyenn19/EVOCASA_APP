package com.mobile.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecommendationModel {
    private Interpreter collaborativeInterpreter;
    private Interpreter contentBasedInterpreter;

    public RecommendationModel(Context context) {
        try {
            collaborativeInterpreter = new Interpreter(loadModelFile(context, "collaborative_model.tflite"));
            contentBasedInterpreter = new Interpreter(loadModelFile(context, "content_based.tflite"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(Context context, String modelName) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Collaborative predict (userId + productId)
    public float predict(int userId, int productId) {

        if (collaborativeInterpreter == null) {
            return 0.0f;
        }

        // 1. Lấy shape của input tensor
        int maxUserIdx = MetadataHelper.getUserCount() - 1;
        int maxProdIdx = MetadataHelper.getProductCount() - 1;
        Log.d("RCM", "predict() — userId=" + userId + "/" + maxUserIdx
                + "  productId=" + productId + "/" + maxProdIdx);

        // Log hoặc kiểm tra index hợp lệ nếu biết maxUserIdx / maxProductIdx
        if (userId < 0 || userId > maxUserIdx || productId < 0 || productId > maxProdIdx) {
            throw new IllegalArgumentException("Index out of bounds userId=" + userId + ", productId=" + productId);
        }


        int[][] userInput = new int[1][1];
        int[][] productInput = new int[1][1];
        userInput[0][0] = userId;
        productInput[0][0] = productId;

        float[][] output = new float[1][1];
        Object[] inputs = {userInput, productInput};

        collaborativeInterpreter.runForMultipleInputsOutputs(inputs, new java.util.HashMap<Integer, Object>() {{
            put(0, output);
        }});

        return output[0][0];
    }


    // Content-based: get similar indices
    public int[] getSimilarProductIndices(int productIdx, int topK) {
        if (contentBasedInterpreter == null) {
            return new int[0];
        }

        int[] input = new int[]{productIdx};
        int[][][] output = new int[1][1][topK];
        contentBasedInterpreter.run(input, output);
        return output[0][0];
    }

    // Record clicks
    public static void recordUserClick(Context context, String productId) {
        SharedPreferences prefs = context.getSharedPreferences("user_clicks", Context.MODE_PRIVATE);
        int count = prefs.getInt(productId, 0);
        prefs.edit().putInt(productId, count + 1).apply();
    }

    public static boolean shouldReloadSuggestions(Context context, String uid, int threshold) {
        SharedPreferences prefs = context.getSharedPreferences("rcm_click_count", Context.MODE_PRIVATE);
        int count = prefs.getInt(uid, 0) + 1;
        prefs.edit().putInt(uid, count).apply();
        return count >= threshold;
    }

    public static void resetClickCount(Context context, String uid) {
        SharedPreferences prefs = context.getSharedPreferences("rcm_click_count", Context.MODE_PRIVATE);
        prefs.edit().putInt(uid, 0).apply();
    }

    // Optional: get top K similar using collaborative
    public List<String> getTopKSimilarCF(String productId, int userIdx, int k) {
        int[] all = MetadataHelper.getAllProductIndices();
        float[] scores = new float[all.length];
        for (int i = 0; i < all.length; i++) {
            scores[i] = predict(userIdx, all[i]);
        }

        Integer[] order = new Integer[all.length];
        for (int i = 0; i < all.length; i++) order[i] = i;
        Arrays.sort(order, (a, b) -> Float.compare(scores[b], scores[a]));

        List<String> result = new ArrayList<>();
        for (int i = 0; i < Math.min(k, order.length); i++) {
            String pid = MetadataHelper.getProductId(all[order[i]]);
            if (!pid.equals(productId)) result.add(pid);
        }
        return result;
    }
}

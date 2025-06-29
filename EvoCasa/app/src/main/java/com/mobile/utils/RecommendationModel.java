package com.mobile.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class RecommendationModel {
    private Interpreter tflite;

    // Constructor: Load the TFLite model
    public RecommendationModel(Context context) {
        try {
            // Tạo Interpreter cho mô hình TFLite
            Interpreter.Options options = new Interpreter.Options();
            tflite = new Interpreter(loadModelFile(context), options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm load mô hình từ thư mục assets
    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("recommendation_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Dự đoán cho một user-item pair
    public float predict(int userId, int productId) {
        // Tạo input tensor tách biệt
        int[][] userInput = new int[1][1];
        int[][] productInput = new int[1][1];

        userInput[0][0] = userId;
        productInput[0][0] = productId;

        // Output tensor
        float[][] output = new float[1][1];

        // Chạy mô hình với inputs tách biệt
        Object[] inputs = {userInput, productInput};
        tflite.runForMultipleInputsOutputs(inputs, new java.util.HashMap<Integer, Object>() {{
            put(0, output);
        }});

        return output[0][0];
    }
    public static void recordUserClick(Context context, String productId) {
        SharedPreferences prefs = context.getSharedPreferences("user_clicks", Context.MODE_PRIVATE);
        int count = prefs.getInt(productId, 0);
        prefs.edit().putInt(productId, count + 1).apply();
    }

}

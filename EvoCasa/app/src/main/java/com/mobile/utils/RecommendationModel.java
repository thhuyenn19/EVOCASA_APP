package com.mobile.utils;

import android.content.Context;
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
        // Khởi tạo tensor inputs (user, product)
        float[][] input = new float[1][2];
        input[0][0] = userId;
        input[0][1] = productId;

        // Output tensor
        float[][] output = new float[1][1];

        // Chạy mô hình TFLite
        tflite.run(input, output);

        // Trả về giá trị dự đoán (score)
        return output[0][0];
    }
}

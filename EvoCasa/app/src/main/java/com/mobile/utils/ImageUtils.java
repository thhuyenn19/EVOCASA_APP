package com.mobile.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;

public class ImageUtils {
    public static Bitmap getResizedBitmap(Context context, int resourceId, int maxWidth, int maxHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resourceId, options);

        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;

        float ratio = Math.min(
                (float) maxWidth / originalWidth,
                (float) maxHeight / originalHeight
        );

        int width = Math.round(originalWidth * ratio);
        int height = Math.round(originalHeight * ratio);

        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(options, width, height);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
} 
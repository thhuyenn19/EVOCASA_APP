package com.mobile.utils; // hoặc đúng package bạn đang dùng

import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.TypefaceSpan;

public class CustomTypefaceSpan extends TypefaceSpan {
    private final Typeface newType;

    public CustomTypefaceSpan(Typeface type) {
        super(""); // family không cần thiết nếu dùng custom typeface
        newType = type;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        apply(ds);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        apply(paint);
    }

    private void apply(Paint paint) {
        paint.setTypeface(newType);
        paint.setFlags(paint.getFlags() | Paint.SUBPIXEL_TEXT_FLAG); // tăng chất lượng hiển thị
    }
}

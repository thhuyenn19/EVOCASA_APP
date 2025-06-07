package com.mobile.utils;

import android.content.Context;
import android.graphics.Typeface;

public class FontUtils {
    private static Typeface bold;
    private static Typeface medium;
    private static Typeface semiBold;
    private static Typeface black;
    private static Typeface italic;
    private static Typeface regular;
    private static Typeface mediumitalic;
    private static boolean initialized = false;

    public static void initFonts(Context context) {
        if (!initialized) {
            bold = Typeface.createFromAsset(context.getAssets(), "fonts/Inter-Bold.otf");
            medium = Typeface.createFromAsset(context.getAssets(), "fonts/Inter-Medium.otf");
            semiBold = Typeface.createFromAsset(context.getAssets(), "fonts/Inter-SemiBold.otf");
            black = Typeface.createFromAsset(context.getAssets(), "fonts/Inter-Black.otf");
            italic = Typeface.createFromAsset(context.getAssets(), "fonts/Inter-Italic.otf");
            regular = Typeface.createFromAsset(context.getAssets(), "fonts/Inter-Regular.otf");
            mediumitalic = Typeface.createFromAsset(context.getAssets(), "fonts/Inter-MediumItalic.otf");
            initialized = true;
        }
    }

    public static Typeface getBold(Context context) {
        if (!initialized) initFonts(context);
        return bold;
    }

    public static Typeface getMedium(Context context) {
        if (!initialized) initFonts(context);
        return medium;
    }

    public static Typeface getSemiBold(Context context) {
        if (!initialized) initFonts(context);
        return semiBold;
    }

    public static Typeface getBlack(Context context) {
        if (!initialized) initFonts(context);
        return black;
    }

    public static Typeface getItalic(Context context) {
        if (!initialized) initFonts(context);
        return italic;
    }

    public static Typeface getRegular(Context context) {
        if (!initialized) initFonts(context);
        return regular;
    }
    public static Typeface getMediumitalic(Context context) {
        if (!initialized) initFonts(context);
        return mediumitalic;
    }


    public static void setBoldFont(Context context, android.widget.TextView textView) {
        textView.setTypeface(getBold(context));
    }

    public static void setMediumFont(Context context, android.widget.TextView textView) {
        textView.setTypeface(getMedium(context));
    }

    public static void setSemiBoldFont(Context context, android.widget.TextView textView) {
        textView.setTypeface(getSemiBold(context));
    }

    public static void setBlackFont(Context context, android.widget.TextView textView) {
        textView.setTypeface(getBlack(context));
    }

    public static void setItalicFont(Context context, android.widget.TextView textView) {
        textView.setTypeface(getItalic(context));
    }

    public static void setRegularFont(Context context, android.widget.TextView textView) {
        textView.setTypeface(getRegular(context));
    }
    public static void setMediumitalic(Context context, android.widget.TextView textView) {
        textView.setTypeface(getMediumitalic(context));
    }
}
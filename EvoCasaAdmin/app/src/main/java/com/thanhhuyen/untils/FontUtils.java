package com.thanhhuyen.untils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

public class FontUtils {
    private static Typeface bold;
    private static Typeface medium;
    private static Typeface semiBold;
    private static Typeface black;
    private static Typeface italic;
    private static Typeface regular;
    private static Typeface mediumitalic;

    private static Typeface Zblack;
    private static Typeface Zbold;
    private static Typeface Zmedium;
    private static Typeface Zregular;
    private static Typeface Zsemibold;
    private static Typeface lightitalic;

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
            lightitalic = Typeface.createFromAsset(context.getAssets(), "fonts/Inter-LightItalic.otf");


            Zblack = Typeface.createFromAsset(context.getAssets(), "fonts/ZenOldMincho-Black.ttf");
            Zbold = Typeface.createFromAsset(context.getAssets(), "fonts/ZenOldMincho-Bold.ttf");
            Zmedium = Typeface.createFromAsset(context.getAssets(), "fonts/ZenOldMincho-Medium.ttf");
            Zregular = Typeface.createFromAsset(context.getAssets(), "fonts/ZenOldMincho-Regular.ttf");
            Zsemibold = Typeface.createFromAsset(context.getAssets(), "fonts/ZenOldMincho-SemiBold.ttf");

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
    public static Typeface getLightitalic(Context context) {
        if (!initialized) initFonts(context);
        return lightitalic;
    }


    public static Typeface getZblack(Context context) {
        if (!initialized) initFonts(context);
        return Zblack;
    }

    public static Typeface getZbold(Context context) {
        if (!initialized) initFonts(context);
        return Zbold;
    }

    public static Typeface getZmedium(Context context) {
        if (!initialized) initFonts(context);
        return Zmedium;
    }

    public static Typeface getZregular(Context context) {
        if (!initialized) initFonts(context);
        return Zregular;
    }

    public static Typeface getZsemibold(Context context) {
        if (!initialized) initFonts(context);
        return Zsemibold;
    }

    public static void setBoldFont(Context context, TextView textView) {
        textView.setTypeface(getBold(context));
    }

    public static void setMediumFont(Context context, TextView textView) {
        textView.setTypeface(getMedium(context));
    }

    public static void setSemiBoldFont(Context context, TextView textView) {
        textView.setTypeface(getSemiBold(context));
    }

    public static void setBlackFont(Context context, TextView textView) {
        textView.setTypeface(getBlack(context));
    }

    public static void setItalicFont(Context context, TextView textView) {
        textView.setTypeface(getItalic(context));
    }

    public static void setRegularFont(Context context, TextView textView) {
        textView.setTypeface(getRegular(context));
    }

    public static void setMediumitalicFont(Context context, TextView textView) {
        textView.setTypeface(getMediumitalic(context));
    }

    public static void setZblackFont(Context context, TextView textView) {
        textView.setTypeface(getZblack(context));
    }

    public static void setZboldFont(Context context, TextView textView) {
        textView.setTypeface(getZbold(context));
    }

    public static void setZmediumFont(Context context, TextView textView) {
        textView.setTypeface(getZmedium(context));
    }

    public static void setZregularFont(Context context, TextView textView) {
        textView.setTypeface(getZregular(context));
    }

    public static void setZsemiboldFont(Context context, TextView textView) {
        textView.setTypeface(getZsemibold(context));
    }
    public static void setLightitalicFont(Context context, TextView textView) {
        textView.setTypeface(getLightitalic(context));
    }

    public static void applyFont(View viewById, Context context, int inter) {

    }
}

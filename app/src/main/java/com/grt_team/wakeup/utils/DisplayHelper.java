
package com.grt_team.wakeup.utils;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;

public class DisplayHelper {

    private static final int softwareBarHeight = 48; // dp

    private DisplayHelper() {
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        return width;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int height = displayMetrics.heightPixels;
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB
                || Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB_MR1) {
            height -= softwareBarHeight * displayMetrics.density;
        }
        return height;
    }

}

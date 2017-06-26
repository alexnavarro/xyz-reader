package com.example.xyzreader.util;

import android.content.Context;
import android.os.Build;

/**
 * Created by alexandrenavarro on 25/06/17.
 */

public class Util {

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        // Versions older than kitkat don't have translucent status bar
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = context.getResources().getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }
}

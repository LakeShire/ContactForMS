package com.example.contacts.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class BaseUtil {

    private static int sWidth = -1;
    private static int sHeight = -1;

    public static int getScreenWidth(Activity activity) {
//        if (sWidth == -1) {
            getDisplayMetrics(activity);
//        }
        return sWidth;
    }

    public static int getScreenHeight(Activity activity) {
//        if (sHeight == -1) {
            getDisplayMetrics(activity);
//        }
        return sHeight;
    }

    public static int dp2px(Context context, float dipValue) {
        if (context == null)
            return (int) (dipValue * 1.5);
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private static void getDisplayMetrics(Activity activity) {
        WindowManager manager = activity.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        sWidth = outMetrics.widthPixels;
        sHeight = outMetrics.heightPixels;
    }

    public static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }
}

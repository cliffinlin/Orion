package com.android.orion.utility;

import com.android.orion.setting.Constant;

public class Log {
    public static final String TAG = Constant.TAG;

    public static void e(Class<?> className, String log) {
        android.util.Log.e(TAG, "{" + className.getName() + "} " + log);
    }

    public static void d(Class<?> className, String log) {
        android.util.Log.d(TAG, "{" + className.getName() + "} " + log);
    }

    public static void i(Class<?> className, String log) {
        android.util.Log.i(TAG, "{" + className.getName() + "} " + log);
    }

    public static void w(Class<?> className, String log) {
        android.util.Log.w(TAG, "{" + className.getName() + "} " + log);
    }
}

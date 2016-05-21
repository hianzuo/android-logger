package com.hianzuo.logger;

import android.os.RemoteException;

import java.text.SimpleDateFormat;

/**
 * Created by Ryan
 * On 2016/4/21.
 */
public class Log {
    public static final int VERBOSE = android.util.Log.VERBOSE;
    public static int DEBUG = android.util.Log.DEBUG;
    public static final int INFO = android.util.Log.INFO;
    public static final int WARN = android.util.Log.WARN;
    public static final int ERROR = android.util.Log.ERROR;
    public static final int ASSERT = android.util.Log.ASSERT;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MMddHH:mm:ss.SSS|");
    private static long splitTime = 0L;

    private static String head() {
        return sdf.format(currentTimeMillis());
    }

    public static void e(String tag, String message) {
        android.util.Log.e(tag, message);
        LogServiceHelper.append(head() + "  E/" + tag + ": " + message);
    }


    public static void v(String tag, String message) {
        android.util.Log.v(tag, message);
        LogServiceHelper.append(head() + "  V/" + tag + ": " + message);
    }

    public static void i(String tag, String message) {
        android.util.Log.i(tag, message);
        LogServiceHelper.append(head() + "  I/" + tag + ": " + message);
    }

    public static void d(String tag, String message) {
        android.util.Log.d(tag, message);
        LogServiceHelper.append(head() + "  D/" + tag + ": " + message);
    }

    public static void w(String tag, String message) {
        android.util.Log.w(tag, message);
        LogServiceHelper.append(head() + "  W/" + tag + ": " + message);
    }

    public static String getStackTraceString(Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }

    public static void println(int priority, String tag, String message) {
        android.util.Log.println(priority, tag, message);
        LogServiceHelper.append(head() + "  PrintLn/" + tag + ": " + message);
    }

    public static void w(String tag, String msg, Throwable e) {
        android.util.Log.w(tag, msg, e);
    }

    public static void e(String tag, String message, Throwable e) {
        android.util.Log.e(tag, message, e);
        String throwMsg = e.getMessage();
        if (null == throwMsg) throwMsg = e.getClass().getSimpleName();
        LogServiceHelper.append(head() + "  E/" + tag + ": " + message + " Throwable:" + throwMsg);
    }

    public static long currentTimeMillis() {
        return System.currentTimeMillis() + splitTime;
    }

    public static void splitTime(long time) {
        splitTime = time;
    }

    public static void flush() {
        LogServiceHelper.flush();
    }

    public static void deleteAll(final DeleteLogCallback callback) {
        LogServiceHelper.deleteAll(new IDeleteLogCallback.Stub() {
            @Override
            public void callback(String result) throws RemoteException {
                callback.callback(result);
            }
        });
    }
}

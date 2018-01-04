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


    public static void e(String tag, String message) {
        android.util.Log.e(tag, message);
        LogServiceHelper.append("E/" + tag + ": " + message);
    }


    public static void v(String tag, String message) {
        android.util.Log.v(tag, message);
        LogServiceHelper.append("V/" + tag + ": " + message);
    }

    public static void i(String tag, String message) {
        android.util.Log.i(tag, message);
        LogServiceHelper.append( "I/" + tag + ": " + message);
    }

    public static void d(String tag, String message) {
        android.util.Log.d(tag, message);
        LogServiceHelper.append( "D/" + tag + ": " + message);
    }

    public static void w(String tag, String message) {
        android.util.Log.w(tag, message);
        LogServiceHelper.append("W/" + tag + ": " + message);
    }

    public static String getStackTraceString(Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }

    public static void println(int priority, String tag, String message) {
        android.util.Log.println(priority, tag, message);
        LogServiceHelper.append("PrintLn/" + tag + ": " + message);
    }

    public static void w(String tag, String msg, Throwable e) {
        android.util.Log.w(tag, msg, e);
    }

    public static void e(String tag, String message, Throwable e) {
        android.util.Log.d(tag, message, e);
        String throwMsg = e.getMessage();
        if (null == throwMsg) throwMsg = e.getClass().getSimpleName();
        LogServiceHelper.append("E/" + tag + ": " + message + " Throwable:" + throwMsg);
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

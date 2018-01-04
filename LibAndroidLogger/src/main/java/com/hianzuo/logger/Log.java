package com.hianzuo.logger;

import android.os.RemoteException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
        LogServiceHelper.append("I/" + tag + ": " + message);
    }

    public static void d(String tag, String message) {
        android.util.Log.d(tag, message);
        LogServiceHelper.append("D/" + tag + ": " + message);
    }

    public static void w(String tag, String message) {
        android.util.Log.w(tag, message);
        LogServiceHelper.append("W/" + tag + ": " + message);
    }

    public static void w(String tag, String message, Throwable e) {
        android.util.Log.w(tag, message, e);
        String throwMsg = getThrowMessage(e);
        LogServiceHelper.append("W/" + tag + ": " + message + " Throwable:" + throwMsg);
    }

    public static void e(String tag, String message, Throwable e) {
        android.util.Log.d(tag, message, e);
        String throwMsg = getThrowMessage(e);
        LogServiceHelper.append("E/" + tag + ": " + message + " Throwable:" + throwMsg);
    }

    public static void println(int priority, String tag, String message) {
        android.util.Log.println(priority, tag, message);
        LogServiceHelper.append("PrintLn/" + tag + ": " + message);
    }

    public static String getStackTraceString(Throwable tr) {
        return android.util.Log.getStackTraceString(tr);
    }

    private static String getThrowMessage(Throwable e) {
        String throwMsg = "null";
        if (null != e) {
            throwMsg = e.getMessage();
            if (null == throwMsg) {
                throwMsg = e.getClass().getSimpleName();
            }
        }
        return throwMsg;
    }

    public static void eThrowable(String tag, Throwable e) {
        if (null == e) {
            return;
        }
        String lines = toString(e);
        eLines(tag, lines);
    }

    public static void eLines(String tag, String lines) {
        if (null != lines && lines.length() > 0) {
            String[] ss = lines.split("\n");
            if (ss.length > 0) {
                for (String s : ss) {
                    Log.e(tag, s.replace("\t", "        "));
                }
                LogServiceHelper.flush();
            }
        }
    }


    private static String toString(Throwable e) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        e.printStackTrace(printWriter);
        printWriter.flush();
        String errMsg = writer.toString();
        printWriter.close();
        try {
            writer.close();
        } catch (IOException ignored) {
        }
        return errMsg;
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

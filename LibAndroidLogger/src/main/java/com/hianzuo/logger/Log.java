package com.hianzuo.logger;

import android.os.RemoteException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryan
 * On 2016/4/21.
 */
public class Log {
    public static final int VERBOSE = android.util.Log.VERBOSE;
    public static final int DEBUG = android.util.Log.DEBUG;
    public static final int INFO = android.util.Log.INFO;
    public static final int WARN = android.util.Log.WARN;
    public static final int ERROR = android.util.Log.ERROR;
    public static final int ASSERT = android.util.Log.ASSERT;


    public static void e(String tag, String... messages) {
        android.util.Log.e(tag, getLogMessage(messages));
        LogServiceHelper.appendLines(getLines("E", tag, messages));
    }

    private static String getLogMessage(String... messages) {
        if (messages.length == 1) {
            return messages[0];
        }
        StringBuilder sb = new StringBuilder();
        for (String message : messages) {
            sb.append(message);
            sb.append("\n");
        }
        return sb.toString();
    }

    private static List<String> getLines(String level, String tag, String... messages) {
        List<String> lines = new ArrayList<>();
        String prefix = level + "/" + tag + ": ";
        String prefixBlank = LogServiceHelper.countStrToString(" ", prefix.length());
        boolean isFirst = true;
        for (String m : messages) {
            if (isFirst) {
                isFirst = false;
                lines.add(prefix + m);
            } else {
                lines.add(prefixBlank + m);
            }
        }
        return lines;
    }


    public static void v(String tag, String... messages) {
        android.util.Log.v(tag, getLogMessage(messages));
        LogServiceHelper.appendLines(getLines("V", tag, messages));
    }

    public static void i(String tag, String... messages) {
        android.util.Log.i(tag, getLogMessage(messages));
        LogServiceHelper.appendLines(getLines("I", tag, messages));
    }

    public static void d(String tag, String... messages) {
        android.util.Log.i(tag, getLogMessage(messages));
        LogServiceHelper.appendLines(getLines("D", tag, messages));
    }

    public static void w(String tag, String... messages) {
        android.util.Log.i(tag, getLogMessage(messages));
        LogServiceHelper.appendLines(getLines("W", tag, messages));
    }

    public static void w(String tag, String messages, Throwable e) {
        Log.w(tag, messages);
        Log.logThrowable(Log.WARN, tag, e);
    }

    public static void e(String tag, String message, Throwable e) {
        Log.e(tag, message);
        Log.logThrowable(Log.ERROR, tag, e);
    }

    public static void println(int priority, String tag, String messages) {
        android.util.Log.println(priority, tag, messages);
        LogServiceHelper.append("PrintLn/" + tag + ": " + messages);
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
        logThrowable(Log.ERROR, tag, e);
    }

    public static void logThrowable(int level, String tag, Throwable e) {
        if (null == e) {
            return;
        }
        String lines = toString(e);
        logLines(level, tag, lines);
    }

    public static void eStackTrace(String tag, StackTraceElement[] elements) {
        logStackTrace(Log.ERROR, tag, elements);
    }

    public static void logStackTrace(int level, String tag, StackTraceElement[] elements) {
        if (null == elements) {
            return;
        }
        List<String> lines = new ArrayList<>();
        for (StackTraceElement element : elements) {
            lines.add(element.toString());
        }
        logStr(level, tag, lines);
    }

    private static void logStr(int level, String tag, List<String> lines) {
        logStr(level, tag, listToArray(lines));
    }

    private static void logStr(int level, String tag, String... str) {
        switch (level) {
            case VERBOSE:
                Log.v(tag, str);
                break;
            case DEBUG:
                Log.d(tag, str);
                break;
            case INFO:
                Log.i(tag, str);
                break;
            case WARN:
                Log.w(tag, str);
                break;
            case ERROR:
                Log.e(tag, str);
                break;
            default:
                throw new IllegalArgumentException("un support log level : " + level);
        }
    }

    public static void eLines(String tag, String lines) {
        logLines(Log.ERROR, tag, lines);
    }

    private static String[] listToArray(List<String> lines) {
        String[] arrays = new String[lines.size()];
        int i = 0;
        for (String line : lines) {
            arrays[i++] = line;
        }
        return arrays;
    }

    private static List<String> formatToLines(String mutiLineMsg) {
        List<String> lines = new ArrayList<>();
        if (null != mutiLineMsg && mutiLineMsg.length() > 0) {
            String[] ss = mutiLineMsg.split("\n");
            if (ss.length > 0) {
                for (String s : ss) {
                    lines.add(s.replace("\t", "        "));
                }
            }
        }
        return lines;
    }

    public static void logLines(int level, String tag, String lines) {
        Log.logStr(level, tag, formatToLines(lines));
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

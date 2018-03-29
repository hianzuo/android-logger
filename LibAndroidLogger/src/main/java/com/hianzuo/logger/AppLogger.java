package com.hianzuo.logger;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Ryan
 * On 14/12/16.
 */
class AppLogger extends Thread {
    private static int flushCount = 200;
    private static int maxCacheCount = 5000;
    private static final SimpleDateFormat fileNameSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE);
    private static final SimpleDateFormat headSdf = new SimpleDateFormat("HH:mm:ss.SSS|", Locale.CHINESE);
    private static final String headSdfBlank = "             ";

    private static String filePrefix = null;
    private static String filePath = null;
    private static final String TAG = "AppLogger";
    private static Context context;
    private static long splitTime = 0L;

    private ALArrayList tempBuffer = new ALArrayList("temp");
    private ALArrayList buffer = new ALArrayList("main");
    private final LockObj mLock = new LockObj();
    private DeleteLogTask deleteLogTask = null;

    private AppLogger() {
        setName("AppLogger_Thread");
        setPriority(1);
        setDaemon(true);
    }

    private static AppLogger appLogger = null;

    public static synchronized AppLogger init(Context context) {
        if (null == appLogger) {
            init(context, null, null, -1, -1);
        }
        return appLogger;
    }

    public static synchronized AppLogger init(Context context, String filePath, String filePrefix, int flushCount, int maxCacheCount) {
        AppLogger.context = context;
        if (null != filePrefix) {
            AppLogger.filePrefix = filePrefix;
        }
        if (null != filePath) {
            AppLogger.filePath = filePath;
        }
        if (flushCount > -1) {
            AppLogger.flushCount = flushCount;
        }
        if (maxCacheCount > -1) {
            AppLogger.maxCacheCount = maxCacheCount;
        }
        if (null == appLogger) {
            appLogger = new AppLogger();
            appLogger.start();
        }
        return appLogger;
    }

    public static synchronized void append(Context context, List<String> lines) {
        init(context);
        appLogger.appendInternal(lines);
    }

    public static synchronized void append(Context context, String line) {
        init(context);
        appLogger.appendInternal(line);
    }

    private synchronized void appendInternal(String line) {
        appendInternal(Collections.singletonList(line));
    }

    private synchronized void appendInternal(List<String> lines) {
        int bufferSize;
        synchronized (mLock) {
            String head = head();
            boolean isFirst = true;
            for (String line : lines) {
                if (isFirst) {
                    isFirst = false;
                    buffer.add(head + line);
                } else {
                    buffer.add(headSdfBlank + line);
                }
            }
            bufferSize = buffer.size();
        }
        if (bufferSize > flushCount) {
            flushLog();
        }
    }

    private long lastFlushTime = 0;

    public synchronized void flushLog() {
        long st = System.currentTimeMillis();
        if (st - lastFlushTime > 500) {
            lastFlushTime = st;
            interrupt();
//            Log.d(TAG, "flush interrupt.");
        }
    }

    private synchronized void deleteLog(DeleteLogCallback callback, String... files) {
        if (null == deleteLogTask || deleteLogTask.isDone()) {
            deleteLogTask = new DeleteLogTask(callback, files);
            interrupt();
        } else {
            callback.callback("日志正在删除中.");
        }
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            boolean lockFailure = false;
            try {
                if (null != deleteLogTask) {
                    if (deleteLogTask.isNew()) {
                        deleteLogTask.run();
                    } else if (deleteLogTask.isDone()) {
                        deleteLogTask = null;
                    }
                } else {
                    ALArrayList buffer = getFlushBuffer();
                    try {
                        lockFailure = flushToFileRetLockFailure(buffer);
                    } finally {
                        exChangeBackOnFlushBuffer(buffer);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "flush exception.", e);
            } finally {
                if (lockFailure) {
                    mLock.waitMillis(200);
                    Log.w(TAG, "lock failure");
                } else {
                    mLock.waitMillis(20000);
//                    Log.d(TAG, "flush finally.");
                }

            }
        }
    }

    private ALArrayList getFlushBuffer() {
        synchronized (mLock) {
            ALArrayList tempExchange = buffer;
            buffer = tempBuffer;
            return tempExchange;
        }
    }

    private void exChangeBackOnFlushBuffer(ALArrayList flushBuffer) {
        synchronized (mLock) {
            tempBuffer = buffer;
            buffer = flushBuffer;
            for (String line : tempBuffer) {
                flushBuffer.add(line);
            }
            tempBuffer.clear();
        }
    }

    private static boolean flushToFileRetLockFailure(List<String> buffer) {
        RandomAccessFile raf = null;
        FileLock fileLock = null;
        if (buffer.size() <= 0) {
            return false;
        }
        boolean lockFailure = false;
        boolean canRemoveBuffer = false;
        try {
            File file = getOutFile();
            if (null != file) {
                raf = new RandomAccessFile(file, "rw");
                fileLock = raf.getChannel().tryLock();
                if (null != fileLock && fileLock.isValid()) {
                    raf.seek(raf.length());
                    String data = bufferToString(buffer);
                    raf.write(data.getBytes());
                    canRemoveBuffer = true;
//                    Log.d(TAG, "flush success(" + buffer.size() + ").");
                } else {
                    lockFailure = true;
                }
            } else {
                canRemoveBuffer = true;
                Log.e(TAG, "can not get out file.");
            }
        } catch (OverlappingFileLockException e) {
            //同一个虚拟机多次锁定同一个文件会报错
            lockFailure = true;
        } catch (IOException e) {
            if (isTryLockFail(e)) {
                lockFailure = true;
            } else {
                Log.e(TAG, "lock exception", e);
            }
        } catch (Exception e) {
            Log.e(TAG, "lock exception", e);
        } finally {
            if (buffer.size() >= maxCacheCount) {
                canRemoveBuffer = true;
                lockFailure = false;
            }
            if (canRemoveBuffer) {
                buffer.clear();
            }
            releaseLock(fileLock);
            closeQuietly(raf);
        }
        return lockFailure;
    }

    private static String bufferToString(List<String> buffer) {
        StringBuilder sb = new StringBuilder();
        for (String line : buffer) {
            sb.append(line).append("\r\n");
        }
        return sb.toString();
    }

    private static boolean isTryLockFail(IOException e) {
        String message = e.getMessage();
        if (null != message) {
            // java.io.IOException: fcntl failed: EAGAIN (Try again)
            if (message.contains("EAGAIN") && message.contains("ry again")) {
                return true;
            }
        }
        return false;
    }

    private static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    private static void releaseLock(FileLock fileLock) {
        try {
            if (fileLock != null) {
                fileLock.release();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    private synchronized static File getOutFile() {
        if (null == filePrefix) {
            filePrefix = null != context ? context.getPackageName().replace(".", "_") : "com_hianzuo_logger";
        }
        String fileName = filePrefix + fileNameSdf.format(new Date()) + ".log";
        File fileDir;
        if (null == filePath) {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                fileDir = new File(Environment.getExternalStorageDirectory(), "logs/");
            } else {
                Log.e("AppLogger", "内存卡不可用.");
                return null;
            }
        } else {
            fileDir = new File(filePath);
        }
        if (fileDir.exists() || fileDir.mkdirs()) {
            File logFile = new File(fileDir, fileName);
            File pf = logFile.getParentFile();
            if (pf.exists() || pf.mkdirs()) {
                return logFile;
            } else {
                Log.e("AppLogger", "日志文件创建失败(" + pf.getAbsolutePath() + ").");
                return null;
            }
        } else {
            Log.e("AppLogger", "日志文件创建失败(" + fileDir.getAbsolutePath() + ").");
            return null;
        }
    }

    public static synchronized void splitTime(long time) {
        splitTime = time;
    }

    public static synchronized void deleteAll(Context context, DeleteLogCallback callback) {
        init(context);
        appLogger.deleteLog(callback);
    }

    public static synchronized void delete(Context context, int beforeDay) {
        init(context);
        File file = getOutFile();
        List<String> files = new ArrayList<>();
        if (null != file) {
            File[] listFiles = file.getParentFile().listFiles();
            if (null != listFiles) {
                for (File f : listFiles) {
                    if (isBeforeDay(f.getName(), beforeDay)) {
                        files.add(f.getAbsolutePath());
                    }
                }
            }
        }
        if (files.size() > 0) {
            appLogger.deleteLog(new DeleteLogCallback() {
                @Override
                public void callback(String result) {
                }
            }, files.toArray(new String[files.size()]));
        }
    }

    private static boolean isBeforeDay(String fileName, int beforeDay) {
        if (null != fileName && fileName.endsWith(".log")) {
            int dIndex = fileName.lastIndexOf(".");
            if (dIndex > 0 && fileName.length() > 12) {
                try {
                    Integer createDay = Integer.valueOf(fileName.substring(dIndex - 10, dIndex).replace("-", ""));
                    Calendar instance = Calendar.getInstance();
                    instance.add(Calendar.DAY_OF_MONTH, -beforeDay);
                    Integer beforeDayInt = Integer.valueOf(fileNameSdf.format(instance.getTime()).replace("-", ""));
                    return createDay < beforeDayInt;
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return false;
    }

    public static synchronized void flush(Context context) {
        if (null == appLogger) {
            init(context);
        }
        appLogger.flushLog();
    }

    private static String head() {
        return headSdf.format(currentTimeMillis());
    }

    private static long currentTimeMillis() {
        return System.currentTimeMillis() + splitTime;
    }

    private static String callers() {
        StackTraceElement[] elements = new Throwable().getStackTrace();
        StringBuilder sb = new StringBuilder();
        for (int i = elements.length - 1; i > -1; i--) {
            StackTraceElement element = elements[i];
            if (element.getClassName().startsWith("com.flyhand")) {
                sb.append(element.getMethodName()).append("(")
                        .append(element.getFileName()).append(":")
                        .append(element.getLineNumber()).append(")\n");
            }
        }
        return sb.toString().trim() + "\n";
    }

    interface DeleteLogCallback {
        void callback(String result);
    }

    class DeleteLogTask {
        private File[] files;
        private int state = 0;
        private DeleteLogCallback callback;

        public DeleteLogTask(DeleteLogCallback callback, String... files) {
            this.files = new File[files.length];
            for (int i = 0; i < files.length; i++) {
                this.files[i] = new File(files[i]);
            }
            this.callback = callback;
        }

        public void run() {
            try {
                state = 1;
                boolean tryAgain = false;
                for (int i = 0; i < 20; i++) {
                    if (this.files.length == 0) {
                        File file = getOutFile();
                        if (null != file) {
                            this.files = file.getParentFile().listFiles();
                        } else if (null == files) {
                            this.files = new File[0];
                        }
                    }
                    tryAgain = false;
                    if (null == this.files) {
                        this.files = new File[0];
                    }
                    for (File f : this.files) {
                        if (f.exists()) {
                            if (!f.delete()) {
                                tryAgain = true;
                            }
                        }
                    }
                    if (!tryAgain) {
                        break;
                    } else {
                        mLock.waitMillis(200);
                    }
                }
                if (tryAgain) {
                    callback.callback("部分日志未删除成功，请关闭应用后手动删除。");
                } else {
                    callback.callback("success");
                }
            } finally {
                state = 2;
            }
        }

        public boolean isNew() {
            return 0 == state;
        }

        public boolean isDone() {
            return 2 == state;
        }
    }


    static class ALArrayList extends ArrayList<String> {
        private String name;

        public ALArrayList(String name) {
            super();
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private class LockObj {
        private boolean isWaiting = false;

        public LockObj() {
            super();
        }

        public synchronized boolean waitMillis(int millis) {
            if (millis > 0) {
                try {
                    isWaiting = true;
                    wait((long) millis);
                    return true;
                } catch (Exception ignored) {
//                ignored.printStackTrace();
                    return false;
                } finally {
                    isWaiting = false;
                }
            } else {
                return true;
            }
        }

        public synchronized boolean waiting() {
            return isWaiting;
        }
    }
}

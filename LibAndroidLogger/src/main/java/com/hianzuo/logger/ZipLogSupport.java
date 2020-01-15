package com.hianzuo.logger;

import android.os.Environment;

import java.io.File;

/**
 * @author moon
 * on 2019/10/16 14:36
 */
public class ZipLogSupport {

    private static final long ZIP_TIME_INTERVAL = 24 * 3600 * 1000L;
    private static long mLastZipTime = 0L;

    private static boolean isSDCardEnable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 压缩日志文件
     */
    public static void zipLogFiles() {
        if ((System.currentTimeMillis() - mLastZipTime) > ZIP_TIME_INTERVAL) {
            if (isSDCardEnable()) {
                File outFile = AppLogger.getOutFile();
                if (null != outFile && outFile.exists()) {
                    File[] files = outFile.getParentFile().listFiles();
                    String todayLogFilePath = AppLogger.getTodayLogFilePath();
                    String path;
                    for (File file : files) {
                        path = file.getAbsolutePath();
                        if (file.getName().equals(todayLogFilePath) || ZipUtils.isZipFile(path)) {
                            continue;
                        }
                        ZipUtils.zipFile(file, path + ".zip");
                        file.delete();
                    }
                    mLastZipTime = System.currentTimeMillis();
                }
            }
        }
    }
}

package com.hianzuo.logger;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * @author moon
 * on 2019/10/16 14:36
 */
public class ZipLogSupport {

    private static boolean isSDCardEnable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 压缩日志文件
     */
    public static void zipLogFiles() {
        if (isSDCardEnable()) {
            File outFile = AppLogger.getOutFile();
            if (null != outFile) {
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
            }
        }
    }
}

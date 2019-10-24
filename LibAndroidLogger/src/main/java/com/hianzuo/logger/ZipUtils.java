package com.hianzuo.logger;


import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Ryan
 * User: Administrator
 * Date: 12-1-26
 * Time: A.M. 9:11
 */
 class ZipUtils {
    public static void unzip(String zipFile, String targetDir) throws Exception {
        FileInputStream fis = new FileInputStream(zipFile);
        ZipUtils.unzip(fis, targetDir);
        closeQuietly(fis);
    }

    public static void unzip(String zipFile, String targetDir, UnzipListener listener) throws Exception {
        FileInputStream fis = new FileInputStream(zipFile);
        ZipUtils.unzip(fis, targetDir, listener);
        closeQuietly(fis);
    }

    public static void unzip(InputStream zipFileName, String outputDirectory) throws IOException {
        ZipUtils.unzip(zipFileName, outputDirectory, null);
    }

    public static void unzip(InputStream zipFileName, String outputDirectory, UnzipListener listener) throws IOException {
        ZipInputStream in = new ZipInputStream(zipFileName);
        try {
            long length = zipFileName.available();
            long progress = 0;
            ZipEntry entry = in.getNextEntry();
            if (null != listener && null != entry) {
                listener.onDeal(entry.getName(), progress, length);
            }
            while (entry != null) {
                File file = new File(outputDirectory);
                file.mkdir();
                if (entry.isDirectory()) {
                    String name = entry.getName();
                    name = name.substring(0, name.length() - 1);
                    file = new File(outputDirectory + File.separator + name);
                    file.mkdirs();
                } else {
                    int count;
                    byte[] data = new byte[512000];
                    file = new File(outputDirectory + File.separator + entry.getName());
                    file.createNewFile();
                    FileOutputStream out = new FileOutputStream(file);
                    BufferedOutputStream dest = new BufferedOutputStream(out, 512000);
                    long entryLength = 0;
                    while ((count = in.read(data, 0, 512000)) != -1) {
                        entryLength += (int) (count * 0.91f);
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                    out.close();
                    progress += entryLength;
                }
                entry = in.getNextEntry();
                if (null != listener && null != entry) {
                    listener.onDeal(entry.getName(), progress, length);
                }
            }
            in.close();
        } finally {
            closeQuietly(in);
        }
    }

    public static boolean isZipFile(String absolutePath) {
        FileInputStream fis = null;
        ZipInputStream in = null;
        try {
            fis = new FileInputStream(absolutePath);
            in = new ZipInputStream(fis);
            ZipEntry entry = in.getNextEntry();
            return null != entry;
        } catch (IOException e) {
            return false;
        } finally {
            closeQuietly(in);
            closeQuietly(fis);
        }
    }

    //read the first file in zip file and return string
    public static String unzipString(InputStream is) {
        return unzipString(is, "utf-8");
    }

    public static String unzipString(InputStream is, String charset) {
        ZipInputStream in = new ZipInputStream(is);
        try {
            ZipEntry entry = in.getNextEntry();
            String s = null;
            if (null != entry) {
                s = inputStreamToString(in, charset);
            }
            in.close();
            return s;
        } catch (Exception ex) {
            return null;
        }
    }

    private static final String TAG = "ZipUtils";

    public static File zipFile(InputStream in, String inName, String path) {
        if (!path.endsWith(".zip")) {
            throw new RuntimeException("path is not zip file");
        }
        File tempZipFile = getZipFile(path, null);
        return zipFile(in, inName, tempZipFile);
    }

    private static File zipFile(InputStream in, String inName, File tempZipFile) {
        if (null == tempZipFile) {
            Log.w(TAG, "create zip file dir failure.");
            return null;
        }
        if (!tempZipFile.exists() || tempZipFile.delete()) {
            boolean createFile;
            try {
                createFile = tempZipFile.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, "create temp zip file error.", e);
                return null;
            }
            if (createFile) {
                FileOutputStream fos = null;
                BufferedOutputStream bos = null;
                try {
                    fos = new FileOutputStream(tempZipFile);
                    bos = new BufferedOutputStream(fos);
                    ZipUtils.zipFile(in, inName, new ZipOutputStream(bos));
                    return tempZipFile;
                } catch (IOException e) {
                    Log.e(TAG, "zip file error.", e);
                    return null;
                } finally {
                    closeQuietly(bos);
                    closeQuietly(fos);
                }
            } else {
                Log.w(TAG, "create temp zip file failure.");
                return null;
            }
        } else {
//            Log.d(TAG, "can not delete old zip file(" + tempZipFile.getName() + ")");
            return null;
        }
    }

    public static File zipFile(File in, String path) {
        if (isZipFile(in.getAbsolutePath())) {
            Log.w(TAG, "the zip file (" + in.getName() + ") is zip file.");
            return in;
        }
        if (!in.isFile()) {
            Log.w(TAG, "the in is not a file.");
            return null;
        }
        File outZipFile = getZipFile(path, in.getName());
        if (null == outZipFile) {
            Log.w(TAG, "create zip file dir failure.");
            return null;
        }
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(in);
            return zipFile(fi, in.getName(), outZipFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            closeQuietly(fi);
        }
    }

    public synchronized static File getZipFile(String path, String name) {
        if (null == path) {
            String dir = System.getProperty("java.io.tmpdir");
            File zipFileDir = new File(dir, "TempZip");
            if (zipFileDir.exists() || zipFileDir.mkdirs()) {
                return new File(zipFileDir, name);
            } else {
                return null;
            }
        } else {
            if (path.toLowerCase().endsWith(".zip")) {
                return new File(path);
            } else {
                return new File(path, name + ".zip");
            }
        }
    }

    private static final int BUFFER = 1024 * 128;//256K

    public static void zipFile(InputStream in, String inName, OutputStream out) throws IOException {
        zipFile(in, inName, getZipOutputStream(out));
    }

    public static void zipFile(String path, String inName, OutputStream out) throws IOException {
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(path);
            zipFile(fi, inName, out);
        } finally {
            closeQuietly(fi);
        }
    }

    private static ZipOutputStream getZipOutputStream(OutputStream out) {
        if (out instanceof ZipOutputStream) {
            return (ZipOutputStream) out;
        } else {
            return new ZipOutputStream(out);
        }
    }

    public static void zipFile(File in, ZipOutputStream out) throws IOException {
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(in);
            zipFile(fi, in.getName(), out);
        } finally {
            closeQuietly(fi);
        }
    }

    public static void zipFile(InputStream fi, String inName, ZipOutputStream out) throws IOException {
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(fi, BUFFER);
            out.putNextEntry(new ZipEntry(inName));
            int count;
            byte[] data = new byte[BUFFER];
            while ((count = bis.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
        } finally {
            closeQuietly(bis);
            closeQuietly(out);
        }
    }

    private static void closeQuietly(Closeable closeable) {
        if(null != closeable){
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String inputStreamToString(InputStream input, String encoding) throws IOException {
        InputStreamReader reader = null;
        StringWriter output = null;
        try {
            char[] buffer = new char[1024 * 4];
            int n;
            output = new StringWriter();
            reader = new InputStreamReader(input, encoding);
            while (-1 != (n = reader.read(buffer))) {
                output.write(buffer, 0, n);
            }
            return output.toString();
        } finally {
            closeQuietly(reader);
            closeQuietly(output);
        }
    }

    public  interface UnzipListener {

        void onDeal(String name, long progress, long count);
    }
}

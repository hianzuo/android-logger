package com.hianzuo.logger;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import java.util.List;

/**
 * Created by Ryan
 * On 2016/4/29.
 */
public class LogService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        LogServiceHelper.init(getApplication());
    }


    @Override
    public IBinder onBind(Intent intent) {
        return new ILogService.Stub() {
            @Override
            public boolean config(String path, String prefix, int flushCount, int maxCacheCount) throws RemoteException {
                AppLogger.init(LogService.this, path, prefix, flushCount, maxCacheCount);
                return true;
            }

            @Override
            public boolean path(String path, String prefix) throws RemoteException {
                AppLogger.init(LogService.this, path, prefix, -1, -1);
                return true;
            }

            @Override
            public boolean append(String line) throws RemoteException {
                AppLogger.append(LogService.this, line);
                return true;
            }

            @Override
            public boolean appendLines(List<String> lines) throws RemoteException {
                AppLogger.append(LogService.this, lines);
                return true;
            }

            @Override
            public boolean flush() throws RemoteException {
                AppLogger.flush(LogService.this);
                return true;
            }

            @Override
            public boolean delete(int beforeDay) throws RemoteException {
                AppLogger.deleteAndZip(LogService.this, beforeDay);
                return true;
            }


            @Override
            public boolean splitTime(long time) throws RemoteException {
                AppLogger.splitTime(time);
                return true;
            }

            @Override
            public void deleteAll(final IDeleteLogCallback callback) throws RemoteException {
                AppLogger.deleteAll(LogService.this, new AppLogger.DeleteLogCallback() {
                    @Override
                    public void callback(String result) {
                        RemoteCallbackList<IDeleteLogCallback> mCallbacks = new RemoteCallbackList<>();
                        mCallbacks.register(callback);
                        LogService.callback(mCallbacks, result);
                        mCallbacks.unregister(callback);
                    }
                });
            }
        };
    }

    public static void callback(RemoteCallbackList<IDeleteLogCallback> callback, String result) {
        final int len = callback.beginBroadcast();
        for (int i = 0; i < len; i++) {
            try {
                callback.getBroadcastItem(i).callback(result);
            } catch (RemoteException e) {
                // The RemoteCallbackList will take care of removing
                // the dead object for us.
            }
        }
        callback.finishBroadcast();
    }


    public static final String ACTION = "com.hianzuo.logger.ILogService";
}

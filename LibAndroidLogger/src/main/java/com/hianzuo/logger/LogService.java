package com.hianzuo.logger;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

/**
 * Created by Ryan
 * On 2016/4/29.
 */
public class LogService extends Service {
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return new ILogService.Stub() {

            @Override
            public boolean path(String path, String prefix) throws RemoteException {
                AppLogger.init(LogService.this, path, prefix);
                return true;
            }

            @Override
            public boolean append(String line) throws RemoteException {
                AppLogger.append(LogService.this, line);
                return true;
            }

            @Override
            public boolean flush() throws RemoteException {
                AppLogger.flush(LogService.this);
                return true;
            }

            @Override
            public boolean delete(int beforeDay) throws RemoteException {
                AppLogger.delete(LogService.this, beforeDay);
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

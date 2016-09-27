package com.hianzuo.logger;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Created by Ryan
 * On 2016/4/29.
 */
public class LogServiceHelper implements ServiceConnection {
    private Application application;
    private ILogService mILogService;
    private final LockObj mLock = new LockObj();
    private final Intent mIntent;
    private static LogServiceHelper helper;
    private static String path;
    private static String prefix;
    private static Application appContext;
    private static final String TAG = "AppLogger";
    private boolean mConnectedBefore = false;

    private LogServiceHelper(Application application) {
        this.application = application;
        mIntent = new Intent();
        mIntent.setAction(LogService.ACTION);
        mIntent.setPackage(application.getPackageName());
        bindILogService();
    }

    public synchronized static void init(Application application) {
        init(application, null, null);
    }

    public synchronized static void init(Application application, String path, String prefix) {
        LogServiceHelper.appContext = application;
        if (null == helper) {
            LogServiceHelper.prefix = prefix;
            LogServiceHelper.path = path;
            LogServiceHelper.helper = new LogServiceHelper(application);
        }
    }

    private void bindILogService() {
        Log.e(TAG, "log service is binding.");
        application.bindService(mIntent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.e(TAG, "log service connected.");
        mILogService = ILogService.Stub.asInterface(service);
        if (null != prefix) __prefix(path, prefix);
        mConnectedBefore = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.e(TAG, "log service disconnected.");
        mILogService = null;
        mLock.waitMillis(1000);
        bindILogService();
    }

    private boolean __prefix(String path, String prefix) {
        ILogService service = this.mILogService;
        if (null != service) {
            try {
                return service.path(path, prefix);
            } catch (RemoteException ignored) {
            }
        }
        return false;
    }

    private boolean __flush() {
        ILogService service = this.mILogService;
        if (null != service) {
            try {
                return service.flush();
            } catch (RemoteException ignored) {
            }
        }
        return false;
    }

    private boolean __delete(int day) {
        ILogService service = this.mILogService;
        if (null != service) {
            try {
                return service.delete(day);
            } catch (RemoteException ignored) {
            }
        }
        return false;
    }

    private boolean __deleteAll(IDeleteLogCallback callback) {
        ILogService service = this.mILogService;
        if (null != service) {
            try {
                service.deleteAll(callback);
                return true;
            } catch (RemoteException ignored) {
            }
        }
        return false;
    }

    private boolean __append(String line) {
        ILogService service = this.mILogService;
        if (null != service) {
            try {
                return service.append(line);
            } catch (RemoteException ignored) {
            }
        }
        return false;
    }

    public static void append(String line) {
        if (null != helper) {
            if (helper.__append(line)) {
                //在LogService Progress中记录成功
            } else {
                if (helper.mConnectedBefore) {
                    Log.e(TAG, "log in other progress.");
                    AppLogger.append(appContext, line);
                } else {
                    Log.e(TAG, "log service not init(" + line + ").");
                }
            }
        }
    }

    public static void flush() {
        if (null != helper) {
            if (helper.__flush()) {
                //在LogService Progress中Flush成功
            } else {
                if (helper.mConnectedBefore) {
                    Log.e(TAG, "flush in other progress.");
                    AppLogger.flush(appContext);
                } else {
                    Log.e(TAG, "log service not init(flush).");
                }
            }
        }
    }

    public static void delete(int day) {
        if (null != helper) {
            if (helper.__delete(day)) {
                //在LogService Progress中Flush成功
            } else {
                if (helper.mConnectedBefore) {
                    Log.e(TAG, "flush in other progress.");
                    AppLogger.delete(appContext, day);
                } else {
                    Log.e(TAG, "log service not init(flush).");
                }
            }
        }
    }

    public static void deleteAll(final IDeleteLogCallback callback) {
        if (null != helper) {
            if (helper.__deleteAll(callback)) {
                //在LogService Progress中Flush成功
            } else {
                if (helper.mConnectedBefore) {
                    Log.e(TAG, "flush in other progress.");
                    AppLogger.deleteAll(appContext, new AppLogger.DeleteLogCallback() {
                        @Override
                        public void callback(String result) {
                            try {
                                callback.callback(result);
                            } catch (RemoteException ignored) {
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "log service not init(flush).");
                }
            }
        }
    }


    private boolean __splitTime(long time) {
        ILogService service = this.mILogService;
        if (null != service) {
            try {
                service.splitTime(time);
                return true;
            } catch (RemoteException ignored) {
            }
        }
        return false;
    }


    public static synchronized void splitTime(long time) {
        if (null != helper) {
            if (helper.__splitTime(time)) {
                //在LogService Progress中 splitTime 成功
            } else {
                if (helper.mConnectedBefore) {
                    Log.e(TAG, "splitTime in other progress.");
                    AppLogger.splitTime(time);
                } else {
                    Log.e(TAG, "log service not init(splitTime).");
                }
            }
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

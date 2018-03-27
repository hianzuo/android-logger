package com.hianzuo.logger.simple;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Android6.0后需要用户同意才能应用才能获取权限
 * 这个方法就是弹出申请权限对话框让用户同意
 * <p>
 * 获取AndroidManifest.xml中声明的所有权限。
 *
 * @author Ryan
 * @date 2017/8/3.
 */
public class PermissionHandler {
    private static final String[] PERMISSION_LIST = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
    };
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 2199;
    private static int requestCount = 0;
    private static boolean callbacked = false;

    public static RequestResultScanner request(final Activity activity, final Callback callback) {
        requestCount = 0;
        callbacked = false;
        String[] permissions = getPackagePermissions(activity);
        if (null == permissions) {
            permissions = PERMISSION_LIST;
        }
        return request(activity, permissions, callback, false);
    }

    private static String[] getPackagePermissions(Activity activity) {
        try {
            String[] permissions = activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
            if (null == permissions || permissions.length == 0) {
                return null;
            }
            return permissions;
        } catch (Exception e) {
            return null;
        }
    }

    private static RequestResultScanner request(final Activity activity, final String[] permissions, final Callback callback, boolean checkDangerous) {
        if (callbacked) {
            return null;
        }
        List<String> needRequestList = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                continue;
            }
            if (checkDangerous) {
                if (!isDangerousPermission(permission)) {
                    continue;
                }
            }
            needRequestList.add(permission);
        }
        if (needRequestList.isEmpty()) {
            callbacked = true;
            callback.success();
            return null;
        }
        if (requestCount >= 2) {
            callbacked = true;
            callback.failure();
            return null;
        }
        requestCount++;
        String[] requestedPermissions = needRequestList.toArray(new String[needRequestList.size()]);
        RequestResultScanner scanner = new RequestResultScanner() {
            @Override
            public void onResult(int requestCode, String[] permissions, int[] grantResults) {
                PermissionHandler.request(activity, permissions, callback, true);
            }
        };
        ActivityCompat.requestPermissions(activity, requestedPermissions, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        return scanner;
    }

    private static final HashSet<Integer> DANGEROUS_PERMISSIONS = new HashSet<Integer>() {
        {
            add("com.google.android.gms.permission.CAR_VENDOR_EXTENSION".hashCode());
            add("com.google.android.gms.permission.CAR_MILEAGE".hashCode());
            add("com.google.android.gms.permission.CAR_FUEL".hashCode());
            add("android.permission.WRITE_CONTACTS".hashCode());
            add("android.permission.GET_ACCOUNTS".hashCode());
            add("android.permission.READ_CONTACTS".hashCode());
            add("android.permission.READ_CALL_LOG".hashCode());
            add("android.permission.READ_PHONE_STATE".hashCode());
            add("android.permission.CALL_PHONE".hashCode());
            add("android.permission.WRITE_CALL_LOG".hashCode());
            add("android.permission.USE_SIP".hashCode());
            add("android.permission.PROCESS_OUTGOING_CALLS".hashCode());
            add("com.android.voicemail.permission.ADD_VOICEMAIL".hashCode());
            add("android.permission.READ_CALENDAR".hashCode());
            add("android.permission.WRITE_CALENDAR".hashCode());
            add("android.permission.CAMERA".hashCode());
            add("android.permission.BODY_SENSORS".hashCode());
            add("android.permission.ACCESS_FINE_LOCATION".hashCode());
            add("com.google.android.gms.permission.CAR_SPEED".hashCode());
            add("android.permission.ACCESS_COARSE_LOCATION".hashCode());
            add("android.permission.READ_EXTERNAL_STORAGE".hashCode());
            add("android.permission.WRITE_EXTERNAL_STORAGE".hashCode());
            add("android.permission.RECORD_AUDIO".hashCode());
            add("android.permission.READ_SMS".hashCode());
            add("android.permission.RECEIVE_WAP_PUSH".hashCode());
            add("android.permission.RECEIVE_MMS".hashCode());
            add("android.permission.RECEIVE_SMS".hashCode());
            add("android.permission.SEND_SMS".hashCode());
            add("android.permission.READ_CELL_BROADCASTS".hashCode());

        }
    };

    private static boolean isDangerousPermission(String permission) {
        return DANGEROUS_PERMISSIONS.contains(permission.hashCode());
    }

    public interface Callback {
        /**
         * 成功获取所有权限
         */
        void success();

        /**
         * 权限获取失败，可能是部分权限获取失败
         */
        void failure();
    }

    public interface RequestResultScanner {
        /**
         * 在Activity回调时回调该方法，以验证权限是否成功获取
         */
        void onResult(int requestCode, String[] permissions, int[] grantResults);
    }

}

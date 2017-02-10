package com.siziksu.barcode.common.manager;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.siziksu.barcode.common.Constants;

public class PermissionManager {

    public static final int REQUEST_CAMERA = 13426;
    public static final int GRANT_CAMERA = 0;

    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.CAMERA
    };

    public PermissionManager() {}

    public boolean verifyPermission(Activity activity, String permission) {
        if (permission.equals(Manifest.permission.CAMERA)) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int selfPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);
                if (selfPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermission(activity, permission);
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkStoragePermissionsResult(int[] grantResults, int item) {
        return grantResults.length != 0 && grantResults[item] == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(Activity activity, String permission) {
        if (permission.equals(Manifest.permission.CAMERA)) {
            Log.i(Constants.TAG, "Camera permission is not granted. Requesting permission");
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_CAMERA
            );
        }
    }
}

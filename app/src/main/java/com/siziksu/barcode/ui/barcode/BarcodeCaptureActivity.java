package com.siziksu.barcode.ui.barcode;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Switch;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.siziksu.barcode.R;
import com.siziksu.barcode.common.Constants;
import com.siziksu.barcode.common.camera.CameraSourcePreview;
import com.siziksu.barcode.common.manager.CameraManager;
import com.siziksu.barcode.common.manager.PermissionManager;

public final class BarcodeCaptureActivity extends AppCompatActivity {

    public static final String BARCODE_KEY = "Barcode";

    private PermissionManager permissionManager;
    private CameraManager cameraManager;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.barcode_capture);
        Switch useFlashSwitch = (Switch) findViewById(R.id.use_flash);
        useFlashSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> cameraManager.setFlash(isChecked)
        );
        cameraManager = new CameraManager();
        cameraManager.setCameraSourcePreview((CameraSourcePreview) findViewById(R.id.preview));
        cameraManager.setListener(value -> {
            Intent data = new Intent();
            data.putExtra(BARCODE_KEY, value);
            setResult(CommonStatusCodes.SUCCESS, data);
            finish();
        });
        permissionManager = new PermissionManager();
        if (permissionManager.verifyPermission(this, Manifest.permission.CAMERA)) {
            createCameraSource();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraManager.startCameraSource(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraManager.stopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraManager.releaseCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionManager.REQUEST_CAMERA:
                if (permissionManager.checkStoragePermissionsResult(grantResults, PermissionManager.GRANT_CAMERA)) {
                    createCameraSource();
                } else {
                    DialogInterface.OnClickListener listener = (dialog, id) -> finish();
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(Constants.TAG)
                           .setMessage(R.string.no_camera_permission)
                           .setPositiveButton(R.string.ok, listener)
                           .show();
                }
                break;
            default:
                Log.i(Constants.TAG, "Got unexpected permission result: " + requestCode);
                break;
        }
    }

    private void createCameraSource() {
        cameraManager.createCameraSource(this);
    }
}

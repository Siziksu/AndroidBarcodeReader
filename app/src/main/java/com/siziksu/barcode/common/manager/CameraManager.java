package com.siziksu.barcode.common.manager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.siziksu.barcode.R;
import com.siziksu.barcode.common.Constants;
import com.siziksu.barcode.common.camera.CameraSource;
import com.siziksu.barcode.common.camera.CameraSourcePreview;

import java.io.IOException;

public class CameraManager {

    private static final int PLAY_SERVICES = 9001;

    private Listener listener;

    private CameraSource cameraSource;
    private CameraSourcePreview preview;

    public void createCameraSource(Context context) {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {

            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> codes = detections.getDetectedItems();
                if (codes.size() != 0) {
                    listener.detectionReceived(codes.valueAt(0).displayValue);
                }
            }
        });
        if (!barcodeDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(Constants.TAG, context.getString(R.string.dependencies_not_available));
            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = context.registerReceiver(null, lowStorageFilter) != null;
            if (hasLowStorage) {
                Toast.makeText(context, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(Constants.TAG, context.getString(R.string.low_storage_error));
            }
        }
        // Creates and starts the camera. Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        CameraSource.Builder builder = new CameraSource.Builder(context, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f);
        // Make sure that auto focus is an available option
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = builder.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        cameraSource = builder.setFlashMode(null).build();
    }

    public void startCameraSource(Activity activity) throws SecurityException {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);
        if (code != ConnectionResult.SUCCESS) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(activity, code, PLAY_SERVICES);
            dialog.show();
        }
        if (cameraSource != null) {
            try {
                preview.start(cameraSource);
            } catch (IOException e) {
                Log.e(Constants.TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setCameraSourcePreview(CameraSourcePreview preview) {
        this.preview = preview;
    }

    public void setFlash(boolean isChecked) {
        cameraSource.setFlashMode(isChecked ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
    }

    public void stopCamera() {
        if (preview != null) {
            preview.stop();
        }
    }

    public void releaseCamera() {
        if (preview != null) {
            preview.release();
        }
    }

    public interface Listener {

        void detectionReceived(String value);
    }
}

package com.siziksu.barcode.common.camera;

import android.Manifest;
import android.content.Context;
import android.support.annotation.RequiresPermission;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.siziksu.barcode.common.Constants;

import java.io.IOException;

public class CameraSourcePreview extends ViewGroup {

    private SurfaceView surfaceView;
    private boolean startRequested;
    private boolean surfaceAvailable;
    private CameraSource cameraSource;

    private int layoutWidth = 0;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        startRequested = false;
        surfaceAvailable = false;
        surfaceView = new SurfaceView(context);
        surfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(surfaceView);
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void start(CameraSource cameraSource) throws IOException, SecurityException {
        if (cameraSource == null) {
            stop();
        }
        this.cameraSource = cameraSource;
        if (this.cameraSource != null) {
            startRequested = true;
            startIfReady();
        }
    }

    public void stop() {
        if (cameraSource != null) {
            cameraSource.stop();
        }
    }

    public void release() {
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private void startIfReady() throws IOException, SecurityException {
        if (startRequested && surfaceAvailable) {
            cameraSource.start(surfaceView.getHolder());
            startRequested = false;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        layoutWidth = w - this.getPaddingLeft() - this.getPaddingRight();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width > height ? height : width;
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int measure = layoutWidth;
        final int layoutWidth = right - left;
        final int layoutHeight = bottom - top;
        int childWidth = layoutWidth;
        int childHeight = (int) (((float) layoutWidth / (float) measure) * measure);
        if (childHeight > layoutHeight) {
            childHeight = layoutHeight;
            childWidth = (int) (((float) layoutHeight / (float) measure) * measure);
        }
        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).layout(0, 0, childWidth, childHeight);
        }
        try {
            startIfReady();
        } catch (SecurityException se) {
            Log.e(Constants.TAG, "Do not have permission to start the camera", se);
        } catch (IOException e) {
            Log.e(Constants.TAG, "Could not start camera source.", e);
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            surfaceAvailable = true;
            try {
                startIfReady();
            } catch (SecurityException se) {
                Log.e(Constants.TAG, "Do not have permission to start the camera", se);
            } catch (IOException e) {
                Log.e(Constants.TAG, "Could not start camera source.", e);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            surfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }
    }
}

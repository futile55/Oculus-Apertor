package org.waoss.oculus.apertor.camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.v4.app.ActivityCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;

import java.io.IOException;

public class CameraSourcePreview extends ViewGroup {

    private static final String TAG = CameraSourcePreview.class.getSimpleName();

    private CameraSource cameraSource;
    private boolean startRequested = false;
    private boolean surfaceAvailable = false;
    private SurfaceView surfaceView;
    private Context context;

    public CameraSourcePreview(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        surfaceView = new SurfaceView(context);
        surfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(surfaceView);
    }

    public void start(CameraSource source) throws IOException {
        if (source != null) {
            this.cameraSource = source;
            startRequested = true;
            startIfReady();
        } else {
            stop();
        }
    }

    private void startIfReady() throws IOException {
        if (startRequested && surfaceAvailable) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) !=
                    PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cameraSource.start(surfaceView.getHolder());
            startRequested = false;
        }
    }

    public void stop() {
        if (cameraSource != null) {
            cameraSource.stop();
        }
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        int previewWidth = 320;
        int previewHeight = 240;
        if (cameraSource != null) {
            Size size = cameraSource.getPreviewSize();
            if (size != null) {
                previewWidth = size.getWidth();
                previewHeight = size.getHeight();
            }
        }
        if (isPortraitMode()) {
            int tmp = previewWidth;
            //noinspection SuspiciousNameCombination
            previewWidth = previewHeight;
            previewHeight = tmp;
        }
        final int viewWidth = right - left;
        final int viewHeight = bottom - top;
        int childWidth;
        int childHeight;
        int childXOffset = 0;
        int childYOffset = 0;
        float widthRatio = (float) viewWidth / (float) previewWidth;
        float heightRatio = (float) viewHeight / (float) previewHeight;
        if (widthRatio > heightRatio) {
            childWidth = viewWidth;
            childHeight = (int) ((float) previewHeight * widthRatio);
            childYOffset = (childHeight - viewHeight) / 2;
        } else {
            childWidth = (int) ((float) previewWidth * heightRatio);
            childHeight = viewHeight;
            childXOffset = (childWidth - viewWidth) / 2;
        }

        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).layout(
                    -1 * childXOffset, -1 * childYOffset,
                    childWidth - childXOffset, childHeight - childYOffset);
        }

        try {
            startIfReady();
        } catch (IOException e) {
            Log.e(TAG, "Could not start camera source.", e);
        }
    }

    private boolean isPortraitMode() {
        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }
        Log.d(TAG, "isPortraitMode() returning true by default");
        return true;
    }

    public void release() {
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(final SurfaceHolder surfaceHolder) {
            surfaceAvailable = true;
            try {
                startIfReady();
            } catch (IOException e) {
                Log.e(TAG, "IO Exception \n {}", e);
            }
        }

        @Override
        public void surfaceChanged(final SurfaceHolder surfaceHolder, final int i, final int i1, final int i2) {
        }

        @Override
        public void surfaceDestroyed(final SurfaceHolder surfaceHolder) {
            surfaceAvailable = false;
        }
    }
}

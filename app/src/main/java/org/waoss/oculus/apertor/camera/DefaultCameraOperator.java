package org.waoss.oculus.apertor.camera;

import android.content.Context;
import com.google.android.gms.vision.CameraSource;

public class DefaultCameraOperator implements CameraOperator {
    private final Context context;
    private CameraSource cameraSource;
    private boolean isFrontFacing;

    public DefaultCameraOperator(final Context context) {
        this.context = context;
    }

    @Override
    public void createCameraSource() {

    }

    @Override
    public void startCameraSource() {

    }

    @Override
    public void createFaceDetector(final Context context) {

    }

    @Override
    public CameraSource getCameraSource() {
        if (cameraSource != null) {
            return cameraSource;
        }
        createCameraSource();
        return cameraSource;
    }

    @Override
    public boolean isFrontFacing() {
        return isFrontFacing;
    }

    @Override
    public void setFrontFacing(final boolean isFrontFacing) {
        this.isFrontFacing = isFrontFacing;
    }
}

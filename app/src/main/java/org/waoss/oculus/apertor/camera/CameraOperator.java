package org.waoss.oculus.apertor.camera;

import android.content.Context;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.face.FaceDetector;

public interface CameraOperator {
    void createCameraSource();

    void startCameraSource();

    FaceDetector createFaceDetector(Context context);

    CameraSource getCameraSource();

    void setCameraSource(CameraSource cameraSource);

    boolean isFrontFacing();

    void setFrontFacing(boolean isFrontFacing);
}

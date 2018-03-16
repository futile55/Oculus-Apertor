package org.waoss.oculus.apertor.camera;

import android.content.Context;
import com.google.android.gms.vision.CameraSource;

public interface CameraOperator {
    void createCameraSource();

    void startCameraSource();

    void createFaceDetector(Context context);

    CameraSource getCameraSource();
}

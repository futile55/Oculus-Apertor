package org.waoss.oculus.apertor.camera;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;

public interface EyesClosedListener {
    void onEyesClosed(Detector.Detections<Face> detections, Face face);
}

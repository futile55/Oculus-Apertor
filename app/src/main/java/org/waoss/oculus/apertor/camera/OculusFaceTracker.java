package org.waoss.oculus.apertor.camera;

import android.graphics.PointF;
import android.util.SparseArray;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

public class OculusFaceTracker extends Tracker<Face> {
    public static final float EYE_OPEN_LIMIT = 0.4f;
    private final SparseArray<PointF> previousProportions = new SparseArray<>();
    private final EyesClosedListener listener;
    private boolean isLeftEyeOpen;
    private boolean isRightEyeOpen;
    private boolean previousIsLeftOpen;
    private boolean previousIsRightOpen;
    public static final long AVERAGE_BLINK_TIME = 350L;
    private EyeState currentEyeState;
    private EyeState previousEyeState = new EyeState(false, System.currentTimeMillis());

    public OculusFaceTracker(EyesClosedListener listener) {
        this.listener = listener;
    }


    @Override
    public void onUpdate(final Detector.Detections<Face> detections, final Face face) {
        updatePreviousProportions(face);

        float leftEyeIsOpenProbability = face.getIsLeftEyeOpenProbability();
        float rightEyeIsOpenProbability = face.getIsRightEyeOpenProbability();
        isLeftEyeOpen = isEyeOpen(leftEyeIsOpenProbability, previousIsLeftOpen);
        isRightEyeOpen = isEyeOpen(rightEyeIsOpenProbability, previousIsRightOpen);
        if (!isLeftEyeOpen && !isRightEyeOpen) {
            // Eyes closed state.
            currentEyeState = new EyeState(true, System.currentTimeMillis());
            if (previousEyeState.isClosed() &&
                    currentEyeState.getTime() - previousEyeState.getTime() > AVERAGE_BLINK_TIME) {
                listener.onEyesClosed(detections, face);
            }
        } else {
            // Eyes open state.
            currentEyeState = new EyeState(false, System.currentTimeMillis());
        }
        if (previousEyeState.isClosed() != currentEyeState.isClosed()) {
            previousEyeState = currentEyeState;
        }
    }

    private void updatePreviousProportions(final Face face) {
        for (Landmark landmark : face.getLandmarks()) {
            PointF position = landmark.getPosition();
            float xProp = (position.x - face.getPosition().x) / face.getWidth();
            float yProp = (position.y - face.getPosition().y) / face.getHeight();
            previousProportions.put(landmark.getType(), new PointF(xProp, yProp));
        }
    }

    private boolean isEyeOpen(float probability, boolean previousValue) {
        if (probability == Face.UNCOMPUTED_PROBABILITY) {
            return previousValue;
        } else {
            return (probability > EYE_OPEN_LIMIT);
        }
    }


}

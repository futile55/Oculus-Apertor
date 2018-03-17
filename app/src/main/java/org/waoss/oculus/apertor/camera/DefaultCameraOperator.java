package org.waoss.oculus.apertor.camera;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.*;
import com.google.android.gms.vision.face.*;
import org.waoss.oculus.apertor.DrivingActivity;

import java.io.IOException;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.makeText;

public class DefaultCameraOperator implements CameraOperator {
    private static final int RC_HANDLE_GMS = 9001;
    private final Activity activity;
    private CameraSource cameraSource;
    private boolean isFrontFacing = true;
    private final EyesClosedListener eyesClosedListener;
    private final String tag;
    private CameraSourcePreview cameraSourcePreview;
    private DrivingActivity.Mode mode;
    public static final long EXERCISE_MODE_TIME = 10000;

    public DefaultCameraOperator(final Activity activity, final String tag,
                                 final EyesClosedListener eyesClosedListener,
                                 final CameraSourcePreview cameraSourcePreview) {
        this.activity = activity;
        this.tag = tag;
        this.eyesClosedListener = eyesClosedListener;
        this.cameraSourcePreview = cameraSourcePreview;
    }

    public void createAndStartCameraSource() {
        createCameraSource();
        startCameraSource();
    }

    @Override
    public void createCameraSource() {
        FaceDetector faceDetector = createFaceDetector(activity.getApplicationContext());
        int facing = CameraSource.CAMERA_FACING_FRONT;
        if (!isFrontFacing()) {
            facing = CameraSource.CAMERA_FACING_BACK;
        }
        cameraSource = new CameraSource.Builder(activity, faceDetector)
                .setFacing(facing)
                .setAutoFocusEnabled(true)
                .setRequestedFps(60.0f)
                .setRequestedPreviewSize(320, 240)
                .build();
    }

    @Override
    public void startCameraSource() {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity.getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(activity, code, RC_HANDLE_GMS);
            dlg.show();
        }
        if (cameraSource != null) {
            try {
                cameraSourcePreview.start(cameraSource);
            } catch (IOException e) {
                Log.w(tag, "Couldn't start camera\n {}", e);
            }
        }
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
    public void setCameraSource(final CameraSource cameraSource) {
        this.cameraSource = cameraSource;
    }

    @Override
    public boolean isFrontFacing() {
        return isFrontFacing;
    }

    @Override
    public void setFrontFacing(final boolean isFrontFacing) {
        this.isFrontFacing = isFrontFacing;
    }

    @Override
    public FaceDetector createFaceDetector(final Context context) {
        FaceDetector detector = new FaceDetector.Builder(context)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setTrackingEnabled(true)
                .setMode(FaceDetector.FAST_MODE)
                .setProminentFaceOnly(isFrontFacing)
                .setMinFaceSize(isFrontFacing ? 0.35f : 0.15f)
                .build();
        Detector.Processor<Face> processor;
        if (isFrontFacing) {
            Tracker<Face> faceTracker = new OculusFaceTracker(eyesClosedListener,
                    getMode() ==
                            DrivingActivity.Mode.DRIVING ? OculusFaceTracker.AVERAGE_BLINK_TIME : EXERCISE_MODE_TIME);
            processor = new LargestFaceFocusingProcessor.Builder(detector, faceTracker).build();
        } else {
            MultiProcessor.Factory<Face> factory = new MultiProcessor.Factory<Face>() {
                @Override
                public Tracker<Face> create(Face face) {
                    return new OculusFaceTracker(eyesClosedListener);
                }
            };
            processor = new MultiProcessor.Builder<>(factory).build();
        }
        detector.setProcessor(processor);
        if (!detector.isOperational()) {
            makeText
                    (activity,
                            "Face Processing is not operational. This maybe due to lack of device storage.",
                            LENGTH_LONG)
                    .show();
        }
        return detector;
    }

    @Override
    public DrivingActivity.Mode getMode() {
        return mode;
    }

    @Override
    public void setMode(final DrivingActivity.Mode mode) {
        this.mode = mode;
    }
}

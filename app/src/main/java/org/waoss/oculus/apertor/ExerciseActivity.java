package org.waoss.oculus.apertor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;
import org.waoss.oculus.apertor.camera.*;

import static org.waoss.oculus.apertor.DrivingActivity.PLAY_BEEP_SOUND;

public class ExerciseActivity extends AppCompatActivity implements EyesClosedListener {

    public static final String TAG = ExerciseActivity.class.getSimpleName();
    private CameraSourcePreview cameraSourcePreview;
    private DefaultCameraOperator defaultCameraOperator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);
        cameraSourcePreview = findViewById(R.id.exercise_preview);
        defaultCameraOperator = new DefaultCameraOperator(this, TAG, this, cameraSourcePreview);
        if (savedInstanceState != null) {
            defaultCameraOperator.setFrontFacing(savedInstanceState.getBoolean("IsFrontFacing"));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (defaultCameraOperator.getCameraSource() != null) {
            defaultCameraOperator.getCameraSource().release();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("IsFrontFacing", defaultCameraOperator.isFrontFacing());
    }

    @Override
    public void onEyesClosed(final Detector.Detections<Face> detections, final Face face) {
        Log.i(TAG, "Eyes closed");
        runOnUiThread(PLAY_BEEP_SOUND);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraSourcePreview.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        defaultCameraOperator.startCameraSource();
    }
}

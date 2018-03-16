package org.waoss.oculus.apertor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;
import org.waoss.oculus.apertor.camera.*;

public class DrivingActivity extends AppCompatActivity implements EyesClosedListener {

    public static final String TAG = DrivingActivity.class.getSimpleName();
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private CameraSourcePreview cameraSourcePreview;
    private View root;
    private DefaultCameraOperator defaultCameraOperator;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving);
        requestPermissions();
        cameraSourcePreview = findViewById(R.id.preview);
        root = findViewById(R.id.root);
        defaultCameraOperator = new DefaultCameraOperator(this, TAG, this,
                cameraSourcePreview);
        if (savedInstanceState != null) {
            defaultCameraOperator.setFrontFacing(savedInstanceState.getBoolean("IsFrontFacing"));
        }
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            defaultCameraOperator.createCameraSource();
        } else {
            requestPermissions();
        }
    }

    private void requestPermissions() {
        Log.w(TAG, "Camera is not granted permission. Requesting for permission");

        final String permissions[] = new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };
        Snackbar.make(root, "Please give permission to access the camera and GPS", Snackbar.LENGTH_INDEFINITE)
                .setAction("OK", listener)
                .show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (defaultCameraOperator.getCameraSource() != null) {
            defaultCameraOperator.getCameraSource().release();
        }
        textToSpeech.shutdown();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("IsFrontFacing", defaultCameraOperator.isFrontFacing());
    }

    @Override
    public void onEyesClosed(final Detector.Detections<Face> detections, final Face face) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraSourcePreview.stop();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        defaultCameraOperator.startCameraSource();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            defaultCameraOperator.createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage("NO permission camera. App sad")
                .setPositiveButton("ok", listener)
                .show();
    }
}

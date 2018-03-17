package org.waoss.oculus.apertor;

import android.Manifest;
import android.app.Activity;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.face.Face;
import org.waoss.oculus.apertor.camera.*;
import org.waoss.oculus.apertor.location.OculusLocationListener;
import org.waoss.oculus.apertor.map.MapsActivity;
import org.waoss.oculus.apertor.service.CrashService;

public class DrivingActivity extends AppCompatActivity implements EyesClosedListener {


    public enum Mode {
        EXERCISE, DRIVING
    }


    public static final String TAG = DrivingActivity.class.getSimpleName();
    public static final Runnable PLAY_BEEP_SOUND = new Runnable() {
        @Override
        public void run() {
            final ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION,
                    ToneGenerator.MAX_VOLUME);
            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 500);
            toneGenerator.release();
        }
    };
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    public static final float LOCATION_REFRESH_DISTANCE = 1;
    private static final int PICK_CONTACT = 1;
    private CameraSourcePreview cameraSourcePreview;
    private View root;
    private DefaultCameraOperator defaultCameraOperator;
    private ListView contactView;
    public static final long LOCATION_REFRESH_TIME = 2000;
    private static final int RC_HANDLE_ACCESS_FINE_LOCATION = 5;

    public static final String MY_PREFERENCES = "MyPrefs";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driving);
        requestPermissions();
        startService(new Intent(this, CrashService.class));
        makeSharedPreferences();
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
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager
                .requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE,
                        new OculusLocationListener(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(PLAY_BEEP_SOUND);
                            }
                        }), Looper.getMainLooper());
    }

    private void makeSharedPreferences() {
        sharedPreferences = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

    }

    private void requestPermissions() {
        Log.w(TAG, "Camera is not granted permission. Requesting for permission");

        final String permissions[] = new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS};
        ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
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

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_CONTACT:
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor cursor = getContentResolver().query(contactData, null, null,
                            null, null);
                    if (cursor.moveToFirst()) {
                        String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        String hasPhone = cursor
                                .getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver()
                                    .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null,
                                            null);
                            phones.moveToFirst();
                            String cNumber = phones
                                    .getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            editor.putString("number", cNumber);
                            editor.commit();
                        }
                    }
                }
        }
    }

    public void onFlipButtonClicked(final View view) {
        defaultCameraOperator.setFrontFacing(!defaultCameraOperator.isFrontFacing());
        if (defaultCameraOperator.getCameraSource() != null) {
            defaultCameraOperator.getCameraSource().release();
            defaultCameraOperator.setCameraSource(null);
        }
        defaultCameraOperator.createAndStartCameraSource();
    }

    public void onShowMapButtonClicked(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_HANDLE_CAMERA_PERM:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[3] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Log.e(TAG, "LOL");
                }
                break;
        }
    }

    public void onContactsButtonClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }


    public void onExerciseButtonClicked(View view) {
        Intent intent = new Intent(this, ExerciseActivity.class);
        startActivity(intent);
    }
}

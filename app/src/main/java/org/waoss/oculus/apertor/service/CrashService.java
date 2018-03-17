package org.waoss.oculus.apertor.service;

import android.app.Service;
import android.content.Intent;
import android.hardware.*;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.*;
import android.os.Process;
import android.widget.Toast;

public class CrashService extends Service {

    private Looper looper;
    private ServiceHandler serviceHandler;
    private SensorManager sensorManager;
    private Sensor sensor;

    @Override
    public void onCreate() {

        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();


        looper = thread.getLooper();
        serviceHandler = new ServiceHandler(looper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();


        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(new SensorEventListener() {
                @Override
                public void onSensorChanged(final SensorEvent sensorEvent) {
                    float x = sensorEvent.values[0];
                    float y = sensorEvent.values[1];
                    float z = sensorEvent.values[2];

                    float gX = x / 9.8f;
                    float gY = y / 9.8f;
                    float gZ = z / 9.8f;

                    double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);
                    if (gForce > 35) {
                        final ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION,
                                ToneGenerator.MAX_VOLUME);
                        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
                        toneGenerator.release();
                    }
                }

                @Override
                public void onAccuracyChanged(final Sensor sensor, final int i) {
                }
            }, sensor, 500);

            stopSelf(msg.arg1);
        }
    }
}

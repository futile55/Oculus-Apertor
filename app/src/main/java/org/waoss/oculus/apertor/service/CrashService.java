package org.waoss.oculus.apertor.service;

import android.app.Service;
import android.content.*;
import android.hardware.*;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.*;
import android.os.Process;
import android.support.annotation.NonNull;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.android.gms.location.places.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import okhttp3.*;

import java.io.IOException;
import java.util.*;

public class CrashService extends Service {

    private Looper looper;
    private ServiceHandler serviceHandler;
    private SensorManager sensorManager;
    private Sensor sensor;
    public static final String TAG = CrashService.class.getSimpleName();
    List<CharSequence> phoneNumbers = new ArrayList<>();
    boolean hasFilledPhoneNumbers = false;

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
        private static final String CRASH_TEXT = "Kindly ignore the message!";

        private ObjectMapper objectMapper = new ObjectMapper();
        private GeoDataClient geoDataClient;
        private OkHttpClient client = new OkHttpClient();

        public ServiceHandler(Looper looper) {
            super(looper);
            geoDataClient = Places.getGeoDataClient(getApplicationContext(), null);
        }

        @Override
        public void handleMessage(Message msg) {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            sensorManager.registerListener(new SensorEventListener() {
                Integer smsCount = 0;

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
                        sendSMS();
                        if (!hasFilledPhoneNumbers) {
                            fillPhoneNumbers();
                        }
                        sendSMSToNearbyHospitals();
                    }
                }

                private void sendSMS() {
                    smsCount++;
                    if (smsCount > 5) {
                        return;
                    }
                    Log.d(TAG, "Into sendSMS()");
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    String number = sharedPreferences.getString("number", "100");
                    if (number != null) {
                        SmsManager.getDefault()
                                .sendTextMessage(number, null,
                                        CRASH_TEXT,
                                        null, null);
                    }
                }

                private void fillPhoneNumbers() {
                    HttpUrl.Builder httpUrlBuilder = HttpUrl
                            .parse("https://maps.googleapis.com/maps/api/place/nearbysearch/json").newBuilder();
                    httpUrlBuilder.addQueryParameter("location", "25.4263508,81.7732333");
                    httpUrlBuilder.addQueryParameter("radius", "1000");
                    httpUrlBuilder.addQueryParameter("types", "hospital");
                    httpUrlBuilder.addQueryParameter("key", "AIzaSyDDyx4sdmsirBzdlmvK6v2-Hmcj7VzosGc");
                    HttpUrl httpUrl = httpUrlBuilder.build();
                    Request request = new Request.Builder()
                            .url(httpUrl)
                            .get()
                            .build();
                    Response response = null;
                    try {
                        response = client.newCall(request).execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (response != null) {
                        try {
                            JsonNode jsonNode = objectMapper.readTree(response.body().byteStream());
                            ArrayNode results = (ArrayNode) jsonNode.get("results");
                            Iterator<JsonNode> resultsIterator = results.elements();
                            while (resultsIterator.hasNext()) {
                                JsonNode nextNode = resultsIterator.next();
                                String placeId = nextNode.get("place_id").asText();
                                geoDataClient.getPlaceById(placeId)
                                        .addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                                            @Override
                                            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                                                if (task.isSuccessful()) {
                                                    PlaceBufferResponse places = task.getResult();
                                                    Place myPlace = places.get(0);
                                                    phoneNumbers.add(myPlace.getPhoneNumber());
                                                    Log.i(TAG, "Place found: " + myPlace.getName());
                                                    places.release();
                                                } else {
                                                    Log.e(TAG, "Place not found.");
                                                }
                                            }
                                        });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        hasFilledPhoneNumbers = true;
                    }
                }

                private void sendSMSToNearbyHospitals() {
                    for (CharSequence phoneNumber : phoneNumbers) {
                        SmsManager.getDefault().sendTextMessage(phoneNumber.toString(), null, CRASH_TEXT, null, null);
                    }

                }

                @Override
                public void onAccuracyChanged(final Sensor sensor, final int i) {
                }
            }, sensor, 500);
        }
    }
}

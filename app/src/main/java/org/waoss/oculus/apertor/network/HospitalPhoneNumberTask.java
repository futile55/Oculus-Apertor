package org.waoss.oculus.apertor.network;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.android.gms.location.places.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import okhttp3.*;

import java.io.IOException;
import java.util.*;

public class HospitalPhoneNumberTask extends AsyncTask<Void, Void, List<CharSequence>> {
    public static final String TAG = HospitalPhoneNumberTask.class.getSimpleName();
    private ObjectMapper objectMapper = new ObjectMapper();
    private GeoDataClient geoDataClient;
    private OkHttpClient client = new OkHttpClient();
    private Context context;

    public HospitalPhoneNumberTask(Context context) {
        this.context = context;
    }

    @Override
    protected List<CharSequence> doInBackground(final Void... voids) {
        final List<CharSequence> phoneNumbers = new ArrayList<>();
        geoDataClient = Places.getGeoDataClient(context, null);
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
        }
        return phoneNumbers;
    }

    @Override
    protected void onPostExecute(final List<CharSequence> charSequences) {
        super.onPostExecute(charSequences);
    }
}

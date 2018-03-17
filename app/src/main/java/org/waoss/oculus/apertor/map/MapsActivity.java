package org.waoss.oculus.apertor.map;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.waoss.oculus.apertor.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final float LOCATION_REFRESH_DISTANCE = 1;
    public static final long LOCATION_REFRESH_TIME = 2000;

    private GoogleMap map;
    private MapLocationListener mapLocationListener;
    private Handler handler;
    private Runnable updateMap = new Runnable() {
        @Override
        public void run() {
            Location currentLocation = mapLocationListener.getCurrentLocation();
            LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            map.addMarker(new MarkerOptions().position(currentLatLng).title("Marker"));
            map.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
            handler.postDelayed(updateMap, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        handler = new Handler();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        mapLocationListener = new MapLocationListener(map);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager
                .requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE,
                        mapLocationListener);
        handler.postDelayed(updateMap, 500);
    }

}

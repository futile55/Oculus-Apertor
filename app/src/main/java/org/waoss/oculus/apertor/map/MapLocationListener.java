package org.waoss.oculus.apertor.map;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import com.google.android.gms.maps.GoogleMap;

public class MapLocationListener implements LocationListener {

    private GoogleMap map;
    private Location currentLocation;

    public MapLocationListener(final GoogleMap map) {
        this.map = map;
    }

    @Override
    public void onLocationChanged(final Location location) {
        currentLocation = location;
    }

    @Override
    public void onStatusChanged(final String s, final int i, final Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(final String s) {

    }

    @Override
    public void onProviderDisabled(final String s) {

    }

    public Location getCurrentLocation() {
        return currentLocation;
    }
}

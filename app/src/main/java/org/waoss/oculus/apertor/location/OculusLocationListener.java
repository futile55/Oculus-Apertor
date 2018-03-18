package org.waoss.oculus.apertor.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class OculusLocationListener implements LocationListener {
    public static final String TAG = OculusLocationListener.class.getSimpleName();
    private final Runnable activity;
    private static final double SPEED_LIMIT = 20.0;
    private Location currentLocation = null;
    private Location previousLocation = null;
    private double time;
    private double previousTime;
    private double currentTime;
    private double speed;

    public OculusLocationListener(final Runnable activity) {
        this.activity = activity;
    }

    @Override
    public void onLocationChanged(final Location location) {

        if (previousLocation == null && currentLocation == null) {
            // Initial state.
            previousLocation = location;
            previousTime = 0;
            return;
        }

        currentLocation = location;
        currentTime = System.currentTimeMillis();

        //time in seconds.
        time = (currentTime - previousTime) / 1000;
        previousTime = currentTime;

        // speed in m/s
        speed = previousLocation.distanceTo(currentLocation) / time;

        if (speed > SPEED_LIMIT) {
            Log.w(TAG, "Dheere chala");
            activity.run();
        }
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
}

package org.waoss.oculus.apertor.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class OculusLocationListener implements LocationListener {
    public static final String TAG = OculusLocationListener.class.getSimpleName();
    private final Runnable activity;

    public OculusLocationListener(final Runnable activity) {
        this.activity = activity;
    }

    @Override
    public void onLocationChanged(final Location location) {
        if (location.getSpeed() > 10) {
            Log.w(TAG, "Dheere chala!");
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

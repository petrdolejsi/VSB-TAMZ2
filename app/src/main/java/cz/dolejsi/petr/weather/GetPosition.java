package cz.dolejsi.petr.weather;

import android.app.Activity;
import android.app.Service;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Petrd on 23.11.17.
 */

public class GetPosition extends MainActivity implements LocationListener {

    SharedPreferences prefs;

    boolean canGetLocation = true;
    Location loc;

    private final static int ALL_PERMISSIONS_RESULT = 101;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

    public GetPosition(){
        //prefs = activity.getPreferences(Activity.MODE_PRIVATE);
    }

    // If the user has not chosen a city yet, return
    // Sydney as the default city
    String getCity(){
        getLocation();
        //  lat=35&lon=139
        if (loc == null) {
            loc.setLatitude(0);
            loc.setLongitude(0);
        }
        return "lat=" + String.valueOf(loc.getLatitude()) + "&lon="  + String.valueOf(loc.getLongitude());
    }

    public Location getLoc () {
        getLocation();
        return loc;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("GPS", "onLocationChanged");
        loc=location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderEnabled(String s) {
        getLocation();
    }

    @Override
    public void onProviderDisabled(String s) {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    private void getLocation() {
        try {
            if (loc == null) {
                loc = new Location("nwm");
            }
            if (canGetLocation) {
                Log.d("GPS", "Can get location");
                if (isGPS) {
                    // from GPS
                    Log.d("GPS", "GPS on");
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                } else if (isNetwork) {
                    // from Network Provider
                    Log.d("GPS", "NETWORK_PROVIDER on");
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                } else {
                    Log.d("GPS", "Set default location");
                    loc.setLatitude(0.0);
                    loc.setLongitude(0.0);
                }
            } else {
                Log.d("GPS", "Can't get location");
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}

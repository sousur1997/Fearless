package android.srrr.com.fearless;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

public class LocationFetch implements LocationListener {
    private Context app_context;
    private Location curr_loc;
    LocationManager locationManager;
    private double latitude, longitude;
    final Looper looper = null;
    private boolean updated = false;

    private Criteria criteria;

    public boolean getUpdated(){
        return updated;
    }

    public LocationFetch(Context ctx) {
        this.app_context = ctx;
        getLocation();
    }

    public double fetchLatitude() {
        return latitude;
    }

    public double fetchLongitude() {
        return longitude;
    }

    public void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(app_context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(app_context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestSingleUpdate(criteria, this, looper);
        //locationManager.removeUpdates(this);
    }

    public void getLocation() {
        //setup the location fetch criteria
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);

        locationManager = (LocationManager) app_context.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(app_context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(app_context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        //locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 5000, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        /*Toast.makeText(app_context, "Search:Police Station" +
                        "\nLat:" + location.getLatitude() + "\nLng:"+location.getLongitude(),
                Toast.LENGTH_LONG).show();*/
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        updated = true;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        app_context.startActivity(settingsIntent);
    }
}

package android.srrr.com.fearless;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import static android.srrr.com.fearless.FearlessConstant.ACTUAL_START_ALERT;
import static android.srrr.com.fearless.FearlessConstant.ACTUAL_STOP_ALERT;
import static android.srrr.com.fearless.FearlessConstant.ALERT_BROADCAST_STOP;
import static android.srrr.com.fearless.FearlessConstant.ALERT_CHANNEL;
import static android.srrr.com.fearless.FearlessConstant.ALERT_COMPLETE;
import static android.srrr.com.fearless.FearlessConstant.ALERT_JSON_FILENAME;

public class AlertService extends Service implements LocationListener{
    private NotificationActionReceiver receiver;
    private String message = "Press CALL to call <First Contact>";
    private AlertControl alertControl;

    private LocationManager locationManager;
    private AlertEvent alertEvent;

    private long locationInterval;
    private int locationUpdateOffset;

    private PreferenceManager prefManager;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.setPriority(100);
        receiver = new NotificationActionReceiver();
        registerReceiver(receiver, filter);

        locationInterval = 60*1000; //set to 60 seconds, it will be changed using settings value
        locationUpdateOffset = (int) locationInterval/(60*1000); //will also be updated using setting value

        alertControl = AlertControl.getInstance(getApplicationContext());
        prefManager = new PreferenceManager(getApplicationContext());

        prefManager.setBool(ALERT_COMPLETE, false);

        alertEvent = new AlertEvent(locationUpdateOffset);

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationInterval, 0, this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            if (intent.getAction().equals(ACTUAL_STOP_ALERT)) {
                //when alert end
                createCacheWithJson(generateEventJSON());
                alertControl.toggleAlreadyAlerted();

                stopForeground(true);
                stopSelf();
            }else if(intent.getAction().equals(ACTUAL_START_ALERT)){
                //when alert starts
                Intent notificationIntent = new Intent(this, AppActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

                Intent closeIntent = new Intent(this, AlertCloseConfirmActivity.class);
                closeIntent.setAction(ALERT_BROADCAST_STOP);
                PendingIntent stopServiceIntent = PendingIntent.getActivity(this, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                String number = "9999999999"; //will update by taking contact from list
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + number));
                PendingIntent callPendingIntent = PendingIntent.getActivity(this, 0, callIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification notification = new NotificationCompat.Builder(this, ALERT_CHANNEL)
                        .setContentTitle("Alert is active")
                        .setContentText(message)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .addAction(R.drawable.close_icon, "Cancel", stopServiceIntent)
                        .addAction(R.drawable.trusted_call, "Call", callPendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setColor(getResources().getColor(R.color.menu_bar_color))
                        .build();

                startForeground(2, notification);
            }
        }
        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getApplicationContext(), "Lat:" + location.getLatitude() + "\nLng:"+location.getLongitude(), Toast.LENGTH_LONG).show();
        alertEvent.addLocation(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        //If the GPS service is disabled, open the system settings to enable it.
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(settingsIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            locationManager.removeUpdates(this);
        }
    }

    private String generateEventJSON(){
        String jsonString = null;
        Gson gson = new Gson();
        jsonString = gson.toJson(alertEvent); //convert object to JSON String
        return jsonString;
    }

    private void createCacheWithJson(String json){
        try {
            File jsonOpFile = new File(getFilesDir(), ALERT_JSON_FILENAME);
            FileOutputStream fout = new FileOutputStream(jsonOpFile);
            OutputStreamWriter writer = new OutputStreamWriter(fout);

            writer.append(json); //add json into file
            writer.close();
            fout.flush();
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package android.srrr.com.fearless;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Locale;

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
    private int historyUpdateOffset;
    private PrevTaskCounter messageTimer, historyUpdateTimer;
    private int smsInterval;
    private boolean automaticMessageRepeat, singleFlag;
    private String address;

    private double latitude, longitude;

    private PreferenceManager prefManager;
    private SharedPreferences preferences;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        singleFlag = true;

        IntentFilter filter = new IntentFilter();
        filter.setPriority(100);
        receiver = new NotificationActionReceiver();
        registerReceiver(receiver, filter);

        alertControl = AlertControl.getInstance(getApplicationContext());
        prefManager = new PreferenceManager(getApplicationContext());

        prefManager.setBool(ALERT_COMPLETE, false);
        preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        locationInterval = 15*1000; //location details will be updated in 15 seconds
        String historyIntervalInSeconds = preferences.getString("key_history_update_interval", null);
        if(historyIntervalInSeconds != null){
            historyUpdateOffset = Integer.parseInt(historyIntervalInSeconds);
        }

        String intervalValue = preferences.getString("automatic_message_repeat_duration", null);
        if(intervalValue != null){ smsInterval = Integer.parseInt(intervalValue);}

        //if automaticMessageRepeat is set, it will send repeatedly using given interval value. otherwise send only once
        automaticMessageRepeat = preferences.getBoolean("key_automatic_message_repeat", true);

        alertEvent = new AlertEvent(historyUpdateOffset);

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationInterval, 0, this);

        messageTimer = null;
        historyUpdateTimer = null;
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

                boolean callEnable = preferences.getBoolean("key_call_enabled", true);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ALERT_CHANNEL)
                        .setContentTitle("Alert is active")
                        .setContentText(message)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .addAction(R.drawable.close_icon, "Cancel", stopServiceIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setColor(getResources().getColor(R.color.menu_bar_color));

                if(callEnable){
                    builder.addAction(R.drawable.trusted_call, "Call", callPendingIntent);
                }

                Notification notification = builder.build();

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
        //Toast.makeText(getApplicationContext(), "Lat:" + location.getLatitude() + "\nLng:"+location.getLongitude(), Toast.LENGTH_LONG).show();
        latitude = location.getLatitude(); longitude = location.getLongitude();

        String tempAddress = getCurrentAddress(location.getLatitude(), location.getLongitude());
        if(tempAddress == null && !isNetworkConnected()) { //if the address is found, otherwise network is not connected
            address = "Current address is not found, Get location from this link\nhttps://maps.google.com/?q="+latitude+","+longitude+"";
        }else{
            address = "Current address: " + tempAddress + "\nView on Google Map: https://maps.google.com/?q="+latitude+","+longitude+"";
        }

        if(automaticMessageRepeat) {
            if (messageTimer == null) {
                messageTimer = new PrevTaskCounter(smsInterval * 10000, 1000) {
                    @Override
                    public void onBeforeCount() {
                        Toast.makeText(getApplicationContext(), "Sending message: " + address, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onTick(long millisUntilFinished) {
                        //noting to do in each second
                    }

                    @Override
                    public void onFinish() {
                        count();//start the timer again
                    }
                };
                messageTimer.count();
            }
        }else {
            //send single SMS and stop
            if (singleFlag) {
                Toast.makeText(getApplicationContext(), "Sending Single SMS: " + address, Toast.LENGTH_LONG).show();
                singleFlag = false;
            }
        }

        if(historyUpdateTimer == null){
            historyUpdateTimer = new PrevTaskCounter(historyUpdateOffset * 1000, 1000) {
                @Override
                public void onBeforeCount() {
                    alertEvent.addLocation(latitude, longitude); //store history details
                }

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    count(); //start the timer again
                }
            };
            historyUpdateTimer.count();
        }
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
        if(receiver != null) {
            unregisterReceiver(receiver);
        }
        if(messageTimer != null)
            messageTimer.cancel(); //cancel the timer

        if(historyUpdateTimer != null)
            historyUpdateTimer.cancel(); //cancel the timer
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

    public String getCurrentAddress(double lat, double lng){
        String address = null;
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try{
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1); //get max one address

            if(addresses != null){
                Address returnAddress = addresses.get(0);
                StringBuilder addressStringBuilder = new StringBuilder("Current Location: ");
                addressStringBuilder.append("Address:" + returnAddress.getAddressLine(0));

                address = addressStringBuilder.toString();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return address;
    }

    private boolean isNetworkConnected(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return (info != null && info.isConnectedOrConnecting());
    }
}

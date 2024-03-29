package safetyapp.srrr.com.fearless;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static safetyapp.srrr.com.fearless.FearlessConstant.ACTUAL_START_ALERT;
import static safetyapp.srrr.com.fearless.FearlessConstant.ACTUAL_STOP_ALERT;
import static safetyapp.srrr.com.fearless.FearlessConstant.ALERT_BROADCAST_STOP;
import static safetyapp.srrr.com.fearless.FearlessConstant.ALERT_CHANNEL;
import static safetyapp.srrr.com.fearless.FearlessConstant.ALERT_COMPLETE;
import static safetyapp.srrr.com.fearless.FearlessConstant.ALERT_JSON_FILENAME;
import static safetyapp.srrr.com.fearless.FearlessConstant.CHANNEL_NAME;
import static safetyapp.srrr.com.fearless.FearlessConstant.CONTACT_LOCAL_FILENAME;
import static safetyapp.srrr.com.fearless.FearlessConstant.PUBLISH_KEY;
import static safetyapp.srrr.com.fearless.FearlessConstant.STOP_ALL_SCR;
import static safetyapp.srrr.com.fearless.FearlessConstant.SUBSCRIBE_KEY;

public class AlertService extends Service implements LocationListener{
    private NotificationActionReceiver receiver;
    private String message = "";
    private AlertControl alertControl;

    private LocationManager locationManager;
    private AlertEvent alertEvent;

    private long locationInterval;
    private int historyUpdateOffset;
    private PrevTaskCounter messageTimer, historyUpdateTimer;
    private int smsInterval, contactCount;
    private boolean automaticMessageRepeat, singleFlag, autoCall, callEnable;
    private String address;

    private double latitude, longitude;
    private Long timestampLong;
    private String timestamp;


    private PreferenceManager prefManager;
    private SharedPreferences preferences;
    private ArrayList<PersonalContact> contactList;

    private CountDownTimer countDownTimer;
    private boolean alertStopped;

    private PNConfiguration pnConfiguration;
    private PubNub pubNub;

    private JsonObject alertMessage;
    private FirebaseUser firebaseUser;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        timestampLong = new Long(System.currentTimeMillis());
        timestamp = timestampLong.toString();
        singleFlag = true;
        alertStopped = false;
        contactList = new ArrayList<>();
        getPersonalContacts(); //read the personal contacts to send SMS and call

        IntentFilter filter = new IntentFilter();
        filter.setPriority(100);
        receiver = new NotificationActionReceiver();
        registerReceiver(receiver, filter);

        alertControl = AlertControl.getInstance(getApplicationContext());
        prefManager = new PreferenceManager(getApplicationContext());

        prefManager.setBool(ALERT_COMPLETE, false);
        preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        locationInterval = 15*1000; //location details will be updated in 15 seconds
        String historyIntervalInSeconds = preferences.getString("key_history_update_interval", "30");
        if(historyIntervalInSeconds != null){
            historyUpdateOffset = Integer.parseInt(historyIntervalInSeconds);
        }

        String intervalValue = preferences.getString("automatic_message_repeat_duration", "5");
        if(intervalValue != null){ smsInterval = Integer.parseInt(intervalValue);}

        String contactCountStr = preferences.getString("key_select_top_contacts", "3");
        if(contactCountStr != null){ contactCount = Integer.parseInt(contactCountStr);}

        //if automaticMessageRepeat is set, it will send repeatedly using given interval value. otherwise send only once
        automaticMessageRepeat = preferences.getBoolean("key_automatic_message_repeat", true);
        callEnable = preferences.getBoolean("key_call_enabled", true);
        autoCall = preferences.getBoolean("automatic_call", true);

        if(callEnable == false){ //if call feature is not enabled, also disable auto call feature
            autoCall = false;
        }

        alertEvent = new AlertEvent(historyUpdateOffset);
//       This was the previous method of collecting location
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, locationInterval, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, locationInterval, 0, this);

        messageTimer = null;
        historyUpdateTimer = null;

        //set 30 mins timer. If alert is not stopped, send notification after 30 mins
        countDownTimer = new CountDownTimer(30*60*1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(alertStopped)
                    cancel();
            }

            @Override
            public void onFinish() {
                askNotification();
                start(); //start again for next 30 minutes
            }
        };

        countDownTimer.start();

        //pubnub initialize
        pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey(SUBSCRIBE_KEY);
        pnConfiguration.setPublishKey(PUBLISH_KEY);
        pubNub = new PubNub(pnConfiguration);

        //get current user id
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        alertMessage = new JsonObject();
    }

    private void askNotification(){
        Intent notificationIntent = new Intent(this, AppActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ALERT_CHANNEL)
                .setContentTitle("Alert is still active")
                .setContentText("Are you safe now? If you feel safe, close the alert")
                .setSmallIcon(R.mipmap.notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.alert_noti_logo))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setColor(getResources().getColor(R.color.menu_bar_color));

        Notification notification = builder.build();
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        manager.notify(5, notification);
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            if (intent.getAction().equals(ACTUAL_STOP_ALERT)) {
                //when alert end
                alertStopped = true;
                createCacheWithJson(generateEventJSON());
                alertControl.setAlreadyAlerted(false);

                stopForeground(true);
                stopSelf();
            }else if(intent.getAction().equals(ACTUAL_START_ALERT)){
                //when alert starts
                Intent notificationIntent = new Intent(this, AppActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

                Intent closeIntent = new Intent(this, AlertCloseConfirmActivity.class);
                closeIntent.setAction(ALERT_BROADCAST_STOP);
                PendingIntent stopServiceIntent = PendingIntent.getActivity(this, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                String number = contactList.get(0).getPhone(); //Number of the first contact
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + number));
                PendingIntent callPendingIntent = PendingIntent.getActivity(this, 0, callIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                if(callEnable){
                    message = "Press CALL button to call " + contactList.get(0).getName();
                }else{
                    message = "Press CANCEL button to close alert";
                }

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ALERT_CHANNEL)
                        .setContentTitle("Alert is active")
                        .setContentText(message)
                        .setSmallIcon(R.mipmap.notification_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.alert_noti_logo))
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .addAction(R.drawable.close_icon, "Cancel", stopServiceIntent)
                        .setStyle(new androidx.media.app.NotificationCompat.MediaStyle())
                        .setColor(getResources().getColor(R.color.menu_bar_color));

                if(callEnable){
                    builder.addAction(R.drawable.trusted_call, "Call", callPendingIntent);
                }

                Notification notification = builder.build();

                //if auto call feature is enabled, call the first contact automatically
                if(autoCall){
                    callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(callIntent);

                }

                startForeground(2, notification);

                //send first sms without location details
                sendMessage("I'm at Risk!!! Trying to send my current address within a few seconds!", contactCount);

                if(isServiceRunning(AllScreenService.class)){
                    Intent stopAllScr = new Intent(AlertService.this, AllScreenService.class);
                    stopAllScr.setAction(STOP_ALL_SCR);
                    ContextCompat.startForegroundService(getApplicationContext(), stopAllScr);
                }
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
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //Toast.makeText(getApplicationContext(), "Lat:" + location.getLatitude() + "\nLng:"+location.getLongitude(), Toast.LENGTH_LONG).show();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        String tempAddress = getCurrentAddress(location.getLatitude(), location.getLongitude());
        if(tempAddress == null && !isNetworkConnected()) { //if the address is found, otherwise network is not connected
            address = "I am in Risk, Location Link:\nhttps://maps.google.com/?q="+latitude+","+longitude+"";
        }else{
            if(tempAddress == null || tempAddress.equals("null")){
                address = "I am at risk, Location Link:\nhttps://maps.google.com/?q="+latitude+","+longitude+"";
            }else{
                address = "I am at risk!!! My " + tempAddress;
            }
        }

        if(automaticMessageRepeat) {
            if (messageTimer == null) {
                messageTimer = new PrevTaskCounter(smsInterval * 60 * 1000, 1000) {
                    @Override
                    public void onBeforeCount() {
                        //Toast.makeText(getApplicationContext(), "Sending message: " + address, Toast.LENGTH_LONG).show();
                       sendMessage(address, contactCount);
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
                sendMessage(address, contactCount);
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

        //add properties to the message that is going to be published in the alert channel
        timestampLong = new Long(System.currentTimeMillis());
        timestamp = timestampLong.toString(); //get the UNIX timestamp
        alertMessage.addProperty("uid",firebaseUser.getUid());
        alertMessage.addProperty("timestamp",timestamp);
        alertMessage.addProperty("latitude",latitude);
        alertMessage.addProperty("longitude",longitude);
        alertMessage.addProperty("message","alert");

        //publish the message in the channel
        pubNub.publish()
                .message(alertMessage)
                .channel(CHANNEL_NAME)
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        //check if the message is published correctly
                    }
                });
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
//          showGPSDisabledAlertToUser();
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

        timestampLong = new Long(System.currentTimeMillis());
        timestamp = timestampLong.toString(); //get the UNIX timestamp
        alertMessage.addProperty("uid",firebaseUser.getUid());
        alertMessage.addProperty("timestamp",timestamp);
        alertMessage.addProperty("latitude",latitude);
        alertMessage.addProperty("longitude",longitude);
        alertMessage.addProperty("message","alert_end");


        pubNub.publish()
                .message(alertMessage)
                .channel(CHANNEL_NAME)
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        //check if the message is published correctly
                    }
                });
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
                StringBuilder addressStringBuilder = new StringBuilder("current ");
                addressStringBuilder.append("location: " + returnAddress.getAddressLine(0));

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

    private void getPersonalContacts(){
        PersonalContact[] contactArr;
        Gson gson = new Gson();

        File file = new File(getFilesDir(), CONTACT_LOCAL_FILENAME);
        if(file.exists()) {
            String jsonStr = readJsonFile(CONTACT_LOCAL_FILENAME); //read local file from array
            contactArr = gson.fromJson(jsonStr, PersonalContact[].class);

            if (contactArr != null) {
                for (PersonalContact item : contactArr) {
                    if (item != null) {
                        contactList.add(item);
                    }
                }
            }
        }
    }

    private String readJsonFile(String filename){
        String listJson = "";
        int n;
        try {
            FileInputStream fis = getApplicationContext().openFileInput(filename);
            StringBuffer fileContent = new StringBuffer();

            byte[] buffer = new byte[4096];
            while((n = fis.read(buffer)) != -1){
                fileContent.append(new String(buffer, 0, n));
            }
            fis.close();
            listJson = fileContent.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listJson;
    }

    private void sendMessage(String message, int contact_count){
        if(contact_count > contactList.size()){ //if list has few contacts, update contact count
            contact_count = contactList.size();
        }

        for(int i = 0; i<contact_count; i++){
            try {
                //***Important code. Will charge money for SMS***//
                SmsManager smsManager = SmsManager.getDefault();
                if(message.length() > 160){
                    ArrayList<String> parts = smsManager.divideMessage(message);
                    smsManager.sendMultipartTextMessage(contactList.get(i).getPhone(), null, parts, null, null);
                }else{
                    smsManager.sendTextMessage(contactList.get(i).getPhone(), null, message, null, null);
                }
                Toast.makeText(getApplicationContext(), "SMS Sent ", Toast.LENGTH_LONG).show();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}

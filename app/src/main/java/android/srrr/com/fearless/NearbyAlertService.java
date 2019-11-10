package android.srrr.com.fearless;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNMembershipResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNSpaceResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNUserResult;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

import static android.srrr.com.fearless.FearlessConstant.ALERT_INIT_START;
import static android.srrr.com.fearless.FearlessConstant.ALERT_RAISE_BROADCAST;
import static android.srrr.com.fearless.FearlessConstant.ALL_SCREEN_CHANNEL;
import static android.srrr.com.fearless.FearlessConstant.CHANNEL_NAME;
import static android.srrr.com.fearless.FearlessConstant.NEARBY_ALERT_CHANNEL;
import static android.srrr.com.fearless.FearlessConstant.NEARBY_ALERT_SEND;
import static android.srrr.com.fearless.FearlessConstant.START_ALERT;
import static android.srrr.com.fearless.FearlessConstant.START_ALL_SCR;
import static android.srrr.com.fearless.FearlessConstant.START_NEARBY_SERVICE;
import static android.srrr.com.fearless.FearlessConstant.STOP_ALL_SCR;
import static android.srrr.com.fearless.FearlessConstant.STOP_NEARBY_SERVICE;
import static android.srrr.com.fearless.FearlessConstant.SUBSCRIBE_KEY;

public class NearbyAlertService extends Service {

    private Notification notification;
    private NotificationActionReceiver receiver;
    private AlertControl aControl;
    private PNConfiguration pnConfiguration;
    private PubNub pubNub;
    private String alertNotification;
    private Gson gsonObject;
    private LocationFetch loc_fetch;
    private Location alertlocation;
    private Location userlocation;
    private SharedPreferences sharedPreferences;

    double lati,longi;                  //stores the current user latitude and longitude
    private float distanceValue;

    private FirebaseUser firebaseUser;
    private boolean checkActivityOpen;
    private boolean addFlag;
    private CopyOnWriteArrayList<NearbyAlertDataModel> objectArray;

    @Override
    public void onCreate() {
        super.onCreate();

        checkActivityOpen = false;
        alertNotification = "{   \"uid\": \"null\",   \"timestamp\": null,   \"latitude\": null,   \"longitude\": null }";
        sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String distanceRadiusValue = sharedPreferences.getString("key_receive_alert_distance","200");
        if(distanceRadiusValue!= null){ distanceValue = Float.parseFloat(distanceRadiusValue);}
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        objectArray = new CopyOnWriteArrayList<>();
        loc_fetch = new LocationFetch(this);
        alertlocation = new Location("");
        userlocation = new Location("");
        gsonObject = new Gson();
        //pnconfigurtion is the pubnub config file and pubNub is the pubnub instance
        pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey(SUBSCRIBE_KEY);
        pubNub = new PubNub(pnConfiguration);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            //if we get a stop request, we stop the service
            if(intent.getAction().equals(STOP_NEARBY_SERVICE)) {
                pubNub.unsubscribe().channels(Arrays.asList(CHANNEL_NAME)).execute();
                stopService(intent);
                stopSelf();

            }
            else if(intent.getAction().equals(START_NEARBY_SERVICE)) {
//                stopSelf(4);
                //startService(intent);
                pubNub.addListener(new SubscribeCallback() {
                    @Override
                    public void status(PubNub pubnub, PNStatus pnStatus) {

                    }

                    @Override
                    public void message(PubNub pubnub, PNMessageResult pnMessageResult) {
                        Log.e("Subscription Result",pnMessageResult.getMessage().toString());
                        alertNotification = pnMessageResult.getMessage().toString();
                        sendNearbyMessageData(alertNotification);
//                        checkIfOutdated();
                    }

                    @Override
                    public void presence(PubNub pubnub, PNPresenceEventResult pnPresenceEventResult) {

                    }

                    @Override
                    public void signal(PubNub pubnub, PNSignalResult pnSignalResult) {

                    }

                    @Override
                    public void user(PubNub pubnub, PNUserResult pnUserResult) {

                    }

                    @Override
                    public void space(PubNub pubnub, PNSpaceResult pnSpaceResult) {

                    }

                    @Override
                    public void membership(PubNub pubnub, PNMembershipResult pnMembershipResult) {

                    }

                    @Override
                    public void messageAction(PubNub pubnub, PNMessageActionResult pnMessageActionResult) {

                    }
                });
                ///subscribe to the particular channel
                pubNub.subscribe().channels(Arrays.asList(CHANNEL_NAME)).execute();
            }
        }

        return START_REDELIVER_INTENT;

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("-----Log-----", "Service Removed");
        super.onTaskRemoved(rootIntent);
    }

    private void sendNearbyMessageData(String jsonAlertString) {
        //a handler is attached to get Location data. This is associated with the main threads messageQueue.
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                loc_fetch.fetchCurrentLocation();
                lati = loc_fetch.fetchLatitude();
                longi = loc_fetch.fetchLongitude();
                Log.e("latitude value",Double.toString(loc_fetch.fetchLatitude()));
                Log.e("longitude value",Double.toString(loc_fetch.fetchLongitude()));
            }
        });

        String currUid = firebaseUser.getUid();             //get current user id
        NearbyAlertDataModel nearbyAlertDataModel = new NearbyAlertDataModel();    //parse the received subscription string
        nearbyAlertDataModel = gsonObject.fromJson(jsonAlertString,nearbyAlertDataModel.getClass());
        Log.e("Parsed Json data:",nearbyAlertDataModel.toString());
        alertlocation.setLatitude(nearbyAlertDataModel.getLatitude());      //alert location is a location instance used to get the distance difference between the user and the alert issuer.
        alertlocation.setLongitude(nearbyAlertDataModel.getLongitude());
        userlocation.setLatitude(lati);             //the current user's location.
        userlocation.setLongitude(longi);
        float distanceInMeters = userlocation.distanceTo(alertlocation);        //current distance
        Log.e("own latitude value",Double.toString(userlocation.getLatitude()));
        Log.e("own longitude value",Double.toString(userlocation.getLatitude()));
        Log.e("Distance", Float.toString(distanceInMeters));
        Log.e("DistanceValue", Float.toString(distanceValue));
        if(currUid.equals(nearbyAlertDataModel.getUid()) || distanceInMeters >= distanceValue ) {
            return;
        }
        else if (distanceInMeters <= distanceValue){
//              if it falls under the defined radius, then an alert is issued.
            Intent notificationIntent = new Intent(this, NearbyAlertsActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);


            Intent closeIntent = new Intent(this, AlertCloseConfirmActivity.class);
            PendingIntent stopServiceIntent = PendingIntent.getActivity(this, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            saveNearbyAlertData(nearbyAlertDataModel);
            //build a notification and then notify the user.
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,NEARBY_ALERT_CHANNEL)
                    .setContentTitle("Someone is asking for your help!")
                    .setContentText("Click the notification to track location")
                    .setSmallIcon(R.mipmap.notification_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.f_logo_noti_circle))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setColor(getResources().getColor(R.color.menu_bar_color))
                    .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle());

            Notification notification = builder.build();
            notification.flags = Notification.FLAG_AUTO_CANCEL;         // cancelable on double click
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            if(nearbyAlertDataModel.getMessage().equals("alert")) {             //send if and only if the message is an alert message, not an "alert_end" message.
                notificationManagerCompat.notify("alertNoti", 4, notification);
            }
            else {
                return;
            }
        }
    }

    private void sendNearbyAlertBroadcast(String jsonString) {
        Intent secondIntent = new Intent(this,NearbyAlertsActivity.class);
        Intent intent = new Intent(NEARBY_ALERT_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        secondIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("alertMessage",jsonString);
        if(checkActivityOpen == false) {
            startActivity(secondIntent);
            checkActivityOpen = true;
        }
        sendBroadcast(intent);

    }

    private void saveNearbyAlertData(NearbyAlertDataModel temp) {
        addFlag = false;
        //clearFile();
        if(objectArray.size() > 0) {    //if alert is from another user , add it to the list of nearby alerts.
            for (NearbyAlertDataModel item : objectArray) {
                if (temp.getUid().equals(item.getUid()) && addFlag == false && temp.getMessage().equals("alert")) {
                    objectArray.set(objectArray.indexOf(item), temp);
                    addFlag = true;         //addflag checks if the alert is added to the list during the iteration or not.
                }
                else if(temp.getUid().equals(item.getUid()) && temp.getMessage().equals("alert_end")){
                    objectArray.remove(objectArray.indexOf(item)); //remove from list if it is an "alert_end" message.
                }
                else{
                    continue;
                }
            }
            if(addFlag == false && temp.getMessage().equals("alert")) {
                objectArray.add(temp);
                addFlag = true;
            }
        }
        else{
            if(temp.getMessage().equals("alert")){
                objectArray.add(temp);
                addFlag = true;
            }
        }

        writeToFile();
    }

    private void writeToFile() {
        String outputStr = gsonObject.toJson(objectArray);
        FileOutputStream outputStream = null;
        try {
            if (outputStr != null && !outputStr.isEmpty()) {
                outputStream = getApplicationContext().openFileOutput(FearlessConstant.NEARBY_ALERT_FILE, MODE_PRIVATE);
                outputStream.write(outputStr.getBytes());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

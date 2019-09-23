package android.srrr.com.fearless;

import android.app.Notification;
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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNMembershipResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNSpaceResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNUserResult;

import java.util.ArrayList;
import java.util.Arrays;

import static android.srrr.com.fearless.FearlessConstant.ALERT_INIT_START;
import static android.srrr.com.fearless.FearlessConstant.ALERT_RAISE_BROADCAST;
import static android.srrr.com.fearless.FearlessConstant.ALL_SCREEN_CHANNEL;
import static android.srrr.com.fearless.FearlessConstant.CHANNEL_NAME;
import static android.srrr.com.fearless.FearlessConstant.NEARBY_ALERT_CHANNEL;
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

    double lati,longi;
    private float distanceValue;

    private FirebaseUser firebaseUser;

    @Override
    public void onCreate() {
        super.onCreate();
        //aControl = AlertControl.getInstance(getApplicationContext());

//      IntentFilter filter = new IntentFilter();
//      filter.setPriority(200);
//      receiver = new NotificationActionReceiver();
//      registerReceiver(receiver, filter);
//      unregisterReceiver(receiver);


        sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String distanceRadiusValue = sharedPreferences.getString("key_receive_alert_distance","200");
        if(distanceRadiusValue!= null){ distanceValue = Float.parseFloat(distanceRadiusValue);}
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        loc_fetch = new LocationFetch(this);
        alertlocation = new Location("");
        userlocation = new Location("");
        gsonObject = new Gson();
        pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey(SUBSCRIBE_KEY);
        pubNub = new PubNub(pnConfiguration);

    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            if(intent.getAction().equals(STOP_NEARBY_SERVICE)) {
                pubNub.unsubscribe().channels(Arrays.asList(CHANNEL_NAME)).execute();
                stopForeground(true);
                stopSelf();

            }
            else if(intent.getAction().equals(START_NEARBY_SERVICE)) {
                pubNub.addListener(new SubscribeCallback() {
                    @Override
                    public void status(PubNub pubnub, PNStatus pnStatus) {

                    }

                    @Override
                    public void message(PubNub pubnub, PNMessageResult pnMessageResult) {
                        Log.e("Subscription Result",pnMessageResult.getMessage().toString());
                        alertNotification = pnMessageResult.getMessage().toString();
                        sendNearbyMessageData(alertNotification);

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
                });
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

        String currUid = firebaseUser.getUid();
        NearbyAlertDataModel nearbyAlertDataModel = new NearbyAlertDataModel();
        nearbyAlertDataModel = gsonObject.fromJson(jsonAlertString,nearbyAlertDataModel.getClass());
        Log.e("Parsed Json data:",nearbyAlertDataModel.toString());
        alertlocation.setLatitude(nearbyAlertDataModel.getLatitude());
        alertlocation.setLongitude(nearbyAlertDataModel.getLongitude());
        userlocation.setLatitude(lati);
        userlocation.setLongitude(longi);
        float distanceInMeters = userlocation.distanceTo(alertlocation);
        Log.e("latitude value",Double.toString(lati));
        Log.e("longitude value",Double.toString(longi));
        Log.e("Distance", Float.toString(distanceInMeters));
        Log.e("DistanceValue", Float.toString(distanceValue));
        if(currUid.equals(nearbyAlertDataModel.getUid()) || distanceInMeters >= distanceValue ) {
            return;
        }
        else if (distanceInMeters <= distanceValue){
//
            Intent notificationIntent = new Intent(this, NearbyAlertsActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);


            Intent closeIntent = new Intent(this, AlertCloseConfirmActivity.class);
            PendingIntent stopServiceIntent = PendingIntent.getActivity(this, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);


            NotificationCompat.Builder builder = new NotificationCompat.Builder(this,NEARBY_ALERT_CHANNEL)
                    .setContentTitle("Someone is asking for your help!")
                    .setContentText("Click the notification to track location")
                    .setSmallIcon(R.mipmap.notification_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.f_logo_noti_circle))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setColor(getResources().getColor(R.color.menu_bar_color))
                    .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle());

            Notification notification = builder.build();
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.notify("alertNoti",4,notification);
        }
    }
}

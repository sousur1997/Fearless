package android.srrr.com.fearless;

import android.app.Notification;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
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

import java.util.Arrays;
import java.util.HashMap;

import static android.srrr.com.fearless.FearlessConstant.ALERT_BROADCAST_STOP;
import static android.srrr.com.fearless.FearlessConstant.ALERT_INIT_START;
import static android.srrr.com.fearless.FearlessConstant.ALERT_RAISE_BROADCAST;
import static android.srrr.com.fearless.FearlessConstant.ALL_SCREEN_CHANNEL;
import static android.srrr.com.fearless.FearlessConstant.ALL_SCR_START_BROADCAST_FILTER;
import static android.srrr.com.fearless.FearlessConstant.CHANNEL_NAME;
import static android.srrr.com.fearless.FearlessConstant.NEARBY_ALERT_CHANNEL;
import static android.srrr.com.fearless.FearlessConstant.PUBLISH_KEY;
import static android.srrr.com.fearless.FearlessConstant.START_ALERT;
import static android.srrr.com.fearless.FearlessConstant.START_ALL_SCR;
import static android.srrr.com.fearless.FearlessConstant.STOP_ALL_SCR;
import static android.srrr.com.fearless.FearlessConstant.STOP_NEARBY_SERVICE;
import static android.srrr.com.fearless.FearlessConstant.SUBSCRIBE_KEY;

public class AllScreenService extends Service {

    private Notification notification;
    private NotificationActionReceiver receiver;
    private AlertControl aControl;
    private PNConfiguration pnConfiguration;
    private PubNub pubNub;
    private String alertNotification;
    private Gson gsonObject;
    private GeofencingClient geofencingClient;
    private FirebaseUser firebaseUser;

    @Override
    public void onCreate() {
        super.onCreate();

        aControl = AlertControl.getInstance(getApplicationContext());

        IntentFilter filter = new IntentFilter();
        filter.setPriority(200);
        receiver = new NotificationActionReceiver();
        registerReceiver(receiver, filter);
        unregisterReceiver(receiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            if(intent.getAction().equals(ALERT_INIT_START)){
                stopForeground(true);
                stopSelf();
                startService();
                aControl.toggleAlertInitiator();

            }else if(intent.getAction().equals(STOP_ALL_SCR)){
                stopForeground(true);
                stopSelf();

            }else if(intent.getAction().equals(START_ALL_SCR)){
                Intent notificationIntent = new Intent(this, AppActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

                Intent allScrIntent = new Intent(this, NotificationActionReceiver.class);
                allScrIntent.setAction(ALERT_RAISE_BROADCAST);
                PendingIntent startAlertIntent = PendingIntent.getBroadcast(this, 0, allScrIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                notification = new NotificationCompat.Builder(this, ALL_SCREEN_CHANNEL)
                        .setContentText("Tap Alert to raise one alert")
                        .setSmallIcon(R.mipmap.notification_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.f_logo_noti_circle))
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .addAction(R.drawable.ic_alert_new_fab_icon, "Alert", startAlertIntent)
                        .setColor(getResources().getColor(R.color.menu_bar_color))
                        .setCategory(Notification.CATEGORY_EMAIL)
                        .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0))
                        .build();

                //notification.when = System.currentTimeMillis();

                startForeground(3, notification);
            }
        }

        return START_REDELIVER_INTENT;

    }
    public void startService(){
        Intent alert_init_intent = new Intent(this, AlertInitiator.class);
        alert_init_intent.setAction(START_ALERT);
        ContextCompat.startForegroundService(this, alert_init_intent);

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

//    public void stopNearbyAlertService(){
//        Intent nearbyAlert = new Intent(this, NearbyAlertService.class);
//        nearbyAlert.setAction(STOP_NEARBY_SERVICE);
//        ContextCompat.startForegroundService(this, nearbyAlert);
//    }

}

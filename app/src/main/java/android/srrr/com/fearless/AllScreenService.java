package android.srrr.com.fearless;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import static android.srrr.com.fearless.FearlessConstant.ALERT_INIT_START;
import static android.srrr.com.fearless.FearlessConstant.ALERT_RAISE_BROADCAST;
import static android.srrr.com.fearless.FearlessConstant.ALL_SCREEN_CHANNEL;
import static android.srrr.com.fearless.FearlessConstant.ALL_SCR_START_BROADCAST_FILTER;
import static android.srrr.com.fearless.FearlessConstant.START_ALERT;
import static android.srrr.com.fearless.FearlessConstant.START_ALL_SCR;
import static android.srrr.com.fearless.FearlessConstant.STOP_ALL_SCR;

public class AllScreenService extends Service {

    private Notification notification;
    private NotificationActionReceiver receiver;
    private AlertControl aControl;
    @Override
    public void onCreate() {
        super.onCreate();

        aControl = AlertControl.getInstance(getApplicationContext());

        IntentFilter filter = new IntentFilter();
        filter.setPriority(200);
        receiver = new NotificationActionReceiver();
        registerReceiver(receiver, filter);
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
                        .setContentTitle("Fearless")
                        .setContentText("Tap Alert to raise one alert")
                        .setSmallIcon(R.mipmap.notification_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .addAction(R.drawable.ic_alert_new_fab_icon, "Alert", startAlertIntent)
                        .setColor(getResources().getColor(R.color.menu_bar_color))
                        .setCategory(Notification.CATEGORY_EMAIL)
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
}

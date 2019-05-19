package android.srrr.com.fearless;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import static android.srrr.com.fearless.FearlessConstant.ACTUAL_ALERT_CALL;
import static android.srrr.com.fearless.FearlessConstant.ACTUAL_START_ALERT;
import static android.srrr.com.fearless.FearlessConstant.ACTUAL_STOP_ALERT;
import static android.srrr.com.fearless.FearlessConstant.ALERT_BROADCAST_CALL;
import static android.srrr.com.fearless.FearlessConstant.ALERT_BROADCAST_STOP;
import static android.srrr.com.fearless.FearlessConstant.ALERT_CHANNEL;
import static android.srrr.com.fearless.FearlessConstant.ALERT_INIT_BROADCAST;
import static android.srrr.com.fearless.FearlessConstant.START_ALERT;
import static android.srrr.com.fearless.FearlessConstant.STOP_ALERT;
import static android.srrr.com.fearless.FearlessConstant.toggleAlreadtAlerted;

public class AlertService extends Service {
    private NotificationActionReceiver receiver;
    private String message = "Press CALL to call <First Contact>";

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.setPriority(100);
        receiver = new NotificationActionReceiver();
        registerReceiver(receiver, filter);
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            if (intent.getAction().equals(ACTUAL_STOP_ALERT)) {
                //when alert end
                toggleAlreadtAlerted();
                stopForeground(true);
                stopSelf();
            }else if(intent.getAction().equals(ACTUAL_START_ALERT)){
                //when alert starts
                Intent notificationIntent = new Intent(this, AppActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

                Intent closeIntent = new Intent(this, NotificationActionReceiver.class);
                closeIntent.setAction(ALERT_BROADCAST_STOP);
                PendingIntent stopServiceIntent = PendingIntent.getBroadcast(this, 0, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                String number = "9999999999"; //will update by taking contact from list
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + number));
                PendingIntent callPendingIntent = PendingIntent.getActivity(this, 0, callIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification notification = new NotificationCompat.Builder(this, ALERT_CHANNEL)
                        .setContentTitle("Alert is active")
                        .setContentText(message)
                        .setSmallIcon(R.mipmap.ic_launcher)
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
}

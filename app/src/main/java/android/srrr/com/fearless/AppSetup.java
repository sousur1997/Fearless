package android.srrr.com.fearless;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.os.Build;
import android.provider.Settings;
import static android.srrr.com.fearless.FearlessConstant.ALERT_CHANNEL;
import static android.srrr.com.fearless.FearlessConstant.ALL_SCREEN_CHANNEL;

public class AppSetup extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //initialize by creating notification channel
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel alertServiceChannel = new NotificationChannel(ALERT_CHANNEL, "Alert Service Channel", NotificationManager.IMPORTANCE_HIGH);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).build();

            alertServiceChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, audioAttributes);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(alertServiceChannel);

            //Notification channel for all screen notification.
            NotificationChannel allScreenNotificationChannel = new NotificationChannel(ALL_SCREEN_CHANNEL, "All Screen Notify Channel", NotificationManager.IMPORTANCE_LOW);
            allScreenNotificationChannel.setSound(null, null);
            notificationManager.createNotificationChannel(allScreenNotificationChannel);
        }
    }
}

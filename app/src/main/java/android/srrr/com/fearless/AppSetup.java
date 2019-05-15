package android.srrr.com.fearless;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class AppSetup extends Application {
    public static final String ALERT_CHANNEL = "AlertServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        //initialize by creating notification channel
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel alertServiceChannel = new NotificationChannel(ALERT_CHANNEL, "Alert Service Channel", NotificationManager.IMPORTANCE_DEFAULT);
            alertServiceChannel.setSound(null, null);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(alertServiceChannel);
        }
    }
}

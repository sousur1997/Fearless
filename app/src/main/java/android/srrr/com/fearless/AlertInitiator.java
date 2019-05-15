package android.srrr.com.fearless;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import static android.srrr.com.fearless.AppSetup.ALERT_CHANNEL;

public class AlertInitiator extends Service {
    private Vibrator alertVibrator;

    public AlertInitiator() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        alertVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent notificaIntent = new Intent(this, AppActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificaIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, ALERT_CHANNEL)
                .setContentTitle("Alert starting soon")
                .setContentText("In Few Seconds")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();


        startForeground(1, notification);
        new NotificationVibrate().execute();
        return START_NOT_STICKY; //will change future
    }

    private class NotificationVibrate extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            //total 7.5 seconds, then call next service.
            for(int i = 0; i<5; i++){ //vibrate for 500 milliseconds, then wait for 1000 milliseconds
                alertVibrator.vibrate(500);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

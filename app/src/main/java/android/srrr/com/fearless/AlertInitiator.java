package android.srrr.com.fearless;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.firebase.database.core.Constants;

import static android.srrr.com.fearless.FearlessConstant.ACTUAL_START_ALERT;
import static android.srrr.com.fearless.FearlessConstant.ALERT_CHANNEL;
import static android.srrr.com.fearless.FearlessConstant.ALERT_INIT_BROADCAST;
import static android.srrr.com.fearless.FearlessConstant.ALERT_INIT_START;
import static android.srrr.com.fearless.FearlessConstant.ALL_SCR_START_BROADCAST_FILTER;
import static android.srrr.com.fearless.FearlessConstant.INITIATOR_CHANNEL;
import static android.srrr.com.fearless.FearlessConstant.INIT_BROADCAST_FILTER;
import static android.srrr.com.fearless.FearlessConstant.START_ALERT;
import static android.srrr.com.fearless.FearlessConstant.START_ALL_SCR;
import static android.srrr.com.fearless.FearlessConstant.STOP_ALERT;

public class AlertInitiator extends Service {
    private Vibrator alertVibrator;
    private NotificationActionReceiver receiver;
    private AsyncTask<Void, Void, Void> alertTask;
    private boolean flag_canceled = false;
    private boolean timerStop = true;

    private AlertControl alertControl;
    private SharedPreferences sharedPreferences;
    private static CountDownTimer alertTimer;

    public AlertInitiator() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        alertTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(timerStop){
                    cancel();
                }
            }

            @Override
            public void onFinish() {
                Toast.makeText(getApplicationContext(), "New Alert Raised", Toast.LENGTH_LONG).show();
                stopForeground(true);
                stopSelf();
                alertControl.toggleAlertInitiator();
                alertControl.toggleAlreadyAlerted();
                startAlert();
            }
        };

        sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        IntentFilter filter = new IntentFilter();
        filter.setPriority(100);
        receiver = new NotificationActionReceiver();
        registerReceiver(receiver, filter);

        alertVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        alertControl = AlertControl.getInstance(getApplicationContext());
        Toast.makeText(getApplicationContext(), "On Service", Toast.LENGTH_LONG).show();

        Intent i = new Intent(ALL_SCR_START_BROADCAST_FILTER);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);

        Intent notificationIntent = new Intent(this, AppActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent initAlertIntent = new Intent(this, NotificationActionReceiver.class);
        initAlertIntent.setAction(ALERT_INIT_BROADCAST);
        PendingIntent stopServiceIntent = PendingIntent.getBroadcast(this, 0, initAlertIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, INITIATOR_CHANNEL)
                .setContentTitle("Alert is initiated")
                .setContentText("If alert is started accidentally, please STOP it now.")
                .setSmallIcon(R.mipmap.notification_icon)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .addAction(R.drawable.close_icon, "Stop Alert", stopServiceIntent)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setColor(getResources().getColor(R.color.menu_bar_color))
                .build();

        startForeground(1, notification);
        long[] pattern = {0, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500};
        alertVibrator.vibrate(pattern, -1);

        timerStop = false;
        alertTimer.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //alertTask = new NotificationVibrate();

        if(intent != null) {
            if (intent.getAction().equals(STOP_ALERT)) {
                //alertTask.cancel(true);
                timerStop = true;
                flag_canceled = true;
                alertVibrator.cancel();

                Intent i = new Intent(INIT_BROADCAST_FILTER);
                LocalBroadcastManager.getInstance(this).sendBroadcast(i);

                if (sharedPreferences.getBoolean("key_all_scr_noti", true)) {
                    if(!isServiceRunning(AlertService.class)) {
                        startAllScreenService();
                    }
                }

                //alertTimer.cancel();
                stopForeground(true);
                stopSelf();

            } else if(intent.getAction().equals(START_ALERT)){
                flag_canceled = false;

                /*Intent i = new Intent(ALL_SCR_START_BROADCAST_FILTER);
                LocalBroadcastManager.getInstance(this).sendBroadcast(i);

                Intent notificationIntent = new Intent(this, AppActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

                Intent initAlertIntent = new Intent(this, NotificationActionReceiver.class);
                initAlertIntent.setAction(ALERT_INIT_BROADCAST);
                PendingIntent stopServiceIntent = PendingIntent.getBroadcast(this, 0, initAlertIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification notification = new NotificationCompat.Builder(this, INITIATOR_CHANNEL)
                        .setContentTitle("Alert is initiated")
                        .setContentText("If alert is started accidentally, please STOP it now.")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .addAction(R.drawable.close_icon, "Stop Alert", stopServiceIntent)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setColor(getResources().getColor(R.color.menu_bar_color))
                        .setVibrate(new long[] {10000})
                        .build();

                startForeground(1, notification);
                long[] pattern = {0, 500, 500, 500, 500, 500, 500, 500, 500, 500, 500};
                alertVibrator.vibrate(pattern, -1);*/
                //alertTimer.start();

                //alertTask.execute();
            }
        }
        return START_NOT_STICKY; //will change future
    }

    public void startAlert(){
        Intent alert_init_intent = new Intent(this, AlertService.class);
        alert_init_intent.setAction(ACTUAL_START_ALERT);
        ContextCompat.startForegroundService(this, alert_init_intent);
    }

    public void startAllScreenService(){
        Intent acc_Scr_service = new Intent(this, AllScreenService.class);
        acc_Scr_service.setAction(START_ALL_SCR);
        ContextCompat.startForegroundService(this, acc_Scr_service);
    }

    /*private class NotificationVibrate extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            //total 7.5 seconds, then call next service.
            for (int i = 0; i < 5; i++) { //vibrate for 500 milliseconds, then wait for 1000 milliseconds
                if(!(isCancelled() || flag_canceled == true)) {
                    alertVibrator.vibrate(500);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else{
                    break; //come out from loop
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!(isCancelled() || flag_canceled == true)) {
                Toast.makeText(getApplicationContext(), "New Alert Raised", Toast.LENGTH_LONG).show();
                stopForeground(true);
                stopSelf();
                alertControl.toggleAlertInitiator();
                alertControl.toggleAlreadyAlerted();
                startAlert();
            }else{
                Toast.makeText(getApplicationContext(), "Alert is canceled", Toast.LENGTH_LONG).show();
            }
        }
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(receiver != null){
            unregisterReceiver(receiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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

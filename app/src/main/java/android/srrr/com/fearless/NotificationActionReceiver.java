package android.srrr.com.fearless;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import static android.srrr.com.fearless.FearlessConstant.ACTUAL_ALERT_CALL;
import static android.srrr.com.fearless.FearlessConstant.ACTUAL_STOP_ALERT;
import static android.srrr.com.fearless.FearlessConstant.ALERT_BROADCAST_CALL;
import static android.srrr.com.fearless.FearlessConstant.ALERT_BROADCAST_STOP;
import static android.srrr.com.fearless.FearlessConstant.ALERT_INIT_BROADCAST;
import static android.srrr.com.fearless.FearlessConstant.STOP_ALERT;
import static android.srrr.com.fearless.FearlessConstant.toggleAlertInitiator;

public class NotificationActionReceiver extends BroadcastReceiver {
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ALERT_INIT_BROADCAST)) {
            Intent alert_init_stop = new Intent(context, AlertInitiator.class);
            alert_init_stop.setAction(STOP_ALERT);
            ContextCompat.startForegroundService(context, alert_init_stop);
            toggleAlertInitiator();

            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
        }

        if (intent.getAction().equals(ALERT_BROADCAST_STOP)) {
            Intent alert_stop = new Intent(context, AlertService.class);
            alert_stop.setAction(ACTUAL_STOP_ALERT);
            ContextCompat.startForegroundService(context, alert_stop);

            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
        }

        /*if (intent.getAction().equals(ALERT_BROADCAST_CALL)) {
            Intent alert_stop = new Intent(context, AlertService.class);
            alert_stop.setAction(ACTUAL_ALERT_CALL);
            ContextCompat.startForegroundService(context, alert_stop);

            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
        }*/
    }
}
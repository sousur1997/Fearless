package safetyapp.srrr.com.fearless;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

public class NotificationActionReceiver extends BroadcastReceiver {
    private AlertControl alertControl;

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        alertControl = AlertControl.getInstance(context);
        if (intent.getAction().equals(FearlessConstant.ALERT_INIT_BROADCAST)) {
            if(isServiceRunning(context, AlertInitiator.class)) {
                if(!isServiceRunning(context, AlertService.class)) {
                    Intent alert_init_stop = new Intent(context, AlertInitiator.class);
                    alert_init_stop.setAction(FearlessConstant.STOP_ALERT);
                    ContextCompat.startForegroundService(context, alert_init_stop);
                    alertControl.toggleAlertInitiator();
                }
            }

            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
        }

        if(intent.getAction().equals(FearlessConstant.ALERT_RAISE_BROADCAST)){
            //start alert initiator service
            Intent alert_init_start = new Intent(context, AllScreenService.class);
            alert_init_start.setAction(FearlessConstant.ALERT_INIT_START);
            ContextCompat.startForegroundService(context, alert_init_start);

            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
        }
    }

    private boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}

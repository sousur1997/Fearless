package android.srrr.com.fearless;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import static android.srrr.com.fearless.FearlessConstant.START_ALL_SCR;

public class BootupService extends Service {

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(sharedPreferences.getBoolean("key_all_scr_noti", true)) {
            startAllScreenService();
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void startAllScreenService(){
        Intent acc_Scr_service = new Intent(this, AllScreenService.class);
        acc_Scr_service.setAction(START_ALL_SCR);
        ContextCompat.startForegroundService(this, acc_Scr_service);
    }
}

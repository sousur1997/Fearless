package safetyapp.srrr.com.fearless;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class PowerReceiver extends BroadcastReceiver {
    private static int power_count = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
            Log.e("It On Receive", "Screen is OFF");
            power_count++;
        }else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
            Log.e("It On Receive", "Screen is ON");
        }else if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
            if(power_count >= 2){
                Toast.makeText(context, "Power Button is pressed :" + power_count +" times", Toast.LENGTH_LONG).show();
                power_count = 0;
            }
        }
    }
}

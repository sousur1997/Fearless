package android.srrr.com.fearless;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import static android.srrr.com.fearless.FearlessConstant.START_ALL_SCR;

public class BootActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boot);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(sharedPreferences.getBoolean("key_all_scr_noti", true)) {
            startAllScreenService();
        }

    }
    public void startAllScreenService(){
        Intent acc_Scr_service = new Intent(this, AllScreenService.class);
        acc_Scr_service.setAction(START_ALL_SCR);
        ContextCompat.startForegroundService(this, acc_Scr_service);
    }
}

package safetyapp.srrr.com.fearless;

import android.content.Intent;
import android.content.SharedPreferences;
import safetyapp.srrr.com.fearless.R;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        stopNearbyAlertService();
        setContentView(R.layout.activity_settings);

        toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Settings");

        if(findViewById(R.id.settings_container) != null){
            if(savedInstanceState != null){
                return;
            }
            getFragmentManager().beginTransaction().add(R.id.settings_container, new SettingsFragment()).commit();
        }

        boolean dark_toggle = sharedPreferences.getBoolean("dark_mode",false);
        if(dark_toggle) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

//        Preference darkModeToggle =


    }


    @Override
    protected void onDestroy() {
        if(sharedPreferences.getBoolean("receive_alerts_preference",true)){
            startNearbyAlertService();
        }
        super.onDestroy();
    }

    private void startNearbyAlertService(){
        Intent nearbyAlert = new Intent(this, NearbyAlertService.class);
        nearbyAlert.setAction(FearlessConstant.START_NEARBY_SERVICE);
        startService(nearbyAlert);
    }

    public void stopNearbyAlertService(){
        Intent nearbyAlert = new Intent(this, NearbyAlertService.class);
        nearbyAlert.setAction(FearlessConstant.STOP_NEARBY_SERVICE);
        stopService(nearbyAlert);
    }

}

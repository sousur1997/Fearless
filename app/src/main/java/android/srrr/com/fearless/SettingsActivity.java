package android.srrr.com.fearless;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import static android.srrr.com.fearless.FearlessConstant.START_NEARBY_SERVICE;
import static android.srrr.com.fearless.FearlessConstant.STOP_NEARBY_SERVICE;

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
        nearbyAlert.setAction(START_NEARBY_SERVICE);
        startService(nearbyAlert);
    }

    public void stopNearbyAlertService(){
        Intent nearbyAlert = new Intent(this, NearbyAlertService.class);
        nearbyAlert.setAction(STOP_NEARBY_SERVICE);
        stopService(nearbyAlert);
    }
}

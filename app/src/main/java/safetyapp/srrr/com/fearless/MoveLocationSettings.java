package safetyapp.srrr.com.fearless;

import android.content.Intent;
import safetyapp.srrr.com.fearless.R;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MoveLocationSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);
        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(callGPSSettingIntent, 1);
        //finish();
    }
}

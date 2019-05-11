package android.srrr.com.fearless;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

public class WorkplaceSetup extends AppCompatActivity {
    private PreferenceManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workplace_setup);


        prefManager = new PreferenceManager(getApplicationContext());
        prefManager.setBool("initial_work_setup", true);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            startActivity(new Intent(WorkplaceSetup.this, AppActivity.class));
        }
        return super.onKeyUp(keyCode, event);
    }
}

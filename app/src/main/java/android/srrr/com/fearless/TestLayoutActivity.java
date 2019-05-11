package android.srrr.com.fearless;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.widget.TextView;

import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;

public class TestLayoutActivity extends AppCompatActivity {

    private SpaceNavigationView spaceNavigationView;
    private TextView count_tv;
    private int counter = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);

        spaceNavigationView = (SpaceNavigationView) findViewById(R.id.space);
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.addSpaceItem(new SpaceItem("", R.drawable.my_location_menu_icon));
        spaceNavigationView.addSpaceItem(new SpaceItem("My Location", R.drawable.my_location_menu_icon));
        spaceNavigationView.shouldShowFullBadgeText(true);
        spaceNavigationView.setCentreButtonIconColorFilterEnabled(false);

        count_tv = findViewById(R.id.count_text_tv);
        count_tv.setText("Count: " + counter);

        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        final BroadcastReceiver receiver = new PowerReceiver();
        registerReceiver(receiver, filter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP ){
            counter++;
            count_tv.setText("Count: " + counter);
        }
        return super.onKeyDown(keyCode, event);
    }
}

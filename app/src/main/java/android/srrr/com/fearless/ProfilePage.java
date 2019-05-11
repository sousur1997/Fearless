package android.srrr.com.fearless;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class ProfilePage extends AppCompatActivity {
    private ImageView verified_badge_iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        verified_badge_iv = findViewById(R.id.verified_badge);
        verified_badge_iv.setColorFilter(getResources().getColor(R.color.verify_badge_color), PorterDuff.Mode.SRC_IN);
    }
}

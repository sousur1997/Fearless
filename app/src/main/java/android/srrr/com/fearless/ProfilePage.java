package android.srrr.com.fearless;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfilePage extends AppCompatActivity {
    private ImageView verified_badge_iv;
    private FirebaseUser user;
    private FloatingActionButton image_select_btn, image_upload_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        verified_badge_iv = findViewById(R.id.verified_badge);
        image_select_btn = findViewById(R.id.image_select_fab);
        image_select_btn.setColorFilter(getResources().getColor(R.color.alert_fab_icon_color), PorterDuff.Mode.SRC_IN);

        image_upload_btn = findViewById(R.id.image_upload_fab);
        image_upload_btn.setColorFilter(getResources().getColor(R.color.alert_fab_icon_color), PorterDuff.Mode.SRC_IN);

        user = FirebaseAuth.getInstance().getCurrentUser();
        //check whether the user is email verified or not.

        if(user.isEmailVerified()){
            verified_badge_iv.setImageDrawable(getDrawable(R.drawable.mail_varified));
            verified_badge_iv.setColorFilter(getResources().getColor(R.color.verify_badge_color), PorterDuff.Mode.SRC_IN);
        }else{
            verified_badge_iv.setImageDrawable(getDrawable(R.drawable.ic_not_verified_icon));
            verified_badge_iv.setColorFilter(getResources().getColor(R.color.not_verified_badge_color), PorterDuff.Mode.SRC_IN);
        }

        verified_badge_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfilePage.this, EmailVerification.class).putExtra("caller", "Profile");
                startActivityForResult(intent, 101);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == 1){
            if(requestCode == 101){
                finish();
                startActivity(getIntent());
            }
        }
    }
}

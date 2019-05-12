package android.srrr.com.fearless;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerification extends AppCompatActivity {
    ImageView smiley;
    TextView verify_text_tv;
    Button verify, skip_btn;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        user = FirebaseAuth.getInstance().getCurrentUser();

        smiley = findViewById(R.id.smiley_iv);
        verify_text_tv = findViewById(R.id.verify_text);
        verify = findViewById(R.id.verify_btn);
        skip_btn = findViewById(R.id.skip_btn);

        if(user.isEmailVerified()){
            smiley.setImageDrawable(getDrawable(R.mipmap.smile));
            verify_text_tv.setText("Email is already verified");
            verify.setEnabled(false);
        }else{
            smiley.setImageDrawable(getDrawable(R.mipmap.sad));
            verify_text_tv.setText("This email is not verified.");
            verify.setEnabled(true);
        }

        skip_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextIntent = null;
                if(getIntent().getStringExtra("caller").equals("Registration")){
                    nextIntent = new Intent(EmailVerification.this, AppActivity.class);
                }else if(getIntent().getStringExtra("caller").equals("Profile")){
                    nextIntent = new Intent(EmailVerification.this, ProfilePage.class);
                }
                try {
                    startActivity(nextIntent);
                    finish();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}

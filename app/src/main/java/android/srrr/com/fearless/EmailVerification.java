package android.srrr.com.fearless;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class EmailVerification extends AppCompatActivity {
    ImageView smiley;
    TextView verify_text_tv;
    Button verify;
    private TextView skip_btn;
    private FirebaseUser user;
    private ConstraintLayout ver_layout;
    private ProgressBar verify_prog;
    private PreferenceManager pref_manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        smiley = findViewById(R.id.smiley_iv);
        verify_text_tv = findViewById(R.id.verify_text);
        verify = findViewById(R.id.verify_btn);
        skip_btn = findViewById(R.id.skip_btn);
        ver_layout = findViewById(R.id.verify_layout);
        verify_prog = findViewById(R.id.verify_progress);

        pref_manager = new PreferenceManager(getApplicationContext());

        if(getIntent().getStringExtra("caller").equals("Profile")){
            skip_btn.setVisibility(View.GONE); //hide the skip button
        }

        verify_prog.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.progress_color), PorterDuff.Mode.SRC_IN);
        verify_prog.setVisibility(View.VISIBLE); //show the loader

        Task user_check_task = FirebaseAuth.getInstance().getCurrentUser().reload(); //update the user
        user_check_task.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                verify_prog.setVisibility(View.INVISIBLE); //if user reload task is done, hide loader
                if(task.isSuccessful()){
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user.isEmailVerified()){
                        smiley.setImageDrawable(getDrawable(R.mipmap.smile));
                        verify_text_tv.setText("Email is already verified");
                    }else{
                        smiley.setImageDrawable(getDrawable(R.mipmap.sad));
                        verify_text_tv.setText("This email is not verified.");
                    }
                }else{
                    Snackbar.make(ver_layout, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });

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

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verify_prog.setVisibility(View.VISIBLE);

                if(pref_manager.getBool("verify_email_sent", false) == false) {
                    if (user.isEmailVerified()) {
                        Snackbar.make(ver_layout, "Email is already verified", Snackbar.LENGTH_LONG).show();
                        verify_prog.setVisibility(View.INVISIBLE);
                    } else {
                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                verify_prog.setVisibility(View.INVISIBLE);
                                if (task.isSuccessful()) {
                                    Snackbar.make(ver_layout, "Verification Email will be sent within few hours ", Snackbar.LENGTH_LONG).show();
                                    pref_manager.setBool("verify_email_sent", true); //mark as verification email sent
                                } else {
                                    Snackbar.make(ver_layout, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }else if(user.isEmailVerified()){
                    Snackbar.make(ver_layout, "Email is already verified", Snackbar.LENGTH_LONG).show();
                    verify_prog.setVisibility(View.INVISIBLE);
                }else{
                    Snackbar.make(ver_layout, "Email is already sent", Snackbar.LENGTH_LONG).show();
                    verify_prog.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}

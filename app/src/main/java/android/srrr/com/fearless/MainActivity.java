package android.srrr.com.fearless;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private ImageView fearless_logo, start_btn;
    private Animation logoAnimation, progress_animation;
    private ProgressBar loader;
    private PreferenceManager pref_manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref_manager = new PreferenceManager(getApplicationContext()); //initialize preference manager

        if(pref_manager.getBool("app_first_time", true) == false) { //if this not is first time
            Toast.makeText(getApplicationContext(), "Not first time", Toast.LENGTH_LONG).show();
            switchActivity();
        }else {
            Toast.makeText(getApplicationContext(), "First time", Toast.LENGTH_LONG).show();
            pref_manager.setBool("app_first_time", false); //mark as the application is not opened for first time
            fearless_logo = findViewById(R.id.fearless_lgo);
            loader = findViewById(R.id.app_loader);
            loader.setVisibility(View.GONE);

            //setup the animation for Logo and the progress bar
            progress_animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.loader_animation);
            progress_animation.setFillEnabled(true);
            progress_animation.setFillAfter(true);

            logoAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fearless_animation);
            logoAnimation.setFillAfter(true);
            logoAnimation.setFillEnabled(true);
            logoAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //when animation ends, start the animation for progressbar
                    loader.setVisibility(View.VISIBLE);
                    loader.startAnimation(progress_animation);

                    //show the progress bar for three seconds, and switch to next activity
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(MainActivity.this, SliderActivity.class));
                        }
                    }, 3000);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            //start the animation for logo
            fearless_logo.startAnimation(logoAnimation);

            start_btn = findViewById(R.id.start_btn);
            start_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, SliderActivity.class));
                }
            });
        }
    }

    private void switchActivity() {
        Intent nextIntent;
        if(FirebaseAuth.getInstance().getCurrentUser() == null){ //if the user is not logged in
            nextIntent = new Intent(MainActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }else{ //when user is logged in, move to main app screen
            nextIntent = new Intent(MainActivity.this, AppActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        startActivity(nextIntent);
        this.finish();
    }
}

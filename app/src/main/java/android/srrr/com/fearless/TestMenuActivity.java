package android.srrr.com.fearless;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TestMenuActivity extends AppCompatActivity{

    Button login, register, main_app, profile_page, prof_setup, testLayBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_menu);

        login = findViewById(R.id.login_screen);
        register = findViewById(R.id.register_scr);
        main_app = findViewById(R.id.main_app);
        profile_page = findViewById(R.id.profile_page_btn);
        prof_setup = findViewById(R.id.prof_setup_btn);
        testLayBtn = findViewById(R.id.test_layout_btn);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestMenuActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestMenuActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        main_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestMenuActivity.this, AppActivity.class);
                startActivity(intent);
            }
        });

        profile_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestMenuActivity.this, ProfilePage.class);
                startActivity(intent);
            }
        });

        prof_setup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestMenuActivity.this, ProfileSetup.class);
                startActivity(intent);
            }
        });

        testLayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestMenuActivity.this, TestLayoutActivity.class);
                startActivity(intent);
            }
        });

    }
}

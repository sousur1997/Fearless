package android.srrr.com.fearless;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText email_ed, pass_ed, confirm_ed;
    private EmailPasswordMatcher emailPasswordMatcher;
    private TextView login_tv;
    private Button register_btn;
    private ProgressBar register_progress;
    private FirebaseAuth mAuth;
    private ConstraintLayout register_layout;
    private PreferenceManager prefManager;
    private TextView skip_text_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_form);

        emailPasswordMatcher = new EmailPasswordMatcher();

        //initialize the views from xml to java
        email_ed = findViewById(R.id.email_ed_reg);
        pass_ed = findViewById(R.id.password_ed_reg);
        confirm_ed = findViewById(R.id.con_pass_ed);

        login_tv = findViewById(R.id.goto_login);
        register_btn = findViewById(R.id.sign_up_btn);
        register_progress = findViewById(R.id.register_prog);
        register_layout = findViewById(R.id.register_layout);
        skip_text_view = findViewById(R.id.skip_tv_reg);

        prefManager = new PreferenceManager(getApplicationContext());

        //set the click events
        login_tv.setOnClickListener(this);
        register_btn.setOnClickListener(this);

        register_progress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.progress_color), PorterDuff.Mode.SRC_IN);
        register_progress.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();

        skip_text_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, AppActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
    }

    private void registerUser(){
        String email = email_ed.getText().toString();
        String password = pass_ed.getText().toString();
        String conf_pass = confirm_ed.getText().toString();

        if(email.isEmpty()){
            email_ed.setError("Email is required");
            email_ed.requestFocus();
            return;
        }

        if(!emailPasswordMatcher.checkemailFormat(email)){
            email_ed.setError("Email format is not correct");
            email_ed.requestFocus();
            return;
        }

        if(password.isEmpty()){
            pass_ed.setError("Please provide password");
            pass_ed.requestFocus();
            return;
        }

        if(!emailPasswordMatcher.checkPasswordCriteria(password)){
            pass_ed.setError("Password Criteria:\n1.Length 8 to 14\n2.Use one special character" +
                    "\n3.Two or more alphabet\n4.Use at least one number");
            pass_ed.requestFocus();
            return;
        }

        if(!password.equals(conf_pass)){
            confirm_ed.setError("Password does not match");
            confirm_ed.requestFocus();
            return;
        }
        register_btn.setText("");
        //Toast.makeText(getApplicationContext(), "Registration", Toast.LENGTH_LONG).show();
        register_progress.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {//completed this task
                register_progress.setVisibility(View.GONE);
                register_btn.setText("Sign Up");
                if(task.isSuccessful()){
                    //Toast.makeText(getApplicationContext(), "Registration Successful", Toast.LENGTH_LONG).show();
                    Snackbar.make(register_layout, "Registration Successful", Snackbar.LENGTH_LONG).show();

                    Intent intent = new Intent(RegisterActivity.this, EmailVerification.class);
                    intent.putExtra("caller", "Registration").setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();

                }else{
                    //Toast.makeText(getApplicationContext(), "Unable to register", Toast.LENGTH_LONG).show();
                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                        Snackbar.make(register_layout, "This Email is already registered", Snackbar.LENGTH_LONG).setAction("Login", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                            }
                        }).show();
                    }else if(task.getException() instanceof FirebaseNetworkException){
                        Snackbar.make(register_layout, "Unable to connect to the network", Snackbar.LENGTH_LONG).show();
                    }else{
                        Snackbar.make(register_layout, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });

        //registration successful. Now set the boolean into shared preference
        prefManager.setBool("initial_profile_setup", false);
        prefManager.setBool("initial_work_setup", false);
        prefManager.setBool("sign_up_flag", true);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.goto_login: //go to login page if the text view is clicked
                intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                break;
            case R.id.sign_up_btn: //call the registerUser if signup button is clicked
                registerUser();
                break;
        }
    }
}

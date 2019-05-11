package android.srrr.com.fearless;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.srrr.com.fearless.R;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText email_ed, pass_ed;
    private TextView register_tv, forget_tv;
    private Button login_btn;
    private ProgressBar login_progress;
    private FirebaseAuth mAuth;
    private ConstraintLayout login_layout;
    private TextView skip_text_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_form);

        //initialize the views from xml to java
        email_ed = findViewById(R.id.email_ed);
        pass_ed = findViewById(R.id.password_ed);

        register_tv = findViewById(R.id.register_switch);
        forget_tv = findViewById(R.id.pass_forget);

        login_btn = findViewById(R.id.login_btn);

        login_progress = findViewById(R.id.login_prog);

        login_layout = findViewById(R.id.login_layout);

        skip_text_view = findViewById(R.id.skip_tv_log);

        //set the click events
        register_tv.setOnClickListener(this);
        forget_tv.setOnClickListener(this);
        login_btn.setOnClickListener(this);

        login_progress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.progress_color), PorterDuff.Mode.SRC_IN);
        login_progress.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();

        skip_text_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, AppActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
    }

    private void loginUser(){
        String email = email_ed.getText().toString();
        String password = pass_ed.getText().toString();

        if(email.isEmpty()){
            email_ed.setError("Please enter Email Id");
            email_ed.requestFocus();
            return;
        }

        if(password.isEmpty()){
            email_ed.setError("Please enter Password");
            email_ed.requestFocus();
            return;
        }

        login_btn.setText("");
        login_progress.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                login_progress.setVisibility(View.GONE);
                login_btn.setText("Login");
                if(task.isSuccessful()){
                    //user successfully logged in
                    Snackbar.make(login_layout, "Successfully Logged In", Snackbar.LENGTH_LONG).show();

                    Intent intent = new Intent(LoginActivity.this, AppActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivity(intent);
                    finish();

                }else{
                    //unable to login
                    if(task.getException() instanceof FirebaseAuthInvalidUserException){
                        Snackbar.make(login_layout, "Account Not Found", Snackbar.LENGTH_LONG).setAction("Sign Up", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(LoginActivity.this, RegisterActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                            }
                        }).show();
                    }else if(task.getException() instanceof FirebaseNetworkException){
                        Snackbar.make(login_layout, "Unable to connect to the network", Snackbar.LENGTH_LONG).show();
                    }else if(task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                        Snackbar.make(login_layout, "Invalid Password, Check again", Snackbar.LENGTH_LONG).show();
                    }else{
                        Snackbar.make(login_layout, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.register_switch: //go to Register page if the user is new
                intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
                break;
            case R.id.pass_forget:
                //password forget
                break;
            case R.id.login_btn:
                //Login
                loginUser();
                break;
        }
    }
}

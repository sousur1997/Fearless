package android.srrr.com.fearless;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.srrr.com.fearless.FearlessConstant.FIRESTORE_USERINFO_COLLECTION;
import static android.srrr.com.fearless.FearlessConstant.LOG_ACCOUNT_SETUP;

public class ProfileSetup extends AppCompatActivity {

    private Button skip, save;
    private EditText full_name, phone_ed, street, city, pin, dob, select_st;
    private List<String> state_list;
    private StringBuffer sb;
    private PreferenceManager prefManager;
    private FirebaseAuth mAuth;

    private FirebaseFirestore firestore;
    private String userId;
    private ConstraintLayout prof_layout;
    private ProgressBar saveProgress;
    private FearlessLog fearlessLog;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_setup);

        //creating objects of the edit texts
        skip = findViewById(R.id.skip_btn);
        save = findViewById(R.id.save_btn);

        full_name = findViewById(R.id.full_name_ed);
        phone_ed = findViewById(R.id.phone_ed);
        street = findViewById(R.id.address_street_ed);
        pin = findViewById(R.id.pin_ed);
        city = findViewById(R.id.city_ed);
        dob = findViewById(R.id.birth_date_ed);
        select_st = findViewById(R.id.state_selector);
        prof_layout = findViewById(R.id.profile_setup_layout);
        saveProgress = findViewById(R.id.save_progress_profile);

        saveProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.progress_color), PorterDuff.Mode.SRC_IN);
        saveProgress.setVisibility(View.INVISIBLE);

        prefManager = new PreferenceManager(getApplicationContext());
        prefManager.setBool("initial_profile_setup", true);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if(mAuth != null)
            userId = mAuth.getCurrentUser().getUid();

        dob.setShowSoftInputOnFocus(false);
        select_st.setShowSoftInputOnFocus(false);

        state_list = new ArrayList<>();
        setupSpinner(state_list);

        final ArrayAdapter<String> stateAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item_text, state_list);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //load content from firebase server
        getUserInfo();

        select_st.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(ProfileSetup.this)
                        .setTitle("Select State")
                        .setAdapter(stateAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                select_st.setText(state_list.get(which).toString());
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(ProfileSetup.this, WorkplaceSetup.class);
            startActivity(intent);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProgress.setVisibility(View.VISIBLE);
                save.setText("");
                save.setEnabled(false);
                UpdateUserToFirebase();
            }
        });

        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog date_dialog = new DatePickerDialog(ProfileSetup.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String date_value = "";
                        date_value = date_value + dayOfMonth + "-" + (month+1) + "-" + year;
                        dob.setText(date_value);
                    }
                }, year, month, day);
                date_dialog.show();
            }
        });

        fearlessLog = FearlessLog.getInstance();
    }

    private void setupSpinner(List<String> state_list){
        state_list.add("Select State"); //initial item
        sb = new StringBuffer();
        BufferedReader br = null;

        try{
            br = new BufferedReader(new InputStreamReader(getAssets().open("state_list.json")));
            String temp;
            while((temp = br.readLine()) != null){
                sb.append(temp);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                br.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        //fetch state list from json file
        String myStateJson = sb.toString();

        //try to parse JSON
        try{
            JSONArray state_array = new JSONArray(myStateJson);
            for(int i = 0; i < state_array.length(); i++){
                JSONObject jsonObject = state_array.getJSONObject(i);
                String state = jsonObject.getString("" + (i+1));
                state_list.add(state);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            startActivity(new Intent(ProfileSetup.this, AppActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
        return super.onKeyUp(keyCode, event);
    }

    private void UpdateUserToFirebase(){
        String name = full_name.getText().toString();
        String phone = phone_ed.getText().toString();
        String city = this.city.getText().toString();
        String street = this.street.getText().toString();
        String state = this.select_st.getText().toString();
        String pin = this.pin.getText().toString();
        String dob = this.dob.getText().toString();
        final User newUser = new User(mAuth.getCurrentUser().getEmail(), name, phone, street, city, state, pin, dob);

        firestore.collection(FIRESTORE_USERINFO_COLLECTION).document(userId).set(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //success
                    fearlessLog.sendLog(LOG_ACCOUNT_SETUP); //send log to the server
                    saveProgress.setVisibility(View.INVISIBLE);
                    save.setText("Save");
                    save.setEnabled(true);

                    Snackbar.make(prof_layout, "Profile details updated Successfully", Snackbar.LENGTH_LONG).show();
                }else{
                    Snackbar.make(prof_layout, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void getUserInfo(){
        saveProgress.setVisibility(View.VISIBLE);
        save.setText("");
        save.setEnabled(false);
        if(userId != null) {
            DocumentReference docRef = firestore.collection(FIRESTORE_USERINFO_COLLECTION).document(userId);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    saveProgress.setVisibility(View.INVISIBLE);
                    save.setText("Save");
                    save.setEnabled(true);
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc != null) {
                            User user = task.getResult().toObject(User.class);
                            if(user != null) {
                                full_name.setText(user.getName());
                                phone_ed.setText(user.getPhone());
                                street.setText(user.getStreet());
                                select_st.setText(user.getState());
                                city.setText(user.getCity());
                                pin.setText(user.getPin());
                                dob.setText(user.getDob());
                            }
                        }
                    }else{
                        Snackbar.make(prof_layout, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }else{
            saveProgress.setVisibility(View.INVISIBLE);
            save.setText("Save");
            save.setEnabled(true);
        }
    }
}

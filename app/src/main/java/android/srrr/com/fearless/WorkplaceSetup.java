package android.srrr.com.fearless;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static android.srrr.com.fearless.FearlessConstant.FIRESTORE_WORKPLACE_COLLECTION;
import static android.srrr.com.fearless.FearlessConstant.LOG_WORKPLACE_SETUP;

public class WorkplaceSetup extends AppCompatActivity {
    private PreferenceManager prefManager;

    private Button save;
    private EditText work_place, phone_ed, street, city, pin, occupation, select_st;
    private List<String> state_list;
    private StringBuffer sb;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String userId;
    private ConstraintLayout prof_layout;
    private ProgressBar saveProgress;
    private FearlessLog fearlessLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workplace_setup);

        //creating objects of the edit texts
        save = findViewById(R.id.save_btn);

        work_place = findViewById(R.id.workplace_name_ed);
        phone_ed = findViewById(R.id.wp_phone_ed);
        street = findViewById(R.id.wp_address_street_ed);
        pin = findViewById(R.id.wp_pin_ed);
        city = findViewById(R.id.wp_city_ed);
        occupation = findViewById(R.id.occupation_ed);
        select_st = findViewById(R.id.wp_state_ed);
        prof_layout = findViewById(R.id.workplace_setup_layout);
        saveProgress = findViewById(R.id.wp_save_progress);

        saveProgress.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.progress_color), PorterDuff.Mode.SRC_IN);
        saveProgress.setVisibility(View.INVISIBLE);

        prefManager = new PreferenceManager(getApplicationContext());
        prefManager.setBool("initial_work_setup", true);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        userId = mAuth.getCurrentUser().getUid();

        select_st.setShowSoftInputOnFocus(false);

        state_list = new ArrayList<>();
        setupSpinner(state_list);

        final ArrayAdapter<String> stateAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item_text, state_list);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //get the workplace information and fill the fields
        getWorkplaceInfo();

        select_st.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(WorkplaceSetup.this)
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

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProgress.setVisibility(View.VISIBLE);
                save.setText("");
                save.setEnabled(false);
                UpdateWorkspaceToFirebase();
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
            startActivity(new Intent(WorkplaceSetup.this, AppActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
        return super.onKeyUp(keyCode, event);
    }

    private void UpdateWorkspaceToFirebase(){
        String wp_name = work_place.getText().toString();
        String phone = phone_ed.getText().toString();
        String city = this.city.getText().toString();
        String street = this.street.getText().toString();
        String state = this.select_st.getText().toString();
        String pin = this.pin.getText().toString();
        String occupation = this.occupation.getText().toString();
        final Workplace newWorkplace = new Workplace(wp_name, phone, street, city, state, pin, occupation);

        firestore.collection(FIRESTORE_WORKPLACE_COLLECTION).document(userId).set(newWorkplace).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //success
                    fearlessLog.sendLog(LOG_WORKPLACE_SETUP); //send log to the server
                    saveProgress.setVisibility(View.INVISIBLE);
                    save.setText("Save");
                    save.setEnabled(true);

                    Snackbar.make(prof_layout, "Workplace details updated Successfully", Snackbar.LENGTH_LONG).show();
                }else{
                    Snackbar.make(prof_layout, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void getWorkplaceInfo(){
        saveProgress.setVisibility(View.VISIBLE);
        save.setText("");
        save.setEnabled(false);
        if(userId != null) {
            DocumentReference docRef = firestore.collection(FIRESTORE_WORKPLACE_COLLECTION).document(userId);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    saveProgress.setVisibility(View.INVISIBLE);
                    save.setText("Save");
                    save.setEnabled(true);
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc != null) {
                            Workplace workplace = task.getResult().toObject(Workplace.class);
                            if(workplace != null) {
                                work_place.setText(workplace.getWp_name());
                                phone_ed.setText(workplace.getWp_phone());
                                street.setText(workplace.getWp_street());
                                select_st.setText(workplace.getWp_state());
                                city.setText(workplace.getWp_city());
                                pin.setText(workplace.getWp_pin());
                                occupation.setText(workplace.getOccupation());
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

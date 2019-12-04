package safetyapp.srrr.com.fearless;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import safetyapp.srrr.com.fearless.R;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static safetyapp.srrr.com.fearless.FearlessConstant.ACTUAL_STOP_ALERT;
import static safetyapp.srrr.com.fearless.FearlessConstant.ALERT_CLOSE_RESULT_CODE;
import static safetyapp.srrr.com.fearless.FearlessConstant.ALERT_JSON_FILENAME;
import static safetyapp.srrr.com.fearless.FearlessConstant.HISTORY_COLLECTION;
import static safetyapp.srrr.com.fearless.FearlessConstant.PENDING_FILENAME;
import static safetyapp.srrr.com.fearless.FearlessConstant.START_ALL_SCR;

public class AlertCloseConfirmActivity extends AppCompatActivity {
    private TextView info;
    private Button closeBtn;
    private ImageView syncBtn;

    private ServiceEndTask endTask;
    private String jsonFileContent;

    private AlertControl aControl;
    private FirebaseDatabase database;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String userId;

    private ConstraintLayout verifyLayout;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_complete_display);

        aControl = AlertControl.getInstance(getApplicationContext());
        sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        info = findViewById(R.id.info_text);
        closeBtn = findViewById(R.id.alert_close_btn);
        syncBtn = findViewById(R.id.sync_btn);
        verifyLayout = findViewById(R.id.verify_layout);

        syncBtn.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        firestore = FirebaseFirestore.getInstance();

        endTask = new ServiceEndTask();

        if(mAuth != null)
            userId = mAuth.getCurrentUser().getUid();

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(aControl.getAlreadyAlerted() == true) {
                    Intent intent = getIntent();
                    //if (intent.getAction().equals(ALERT_BROADCAST_STOP)) {
                        if(isServiceRunning(AlertService.class)) {
                            Intent alert_stop = new Intent(AlertCloseConfirmActivity.this, AlertService.class);
                            alert_stop.setAction(ACTUAL_STOP_ALERT);
                            setResult(ALERT_CLOSE_RESULT_CODE, intent);
                            ContextCompat.startForegroundService(AlertCloseConfirmActivity.this, alert_stop);
                        }
                        setResult(ALERT_CLOSE_RESULT_CODE, intent);
                        aControl.setAlreadyAlerted(false);
                        //endTask.execute();

                        jsonFileContent = readCacheJson(); //read json after returning back from service
                        pendingEventListManage(jsonFileContent);

                        info.setText("Please sync alert history to the server. . .");

                        //hide the close button and show sync button
                        closeBtn.setVisibility(View.INVISIBLE);
                        syncBtn.setVisibility(View.VISIBLE);

                        if(sharedPreferences.getBoolean("key_all_scr_noti", true)) {
                            startAllScreenService();
                        }
                    //}
                }
            }
        });

        syncBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncHistory();
            }
        });
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private class ServiceEndTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            jsonFileContent = readCacheJson(); //read json after returning back from service
            pendingEventListManage(jsonFileContent);
            Log.e("----LOG----", "Background Task");
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            info.setText("Please sync alert history to the server. . .");
            //Toast.makeText(getApplicationContext(), jsonFileContent, Toast.LENGTH_LONG).show();

            //hide the close button and show sync button
            closeBtn.setVisibility(View.INVISIBLE);
            syncBtn.setVisibility(View.VISIBLE);
        }
    }

    public void startAllScreenService(){
        Intent acc_Scr_service = new Intent(this, AllScreenService.class);
        acc_Scr_service.setAction(START_ALL_SCR);
        ContextCompat.startForegroundService(this, acc_Scr_service);
    }

    private void syncHistory(){
        Gson gson = new Gson();
        String pendingFileContent;
        AlertEvent[] pendingArr;
        List<AlertEvent> pendingList;
        Map<String, AlertEvent> finalMap = new HashMap<>();

        jsonFileContent = readCacheJson(); //read json after returning back from service
        pendingEventListManage(jsonFileContent);

        final File pendingFile = new File(getFilesDir(), PENDING_FILENAME);
        if(pendingFile.exists()){ //if file is present, then there are some pending event history
            pendingFileContent = readPendingListFile(PENDING_FILENAME);
            pendingArr = gson.fromJson(pendingFileContent, AlertEvent[].class);

            pendingList = new ArrayList<>(Arrays.asList(pendingArr));

            //if the event has at least one location history, then add it to the final list
            for(AlertEvent event : pendingList){
                if(event != null) {
                    if (event.hasLocationHistory()) {
                        finalMap.put(event.getTimestamp(), event);
                    }
                }
            }

            String key = "UpdateTime:" + new Long(System.currentTimeMillis()).toString();
            Snackbar.make(verifyLayout, "Please Wait. Syncing. . .", Snackbar.LENGTH_LONG).show();

            DatabaseReference reference = database.getReference();
            reference.child(HISTORY_COLLECTION).child(userId).child(key).setValue(finalMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Snackbar.make(verifyLayout, "Sync Complete", Snackbar.LENGTH_LONG).show();
                        if(pendingFile.delete()){
                            Snackbar.make(verifyLayout, "All pending data synced", Snackbar.LENGTH_LONG).show();
                        }
                    }else if(task.getException() instanceof FirebaseNetworkException){
                        Snackbar.make(verifyLayout, "Please check your network connection", Snackbar.LENGTH_LONG).show();
                    }else{
                        Snackbar.make(verifyLayout, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                }
            });

        }else{
            Snackbar.make(verifyLayout, "History list is already updated", Snackbar.LENGTH_LONG).show();
        }
    }

    private void pendingEventListManage(String eventJson){
        Gson gson = new Gson();
        AlertEvent currentEvent = gson.fromJson(eventJson, AlertEvent.class); //convert json string to AlertEvent
        AlertEvent[] pendingEventList = null;
        ArrayList<AlertEvent> finalList = new ArrayList<>();

        String pendingJson;

        File pendingFile = new File(getFilesDir(), PENDING_FILENAME);
        if(pendingFile.exists()){ //if file is present, then there are some pending event history
            pendingJson = readPendingListFile(PENDING_FILENAME);
            pendingEventList = gson.fromJson(pendingJson, AlertEvent[].class);
        }

        if(pendingEventList != null){
            finalList = new ArrayList<>(Arrays.asList(pendingEventList)); //convert pendinglists to array list
        }
        finalList.add(currentEvent); //add current event and make it as json file again
        pendingJson = gson.toJson(finalList);

        //write back to the file
        writePendingListFile(PENDING_FILENAME, pendingJson);
        Log.e("Pending Json Data: ", pendingJson);
    }

    private String readPendingListFile(String filename){
        String pendingListJson = "";
        int n;
        try {
            FileInputStream fis = getApplicationContext().openFileInput(filename);
            StringBuffer fileContent = new StringBuffer();

            byte[] buffer = new byte[4096];
            while((n = fis.read(buffer)) != -1){
                fileContent.append(new String(buffer, 0, n));
            }
            fis.close();
            pendingListJson = fileContent.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pendingListJson;
    }

    private void writePendingListFile(String filename, String content){
        try {
            File jsonOpFile = new File(getFilesDir(), filename);
            FileOutputStream fout = new FileOutputStream(jsonOpFile);
            OutputStreamWriter writer = new OutputStreamWriter(fout);

            writer.append(content); //add json into file
            writer.close();
            fout.flush();
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readCacheJson(){
        String json = "";
        int n;
        try {
            FileInputStream fis = getApplicationContext().openFileInput(ALERT_JSON_FILENAME);
            StringBuffer fileContent = new StringBuffer("");

            byte[] buffer = new byte[4096];
            while((n = fis.read(buffer)) != -1){
                fileContent.append(new String(buffer, 0, n));
            }

            json = fileContent.toString();
            fis.close();

            File file = new File(getFilesDir(), ALERT_JSON_FILENAME);
            if(file.exists()){
                file.delete(); //delete the file after reading
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isServiceRunning(AlertService.class)){
            finish();
        }
    }
}

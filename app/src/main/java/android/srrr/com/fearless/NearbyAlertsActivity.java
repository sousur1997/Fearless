package android.srrr.com.fearless;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.FileObserver;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class NearbyAlertsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private NearbyAlertBroadcastReceiver nearbyAlertBroadcastReceiver;
    private RecyclerView recyclerView;
    private ArrayList<NearbyAlertDataModel> nearbyAlertList;
    private ImageView nearbyAlertBackImage;
    private TextView nearbyAlertTextView;
    private SwipeRefreshLayout refreshLayout;
    private Gson gson;
    Handler mHandler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_alerts);
        mToolbar = findViewById(R.id.toolbar3);
        mToolbar.setTitle("Nearby Alerts");
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        recyclerView = findViewById(R.id.nearby_alerts_list_view);
        nearbyAlertBackImage = findViewById(R.id.nearby_alerts_back_image);
        nearbyAlertTextView = findViewById(R.id.nearby_alerts_back_tv);
        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setEnabled(false);
        gson = new Gson();
        createRecyclerView();
        this.mHandler = new Handler();
        runnable.run();

    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //nearbyAlertBroadcastReceiver = new NearbyAlertBroadcastReceiver();
        //registerReceiver(nearbyAlertBroadcastReceiver, new IntentFilter(FearlessConstant.NEARBY_ALERT_SEND));
    }

    private void createRecyclerView(){              //create the recyclerView
        nearbyAlertList = new ArrayList<>();
        loadFromFile();
        buildRecyclerView();
    }

    private void buildRecyclerView() {
        NearbyAlertListAdapter adapter= new NearbyAlertListAdapter(nearbyAlertList); //builds the recyclerview from the json file
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(adapter);
        if(nearbyAlertList.size() > 0) {                            //if list size is zero, show no items on recyclerview
            recyclerView.setVisibility(View.VISIBLE);
            nearbyAlertBackImage.setVisibility(View.INVISIBLE);
            nearbyAlertTextView.setVisibility(View.INVISIBLE);
        }
        else{
            nearbyAlertBackImage.setVisibility(View.VISIBLE);
            nearbyAlertTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        }
    }
    private void loadFromFile() {
        //loads the json object from the file
        FileInputStream inputStream = null;

        try {
            inputStream = openFileInput(FearlessConstant.NEARBY_ALERT_FILE);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String input;

            while((input = bufferedReader.readLine()) != null ){
                stringBuilder.append(input);
            }
            Type itemType = new TypeToken<ArrayList<NearbyAlertDataModel>>(){}.getType();
            nearbyAlertList = gson.fromJson(stringBuilder.toString(),itemType);

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(inputStream != null ) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


   private final Runnable runnable = new Runnable() {       //refreshes the activity every 5 seconds to check any change in the json file
       @Override
       public void run() {
           createRecyclerView();
           NearbyAlertsActivity.this.mHandler.postDelayed(runnable,5000);
       }
   };

}

package android.srrr.com.fearless;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNMembershipResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNSpaceResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNUserResult;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import static android.srrr.com.fearless.FearlessConstant.SUBSCRIBE_KEY;

public class NearbyAlertMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private Toolbar toolbar;
    private NearbyAlertDataModel obj;
    private Gson gson;
    private int index;
    private CopyOnWriteArrayList<NearbyAlertDataModel> nearbyAlertList;
    private CopyOnWriteArrayList<LatLng> currentUserAlertList;
    private PNConfiguration pnConfiguration;
    private PubNub pubNub;
    Handler mHandler;
//    boolean flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        nearbyAlertList = new CopyOnWriteArrayList<>();
        gson = new Gson();
//        flag = false;
        super.onCreate(savedInstanceState);
        //create a pubnub instance as we will listen to the channel for future updates for a particular alert event, if the location changes or not.
        pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey(SUBSCRIBE_KEY);
        pubNub = new PubNub(pnConfiguration);
        setContentView(R.layout.activity_nearby_alert_map);
        toolbar = findViewById(R.id.nearby_alert_map_view_toolbar);
        Intent intent = getIntent();
        index = intent.getIntExtra(FearlessConstant.NEARBY_ALERT_OBJECT_KEY, -1);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.alert_location_map);
        mapFragment.getMapAsync(this);
        //attach a handler
        this.mHandler = new Handler();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        centreMapOnLocation("Current Location");
        runnable.run();

    }

    @Override
    protected void onStop() {
        super.onStop();
        pubNub.unsubscribe();
    }

    public void centreMapOnLocation(String title) {
        currentUserAlertList = new CopyOnWriteArrayList<>();
        //load the alert list from  the specified file
        loadFromFile();
        if (nearbyAlertList.size() > 0) {
            // if list size greater than zero, then update the location on map
            obj = nearbyAlertList.get(index);
            if(currentUserAlertList.size() == 0) {
                currentUserAlertList.add(new LatLng(obj.getLatitude(),obj.getLongitude()));
            }
            toolbar.setTitle(obj.getReadableTime());
            Log.e("user longitude", Double.toString(obj.getLatitude()));
            Log.e("user longitude", Double.toString(obj.getLongitude()));
            LatLng userLocation = new LatLng(obj.getLatitude(), obj.getLongitude());
            PolylineOptions options = new PolylineOptions().addAll(currentUserAlertList).width(5).color(Color.BLUE).geodesic(true);

            map.addPolyline(options);
            map.addMarker(new MarkerOptions().position(currentUserAlertList.get(0)).title("Start Position"));
            if(currentUserAlertList.size() > 1)
                map.addMarker(new MarkerOptions().position(currentUserAlertList.get(currentUserAlertList.size()-1)).title("Last position"));

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentUserAlertList.get(0), 18));
        } else {
            //else destroy the activity
            finish();
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

            while ((input = bufferedReader.readLine()) != null) {
                stringBuilder.append(input);
            }
            Type itemType = new TypeToken<CopyOnWriteArrayList<NearbyAlertDataModel>>() {
            }.getType();
            nearbyAlertList = gson.fromJson(stringBuilder.toString(), itemType);

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            checkIfOutdated();
            centreMapOnLocation("Alert Location");
            pubNub.addListener(new SubscribeCallback() {
                @Override
                public void status(PubNub pubnub, PNStatus pnStatus) {

                }

                @Override
                public void message(PubNub pubnub, PNMessageResult pnMessageResult) {
                    String tempJson = pnMessageResult.getMessage().toString();
                    NearbyAlertDataModel tempData = gson.fromJson(tempJson, NearbyAlertDataModel.class);
                    obj = tempData;
                    LatLng tempLoc = new LatLng(tempData.getLatitude(),tempData.getLongitude());
                    if(tempData.getUid().equals(obj.getUid())) {
                        currentUserAlertList.add(tempLoc);
                    }
                }

                @Override
                public void presence(PubNub pubnub, PNPresenceEventResult pnPresenceEventResult) {

                }

                @Override
                public void signal(PubNub pubnub, PNSignalResult pnSignalResult) {

                }

                @Override
                public void user(PubNub pubnub, PNUserResult pnUserResult) {

                }

                @Override
                public void space(PubNub pubnub, PNSpaceResult pnSpaceResult) {

                }

                @Override
                public void membership(PubNub pubnub, PNMembershipResult pnMembershipResult) {

                }

                @Override
                public void messageAction(PubNub pubnub, PNMessageActionResult pnMessageActionResult) {

                }
            });
            NearbyAlertMapActivity.this.mHandler.postDelayed(runnable, 30000);
        }
    };


    private void checkIfOutdated() {
        Long timestampLong = new Long(System.currentTimeMillis());
        if (nearbyAlertList.size() > 0) {
            for (NearbyAlertDataModel item : nearbyAlertList) {
                if ((timestampLong - item.getTimestamp()) > 15 * 60 * 90) {        //if alert time is greater than 15 minutes, remove it from list.
                    nearbyAlertList.remove(nearbyAlertList.indexOf(item));
                }
            }
        }
    }
}


package android.srrr.com.fearless;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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

    private SharedPreferences sharedPreferences;
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
    private float distanceRadius;
    private LatLng selfLocation;
    private LocationFetch locationFetch;

    private long interval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String distanceRadiusValue = sharedPreferences.getString("key_receive_alert_distance","200");
        if(distanceRadiusValue!= null){ distanceRadius = Float.parseFloat(distanceRadiusValue);}
        nearbyAlertList = new CopyOnWriteArrayList<>();
        gson = new Gson();
        interval = 30000;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_alert_map);
        toolbar = findViewById(R.id.nearby_alert_map_view_toolbar);
        Intent intent = getIntent();
        index = intent.getIntExtra(FearlessConstant.NEARBY_ALERT_OBJECT_KEY, -1);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.alert_location_map);
        mapFragment.getMapAsync(this);
        locationFetch = new LocationFetch(this);
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
        locationFetch.fetchCurrentLocation();
        selfLocation = new LatLng(locationFetch.fetchLatitude(),locationFetch.fetchLongitude());
        currentUserAlertList = new CopyOnWriteArrayList<>();
        //load the alert list from  the specified file
        loadFromFile();
        if (nearbyAlertList.size() > 0) {
            // if list size greater than zero, then update the location on map
            obj = nearbyAlertList.get(index);
            toolbar.setTitle(obj.getReadableTime());
            LatLng alertLocation = new LatLng(obj.getLatitude(), obj.getLongitude());
            map.clear();
            map.addMarker(new MarkerOptions().position(alertLocation).title("Alert Location"));
            map.addMarker(new MarkerOptions().position(selfLocation).title("Self Location"));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(alertLocation, 18));
            CircleOptions circleOptions = new CircleOptions()
                    .center(alertLocation)
                    .strokeColor(0x22F5FC05)
                    .fillColor(0x22F5FC05)
                    .radius(distanceRadius);
            map.addCircle(circleOptions);
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
            centreMapOnLocation("Alert Location");
            NearbyAlertMapActivity.this.mHandler.postDelayed(runnable, 30000);
        }
    };

}


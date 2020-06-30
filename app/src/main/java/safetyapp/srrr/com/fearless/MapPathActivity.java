package safetyapp.srrr.com.fearless;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import safetyapp.srrr.com.fearless.R;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MapPathActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {
    private Toolbar toolbar;
    private int index;
    private GoogleMap gMap;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_path);

        toolbar = findViewById(R.id.map_view_toolbar);

        Intent intent = getIntent();
        index = intent.getIntExtra(FearlessConstant.HISTORY_INDEX_KEY, -1);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(MapPathActivity.this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.path_map);
        mapFragment.getMapAsync(this);
    }

    private void setupMapPath(int index) throws IndexOutOfBoundsException{
        AlertEvent[] AlertArr;
        ArrayList<AlertEvent> AlertList = new ArrayList<>();
        String jsonString = readJsonFile(FearlessConstant.HISTORY_LIST_FILE);
        Gson gson = new Gson();

        AlertArr = gson.fromJson(jsonString, AlertEvent[].class);
        AlertList.addAll(Arrays.asList(AlertArr));

        AlertEvent event = AlertList.get(index);
        toolbar.setTitle(event.getReadableTime());

        ArrayList<LatLng> pathCoordinates = new ArrayList<>();
        for(LocationLatLng latlng : event.getLocationArray()){
            pathCoordinates.add(new LatLng(latlng.getLat(), latlng.getLng()));
        }

        PolylineOptions options = new PolylineOptions().addAll(pathCoordinates).width(5).color(Color.BLUE).geodesic(true);

        gMap.addPolyline(options);
        gMap.addMarker(new MarkerOptions().position(pathCoordinates.get(0)).title("Start Position"));
        if(pathCoordinates.size() > 1)
            gMap.addMarker(new MarkerOptions().position(pathCoordinates.get(pathCoordinates.size()-1)).title("Last position"));

        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pathCoordinates.get(0), 18));
    }
    private String readJsonFile(String filename){
        String listJson = "";
        int n;
        try {
            FileInputStream fis = getApplicationContext().openFileInput(filename);
            StringBuffer fileContent = new StringBuffer();

            byte[] buffer = new byte[4096];
            while((n = fis.read(buffer)) != -1){
                fileContent.append(new String(buffer, 0, n));
            }

            listJson = fileContent.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listJson;
    }

    @Override
    public void onPolylineClick(Polyline polyline) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;
        boolean dark_toggle = sharedPref.getBoolean("dark_mode",false);
        if(dark_toggle) {
            try{
                boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.gmap_style));
                if(!success){
                    Log.e("Error","Map Parsing failed!");
                }
            }catch (Resources.NotFoundException e) {
                Log.e("Error!","Can't find style", e);
            }
        }
        else {
            boolean success = googleMap.setMapStyle(null);
            if(!success){
                Log.e("Error","Map reset failed!");
            }
        }
        if(index > -1){
            setupMapPath(index);
        }
    }
}

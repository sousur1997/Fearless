package android.srrr.com.fearless;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.content.Context.LOCATION_SERVICE;
import static android.srrr.com.fearless.FearlessConstant.CALL_PERMISSION;
import static android.srrr.com.fearless.FearlessConstant.LOCATION_PERMISSION;

public class HomeFragment extends Fragment implements OnMapReadyCallback{

    FloatingActionMenu floatingActionMenu;
    FloatingActionButton pol_fab, hos_fab;
    private GoogleMap gMap;
    private LocationManager locationManager;
    private Location locaton;
    private Double lat, lng;
    private Boolean permission = false;
    private FirebaseAuth mAuth;
    private boolean firstTime = true;
    private CoordinatorLayout main_coord;
    private SharedPreferences sharedPref;

    private LocationFetch loc_fetch;

    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    PendingResult<LocationSettingsResult> result;
    final static int REQUEST_LOCATION = 199;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home_tab_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.get_curr_loc_menu){
            pointCurrentLocation();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        floatingActionMenu = getView().findViewById(R.id.fab_menu);
        floatingActionMenu.setIconAnimated(false);

        pol_fab = getView().findViewById(R.id.police_fab);
        hos_fab = getView().findViewById(R.id.hospital_fab);
        main_coord = getActivity().findViewById(R.id.main_coordinator);

        loc_fetch = new LocationFetch(getActivity().getApplicationContext());
        //start Async Task
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            Toast.makeText(getActivity().getApplicationContext(), "The user is: " + user.getEmail(), Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getActivity().getApplicationContext(), "You are not logged in", Toast.LENGTH_LONG).show();
        }

        if(runtime_permission()) { //check for runtime permissions
            new LocationUpdateTask().execute();
        }
        runtime_call_permission();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.g_map);
        mapFragment.getMapAsync(this);

        //use the location service
        pol_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapWithLocation("Police Station");
            }
        });

        hos_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMapWithLocation("Hospital");
            }
        });
    }

    public void pointCurrentLocation(){
        loc_fetch.fetchCurrentLocation();
        lat = loc_fetch.fetchLatitude();
        lng = loc_fetch.fetchLongitude();
        /*Toast.makeText(getActivity().getApplicationContext(), "Search:Police Station" +
                        "\nLat:" + lat + "\nLng:"+lng,
                Toast.LENGTH_LONG).show();*/

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledAlertToUser();
        } else {
            if (lat != 0.0 && lng != 0.0) {
                centreMapOnLocation(lat, lng, "Your Current Location");
            }
        }
    }

    private void showGPSDisabledAlertToUser() {
        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(callGPSSettingIntent);
    }

    private void openMapWithLocation(String search_string) {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledAlertToUser();
        } else {
            loc_fetch.fetchCurrentLocation();
            lat = loc_fetch.fetchLatitude();
            lng = loc_fetch.fetchLongitude();

            //open the google map using the search string and latitude and longitude
            Uri googleMapUri = Uri.parse("geo: " + lat + ", " + lng + "?q=" + search_string + "");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, googleMapUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
    }

    private boolean runtime_permission() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(
                getActivity().getApplicationContext(),
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                        ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION);
            return true;
        }
        return false;
    }

    private boolean runtime_call_permission() {
        if (Build.VERSION.SDK_INT >= 21 && ContextCompat.checkSelfPermission(
                getActivity().getApplicationContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{CALL_PHONE}, CALL_PERMISSION);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

            } else {
                runtime_permission(); //if the permissions are not available, ask for the permission
            }
        }

        if(requestCode == CALL_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else{
                runtime_call_permission();
            }
        }
    }

    public void centreMapOnLocation(Double lat, Double lng, String title) {

        LatLng userLocation = new LatLng(lat, lng);
        gMap.clear();
        gMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 18));
        gMap.getUiSettings().setMapToolbarEnabled(false);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        loc_fetch.fetchCurrentLocation();
        pointCurrentLocation();
        new LocationUpdateTask().execute();
    }

    private class LocationUpdateTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            while(loc_fetch.getUpdated() == false){
                ; //when the location is not updated, wait for sometimes
            }
            lat = loc_fetch.fetchLatitude();
            lng = loc_fetch.fetchLongitude();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            pointCurrentLocation();
        }
    }
}

package safetyapp.srrr.com.fearless;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.SEND_SMS;
import static android.content.Context.LOCATION_SERVICE;
import static safetyapp.srrr.com.fearless.FearlessConstant.ALL_PERMISSION;

public class HomeFragment extends Fragment implements OnMapReadyCallback{

    FloatingActionMenu floatingActionMenu;
    FloatingActionButton pol_fab, hos_fab;
    private GoogleMap gMap;
    private LocationManager locationManager;
    private Double lat, lng;
    private FirebaseAuth mAuth;
    private CoordinatorLayout main_coord;
    private SharedPreferences sharedPref;

    private LocationFetch loc_fetch;

    String[] Permissions = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, READ_CONTACTS, SEND_SMS, ACCESS_BACKGROUND_LOCATION};
    String[] LocationPermissions = {ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        if(hasPermission(getActivity().getApplicationContext(), getActivity(), Permissions ).equals("False")){
            showPermissionClarification();
            new LocationUpdateTask().execute();
        }
        else if(hasPermission(getActivity().getApplicationContext(), getActivity(), Permissions ).equals("Denied")) {
            neverAskAgain();
        }

       /* if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasPermission(getActivity().getApplicationContext(), getActivity(), LocationPermissions).equals("True")) {
            showGPSDisabledAlertToUser();
        }*/

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
            Toast.makeText(getActivity().getApplicationContext(), "Welcome " + user.getEmail(), Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getActivity().getApplicationContext(), "You are not logged in!", Toast.LENGTH_LONG).show();
        }

        if(hasPermission(getActivity().getApplicationContext(), getActivity(), Permissions ).equals("False")){
            showPermissionClarification();
            new LocationUpdateTask().execute();
        }
        else if(hasPermission(getActivity().getApplicationContext(), getActivity(), Permissions ).equals("Denied")) {
            neverAskAgain();
        }

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
//        centreMapOnLocation(lat, lng, "Your Current Location");

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && hasPermission(getActivity().getApplicationContext(), getActivity(), LocationPermissions).equals("True")) {
            showGPSDisabledAlertToUser();
        }
        if (lat != 0.0 && lng != 0.0) {
            centreMapOnLocation(lat, lng, "Your Current Location");
        }
    }

    private void showGPSDisabledAlertToUser() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("To access your location more precisely, we would like you to enable GPS from this settings menu. Please select 'High Accuracy' from the menu.")
                .setCancelable(true)
                .setPositiveButton("Yes, go ahead", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                })
                .setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void openMapWithLocation(String search_string) {
        loc_fetch.fetchCurrentLocation();
        lat = loc_fetch.fetchLatitude();
        lng = loc_fetch.fetchLongitude();

        //open the google map using the search string and latitude and longitude
        Uri googleMapUri = Uri.parse("geo: " + lat + ", " + lng + "?q=" + search_string + "");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, googleMapUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
//        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            showGPSDisabledAlertToUser();
//        }
    }

    private static String hasPermission(Context context, Activity activity, String... Permissions){

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && Permissions != null){
            for(String permission : Permissions){
                if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    if (PermissionUtility.neverAskAgain(activity, permission)) {
                        return "Denied";
                    }
                    else {
                        return "False";
                    }
                }
            }
        }
        return "True";
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == ALL_PERMISSION){
            for(int i = 0; i<grantResults.length; i++){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    hasPermission(getActivity().getApplicationContext(), getActivity(), Permissions);

                }
                else{
                    for(String permission: Permissions) {
                        PermissionUtility.setShouldShowStatus(getActivity(), permission);
                    }

                }
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
        boolean dark_toggle = sharedPref.getBoolean("dark_mode",false);
        if(dark_toggle) {
            try{
                boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),R.raw.gmap_style));
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

    private void showPermissionClarification() {
        ((TextView) new AlertDialog.Builder(getActivity())
                .setTitle("Hello User!")
                .setCancelable(false)
                .setPositiveButton("I Understand", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(Permissions, ALL_PERMISSION);
                        dialogInterface.cancel();
                    }
                })
                .setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getActivity().finish();
                    }
                })
                .setMessage(Html.fromHtml("<p>This app needs the following permissions to ensure your safety.</p>"
                        +"<p><h3>Location</h3> This permission is needed to show you nearby emergency services and to send this information to your contacts during an emergency. Also you will be notified if any Fearless user is in trouble nearby. (You can change this in settings) <h6>Please allow the location access 'Allow all the time' because your location will be sent to your contacts continuously in an emergency even if your phone is locked.</h6> </p>"
                        +"<p><h3>Contacts</h3> This permission is needed to add your most trusted contacts to the app to inform them during any emergency. </p>"
                        +"<p><h3>SMS</h3> This permission is needed to inform your trusted contacts (which you have added to this app) about your location during an emergency. <h6>**Operator charges will apply to send SMS!**</h6> </p>"
                ))
                .show()
                // Need to be called after show(), in order to generate hyperlinks
                .findViewById(android.R.id.message))
                .setMovementMethod(LinkMovementMethod.getInstance());

    }

    private void neverAskAgain() {
        ((TextView) new AlertDialog.Builder(getActivity())
                .setTitle("Hello User!")
                .setCancelable(false)
                .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        dialogInterface.cancel();
                    }
                })
                .setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getActivity().finish();
                    }
                })
                .setMessage(Html.fromHtml("<p>This app needs the following permissions to ensure your safety.</p>"
                        +"<p><h3>Location</h3> This permission is needed to show you nearby emergency services and to send this information to your contacts during an emergency. Also you will be notified if any Fearless user is in trouble nearby. (You can change this in settings) <h6>Please allow the location access 'Allow all the time' because your location will be sent to your contacts continuously in an emergency even if your phone is locked.</h6> </p>"
                        +"<p><h3>Contacts</h3> This permission is needed to add your most trusted contacts to the app to inform them during any emergency. </p>"
                        +"<p><h3>SMS</h3> This permission is needed to inform your trusted contacts (which you have added to this app) about your location during an emergency. <h6>**Operator charges will apply to send SMS!**</h6> </p>"
                        +"<p><h3>As you have denied it, you can go to settings and allow the app permissions to continue.</h3> </p>"
                ))
                .show()
                // Need to be called after show(), in order to generate hyperlinks
                .findViewById(android.R.id.message))
                .setMovementMethod(LinkMovementMethod.getInstance());

    }


}








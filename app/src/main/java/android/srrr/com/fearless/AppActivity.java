package android.srrr.com.fearless;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.bottomappbar.BottomAppBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.srrr.com.fearless.FearlessConstant.ALERT_BROADCAST_STOP;
import static android.srrr.com.fearless.FearlessConstant.ALERT_CLOSE_REQUEST_CODE;
import static android.srrr.com.fearless.FearlessConstant.ALERT_CLOSE_RESULT_CODE;
import static android.srrr.com.fearless.FearlessConstant.ALL_SCREEN_CHANNEL;
import static android.srrr.com.fearless.FearlessConstant.ALL_SCR_START_BROADCAST_FILTER;
import static android.srrr.com.fearless.FearlessConstant.CONTACT_LOCAL_FILENAME;
import static android.srrr.com.fearless.FearlessConstant.CONTACT_UPDATE_REQUEST;
import static android.srrr.com.fearless.FearlessConstant.HELP_URL;
import static android.srrr.com.fearless.FearlessConstant.HISTORY_LIST_FILE;
import static android.srrr.com.fearless.FearlessConstant.INIT_BROADCAST_FILTER;
import static android.srrr.com.fearless.FearlessConstant.LOG_LOGOUT;
import static android.srrr.com.fearless.FearlessConstant.LOG_SIGN_UP;
import static android.srrr.com.fearless.FearlessConstant.PICK_CONTACT;
import static android.srrr.com.fearless.FearlessConstant.PROFILE_ACTIVITY_CODE;
import static android.srrr.com.fearless.FearlessConstant.SETTINGS_ACTIVITY_REQUEST;
import static android.srrr.com.fearless.FearlessConstant.START_ALERT;
import static android.srrr.com.fearless.FearlessConstant.START_ALL_SCR;
import static android.srrr.com.fearless.FearlessConstant.STOP_ALERT;
import static android.srrr.com.fearless.FearlessConstant.STOP_ALL_SCR;

public class AppActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    private int[] tab_icons = {R.mipmap.history, R.mipmap.home_icon, R.mipmap.call_icon};
    private TextView acc_badge, work_badge, profile_email;

    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private BottomAppBar bAppBar;

    private MenuItem acc_setup, work_setup, login_menu_item, register_item, acc_setu_grp_item, setting_item;
    private Menu nav_menu;
    private boolean logged_in = false;
    private PreferenceManager prefManager;
    private boolean firstTime = false;
    private CircleImageView profile_image;
    private View HeaderView;
    private FloatingActionButton alert_fab;
    private FirebaseUser user;
    private String userId;
    private StorageReference storage;
    private StorageReference profileImageReference;
    private View profile_image_prog;
    private ArrayList<PersonalContact> contactList;

    private AlertControl aControl;
    private BroadcastReceiver receiver;

    private SharedPreferences sharedPreferences;
    private FearlessLog fearlessLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        contactList = new ArrayList<>();

        sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(AppActivity.this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        aControl = AlertControl.getInstance(getApplicationContext());

        prefManager = new PreferenceManager(getApplicationContext()); //setup the preference manager to store data

        aControl.setAlertInitiator(false);
        //aControl.setAlreadyAlerted(false);

        toolbar = findViewById(R.id.toolbar);
        bAppBar = findViewById(R.id.bottomAppBar);
        navView = findViewById(R.id.nav_menu);

        alert_fab = findViewById(R.id.alert_fab);

        if(aControl.getAlreadyAlerted() == false){
            alert_fab.setImageDrawable(getDrawable(R.drawable.ic_alert_new_fab_icon));
        }else{
            alert_fab.setImageDrawable(getDrawable(R.drawable.close_icon));
        }

        if(aControl.getAlertInit() == false){
            alert_fab.setImageDrawable(getDrawable(R.drawable.ic_alert_new_fab_icon));
        }else{
            alert_fab.setImageDrawable(getDrawable(R.drawable.close_icon));
        }

        setSupportActionBar(toolbar);
        setSupportActionBar(bAppBar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        viewPager = findViewById(R.id.view_pager);
        viewPager = findViewById(R.id.view_pager);

        HeaderView = navView.getHeaderView(0);
        profile_image = HeaderView.findViewById(R.id.profile_image_view);
        profile_email = HeaderView.findViewById(R.id.nav_header_textView);
        profile_image_prog = HeaderView.findViewById(R.id.load_image_progress);

        profile_image_prog.setVisibility(View.INVISIBLE); //at first it will not be shown.

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            userId = user.getUid();
        }else{
            //when user is not logged in, disable alert button
            alert_fab.setBackgroundColor(Color.GRAY);
            alert_fab.setImageDrawable(getDrawable(R.drawable.alert_inactive));
        }

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppActivity.this, ProfilePage.class);
                startActivityForResult(intent, PROFILE_ACTIVITY_CODE);
            }
        });

        setupViewPager(viewPager);

        viewPager.setCurrentItem(1);
        setupTabIcons();

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        navView.setNavigationItemSelectedListener(this);

        nav_menu = navView.getMenu();

        acc_setup = nav_menu.findItem(R.id.account_setup_item);
        work_setup = nav_menu.findItem(R.id.workplace_setup_item);
        login_menu_item = nav_menu.findItem(R.id.signout_item);
        register_item = nav_menu.findItem(R.id.sign_up_item);
        acc_setu_grp_item = nav_menu.findItem(R.id.acc_setup_group_item);
        setting_item = nav_menu.findItem(R.id.app_settings_menu);

        //if user is logged in, set the menu item as Sign Out
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            login_menu_item.setIcon(R.drawable.ic_logout);
            login_menu_item.setTitle("Sign Out");
            register_item.setVisible(false); //When logged in hide the sign up item
            acc_setu_grp_item.setVisible(true); //when logged in, show the account update group
            profile_image.setEnabled(true); //we can click on the image icon
            profile_email.setText(user.getEmail());
            setting_item.setVisible(true);
            retrieveImageToImageView(); //retrieve image and set as profile image

            logged_in = true;

            //start All screen notification when logged in.
            if(aControl.getAlertInit() == false && aControl.getAlreadyAlerted() == false) {
                if (sharedPreferences.getBoolean("key_all_scr_noti", true)) {
                    startAllScrNoti();
                }
            }

        }else{
            login_menu_item.setIcon(R.drawable.ic_login);
            login_menu_item.setTitle("Login");
            register_item.setVisible(true); //When logged out or newly started app show the sign up item
            acc_setu_grp_item.setVisible(false); //when not logged in, hide the account update group
            profile_image.setEnabled(false); //we cannot click on the image icon
            profile_email.setText(getResources().getString(R.string.please_login));
            setting_item.setVisible(false);

            logged_in = false;
        }

        acc_badge = (TextView) acc_setup.getActionView();
        work_badge = (TextView) work_setup.getActionView();

        init_badges();

        if(prefManager.getBool("sign_up_flag", false) == true){ //first time sign up
            drawerLayout.openDrawer(Gravity.LEFT);
            prefManager.setBool("sign_up_flag", false); //reset the flag after opening drawer
        }

        alert_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user != null) {
                    getPersonalContacts();
                    if (contactList.size() > 0) {
                        if (aControl.getAlertInit() == false) {
                            //alert_fab.setImageDrawable(getDrawable(R.drawable.close_icon));
                            if (aControl.getAlreadyAlerted() == false) {
                                startService();
                                if(isServiceRunning(AllScreenService.class)) { //if service is active, then close it
                                    stopAllScrNoti();
                                }
                                aControl.toggleAlertInitiator();
                            } else {
                                Intent intent = new Intent(AppActivity.this, AlertCloseConfirmActivity.class);
                                intent.setAction(ALERT_BROADCAST_STOP);
                                startActivityForResult(intent, ALERT_CLOSE_REQUEST_CODE);
                            }
                        } else {
                            stopService();
                            alert_fab.setImageDrawable(getDrawable(R.drawable.ic_alert_new_fab_icon));
                            aControl.toggleAlertInitiator();
                        }
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AppActivity.this)
                                .setCancelable(false)
                                .setTitle("Cannot Raise Alert!")
                                .setMessage("You have no personal contact in your list. Add at least one contact to raise alert")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }else{
                    AlertDialog dialog;
                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(AppActivity.this);
                    dialogBuilder.setTitle("Alert failed");
                    dialogBuilder.setMessage("Alert feature is not available for Guest Users");
                    dialogBuilder.setCancelable(false);
                    dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog = dialogBuilder.create();
                    dialog.show();
                }
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(INIT_BROADCAST_FILTER);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                alert_fab.setImageDrawable(getDrawable(R.drawable.ic_alert_new_fab_icon));
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

        IntentFilter all_filter = new IntentFilter();
        all_filter.addAction(ALL_SCR_START_BROADCAST_FILTER);

        BroadcastReceiver all_scrReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                alert_fab.setImageDrawable(getDrawable(R.drawable.close_icon));
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(all_scrReceiver, all_filter);

        fearlessLog = FearlessLog.getInstance();
    }

    private void startAllScrNoti(){
        Intent all_scr_alert = new Intent(this, AllScreenService.class);
        all_scr_alert.setAction(START_ALL_SCR);
        ContextCompat.startForegroundService(this, all_scr_alert);
    }

    public void stopAllScrNoti(){
        Intent all_scr_alert = new Intent(this, AllScreenService.class);
        all_scr_alert.setAction(STOP_ALL_SCR);
        ContextCompat.startForegroundService(this, all_scr_alert);
    }

    private void retrieveImageToImageView(){
        storage = FirebaseStorage.getInstance().getReference();
        profileImageReference = storage.child("ProfileImages/" + userId + ".jpg"); //store image with <userId>.jpg

        profile_image_prog.setVisibility(View.VISIBLE);
        profileImageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profile_image.setImageBitmap(bitmap);
                profile_image_prog.setVisibility(View.INVISIBLE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                profile_image.setImageDrawable(getDrawable(R.mipmap.user_icon)); //if it fails to load image, set the image as default image
                profile_image_prog.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void startService(){
        Intent alert_init_intent = new Intent(this, AlertInitiator.class);
        alert_init_intent.setAction(START_ALERT);
        ContextCompat.startForegroundService(this, alert_init_intent);
    }

    public void stopService(){
        Intent alert_init_stop = new Intent(this, AlertInitiator.class);
        alert_init_stop.setAction(STOP_ALERT);
        ContextCompat.startForegroundService(this, alert_init_stop);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    private void init_badges(){
        acc_badge.setGravity(Gravity.CENTER_VERTICAL);
        work_badge.setGravity(Gravity.CENTER_VERTICAL);

        acc_badge.setTextSize(30);
        work_badge.setTextSize(30);

        acc_badge.setTypeface(null, Typeface.BOLD);
        work_badge.setTypeface(null, Typeface.BOLD);

        acc_badge.setTextColor(Color.RED);
        work_badge.setTextColor(Color.RED);

        if(prefManager.getBool("initial_profile_setup", false) == false)
            acc_badge.setText("•");

        if(prefManager.getBool("initial_work_setup", false) == false)
            work_badge.setText("•");
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item)){

        }
        return super.onOptionsItemSelected(item);
    }

    private void setupTabIcons(){
        tabLayout.getTabAt(0).setIcon(tab_icons[0]);
        tabLayout.getTabAt(1).setIcon(tab_icons[1]);
        tabLayout.getTabAt(2).setIcon(tab_icons[2]);
    }

    private void setupViewPager(ViewPager viewPager){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HistoryFragment(), "History");
        adapter.addFragment(new HomeFragment(), "Home");
        adapter.addFragment(new SosContactFragment(), "Contacts");
        viewPager.setAdapter(adapter);
    }

    private void signOut(){
        fearlessLog.sendLog(LOG_LOGOUT); //send log to the server
        FirebaseAuth.getInstance().signOut();
        //Clear the preference variables:
        prefManager.setBool("verify_email_sent", false);
        Toast.makeText(getApplicationContext(), "Sign Out", Toast.LENGTH_LONG).show();

        //delete the history file, and contact file:
        fileDelete(HISTORY_LIST_FILE);
        fileDelete(CONTACT_LOCAL_FILENAME);


        //after signing out, restart the current activity
        Intent loginIntent = getIntent();
        finish();
        startActivity(loginIntent);
    }

    private void fileDelete(String filename){
        File file = new File(getFilesDir(), filename);
        if(file.exists()){
            file.delete();
            if(file.exists()){
                try {
                    file.getCanonicalFile().delete();
                    if(file.exists()){
                        getApplicationContext().deleteFile(file.getName());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.signout_item:
                if(logged_in) {
                    if (aControl.getAlreadyAlerted() == false && aControl.getAlertInit() == false) {
                        signOut();
                        stopAllScrNoti();
                    }else{
                        AlertDialog dialog;
                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                        dialogBuilder.setTitle("Sign out Failed");
                        dialogBuilder.setMessage("One alert is active now. Please close it before signing out");
                        dialogBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        dialog = dialogBuilder.create();
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                }else{
                    startActivity(new Intent(AppActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    finish();
                }
                return true;
            case R.id.account_setup_item:
                startActivity(new Intent(AppActivity.this, ProfileSetup.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                return true;
            case R.id.workplace_setup_item:
                startActivity(new Intent(AppActivity.this, WorkplaceSetup.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                return true;
            case R.id.sign_up_item:
                startActivity(new Intent(AppActivity.this, RegisterActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();
                return true;
            case R.id.about_us_page:
                startActivity(new Intent(AppActivity.this, AboutUs.class));
                return true;
            case R.id.app_settings_menu:
                if(aControl.getAlreadyAlerted() == false){
                    startActivityForResult(new Intent(AppActivity.this, SettingsActivity.class), SETTINGS_ACTIVITY_REQUEST);
                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(AppActivity.this)
                            .setCancelable(false)
                            .setTitle("Cannot Open Settings Page")
                            .setMessage("One alert is active. Please close alert to enter into the Settings page")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                return true;
            case R.id.nav_item_help:
                Uri uri = Uri.parse(HELP_URL);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            default:
                return false;
        }
    }

    private void getPersonalContacts(){
        PersonalContact[] contactArr;
        Gson gson = new Gson();

        contactList = new ArrayList<>();
        File file = new File(getFilesDir(), CONTACT_LOCAL_FILENAME);
        if(file.exists()) {
            String jsonStr = readJsonFile(CONTACT_LOCAL_FILENAME); //read local file from array
            contactArr = gson.fromJson(jsonStr, PersonalContact[].class);

            if (contactArr != null) {
                for (PersonalContact item : contactArr) {
                    if (item != null) {
                        contactList.add(item);
                    }
                }
            }
        }
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
            fis.close();
            listJson = fileContent.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return listJson;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(aControl.getAlertInit() == false && aControl.getAlreadyAlerted() == false) {
            if (key.equals("key_all_scr_noti")) {
                if (sharedPreferences.getBoolean(key, true)) {
                    startAllScrNoti();
                } else {
                    stopAllScrNoti();
                }
            }
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == PROFILE_ACTIVITY_CODE){
            retrieveImageToImageView();
        }
        if(requestCode == PICK_CONTACT || requestCode == CONTACT_UPDATE_REQUEST){
            for(Fragment fragment : getSupportFragmentManager().getFragments()){
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
        if(requestCode == ALERT_CLOSE_REQUEST_CODE){
            if(resultCode == ALERT_CLOSE_RESULT_CODE){
                alert_fab.setImageDrawable(getDrawable(R.drawable.ic_alert_new_fab_icon));
            }
        }
        if(requestCode == SETTINGS_ACTIVITY_REQUEST){

        }
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

    @Override
    protected void onResume() {
        super.onResume();
        if(aControl.getAlreadyAlerted() == false){
            alert_fab.setImageDrawable(getDrawable(R.drawable.ic_alert_new_fab_icon));
        }else{
            alert_fab.setImageDrawable(getDrawable(R.drawable.close_icon));
        }

        if(isServiceRunning(AlertService.class) || isServiceRunning(AlertInitiator.class)){
            alert_fab.setImageDrawable(getDrawable(R.drawable.close_icon));
        }else{
            alert_fab.setImageDrawable(getDrawable(R.drawable.ic_alert_new_fab_icon));
        }
    }
}

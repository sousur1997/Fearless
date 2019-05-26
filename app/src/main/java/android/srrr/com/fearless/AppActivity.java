package android.srrr.com.fearless;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.srrr.com.fearless.FearlessConstant.PROFILE_ACTIVITY_CODE;
import static android.srrr.com.fearless.FearlessConstant.START_ALERT;
import static android.srrr.com.fearless.FearlessConstant.STOP_ALERT;

public class AppActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    private int[] tab_icons = {R.mipmap.history, R.mipmap.home_icon, R.mipmap.call_icon};
    private TextView acc_badge, work_badge, profile_email;

    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private BottomAppBar bAppBar;

    private MenuItem acc_setup, work_setup, login_menu_item, register_item, acc_setu_grp_item;
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

    private AlertControl aControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);
        aControl = AlertControl.getInstance(getApplicationContext());

        prefManager = new PreferenceManager(getApplicationContext()); //setup the preference manager to store data

        aControl.setAlertInitiator(false);

        toolbar = findViewById(R.id.toolbar);
        bAppBar = findViewById(R.id.bottomAppBar);
        navView = findViewById(R.id.nav_menu);

        alert_fab = findViewById(R.id.alert_fab);

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
        if(user != null)
            userId = user.getUid();

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(AppActivity.this, ProfilePage.class), PROFILE_ACTIVITY_CODE);
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

        //if user is logged in, set the menu item as Sign Out
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            login_menu_item.setIcon(R.drawable.ic_logout);
            login_menu_item.setTitle("Sign Out");
            register_item.setVisible(false); //When logged in hide the sign up item
            acc_setu_grp_item.setVisible(true); //when logged in, show the account update group
            profile_image.setEnabled(true); //we can click on the image icon
            profile_email.setText(user.getEmail());
            retrieveImageToImageView(); //retrieve image and set as profile image

            logged_in = true;
        }else{
            login_menu_item.setIcon(R.drawable.ic_login);
            login_menu_item.setTitle("Login");
            register_item.setVisible(true); //When logged out or newly started app show the sign up item
            acc_setu_grp_item.setVisible(false); //when not logged in, hide the account update group
            profile_image.setEnabled(false); //we cannot click on the image icon
            profile_email.setText(getResources().getString(R.string.please_login));

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
                if(aControl.getAlertInit() == false){
                    if(aControl.getAlreadyAlerted() == false){
                        startService();
                        aControl.toggleAlertInitiator();
                    }else{
                        Toast.makeText(getApplicationContext(), "One alert is active", Toast.LENGTH_LONG).show();
                    }
                }else{
                    stopService();
                    aControl.toggleAlertInitiator();
                }
            }
        });
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
        if(aControl.getAlreadyAlerted() == true){ //when alert is already active, does not signout
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
        }else {
            FirebaseAuth.getInstance().signOut();
            //Clear the preference variables:
            prefManager.setBool("verify_email_sent", false);
            Toast.makeText(getApplicationContext(), "Sign Out", Toast.LENGTH_LONG).show();

            //after signing out, restart the current activity
            Intent loginIntent = getIntent();
            finish();
            startActivity(loginIntent);
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.signout_item:
                if(logged_in) {
                    signOut();
                }else{
                    startActivity(new Intent(AppActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                }
                return true;
            case R.id.account_setup_item: //by pressing account setup, go to the account setup page, then refresh
                startActivity(new Intent(AppActivity.this, ProfileSetup.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));

                return true;
            case R.id.workplace_setup_item: //by pressing account setup, go to the account setup page, then refresh
                startActivity(new Intent(AppActivity.this, WorkplaceSetup.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                return true;
            case R.id.sign_up_item: //by pressing account setup, go to the account setup page, then refresh
                startActivity(new Intent(AppActivity.this, RegisterActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
                return true;
            default:
                return false;
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
    }
}

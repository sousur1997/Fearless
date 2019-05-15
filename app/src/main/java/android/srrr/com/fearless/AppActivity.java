package android.srrr.com.fearless;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.bottomappbar.BottomAppBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

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

    private boolean alert_initiator = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app);

        prefManager = new PreferenceManager(getApplicationContext()); //setup the preference manager to store data

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

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AppActivity.this, ProfilePage.class));
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
            profile_email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

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
                if(alert_initiator == false){
                    startService();
                    alert_initiator = true;
                }else{
                    stopService();
                    alert_initiator = false;
                }
            }
        });
    }

    public void startService(){
        Intent alert_init_intent = new Intent(this, AlertInitiator.class);
        ContextCompat.startForegroundService(this, alert_init_intent);
    }

    public void stopService(){
        Intent alert_init_stop = new Intent(this, AlertInitiator.class);
        stopService(alert_init_stop);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.signout_item:
                if(logged_in) {
                    Toast.makeText(getApplicationContext(), "Sign Out", Toast.LENGTH_LONG).show();
                    FirebaseAuth.getInstance().signOut();
                    //Clear the preference variables:
                    prefManager.setBool("verify_email_sent", false);

                    //after signing out, restart the current activity
                    Intent loginIntent = getIntent();
                    finish();
                    startActivity(loginIntent);
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
}

package android.srrr.com.fearless;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SliderActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button skip_btn, next_btn;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceManager = new PreferenceManager(this);
        if(!preferenceManager.isFirstTimeLaunch()){ //If this is not for the first time, open actual activity
            launchRegisterScreen();
            finish();
        }

        //Transparent the notification bar
        if(Build.VERSION.SDK_INT >= 21){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_slider);

        //Create objects from the views from activity xml
        viewPager = findViewById(R.id.view_pager);
        dotLayout = findViewById(R.id.layoutDots);
        skip_btn = findViewById(R.id.btn_skip);
        next_btn = findViewById(R.id.btn_next);

        //Add layouts into the layouts array
        layouts = new int[] {R.layout.slide1, R.layout.slide2, R.layout.slide3, R.layout.slide4, R.layout.slide5};

        addBottomDots(0);

        //Change the status bar transparent
        changeStatusBarColor();

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        skip_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchRegisterScreen();
            }
        });

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = getItem(1);
                if(current < layouts.length){
                    viewPager.setCurrentItem(current);
                }else{
                    launchRegisterScreen();
                }
            }
        });
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            if(position == layouts.length - 1){ //If it is last page, show Got it
                next_btn.setText(getString(R.string.start));
                skip_btn.setVisibility(View.GONE);
            }else{ //If left pages are left, show next pages, and skip button is visible
                next_btn.setText(getString(R.string.next));
                skip_btn.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void addBottomDots(int current_page) {
        dots = new TextView[layouts.length]; //Add same number of dots that present in the layouts
        int[] active_col = getResources().getIntArray(R.array.array_dot_active);
        int[] inactive_col = getResources().getIntArray(R.array.array_dot_inactive);

        dotLayout.removeAllViews();
        for(int i = 0; i<dots.length; i++){
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226"));
            dots[i].setTextSize(30);
            dots[i].setTextColor(inactive_col[current_page]); //set all color to inactive
            dotLayout.addView(dots[i]);
        }

        if(dots.length > 0)
            dots[current_page].setTextColor(active_col[current_page]); //set current to active
    }

    private int getItem(int i){
        return viewPager.getCurrentItem() + i;
    }

    private void launchRegisterScreen(){
        preferenceManager.setFirstTimeLaunch(false);
        startActivity(new Intent(SliderActivity.this, RegisterActivity.class));
        finish();
    }

    private void changeStatusBarColor(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public class MyViewPagerAdapter extends PagerAdapter{
        private LayoutInflater layoutInflater;

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}

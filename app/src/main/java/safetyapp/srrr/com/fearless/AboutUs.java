package safetyapp.srrr.com.fearless;

import android.animation.ArgbEvaluator;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.viewpager.widget.ViewPager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class AboutUs extends AppCompatActivity {

    //just add the classes to the  main application. modify the layout if found not good enough.

    ViewPager viewPager;
    Adapter adapter;
    List<Model> model;
    Integer colors[] = null;
    ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean dark_toggle = sharedPreferences.getBoolean("dark_mode",false);
        if(dark_toggle) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        model = new ArrayList<Model>();
        //adds image,name,number and description to the model
        model.add(new Model(R.mipmap.cu_logo, "About The App", "Fearless (A Safety Application) was initially developed as a Software Engineering assignment for M.Sc program, University of Calcutta.", null));
        model.add(new Model(R.mipmap.ic_code,"Developers","Lead Developer:\n\n" + "Soumyadeep Sur\n\n" + "Associate Developer:\n\n" + "Rohit Ghosal\n","applicationfearless@gmail.com"));
        model.add(new Model(R.mipmap.ic_idea,"Special thanks to:","Souvik Das\nOur guide who advised us to improve this application in various ways." + "\n\nDr. Nabendu Chaki\nProfessor, the principle motivator behind the project. ",null));
        model.add(new Model(R.mipmap.ic_friends,"And also\n","\nNabanita Dey\nSamprita Roy Choudhury\nDebolina Saha\nRittik Mondal\nRikan Saha\n\nAnd everyone who is a part of our journey.", null));
        adapter = new Adapter(model,this);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setPadding(100,0,100,0);



        Integer temp[] = {
                getResources().getColor(R.color.card_back_0),
                getResources().getColor(R.color.card_back_1),
                getResources().getColor(R.color.card_back_5),
                getResources().getColor(R.color.card_back_6),

        };

        colors = temp;

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

                if (i < (adapter.getCount() -1) && i < (colors.length - 1)) {
                    viewPager.setBackgroundColor(
                            (Integer)argbEvaluator.evaluate(v,colors[i],colors[i + 1])
                    );
                }
                else {
                    viewPager.setBackgroundColor(colors[colors.length - 1]);
                }
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }
}

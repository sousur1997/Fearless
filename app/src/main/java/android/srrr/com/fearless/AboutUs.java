package android.srrr.com.fearless;

import android.animation.ArgbEvaluator;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        model = new ArrayList<Model>();
        //adds image,name,number and description to the model
        model.add(new Model(R.mipmap.cu_logo, "Software Engg Project", "Fearless (A Safety Application) is developed as a Software Engineering assignment for MSc Computer Science (University of Calcutta) Sem 2.\nUnder the supervision of \nDr. Nabendu Chaki", null));
        model.add(new Model(R.mipmap.ss_image,"Soumyadeep Sur","Lead Programmer/Designer of Android/Web\n" + "Commander-in-chief of this project. He has designed and implemented most of the features of the android app, and Fearless Admin/Web.","sousur1997@gmail.com"));
        model.add(new Model(R.mipmap.rg_image,"Rohit Ghosal","Associate Programmer/Designer Android/Web\n" + "Main adviser of the project. He helped the commander whenever needed and designed/developed Fearless web","ghosal.rohit@yahoo.com "));
        model.add(new Model(R.mipmap.rm_image,"Rittik Mondal","Tested the system in almost every scenarios possible.He is the main analyst who designed and developed the documents. Designed small modules for Fearless app","rittikmondal1997@gmail.com"));
        model.add(new Model(R.mipmap.rs_image,"Rikan Saha","Member. Gone through a lot of documents and helped to document Fearless. He also encounters many possible errors in a system","rikansaha1998@gmail.com "));

        adapter = new Adapter(model,this);

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setPadding(100,0,100,0);

        Integer temp[] = {
                getResources().getColor(R.color.card_back_0),
                getResources().getColor(R.color.card_back_1),
                getResources().getColor(R.color.card_back_2),
                getResources().getColor(R.color.card_back_3),
                getResources().getColor(R.color.card_back_4),
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

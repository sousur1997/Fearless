package safetyapp.srrr.com.fearless;

import android.animation.ArgbEvaluator;
import safetyapp.srrr.com.fearless.R;
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
        model.add(new Model(R.mipmap.cu_logo, "About The App", "Fearless (A Safety Application) was initially developed as a Software Engineering assignment for M.Sc program, University of Calcutta.", null));
        model.add(new Model(R.mipmap.ic_code,"Developers","Lead Developer:\n" + "Soumyadeep Sur\n" + "Associate Developer:\n" + "Rohit Ghosal\n" + "Tester/Advisor:\n" + "Rittik Mondal\n","applicationfearless@gmail.com"));
        model.add(new Model(R.mipmap.ic_idea,"Special thanks to:","Souvik Das\nOur guide who advised us to improve this application in various ways." + "\n\nDr. Nabendu Chaki\nProfessor, the principle motivator behind the project ",null));
        model.add(new Model(R.mipmap.ic_friends,"And also\n","\nNabanita Dey\nSamprita Roy Choudhury\nDebolina Saha\n\nOur friends who tested and provided feedback on the app whenever asked.", null));
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

package safetyapp.srrr.com.fearless;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.airbnb.lottie.LottieAnimationView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class Adapter extends PagerAdapter {

    private List<Model> model;
    private LayoutInflater layoutInflater;
    private Context context;
    private String address;
    private LottieAnimationView treeAnimView;

    public Adapter(List<Model> model, Context context) {
        this.model = model;
        this.context = context;
    }

    @Override
    public int getCount() {
        return model.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view.equals(o);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        layoutInflater = LayoutInflater.from(context);
        View view;
        if(position == model.size() - 1){
            view = layoutInflater.inflate(R.layout.thank_you_page, container, false);
            treeAnimView = view.findViewById(R.id.tree_anim_view);
            //treeAnimView.playAnimation();
        }else {
            view = layoutInflater.inflate(R.layout.about_us_item_new, container, false);

            //CircleImageView profile = view.findViewById(R.id.profile_image);

            TextView name = view.findViewById(R.id.heading_name);
            TextView text = view.findViewById(R.id.about_desc);
            //Button mail = view.findViewById(R.id.mail);

            //profile.setImageResource(model.get(position).getImage());
            name.setText(model.get(position).getTitle());
            text.setText(model.get(position).getDescription());

            name.setTextColor(Color.BLACK);
            text.setTextColor(Color.BLACK);

            //address = model.get(position).getEmail();

//        mail.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(view.getContext(), "Mail to: "+ model.get(position).getEmail(), Toast.LENGTH_SHORT).show();
//                /*this contains an arraylist which contains the email address,subject
//                  this is currently set to a dummy value
//                 */
//                address = model.get(position).getEmail();
//                Intent intent = new Intent(Intent.ACTION_SENDTO);
//                intent.setData(Uri.parse("mailto:"));
//                intent.putExtra(Intent.EXTRA_EMAIL,new String[]{address});
//                intent.putExtra(Intent.EXTRA_SUBJECT, "Fearless feedback");
//                if (intent.resolveActivity(context.getPackageManager()) != null) {
//                    context.startActivity(intent);
//                }
//
//            }
//        });
//
//        if(address == null){
//            mail.setVisibility(View.INVISIBLE);
//        }
        }

        container.addView(view,0);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    public void play_last_animation(){
        treeAnimView.playAnimation();
    }
}
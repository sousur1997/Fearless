package android.srrr.com.fearless;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfilePage extends AppCompatActivity {
    private ImageView verified_badge_iv;
    private TextView phone, address, dob, occupation, workplace_TV, wp_phone, wp_address, name, email;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private FloatingActionButton image_select_btn, image_upload_btn, edit_btn;
    private String userId;
    private CoordinatorLayout prof_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_page);

        phone = findViewById(R.id.phone_tv);
        address = findViewById(R.id.res_addr_tv);
        dob = findViewById(R.id.dob_tv);
        occupation = findViewById(R.id.occupation_tv);
        workplace_TV = findViewById(R.id.workplace_tv);
        wp_phone = findViewById(R.id.wp_phone_tv);
        wp_address = findViewById(R.id.wp_address_tv);
        name = findViewById(R.id.profile_name);
        email = findViewById(R.id.mail_address);
        prof_layout=findViewById(R.id.profile_layout);
        edit_btn=findViewById(R.id.edit_button);

        verified_badge_iv = findViewById(R.id.verified_badge);
        image_select_btn = findViewById(R.id.image_select_fab);
        image_select_btn.setColorFilter(getResources().getColor(R.color.alert_fab_icon_color), PorterDuff.Mode.SRC_IN);

        image_upload_btn = findViewById(R.id.image_upload_fab);
        image_upload_btn.setColorFilter(getResources().getColor(R.color.alert_fab_icon_color), PorterDuff.Mode.SRC_IN);

        user = FirebaseAuth.getInstance().getCurrentUser();
        userId = user.getUid();

        email.setText(user.getEmail());
        firestore = FirebaseFirestore.getInstance();

        getProfileDetails();

        //check whether the user is email verified or not.
        if(user.isEmailVerified()){
            verified_badge_iv.setImageDrawable(getDrawable(R.drawable.mail_varified));
            verified_badge_iv.setColorFilter(getResources().getColor(R.color.verify_badge_color), PorterDuff.Mode.SRC_IN);
        }else{
            verified_badge_iv.setImageDrawable(getDrawable(R.drawable.ic_not_verified_icon));
            verified_badge_iv.setColorFilter(getResources().getColor(R.color.not_verified_badge_color), PorterDuff.Mode.SRC_IN);
        }

        verified_badge_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfilePage.this, EmailVerification.class).putExtra("caller", "Profile");
                startActivityForResult(intent, 101);
            }
        });

        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfilePage.this, ProfileSetup.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == 1){
            if(requestCode == 101){
                finish();
                startActivity(getIntent());
            }
        }
    }

    private void getProfileDetails(){
        if(userId != null) {
            DocumentReference docRef = firestore.collection(getResources().getString(R.string.FIRESTORE_USERINFO_COLLECTION)).document(userId);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc != null) {
                            User user = task.getResult().toObject(User.class);
                            if(user != null) {
                                name.setText(user.getName());
                                phone.setText(user.getPhone());
                                String address_str = user.getStreet() + ", " + user.getState() + ", " + user.getCity() + ", " + user.getPin();
                                address.setText(address_str);
                                dob.setText(user.getDob());
                            }
                        }
                    }else{
                        Snackbar.make(prof_layout, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                }
            });

            docRef = firestore.collection(getResources().getString(R.string.FIRESTORE_WORKPLACE_COLLECTION)).document(userId);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot doc = task.getResult();
                        if (doc != null) {
                            Workplace workplace = task.getResult().toObject(Workplace.class);
                            if(workplace != null) {
                                workplace_TV.setText(workplace.getWp_name());
                                wp_phone.setText(workplace.getWp_phone());
                                String address_str = workplace.getWp_street() + ", " + workplace.getWp_state() + ", " + workplace.getWp_city() + ", " + workplace.getWp_pin();
                                wp_address.setText(address_str);
                                occupation.setText(workplace.getOccupation());
                            }
                        }
                    }else{
                        Snackbar.make(prof_layout, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}

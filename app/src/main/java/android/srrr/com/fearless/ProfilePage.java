package android.srrr.com.fearless;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

import static android.srrr.com.fearless.FearlessConstant.SELECT_FILE;

public class ProfilePage extends AppCompatActivity {
    private ImageView verified_badge_iv, profileImage;
    private TextView phone, address, dob, occupation, workplace_TV, wp_phone, wp_address, name, email;
    private FirebaseUser user;
    private FirebaseFirestore firestore;
    private FloatingActionButton image_select_btn, image_upload_btn, edit_btn;
    private String userId;
    private CoordinatorLayout prof_layout;
    private String userChosenTask;
    private StorageReference storage;
    private StorageReference profileImageReference;
    private Uri imageUriPath;

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
        profileImage = findViewById(R.id.profile_image);

        verified_badge_iv = findViewById(R.id.verified_badge);
        image_select_btn = findViewById(R.id.image_select_fab);
        image_select_btn.setColorFilter(getResources().getColor(R.color.alert_fab_icon_color), PorterDuff.Mode.SRC_IN);

        image_upload_btn = findViewById(R.id.image_upload_fab);
        image_upload_btn.setColorFilter(getResources().getColor(R.color.alert_fab_icon_color), PorterDuff.Mode.SRC_IN);

        //setup user reference from Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
            userId = user.getUid();

        email.setText(user.getEmail());
        firestore = FirebaseFirestore.getInstance();

        //at first the upload image button will not ve visible
        image_upload_btn.hide();

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

        image_upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImageToServer();
            }
        });

        image_select_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryIntent();
            }
        });
    }

    private void retrieveImageToImageView(){
        storage = FirebaseStorage.getInstance().getReference();
        profileImageReference = storage.child("ProfileImages/" + userId + ".jpg"); //store image with <userId>.jpg

        profileImageReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profileImage.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                profileImage.setImageDrawable(getDrawable(R.mipmap.user_icon)); //if it fails to load image, set the image as default image
            }
        });
    }

    private void saveImageToServer(){
        final ProgressDialog progressDialog = new ProgressDialog(ProfilePage.this);
        progressDialog.setTitle("Uploading Image");
        progressDialog.setCancelable(false);
        progressDialog.show();

        //setup the storage reference for profile image
        storage = FirebaseStorage.getInstance().getReference();
        profileImageReference = storage.child("ProfileImages/" + userId + ".jpg"); //store image with <userId>.jpg
        profileImageReference.putFile(imageUriPath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Snackbar.make(prof_layout, "Profile image updated successfully", Snackbar.LENGTH_LONG).show();
                image_upload_btn.hide();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Snackbar.make(prof_layout, e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        })
        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                progressDialog.setMessage("Uploaded: " + ((int)progress) + "%...");
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
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == SELECT_FILE){
                openSelectFromGalleryResult(data);
            }
        }
    }

    private void getProfileDetails(){
        if(userId != null) {
            final ProgressDialog progressDialog = new ProgressDialog(ProfilePage.this);
            progressDialog.setTitle("Please Wait");
            progressDialog.setMessage("Getting details from server...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            retrieveImageToImageView();

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
                        progressDialog.dismiss();
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
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void galleryIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), SELECT_FILE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(userChosenTask.equals("Choose from Gallery")){
                        galleryIntent();
                    }
                }else{
                    //handling deny
                }
        }
    }

    private void openSelectFromGalleryResult(Intent data){
        Bitmap bitmap = null;
        if(data != null){
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                imageUriPath = data.getData(); //store the uri of the image to upload
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        profileImage.setImageBitmap(bitmap);
        image_upload_btn.show();
    }
}

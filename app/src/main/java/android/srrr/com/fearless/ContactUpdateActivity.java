package android.srrr.com.fearless;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static android.srrr.com.fearless.FearlessConstant.CONTACT_LIST_INDEX_EXTRA;
import static android.srrr.com.fearless.FearlessConstant.CONTACT_NAME_CHANGE_EXTRA;
import static android.srrr.com.fearless.FearlessConstant.CONTACT_NAME_EXTRA;
import static android.srrr.com.fearless.FearlessConstant.CONTACT_PHONE_CHANGE_EXTRA;
import static android.srrr.com.fearless.FearlessConstant.CONTACT_PHONE_EXTRA;

public class ContactUpdateActivity extends AppCompatActivity {

    private EditText contactNameEd, contactPhoneEd;
    private Button updateBtn;
    private Intent intent;
    String oldName, oldPhone;
    int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_update);

        contactNameEd = findViewById(R.id.contact_name_ed);
        contactPhoneEd = findViewById(R.id.contact_phone_ed);
        updateBtn = findViewById(R.id.contact_update_btn);

        intent = getIntent();
        contactNameEd.setText(intent.getStringExtra(CONTACT_NAME_EXTRA));
        contactPhoneEd.setText(intent.getStringExtra(CONTACT_PHONE_EXTRA));
        index = intent.getIntExtra(CONTACT_LIST_INDEX_EXTRA, -1);

        //take the old name and phone to check whether they are updated or not
        oldName = contactNameEd.getText().toString();
        oldPhone = contactPhoneEd.getText().toString();

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTask();
            }
        });
    }

    private void updateTask(){
        //if the mobile number is not in correct form, return an error
        String phoneNumber = contactPhoneEd.getText().toString();

        phoneNumber = phoneNumber.replaceAll("\\s+", "");
        if(phoneNumber.length() > 10){
            if(phoneNumber.length() == 13) {
                if(!phoneNumber.startsWith("+91")) {
                    contactPhoneEd.setError("Phone number will be either ten digits long or\n+91 can be added at the beginning");
                    contactPhoneEd.requestFocus();
                    return;
                }
            }else{
                contactPhoneEd.setError("Phone number will be either ten digits long or\n+91 can be added at the beginning");
                contactPhoneEd.requestFocus();
                return;
            }
        }else if(phoneNumber.length() < 10){
            contactPhoneEd.setError("Phone number must be at least ten digits long");
            contactPhoneEd.requestFocus();
            return;
        }

        if(oldName.equals(contactNameEd.getText().toString()) && oldPhone.equals(contactPhoneEd.getText().toString()) ){
            finish();
            return; //if no change is found, do not do anything
        }

        //Otherwise send back to previous page with updated value
        Intent sendBack = new Intent();
        sendBack.putExtra(CONTACT_NAME_CHANGE_EXTRA, contactNameEd.getText().toString());
        sendBack.putExtra(CONTACT_PHONE_CHANGE_EXTRA, contactPhoneEd.getText().toString());
        sendBack.putExtra(CONTACT_LIST_INDEX_EXTRA, index);
        setResult(Activity.RESULT_OK, sendBack);
        finish();
    }
}

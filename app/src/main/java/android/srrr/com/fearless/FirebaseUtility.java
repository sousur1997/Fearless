package android.srrr.com.fearless;

import com.google.firebase.auth.FirebaseAuth;

public class FirebaseUtility {
    private FirebaseAuth mAuth;

    public FirebaseUtility(){
        mAuth = FirebaseAuth.getInstance();
    }

    public void CreateNewUser(String email, String pass){

    }
}

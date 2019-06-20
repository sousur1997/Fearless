package android.srrr.com.fearless;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import static android.srrr.com.fearless.FearlessConstant.LOG_COLLECTION;

public class FearlessLog {
    private static FearlessLog _instance = null;
    private static FirebaseAuth mAuth;
    private static FirebaseFirestore firestore;
    private static String userId;

    private FearlessLog(){
        //private constructor for singleton class
    }

    public static FearlessLog getInstance(){
        if(_instance == null){
            _instance = new FearlessLog();
        }
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        return _instance;
    }

    public static void sendLog(String message){
        Long timestamp = System.currentTimeMillis();
        Map<String, String> logMap = new HashMap<>();
        logMap.put(timestamp.toString(), message);

        if(mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        }

        if(userId != null) {
            firestore.collection(LOG_COLLECTION).document(userId).set(logMap, SetOptions.merge());
        }
    }
}

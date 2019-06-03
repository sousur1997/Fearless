package android.srrr.com.fearless;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    Context _context;

    //Shared Preference Mode
    int PRIVATE_MODE = 0;

    //Set preference file name
    private static final String PREF_FILE = "fearless_pref";
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public PreferenceManager(Context ctx){
        this._context = ctx;
        sp = _context.getSharedPreferences(PREF_FILE, PRIVATE_MODE);
        editor = sp.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime){
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public void setBool(String key, boolean value){
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean getBool(String key, boolean default_val){
        return sp.getBoolean(key, default_val);
    }

    public boolean isFirstTimeLaunch(){
        return sp.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public SharedPreferences getSharedPref(){
        return sp;
    }
}

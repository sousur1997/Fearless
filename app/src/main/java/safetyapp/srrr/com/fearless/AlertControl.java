package safetyapp.srrr.com.fearless;

import android.content.Context;

class AlertControl {
    private static AlertControl ourInstance = null;
    private static PreferenceManager preferenceManager;
    private static boolean already_alerted = false;
    public static boolean alert_intiator = false;

    static AlertControl getInstance(Context ctx) {
        if(ourInstance == null)
            ourInstance = new AlertControl(ctx);

        return ourInstance;
    }

    private AlertControl(Context ctx) {
        preferenceManager = new PreferenceManager(ctx);
        already_alerted = preferenceManager.getBool("already_alerted", false);
    }

    public static boolean getAlertInit(){
        return alert_intiator;
    }

    public static void setAlertInitiator(boolean state){
        alert_intiator = state;
    }

    public static void toggleAlertInitiator(){
        if(alert_intiator == true){
            alert_intiator = false;
        }else{
            alert_intiator = true;
        }
    }

    public static boolean getAlreadyAlerted(){
        return already_alerted;
    }

    public static void setAlreadyAlerted(boolean state){
        already_alerted = state;
        preferenceManager.setBool("already_alerted" ,already_alerted);
    }

    public static void toggleAlreadyAlerted(){
        if(already_alerted == true){
            already_alerted = false;
        }else{
            already_alerted = true;
        }
        preferenceManager.setBool("already_alerted" ,already_alerted);
    }
}

package android.srrr.com.fearless;

public class FearlessConstant {
    public static final String START_ALERT = "Start_Alert";
    public static final String STOP_ALERT = "Stop_Alert";
    public static final String ACTUAL_START_ALERT = "Actual_Alert_Start";
    public static final String ACTUAL_STOP_ALERT = "Actual_Alert_Stop";
    public static final String ACTUAL_ALERT_CALL = "Call_Trusted_Contact";
    public static final String ALERT_INIT_BROADCAST = "alert_init_broadcast";
    public static final String ALERT_BROADCAST_STOP = "alert_broadcast_stop";
    public static final String ALERT_BROADCAST_CALL = "alert_broadcast_call";
    public static final String ALERT_CHANNEL = "AlertServiceChannel";

    public static boolean alert_intiator = false;
    public static boolean already_alerted = false;

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
    }

    public static void toggleAlreadtAlerted(){
        if(already_alerted == true){
            already_alerted = false;
        }else{
            already_alerted = true;
        }
    }
}

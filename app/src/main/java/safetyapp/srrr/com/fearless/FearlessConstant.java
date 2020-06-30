package safetyapp.srrr.com.fearless;

public class FearlessConstant {
    public static final String START_ALERT = "Start_Alert";
    public static final String STOP_ALERT = "Stop_Alert";
    public static final String ACTUAL_START_ALERT = "Actual_Alert_Start";
    public static final String ACTUAL_STOP_ALERT = "Actual_Alert_Stop";
    public static final String ACTUAL_ALERT_CALL = "Call_Trusted_Contact";
    public static final String ALERT_INIT_BROADCAST = "alert_init_broadcast";
    public static final String ALERT_BROADCAST_STOP = "alert_broadcast_stop";
    public static final String ALERT_BROADCAST_CALL = "alert_broadcast_call";

    public static final String NEARBY_ALERT_CHANNEL = "NearbyAlertChannel";
    public static final String ALERT_CHANNEL = "AlertServiceChannel";
    public static final String ALL_SCREEN_CHANNEL = "allScreenChannel";
    public static final String INITIATOR_CHANNEL = "initiator_noti_channel";

    public static final String START_ALL_SCR = "All_Screen_Service";
    public static final String STOP_ALL_SCR = "All_Screen_Service_Stop";
    public static final String ALERT_RAISE_BROADCAST = "alert_raise_broadcast";
    public static final String ALERT_INIT_START = "alert_init_start";
    public static final String START_NEARBY_SERVICE = "Nearby_alert_start";
    public static final String STOP_NEARBY_SERVICE = "Nearby_alert_stop";

    public static final int SELECT_FILE = 2;
    public static final int ALL_PERMISSION = 500;
    public static final int PROFILE_ACTIVITY_CODE = 202;
    public static final int LOCATION_PERMISSION = 100;
    public static final int CALL_PERMISSION = 150;
    public static final int PICK_CONTACT_PERMISSION = 170;
    public static int REQUEST_MULTIPLE_PERMISSIONS = 124;
    public static final int PICK_CONTACT = 200;
    public static final String ALERT_JSON_FILENAME = "Alert_Event_file.json";
    public static final String ALERT_COMPLETE = "Alert_Event_complete";
    public static final String PENDING_FILENAME = "pending_events.json";
    public static final String HISTORY_COLLECTION = "EventHistory";
    public static final String FIRESTORE_USERINFO_COLLECTION = "UserInformation";
    public static final String FIRESTORE_WORKPLACE_COLLECTION = "WorkplaceInformation";
    public static final String HISTORY_LIST_FILE = "History_List.json";
    public static final String HISTORY_INDEX_KEY = "history_index";
    public static final String CONTACT_LOCAL_FILENAME = "local_contact.json";
    public static final String CONTACT_UPLOAD_PENDING = "pending_contact_upoad";
    public static final String CONTACT_COLLECTION = "PersonalContacts";
    public static final int MAX_CONTACT_TO_ADD = 10;
    public static final int SOS_NUMBER_COUNT = 9;
    public static final int ALERT_CLOSE_RESULT_CODE = 222;
    public static final int ALERT_CLOSE_REQUEST_CODE = 223;
    public static final String INIT_BROADCAST_FILTER = "init_broadcast_filter";
    public static final String ALL_SCR_START_BROADCAST_FILTER = "all_screen_start_broadcast_filter";

    public static final String CONTACT_NAME_EXTRA = "contactName";
    public static final String CONTACT_PHONE_EXTRA = "contactPhone";
    public static final String CONTACT_NAME_CHANGE_EXTRA = "contactNameChange";
    public static final String CONTACT_PHONE_CHANGE_EXTRA = "contactPhoneChange";
    public static final String CONTACT_LIST_INDEX_EXTRA = "listIndex";
    public static final int CONTACT_UPDATE_REQUEST = 301;

    public static final int SETTINGS_ACTIVITY_REQUEST = 600;

    public static final String LOG_COLLECTION = "LogCollection";

    //Log Messages
    public static final String LOG_SIGN_UP = "Registered into Fearless";
    public static final String LOG_LOGIN = "Logged in into the system";
    public static final String LOG_LOGOUT = "Logged Out from the system";
    public static final String LOG_EMAIL_VERIFY_REQUEST = "Requested to verify email";
    public static final String LOG_CONTACT_UPDATE = "Contact Lists Updated";
    public static final String LOG_ACCOUNT_SETUP = "Provide/Update Personal Information";
    public static final String LOG_WORKPLACE_SETUP = "Provide/Update Workplace Information";


    public static final String HELP_URL = "https://fearless-238805.web.app/help.html";
    public static final String FEEDBACK_URL = "https://docs.google.com/forms/d/e/1FAIpQLSewQMFsLwhKzhJJN7UaQXcK1YNKg9BNLbS3sRhHDwRQb2sgxQ/viewform";
    public static final String TERMS_URL = "https://fearless-238805.firebaseapp.com/privacy_policy.html";
    public static final String TERMS_URL_2 = "https://fearless-238805.firebaseapp.com/terms_and_conditions.html";

    //pubnub keys
    public static final String SUBSCRIBE_KEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
    public static final String PUBLISH_KEY = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

    //pubnub channel name
    public static final String CHANNEL_NAME = "alertChannel";

    //custom action
    public static final String NEARBY_ALERT_SEND = "fearless_alert_send";

    //nearby alert activity loading file
    public static final String NEARBY_ALERT_FILE = "nearby_alert_list_file.json";
    public static final String NEARBY_ALERT_OBJECT_KEY = "nearby_alert_object_key";


}

package safetyapp.srrr.com.fearless;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresApi;

/*  This wonderful code has been taken from this website:
    https://blog.usejournal.com/method-to-detect-if-user-has-selected-dont-ask-again-while-requesting-for-permission-921b95ded536
    Thanks to him!
 */

public class PermissionUtility {

    @RequiresApi(api = Build.VERSION_CODES.M)
    /*checks if the user has denied the permission*/
    public static boolean neverAskAgain(final Activity activity, final String permission){
        final boolean prevShouldShowStatus = getRationaleDisplayStatus(activity,permission);
        final boolean currShouldShowStatus = activity.shouldShowRequestPermissionRationale(permission);
        return prevShouldShowStatus != currShouldShowStatus;

    }
    /*determines if the app should display the permission dialog again*/
    public static void setShouldShowStatus(final Context context, final String permission) {
        SharedPreferences genPrefs = context.getSharedPreferences("GENERIC_PREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = genPrefs.edit();
        editor.putBoolean(permission, true);
        editor.commit();
    }
    /*checks the shared preference status*/
    public static boolean getRationaleDisplayStatus(final Context context, final String permission) {
        SharedPreferences genPrefs = context.getSharedPreferences("GENERIC_PREFERENCES", Context.MODE_PRIVATE);
        return genPrefs.getBoolean(permission, false);
    }
}

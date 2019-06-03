package android.srrr.com.fearless;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.common.util.concurrent.Runnables;

import java.util.List;
import java.util.Locale;

public class AlertUtility {
    private static AlertUtility _instance = null;
    private Context context;
    private int smsInterval;
    private SharedPreferences preferences;
    private MessageTask messageTask;
    private CountDownTimer timer;
    private boolean flag = true;
    private static Thread messageThread = null;
    private static MessageRunnable messageRunnable;
    private AlertListener listener;
    private static String address;

    private AlertUtility(Context context, AlertListener listener, Location location){
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.listener = listener;

        //get the sms interval from preference.
        String intervalValue = preferences.getString("automatic_message_repeat_duration", null);
        if(intervalValue != null){
            smsInterval = Integer.parseInt(intervalValue);
            Toast.makeText(context, "Interval value is: " + smsInterval, Toast.LENGTH_LONG).show();

            timer = new CountDownTimer(smsInterval*10000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    //noting to do in each second
                }

                @Override
                public void onFinish() {
                    flag = true;
                }
            };
            messageRunnable = new MessageRunnable();
        }
    }

    public static AlertUtility getInstance(Context context, AlertListener listener, Location location){
        if(_instance == null){
            _instance = new AlertUtility(context, listener, location);
        }
        address = _instance.getCurrentAddress(location.getLatitude(), location.getLongitude());
        if(messageThread == null){
            messageThread = new Thread(messageRunnable);
            messageThread.start();
        }
        return _instance;
    }

    public String getCurrentAddress(double lat, double lng){
        String address = null;
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try{
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1); //get max one address

            if(addresses != null){
                Address returnAddress = addresses.get(0);
                StringBuilder addressStringBuilder = new StringBuilder("Current Location: ");
                addressStringBuilder.append("Address:" + returnAddress.getAddressLine(0));

                address = addressStringBuilder.toString();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return address;
    }

    private class MessageRunnable implements Runnable {
        @Override
        public void run() {
            while(true){
                if(flag){ //if flag is true, set it false, it will be again set by counter
                    listener.onTimerEnds(address);
                    flag = false;
                    timer.start();
                }
            }
        }
    }

    private class MessageTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            while(true){
                if(flag){ //if flag is true, set it false, it will be again set by counter
                    Toast.makeText(context, "Sending Message", Toast.LENGTH_LONG);
                    flag = false;
                    timer.start();
                }
            }
        }
    }
}

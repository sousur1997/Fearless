package safetyapp.srrr.com.fearless;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class NearbyAlertBroadcastReceiver extends BroadcastReceiver {

    private boolean addFlag;
    private Gson gsonObject = new Gson();
    private ArrayList<NearbyAlertDataModel> objectArray;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(FearlessConstant.NEARBY_ALERT_SEND)) {
            loadFromFile(context);
            addFlag = false;
            String receiptString = intent.getStringExtra("alertMessage");
            Log.e("Received Message", receiptString);
            NearbyAlertDataModel temp = gsonObject.fromJson(receiptString,NearbyAlertDataModel.class);
            if(objectArray.size() > 0) {
                for (NearbyAlertDataModel item : objectArray) {
                    if (temp.getUid().equals(item.getUid()) && addFlag == false && temp.getMessage().equals("alert")) {
                        objectArray.set(objectArray.indexOf(item), temp);
                        addFlag = true;
                    }
                    else if(temp.getUid().equals(item.getUid()) && temp.getMessage().equals("alert_end")){
                        objectArray.remove(objectArray.indexOf(item));
                    }
                    else{
                        continue;
                    }
                }
                if(addFlag == false && temp.getMessage().equals("alert")) {
                    objectArray.add(temp);
                    addFlag = true;
                }
            }
            else{
                if(temp.getMessage().equals("alert")){
                    objectArray.add(temp);
                    addFlag = true;
                }
            }
//            Log.e("ArraySize", Integer.toString(objectArray.size()));
            String outputStr = gsonObject.toJson(objectArray);
            FileOutputStream outputStream = null;
            try {
                if (outputStr != null && !outputStr.isEmpty()) {
                    outputStream = context.openFileOutput(FearlessConstant.NEARBY_ALERT_FILE, MODE_PRIVATE);
                    outputStream.write(outputStr.getBytes());
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

//    public String getJsonString() {
//        return jsonStr;
//    }

    private void loadFromFile(Context context) {
        //loads the json object from the file
        objectArray =  new ArrayList<>();
        FileInputStream inputStream = null;

        try {
            inputStream = context.openFileInput(FearlessConstant.NEARBY_ALERT_FILE);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String input;

            while((input = bufferedReader.readLine()) != null ){
                stringBuilder.append(input);
            }
            Type itemType = new TypeToken<ArrayList<NearbyAlertDataModel>>(){}.getType();
            objectArray = gsonObject.fromJson(stringBuilder.toString(),itemType);

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(inputStream != null ) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

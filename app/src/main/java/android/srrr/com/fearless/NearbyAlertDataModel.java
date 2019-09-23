package android.srrr.com.fearless;

import android.support.annotation.NonNull;

public class NearbyAlertDataModel {
    private double latitude,longitude;
    private long timestamp;
    private String uid;

//    public NearbyAlertDataModel(double latitude, double longitude, long timestamp, String userId) {
//        this.latitude = latitude;
//        this.longitude = longitude;
//        this.timestamp = timestamp;
//        this.userId = userId;
//    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String userId) {
        this.uid = uid;
    }

    @NonNull
    @Override
    public String toString() {
//        return super.toString();
         String retStr = new String();
         retStr = "latitude:" +getLatitude()+"longitude:"+getLongitude()+"timestamp:"+getTimestamp()+"userid:"+getUid();
         return retStr;
    }
}

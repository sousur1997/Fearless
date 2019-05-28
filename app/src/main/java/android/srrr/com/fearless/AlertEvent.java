package android.srrr.com.fearless;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlertEvent {
    private String timestamp;
    private int minuteOffset;
    private List<LocationLatLng> locationArray;

    //create an event with current timestamp, minute offset
    public AlertEvent(int minuteOffset){
        Long timestampLong = new Long(System.currentTimeMillis());
        this.timestamp = timestampLong.toString(); //get the UNIX timestamp
        this.minuteOffset = minuteOffset;
        locationArray = new ArrayList<>();
    }

    public AlertEvent(){

    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getMinuteOffset() {
        return minuteOffset;
    }

    public void setMinuteOffset(int minuteOffset) {
        this.minuteOffset = minuteOffset;
    }

    public List<LocationLatLng> getLocationArray() {
        return locationArray;
    }
    public void addLocation(double lat, double lng){
        LocationLatLng loc = new LocationLatLng(lat, lng);
        locationArray.add(loc);
    }

    public boolean hasLocationHistory(){
        if(locationArray.size() > 0)
            return true;
        return false;
    }

    @Override
    public String toString() {
        String AlertStr = "";
        AlertStr += "{\n\ttimestamp:"+timestamp+"\n\toffset:"+minuteOffset+"\n\tLocation:"+locationArray.toString()+"\n}";
        return AlertStr;
    }

    public String getReadableTime(){
        String pattern = "EEEE MMMM yyyy hh:mm:ss a";
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, new Locale("en", "IN"));
        Date date = new Date(Long.parseLong(timestamp));
        String dateStr = dateFormat.format(date);
        return dateStr;
    }
}

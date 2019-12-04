package safetyapp.srrr.com.fearless;

public class Workplace {
    private String wp_name, wp_phone, wp_city, wp_street, wp_state, wp_pin, occupation;

    public Workplace(){
        this("", "", "", "", "", "", "");
    }

    public Workplace(String wp_name, String wp_phone, String wp_street, String wp_city, String wp_state, String wp_pin, String occupation) {
        this.wp_name = wp_name;
        this.wp_phone = wp_phone;
        this.wp_street = wp_street;
        this.wp_city = wp_city;
        this.wp_state = wp_state;
        this.wp_pin = wp_pin;
        this.occupation = occupation;
    }

    public String getWp_street() {
        return wp_street;
    }

    public void setWp_street(String wp_street) {
        this.wp_street = wp_street;
    }

    public String getWp_name() {
        return wp_name;
    }

    public void setWp_name(String wp_name) {
        this.wp_name = wp_name;
    }

    public String getWp_phone() {
        return wp_phone;
    }

    public void setWp_phone(String wp_phone) {
        this.wp_phone = wp_phone;
    }

    public String getWp_city() {
        return wp_city;
    }

    public void setWp_city(String wp_city) {
        this.wp_city = wp_city;
    }

    public String getWp_state() {
        return wp_state;
    }

    public void setWp_state(String wp_state) {
        this.wp_state = wp_state;
    }

    public String getWp_pin() {
        return wp_pin;
    }

    public void setWp_pin(String wp_pin) {
        this.wp_pin = wp_pin;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }
}

package android.srrr.com.fearless;

public class User {
    private String email, name, phone, street, city, state, pin, dob;
    private Workplace workplace;

    public Workplace getWorkplace() {
        return workplace;
    }

    public void setWorkplace(Workplace workplace) {
        this.workplace = workplace;
    }

    public User(){ //The non parameterized constructor, set all strings as blank
        this("", "", "", "", "","", "", "", new Workplace());
    }
    public User(String email, String name, String phone, String street, String city, String state, String pin, String dob, Workplace workplace) {
        this.email = email;
        this.name = name;
        this.phone = phone;
        this.street = street;
        this.city = city;
        this.state = state;
        this.pin = pin;
        this.dob = dob;
        this.workplace = workplace;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }
}

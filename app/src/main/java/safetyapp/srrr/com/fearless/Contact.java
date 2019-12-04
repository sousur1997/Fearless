package safetyapp.srrr.com.fearless;

public class Contact {
    private String name, phone;
    public Contact(String name, String number){
        this.name = name;
        this.phone = number;
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
}

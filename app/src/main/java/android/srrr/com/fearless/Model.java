package android.srrr.com.fearless;

public class Model {

    private int image;
    private String title;
    private String description;
    private String email;

    public Model(int image, String title, String description, String email) {
        this.image = image;
        this.title = title;
        this.description = description;
        this.email = email;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

package android.srrr.com.fearless;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailPasswordMatcher {

    private Pattern pattern;
    private Matcher matcher;

    private static final String PASSWORD_MATCH_RE = "((?=.*[(){}?><|*~!@#$%^&+/-])(?=.*[A-Za-z]{2,})(?=.*\\d).{8,14})";
    private static final String EMAIL_RE = "^(.+)@(.+)$";

    /*
    The password criteria
    Length must be in range (8 to 14)
    Must be one special character
    Two or more than two alphabetic characters
    At least one numeric characters
     */

    public EmailPasswordMatcher(){

    }

    public Boolean checkPasswordCriteria(String password){
        pattern = Pattern.compile(PASSWORD_MATCH_RE);
        matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public Boolean checkemailFormat(String email){
        pattern = Pattern.compile(EMAIL_RE);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }
}

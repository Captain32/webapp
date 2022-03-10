package webapp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class User {
    private String id;
    private final String username;
    private String password;
    private String salt;
    private String first_name;
    private String last_name;
    private final String account_created;
    private String account_updated;
    private boolean verified;
    private String verified_on;

    public User(String id, String username, String password, String salt, String first_name, String last_name, String account_created, String account_updated, boolean encrypt, boolean verified, String verified_on) {
        this.id = id;
        this.username = username;
        if (!encrypt)
            this.password = BCrypt.hashpw(password, salt);
        else
            this.password = password;
        this.salt = salt;
        this.first_name = first_name;
        this.last_name = last_name;
        this.account_created = account_created;
        this.account_updated = account_updated;
        this.verified = verified;
        this.verified_on = verified_on;
    }

    public void setPassword(String password) {
        this.password = BCrypt.hashpw(password, salt);
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public void setAccount_updated(String account_updated) {
        this.account_updated = account_updated;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonIgnore
    public String getSalt() {
        return salt;
    }

    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getAccount_created() {
        return account_created;
    }

    public String getAccount_updated() {
        return account_updated;
    }

    public boolean getVerified() {
        return verified;
    }

    public String getVerified_on() {
        return verified_on;
    }
}

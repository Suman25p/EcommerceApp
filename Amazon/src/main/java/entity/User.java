package entity;

public class User {

    private int user_id;
    private String user_name;
    private String phoneNumber;
    private String email_id;

    public int getUser_id() { return user_id; }

    public void setUser_id(int user_id) { this.user_id = user_id; }

    public String getUser_name() { return user_name; }

    public void setUser_name(String user_name) { this.user_name = user_name; }

    public String getPhoneNumber() { return phoneNumber; }

    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail_id() { return email_id; }

    public void setEmail_id(String email_id) { this.email_id = email_id; }
}
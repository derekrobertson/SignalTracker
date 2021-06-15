package com.b183237x.signaltracker;

import com.google.gson.annotations.SerializedName;


public class User {

    @SerializedName("user_id")
    private String user_id;

    @SerializedName("first_name")
    private String first_name;

    @SerializedName("last_name")
    private String last_name;

    @SerializedName("email")
    private String email;

    // Password is not returned from GET to API!
    @SerializedName("password")
    private String password;

    @SerializedName("role")
    private String role;

    @SerializedName("login_failure_count")
    private String login_failure_count;

    @SerializedName("login_locked_timestamp")
    private String login_locked_timestamp;

    @SerializedName("timestamp")
    private String timestamp;



    public User(String firstName, String lastName, String email, String password, String role) {
        this.first_name = firstName;
        this.last_name = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // user_id is read only
    public String getUserId() {
        return user_id;
    }

    // first_name is read / write
    public String getFirstName() {
        return first_name;
    }

    public void setFirstName(String firstName) {
        this.first_name = firstName;
    }

    // last_name is read / write
    public String getLastName() {
        return last_name;
    }

    public void setLastName(String lastName) {
        this.last_name = lastName;
    }

    // email is read / write
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // role is read only
    public String getRole() {
        return role;
    }

    // login_failure_count is read only
    public String getLoginLockedFailureCount() {
        return login_failure_count;
    }

    // login_locked_timestamp is read only
    public String getLoginLockedTimestamp() {
        return login_locked_timestamp;
    }

    // Update timestamp is read only
    public String getTimestamp() {
        return timestamp;
    }





}

package com.b183237x.signaltracker;

// class that holds information about this app instance in memory for quick retrieval
// Singleton pattern


public class AppProperties {
    private String uuid;
    private String user_id;
    private String email;
    private String password;
    private String device_id;
    private static AppProperties instance;

    private AppProperties() {
        // private to prevent external instatiation
    }

    // Returns the singleton instance of this class, creating it if necessary
    public static AppProperties getInstance() {
        if (instance == null) {
            instance = new AppProperties();
        }
        return instance;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserId() {
        return this.user_id;
    }

    public void setUserId(String userId) {
        this.user_id = userId;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceId() {
        return this.device_id;
    }

    public void setDeviceId(String deviceId) {
        this.device_id = deviceId;
    }

}

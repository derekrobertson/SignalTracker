package com.b183237x.signaltracker.pojomodels;

import com.google.gson.annotations.SerializedName;

public class Device {

    @SerializedName("device_id")
    private String device_id;

    @SerializedName("user_id")
    private String user_id;

    @SerializedName("manufacturer")
    private String manufacturer;

    @SerializedName("model")
    private String model;

    @SerializedName("serial_no")
    private String serial_no;

    @SerializedName("android_version")
    private String android_version;

    @SerializedName("timestamp")
    private String timestamp;


    public Device(String userId, String manufacturer, String model, String serialNo, String androidVersion) {
        this.user_id = userId;
        this.manufacturer = manufacturer;
        this.model = model;
        this.serial_no = serialNo;
        this.android_version = androidVersion;
    }

    // device_id is read only
    public String getDeviceId() {
        return this.device_id;
    }

    // user_id_name is read / write
    public String getUserId() {
        return this.user_id;
    }
    public void setUserId(String userId) {
        this.user_id = userId;
    }

    // manufacturer is read / write
    public String getManufacturer() {
        return this.manufacturer;
    }
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    // model is read / write
    public String getModel() {
        return this.model;
    }
    public void setModel(String model) {
        this.model = model;
    }

    // serial_no is read / write
    public String getSerialNo() {
        return this.serial_no;
    }
    public void setSerialNo(String serialNo) {
        this.serial_no = serialNo;
    }

    // android_version is read / write
    public String getAndroidVersion() {
        return this.android_version;
    }
    public void setAndroidVersion(String androidVersion) {
        this.android_version = androidVersion;
    }

    // Update timestamp is read only
    public String getTimestamp() {
        return timestamp;
    }
}

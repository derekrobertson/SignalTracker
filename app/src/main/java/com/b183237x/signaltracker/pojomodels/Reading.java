package com.b183237x.signaltracker.pojomodels;

import com.google.gson.annotations.SerializedName;

public class Reading {

    @SerializedName("reading_id")
    private String reading_id;

    @SerializedName("device_id")
    private String device_id;

    @SerializedName("celltower_id")
    private String celltower_id;

    @SerializedName("latitude")
    private String latitude;

    @SerializedName("longitude")
    private String longitude;

    @SerializedName("signal_type")
    private String signal_type;

    @SerializedName("signal_value")
    private String signal_value;

    @SerializedName("timestamp")
    private String timestamp;

    public Reading(String deviceId, String celltowerId, String latitude, String longitude,
                       String signalType, String signalValue) {
        this.device_id = deviceId;
        this.celltower_id = celltowerId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.signal_type = signalType;
        this.signal_value = signalValue;
    }

    // reading_id is read only
    public String getReadingId() {
        return this.reading_id;
    }

    // device_id is read / write
    public String getDeviceId() {
        return this.device_id;
    }
    public void setDeviceId(String deviceId) {
        this.device_id = deviceId;
    }

    // celltower_id is read / write
    public String getCelltowerId() {
        return this.celltower_id;
    }
    public void setCelltowerId(String celltowerId) {
        this.celltower_id = celltowerId;
    }

    // latitude is read / write
    public String getLatitude() {
        return this.latitude;
    }
    public void setLatitude(String latitude) { this.latitude = latitude; }

    // longitude is read / write
    public String getLongitude() {
        return this.longitude;
    }
    public void setLongitude(String longitude) { this.longitude = longitude; }

    // signal_type is read / write
    public String getSignalType() {
        return this.signal_type;
    }
    public void setSignalType(String signalType) { this.signal_type = signalType; }

    // signal_value is read / write
    public String getSignalValue() {
        return this.signal_value;
    }
    public void setSignalValue(String signalValue) { this.signal_value = signalValue; }

    // Update timestamp is read only
    public String getTimestamp() {
        return timestamp;
    }

}

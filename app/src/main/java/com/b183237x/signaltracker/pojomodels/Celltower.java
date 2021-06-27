package com.b183237x.signaltracker.pojomodels;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

public class Celltower {

    @SerializedName("celltower_id")
    private String celltower_id;

    @SerializedName("celltower_name")
    private String celltower_name;

    @SerializedName("location_area_code")
    private String location_area_code;

    @SerializedName("mobile_country_code")
    private String mobile_country_code;

    @SerializedName("mobile_network_code")
    private String mobile_network_code;

    @SerializedName("latitude")
    private String latitude;

    @SerializedName("longitude")
    private String longitude;

    @SerializedName("timestamp")
    private String timestamp;


    public Celltower(String celltowerName, String locationAreaCode, String mobileCountryCode,
                     String mobileNetworkCode, String latitude, String longitude) {
        this.celltower_name = celltowerName;
        this.location_area_code = locationAreaCode;
        this.mobile_country_code = mobileCountryCode;
        this.mobile_network_code = mobileNetworkCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // celltower_id is read only
    public String getCelltowerId() {
        return this.celltower_id;
    }

    // celltower_name is read / write
    public String getCelltowerName() {
        return this.celltower_name;
    }
    public void setCelltowerName(String celltowerName) {
        this.celltower_name = celltowerName;
    }

    // location_area_code is read / write
    public String getLocationAreaCode() {
        return this.location_area_code;
    }
    public void setLocationAreaCode(String locationAreaCode) { this.location_area_code = locationAreaCode; }

    // mobile_country_code is read / write
    public String getMobileCountryCode() { return this.mobile_country_code; }
    public void setMobileCountryCode(String mobileCountryCode) { this.mobile_country_code = mobileCountryCode; }

    // mobile_network_code is read / write
    public String getMobileNetworkCode() { return this.mobile_network_code; }
    public void setMobileNetworkCode(String mobileNetworkCode) { this.mobile_network_code = mobileNetworkCode; }

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

    // Update timestamp is read only
    public String getTimestamp() {
        return timestamp;
    }

}

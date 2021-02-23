package com.b183237x.signaltracker;

import android.content.Context;
import android.content.SharedPreferences;

public enum ServiceState {
    STARTED,
    STOPPED;

    private static final String itemKey = "STATE";
    private static final String containerName = "TRACKERSERVICE";

    public static void setServiceState(Context context, ServiceState state) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(containerName, 0);
        sharedPrefs.edit()
                .putString(itemKey, state.toString())
                .apply();
    }

    public static ServiceState getServiceState(Context context) {
        SharedPreferences sharedPrefs = context.getSharedPreferences(containerName, 0);
        String value = sharedPrefs.getString(itemKey, String.valueOf(ServiceState.STOPPED));
        return ServiceState.valueOf(value);
    }


}
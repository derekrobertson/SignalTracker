package com.b183237x.signaltracker;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.*;
import android.util.Log;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import android.os.Bundle;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Criteria;
import android.telephony.TelephonyManager;
import android.widget.Toast;


public class TrackerService extends Service {

    private static final String PACKAGE_NAME = "com.b183237x.signaltracker";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";
    private static final String TAG = TrackerService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 12345678;
    private NotificationManager notificationManager;
    public static final String CHANNEL_ID = "SignalTrackerServiceChannel";
    public static final String CHANNEL_NAME = "Signal Tracker Service Channel";
    private PowerManager.WakeLock wakeLock = null;
    private boolean isServiceStarted = false;

    private LocationListener locationListener;
    private LocationManager locationManager;
    private static Location lastLocation = null;

    private SignalTrackerDatabaseHelper signalTrackerDatabaseHelper;
    private SQLiteDatabase db;


    private final IBinder binder = new TrackerServiceBinder();
    public class TrackerServiceBinder extends Binder {
        TrackerService getTrackerService() {
            return TrackerService.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        // Get a handle to the SQLite db
        signalTrackerDatabaseHelper = new SignalTrackerDatabaseHelper(this);
        try {
            db = signalTrackerDatabaseHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            Toast toast = Toast.makeText(this, "Unable to access database",
                    Toast.LENGTH_SHORT);
            toast.show();
            return;
        }


        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Create a notification channel for the foreground service (only if Android Oreo+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            notificationManager.createNotificationChannel(serviceChannel);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        return START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // Called when MainActivity comes to foreground and binds with the service.
        // We remove the Foreground service when that happens.
        Log.i(TAG, "reached onBind()");
        stopForeground(true);
        return binder;
    }


    @Override
    public void onRebind(Intent intent) {
        // Called when MainActivity returns to the foreground and binds to the service again.
        // We remove the Foreground service when that happens.
        Log.i(TAG, "reached onRebind()");
        stopForeground(true);
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // Called when MainActivity unbinds from the service.
        // We now need to promote the service to a Foreground service so it keeps running.
        Log.i(TAG, "reached onUnbind()");
        startForeground(NOTIFICATION_ID, getNotification());
        return true; // Ensures onRebind() is called when a client re-binds.
    }


    public void enableSignalUpdates() {
        Log.i(TAG, "Enabling signal updates");
        startService(new Intent(getApplicationContext(), TrackerService.class));

        // Create a new Location listener for GPS location updates
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                lastLocation = location;
                // When the location changes, we want to record the signal strength
                recordSignalStrength();
            }

            @Override
            public void onProviderDisabled(String arg0) {}

            @Override
            public void onProviderEnabled(String arg) {}

            @Override
            public void onStatusChanged(String arg0, int arg1, Bundle bundle) {}
        };

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(new Criteria(), true);
        if (provider != null) {
            try {
                locationManager.requestLocationUpdates(provider, 30000, 5,
                        locationListener);
            } catch (SecurityException e) {
                // permission not enabled!
                Toast toast = Toast.makeText(this, "Unable to access location",
                        Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        }

        enableWakeLock();
        isServiceStarted = true;
        ServiceState.setServiceState(this, ServiceState.STARTED);
    }


    public void disableSignalUpdates() {
        Log.i(TAG, "Disabling signal updates");
        locationManager.removeUpdates(locationListener);
        disableWakeLock();
        stopSelf();
        isServiceStarted = false;
        ServiceState.setServiceState(this, ServiceState.STOPPED);
    }



    private Notification getNotification() {

        Intent intent = new Intent(this, TrackerService.class);

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // The PendingIntent to launch activity
        PendingIntent activityPendingIntent =
                PendingIntent.getActivity(this, 0,
                        new Intent(this, MainActivity.class), 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(activityPendingIntent)
                .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }

        return builder.build();
    }


    @Override
    public void onDestroy() {
        // Destroy the location listener
        locationListener = null;
        locationManager = null;
        // Close down database handle
        db.close();
        db = null;
        // If the app is just killed, ensure we record that the service is stopped
        ServiceState.setServiceState(this, ServiceState.STOPPED);

        super.onDestroy();
    }


    private void recordSignalStrength() {
        // Function uses TelephonyManager to get the current network type (2g, 3g, 4g) and the
        // relative signal strength to the Cell tower that we are currently connected to.
        // NOTE: Can only ever be connected to a single cell tower and single gen of network
        // Once value is retrieved it is saved to the local database

        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
        if (cellInfoList != null) {
            for (CellInfo cellInfo: cellInfoList) {
                if (cellInfo.isRegistered()) {
                    if (cellInfo instanceof CellInfoGsm) {
                        CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                        CellSignalStrengthGsm cellSignalStrengthGsm = cellInfoGsm.getCellSignalStrength();
                        saveToDatabase("2G", cellSignalStrengthGsm.getAsuLevel());
                    }
                    if (cellInfo instanceof CellInfoWcdma) {
                        CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                        CellSignalStrengthWcdma cellSignalStrengthWcdma = cellInfoWcdma.getCellSignalStrength();
                        saveToDatabase("3G", cellSignalStrengthWcdma.getAsuLevel());
                    }
                    if (cellInfo instanceof CellInfoLte) {
                        CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                        CellSignalStrengthLte cellSignalStrengthLte = cellInfoLte.getCellSignalStrength();
                        saveToDatabase("4G", cellSignalStrengthLte.getAsuLevel());
                    }
                }
            }
        }
    }


    private void saveToDatabase(String signalType, Integer signalValue) {
        // Saves an entry to the READINGS database table and logs it
        if (lastLocation != null) {
            Double latitude =  lastLocation.getLatitude();
            Double longitude = lastLocation.getLongitude();
            ContentValues readingsValues = new ContentValues();
            readingsValues.put("latitude", latitude);
            readingsValues.put("longitude", longitude);
            readingsValues.put("signal_type", signalType);
            readingsValues.put("signal_value", signalValue);
            readingsValues.put("uploaded", 0);
            Log.i(TAG, "Saving reading to database:");
            Log.i(TAG, "Latitude=" + String.valueOf(latitude) +
                                ", Longitude=" + String.valueOf(longitude) +
                                ", Signal type=" + signalType +
                                ", Signal value=" + String.valueOf(signalValue));
            db.insert("READINGS", null, readingsValues);
        }
    }


    private void enableWakeLock() {
        // Get a wakelock to prevent interference from Android Doze mode
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "TrackerService::lock");
        wakeLock.acquire();
    }


    private void disableWakeLock() {
        // Destroy the wakeLock if its in use
        try {
            if (wakeLock != null) {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not release wake lock: " + e.getMessage().toString());
        }
    }


    // Returns true if the service is running as a foreground service
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }

}

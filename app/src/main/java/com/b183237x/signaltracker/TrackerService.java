package com.b183237x.signaltracker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import java.util.Calendar;
import android.os.Bundle;
import android.location.LocationListener;
import android.location.Location;
import android.location.LocationManager;
import android.location.Criteria;



public class TrackerService extends Service {

    public static final String CHANNEL_ID = "SignalTrackerServiceChannel";
    public static final String CHANNEL_NAME = "Signal Tracker Service Channel";
    private PowerManager.WakeLock wakeLock = null;
    private boolean isServiceStarted = false;
    private LocationListener listener;
    private LocationManager locManager;
    private static Location lastLocation = null;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // We don't need to bind to a Foreground service but we do need to override this method
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        // Create a new Location listener for GPS location updates
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                lastLocation = location;
                Log.i("Latitude", Double.toString(lastLocation.getLatitude()));
                Log.i("Longitude", Double.toString(lastLocation.getLongitude()));
            }

            @Override
            public void onProviderDisabled(String arg0) {}

            @Override
            public void onProviderEnabled(String arg) {}

            @Override
            public void onStatusChanged(String arg0, int arg1, Bundle bundle) {}
        };

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = locManager.getBestProvider(new Criteria(), true);
        if (provider != null) {
            try {
                locManager.requestLocationUpdates(provider, 10000, 1, listener);
            }
            catch (SecurityException e) {
                // permission not enabled!

            }
        }

        Notification notification = createNotification();
        startForeground(1, notification);
    }


    private Notification createNotification() {
        // Create a notification channel for the foreground service (only if Android Oreo+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);


        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        return notification;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        // Destroy the location listener
        if (locManager != null && listener != null) {
                locManager.removeUpdates(listener);
        }
        locManager = null;
        listener = null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();
            if (action.equals(Actions.START.name())) { startService(); }
            if (action.equals(Actions.STOP.name())) { stopService(); }
        }
        return START_STICKY;
    }

    private void startService() {
        if (isServiceStarted) {
            return;
        }

        isServiceStarted = true;
        ServiceState.setServiceState(this, ServiceState.STARTED);

        // Get a wakelock to prevent interference from Android Doze mode
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "TrackerService::lock");
        wakeLock.acquire();

        // Register a handler for every 5 minutes, then do an initial call to the runner
        // This will then place another call to same runner for next interval (perpetual loop)
        Handler handler = new Handler();
        final Runnable runner = new Runnable() {
            @Override
            public void run() {
                Log.i("Logger", "Logged at:" + Calendar.getInstance().getTime());

                handler.postDelayed(this, 2000);
            }
        };
        handler.postDelayed(runner, 2000);
    }


    private void stopService() {
        // Destroy the wakeLock if its in use
        try {
            if (wakeLock != null) {
                if (wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }
            stopForeground(true);
            stopSelf();
        } catch (Exception e) {
            Log.e("Logger", "Service stopped without being started: " + e.getMessage().toString());
        }
        isServiceStarted = false;
        ServiceState.setServiceState(this, ServiceState.STOPPED);
    }


}

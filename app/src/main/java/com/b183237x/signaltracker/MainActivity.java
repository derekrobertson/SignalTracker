package com.b183237x.signaltracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Build;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.pm.PackageManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    // Permissions definitions
    private int REQUEST_CODE_FINE_LOCATION = 101;
    private int REQUEST_CODE_BACKGROUND_LOCATION = 102;
    private Boolean permissionFineLocation = false;
    private Boolean permissionBackgroundLocation = false;

    // A reference to the service used to get location updates.
    private TrackerService trackerService = null;

    Button btnCheckPermissions;
    Button btnStartService;
    Button btnStopService;
    TextView tvServiceState;

    private boolean bound = false;



    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            TrackerService.TrackerServiceBinder trackerServiceBinder =
                    (TrackerService.TrackerServiceBinder) binder;
            trackerService = trackerServiceBinder.getTrackerService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            trackerService = null;
            bound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    protected void onStart() {
        super.onStart();

        ////////////////// FOR TESTING API - REMOVE WHEN DONE ////////////////
        /*
        RestApiInterface apiService = RestApiClient.getClient(this, "boss@boss.com", "RAN01gers")
                .create(RestApiInterface.class);
        Call<User> call = apiService.getUserByEmail("boss@boss.com");
        call.enqueue(new Callback<User>() {
             @Override
             public void onResponse(Call<User> call, Response<User> response) {
                 if (response.isSuccessful()) {
                     User user = response.body();
                     Log.d("SignalTracker", "Response = " + user.getUserId());
                 } else {
                     Log.d("SignalTracker", response.errorBody().toString());
                 }
             }

             @Override
             public void onFailure(Call<User> call, Throwable t) {
                 t.printStackTrace();
             }
         });

         */
        ////////////////// FOR TESTING API - REMOVE WHEN DONE ////////////////


        btnCheckPermissions = findViewById(R.id.buttonCheckPermissions);
        btnStartService = findViewById(R.id.buttonStartService);
        btnStopService = findViewById(R.id.buttonStopService);
        tvServiceState = findViewById(R.id.tvServiceState);

        btnCheckPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for correct permissions
                checkLocationBackgroundPermission();
            }
        });


        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the TrackerService
                trackerService.enableSignalUpdates();
                btnStartService.setEnabled(false);
                btnStopService.setEnabled(true);
                tvServiceState.setText(String.valueOf(ServiceState.getServiceState(getApplicationContext())));
            }
        });


        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop the TrackerService
                trackerService.disableSignalUpdates();
                btnStartService.setEnabled(true);
                btnStopService.setEnabled(false);
                tvServiceState.setText(String.valueOf(ServiceState.getServiceState(getApplicationContext())));
            }
        });

        checkLocationFinePermission();

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, TrackerService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);


        // TODO: FIX THIS AS METHOD COULD FINISH B4 BIND IS UPDATED
        if (ServiceState.getServiceState(this) == ServiceState.STARTED) {
            btnStartService.setEnabled(false);
            btnStopService.setEnabled(true);
        }
        if (ServiceState.getServiceState(this) == ServiceState.STOPPED) {
            btnStartService.setEnabled(true);
            btnStopService.setEnabled(false);
        }
    }


    @Override
    protected void onStop() {
        // Unbind from the service. This signals to the service that this activity is no longer
        // in the foreground, and the service can respond by promoting itself to a foreground
        // service.
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }
        super.onStop();
    }



    protected void checkLocationFinePermission() {

        // Check if we have the necessary permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            permissionFineLocation = true;
        }

        // NOTE:  For location, Foreground permissions must have already been allowed before
        // background permissions can be requested (API 30+)
        if (!permissionFineLocation) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    REQUEST_CODE_FINE_LOCATION);
        }
    }

    protected void checkLocationBackgroundPermission() {

        // Check if we have the necessary permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            permissionBackgroundLocation = true;
            btnStartService.setEnabled(true);
        }

        // NOTE:  For location, Foreground permissions must have already been allowed before
        // background permissions can be requested (API 30+)
        if (!permissionBackgroundLocation) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    REQUEST_CODE_BACKGROUND_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                            String[] permissions, int[] grantResults) {

        if (requestCode == REQUEST_CODE_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionFineLocation = true;
            } else {
                quitApp();
            }
        }

        if (requestCode == REQUEST_CODE_BACKGROUND_LOCATION) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                permissionBackgroundLocation = true;
            } else {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionBackgroundLocation = true;
                } else {
                    Toast.makeText(this,
                            "Signal Tracker service cannot start without background location permissions",
                            Toast.LENGTH_LONG).show();
                }
            }
        }

        if (permissionFineLocation && permissionBackgroundLocation) {
            // We have all required permissions so enable the start service button
            btnStartService.setEnabled(true);
        }

    }


    private void quitApp() {
        Toast.makeText(this, "Location permissions were refused, Signal Tracker was closed",
                Toast.LENGTH_LONG).show();
        MainActivity.this.finish();
    }

}
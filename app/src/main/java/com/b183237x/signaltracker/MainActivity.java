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

import com.b183237x.signaltracker.pojomodels.Device;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;

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

    TextView tvErrorMsg;
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

        // Get refs to the form fields and buttons
        tvErrorMsg = findViewById(R.id.tvErrorMsg);
        btnCheckPermissions = findViewById(R.id.btnCheckPermissions);
        btnStartService = findViewById(R.id.btnStartService);
        btnStopService = findViewById(R.id.btnStopService);
        tvServiceState = findViewById(R.id.tvServiceState);

        // Generate a unique app instance ID and save it in the shared creds if not already there
        // This allows us to track the device that the app is running on if a user uses the app
        // on multiple devices
        Context context = getApplicationContext();
        String uniqueId = "";
        uniqueId = SharedPrefsUtil.getPrefVal(this, "uuid");

        // If the uniqueid already exists in our encrypted sharedprefs, we will use it, otherwise
        // we will call the REST API to create this device
        if (!uniqueId.equals("")) {
            AppProperties.getInstance().setUuid(uniqueId);
        } else {
            uniqueId = UUID.randomUUID().toString();
            AppProperties.getInstance().setUuid(uniqueId);
            SharedPrefsUtil.setPrefVal(this, "uuid", uniqueId);
        }

        // Now call REST API to create this device if not already present
        RestApiInterface apiService = RestApiClient.getAuthClient(getApplicationContext(),
                        AppProperties.getInstance().getEmail(), AppProperties.getInstance().getPassword())
                .create(RestApiInterface.class);
        Call<Device> call = apiService.createDevice(new Device(AppProperties.getInstance().getUserId(),
                Build.MANUFACTURER,
                Build.MODEL,
                uniqueId,
                String.valueOf(Build.VERSION.SDK_INT)
        ));


        call.enqueue(new Callback<Device>() {
            @Override
            public void onResponse(Call<Device> call, Response<Device> response) {
                if (response.isSuccessful()) {
                    Device device = response.body();
                    SharedPrefsUtil.setPrefVal(context, "device_id", device.getDeviceId());
                    AppProperties.getInstance().setDeviceId(device.getDeviceId());
                } else {
                    displayError("Unable to register this device");
                    Log.d("SignalTracker", response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<Device> call, Throwable t) {
                displayError("Error making call to remote API");
                t.printStackTrace();
                finish();
            }
        });


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


    // Display an error for the user
    private void displayError(String message) {
        if (tvErrorMsg.getVisibility() == View.INVISIBLE) {
            tvErrorMsg.setVisibility(View.VISIBLE);
        }
        tvErrorMsg.setText(message);
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
package com.b183237x.signaltracker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Build;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.pm.PackageManager;
import android.widget.Toast;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    // Permissions defintions
    private int REQUEST_CODE_FINE_LOCATION = 101;
    private int REQUEST_CODE_BACKGROUND_LOCATION = 102;
    private Boolean permissionFineLocation = false;
    private Boolean permissionBackgroundLocation = false;

    Button btnCheckPermissions;
    Button btnStartService;
    Button btnStopService;


    @Override
    protected void onStart() {
        super.onStart();
        checkLocationFinePermission();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCheckPermissions = findViewById(R.id.buttonCheckPermissions);
        btnStartService = findViewById(R.id.buttonStartService);
        btnStartService.setEnabled(false);
        btnStopService = findViewById(R.id.buttonStopService);


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
                actionOnService(Actions.START);

            }
        });


        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop the TrackerService
                actionOnService(Actions.STOP);
            }
        });
    }


    private void actionOnService(Actions action) {
        if (ServiceState.getServiceState(this) == ServiceState.STOPPED && action == Actions.STOP) {
            return;
        }
        Intent serviceIntent = new Intent(this, TrackerService.class);
        serviceIntent.setAction(action.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
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
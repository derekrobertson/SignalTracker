package com.b183237x.signaltracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    // Call onCreateAccountClicked() when the button is clicked
    public void onCreateAccountClicked(View view) {

    }

    public void onBackToLoginClicked(View view) {
        // Finish this activity and go back to login screen
        finish();
    }
}
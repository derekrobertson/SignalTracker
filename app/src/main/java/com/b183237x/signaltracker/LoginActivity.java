package com.b183237x.signaltracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.b183237x.signaltracker.pojomodels.User;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    TextView tvErrorMsg;
    TextView tvEmail;
    TextView tvPassword;
    Button btnLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Grab refs to the login form items
        tvErrorMsg = findViewById(R.id.ErrorMessage);
        tvEmail = findViewById(R.id.Email);
        tvPassword = findViewById(R.id.Password);
        btnLogin = findViewById(R.id.btnLogin);

        // Check if we have the previous email used to login saved in SharedPrefs
        // If so, pre-populate the email field
        String email = SharedPrefsUtil.getPrefVal(getApplicationContext(), "email");
        if (!email.equals("")) {
            tvEmail.setText(email);
        }

    }


    // Display an error for the user
    private void displayError(String message) {
        if (tvErrorMsg.getVisibility() == View.INVISIBLE) {
            tvErrorMsg.setVisibility(View.VISIBLE);
        }
        tvErrorMsg.setText(message);
    }


    // Call onLoginClicked() when the login button is clicked
    public void onLoginClicked(View view) {
        String email = tvEmail.getText().toString();
        String password = tvPassword.getText().toString();
        btnLogin.setEnabled(false);

        // We will call the REST API to find matching user authenticating with the
        // email and password the user provided
        // If these are correct, they are saved into the encrypted shared prefs for use later
        RestApiInterface apiService = RestApiClient.getAuthClient(this, email, password )
                .create(RestApiInterface.class);
        Call<User> call = apiService.getUserByEmail(email);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();

                    // Save the user's creds to our encrypted shared prefs
                    SharedPrefsUtil.setPrefVal(getApplicationContext(), "user_id", user.getUserId());
                    SharedPrefsUtil.setPrefVal(getApplicationContext(), "email", email);
                    SharedPrefsUtil.setPrefVal(getApplicationContext(), "password", password);

                    // And populate the AppProperties object for quick retrieval
                    AppProperties.getInstance().setUserId(user.getUserId());
                    AppProperties.getInstance().setEmail(email);
                    AppProperties.getInstance().setPassword(password);

                    // Load the main application activity
                    Toast.makeText(getApplicationContext(), "User logged in",
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                } else {
                    btnLogin.setEnabled(true);
                    displayError("Error during login");
                    Log.d("SignalTracker", response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                btnLogin.setEnabled(true);
                displayError("Error making call to remote API");
                Log.d("SignalTracker", "Error making call to /users remote API");
                t.printStackTrace();
            }
        });
    }

    // Call onRegisterClicked() when the register button is clicked
    public void onRegisterClicked(View view) {
        // Load the registration Activity
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }












}
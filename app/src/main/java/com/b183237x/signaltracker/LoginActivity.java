package com.b183237x.signaltracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.b183237x.signaltracker.pojomodels.User;

import java.io.IOException;
import java.security.GeneralSecurityException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    TextView tvErrorMsg;
    TextView tvEmail;
    TextView tvPassword;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    protected void onStart() {
        super.onStart();

        tvErrorMsg = findViewById(R.id.ErrorMessage);
        tvEmail = findViewById(R.id.Email);
        tvPassword = findViewById(R.id.Password);


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
        String username = tvEmail.getText().toString();
        String password = tvPassword.getText().toString();

        RestApiInterface apiService = RestApiClient.getAuthClient(this, username, password )
                .create(RestApiInterface.class);
        Call<User> call = apiService.getUserByEmail(username);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();

                    // Save the user's creds to our encrypted shared prefs
                    try {
                        SharedPrefsUtil.setPrefVal(getApplicationContext(), "id", user.getUserId());
                        SharedPrefsUtil.setPrefVal(getApplicationContext(), "email", username);
                        SharedPrefsUtil.setPrefVal(getApplicationContext(), "password", password);
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(getApplicationContext(), "User logged in",
                            Toast.LENGTH_SHORT).show();
                    // Load the main application activity
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                } else {
                    displayError("Error during login");
                    Log.d("SignalTracker", response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
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
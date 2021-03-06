package com.b183237x.signaltracker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.b183237x.signaltracker.pojomodels.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    TextView tvErrorMsg;
    TextView tvFirstName;
    TextView tvLastName;
    TextView tvEmail;
    TextView tvPassword;
    TextView tvPasswordConfirm;
    Button btnCreateAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Grab refs to the form fields
        tvErrorMsg = findViewById((R.id.ErrorMessage));
        tvFirstName = findViewById(R.id.FirstName);
        tvLastName = findViewById(R.id.LastName);
        tvEmail = findViewById(R.id.Email);
        tvPassword = findViewById(R.id.Password);
        tvPasswordConfirm = findViewById(R.id.PasswordConfirm);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
    }

    // Call onCreateAccountClicked() when the button is clicked
    public void onCreateAccountClicked(View view) {
        String firstName = tvFirstName.getText().toString();
        String lastName = tvLastName.getText().toString();
        String email = tvEmail.getText().toString();
        String password = tvPassword.getText().toString();
        String passwordConfirm = tvPasswordConfirm.getText().toString();


        // Validation of form fields
        if (firstName.length() < 2) {
            displayError("First name must be at least 2 characters");
            return;
        }
        if (lastName.length() < 2) {
            displayError("Last name must be at least 2 characters");
            return;
        }

        if (!email.matches("^(.+)@(.+)$")) {
            displayError("Must enter a properly formed email address");
            return;
        }

        if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,12}$")) {
            displayError("Password must contain a number, have upper and lower case characters and be 8 to 12 characters long");
            return;
        } else {
            if (!passwordConfirm.equals(password)) {
                displayError("Password confirmation does not match password");
                return;
            }
        }

        // If we get here, form is validated
        tvErrorMsg.setVisibility(View.INVISIBLE);
        createAccount(firstName, lastName, email, password);
    }


    // Display an error for the user
    private void displayError(String message) {
        if (tvErrorMsg.getVisibility() == View.INVISIBLE) {
            tvErrorMsg.setVisibility(View.VISIBLE);
        }
        tvErrorMsg.setText(message);
    }


    private void createAccount(String firstName, String lastName, String email, String password) {

        btnCreateAccount.setEnabled(false);
        // We create a REST API client, using the API key only, which is allowed to register
        // new accounts.
        RestApiInterface apiService = RestApiClient.getApiKeyClient(this)
                .create(RestApiInterface.class);
        Call<User> call = apiService.createUser(new User(firstName, lastName, email, password, "USER"));
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    // Close this activity and fallback to LoginActivity
                    Toast.makeText(getApplicationContext(), "User account created, now login",
                            Toast.LENGTH_LONG).show();
                    Log.d("SignalTracker", "New user account successfully registered");
                    finish();
                } else {
                    if (response.code() == 409) {
                        // User already exists
                        Toast.makeText(getApplicationContext(), "User account already exists",
                                Toast.LENGTH_LONG).show();
                        Log.d("SignalTracker", "User account for this email already exists");
                    } else {
                        Toast.makeText(getApplicationContext(), "Could not create user account",
                                Toast.LENGTH_LONG).show();
                        Log.d("SignalTracker", "Failure when registering new user account");
                    }
                    btnCreateAccount.setEnabled(true);
                    Log.d("SignalTracker", response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                btnCreateAccount.setEnabled(true);
                displayError("Error making call to remote API");
                Log.d("SignalTracker", "Error making call to /users remote API");
                t.printStackTrace();
            }
        });

    }

    public void onBackToLoginClicked(View view) {
        // Finish this activity and go back to login screen
        finish();
    }






}
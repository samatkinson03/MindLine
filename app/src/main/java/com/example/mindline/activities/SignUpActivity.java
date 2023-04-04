package com.example.mindline.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mindline.R;
import com.example.mindline.activities.TimeLineActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private FirebaseAuth mAuth;

    private TextInputEditText mEmailEditText;
    private TextInputEditText mPasswordEditText;
    private TextInputEditText mConfirmPasswordEditText;
    private DatePicker mDatePicker;
    private Button mSignUpButton;

    private Handler emailVerificationHandler;
    private Runnable emailVerificationRunnable;

    private static final String PREFS_NAME = "user_data";
    private static final String DOB_KEY = "date_of_birth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        mEmailEditText = findViewById(R.id.email_edit_text);
        mPasswordEditText = findViewById(R.id.password_edit_text);
        mConfirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        mDatePicker = findViewById(R.id.date_picker);
        mSignUpButton = findViewById(R.id.sign_up_button);
        mSignUpButton.setOnClickListener(view -> {
            String email = mEmailEditText.getText().toString();
            String password = mPasswordEditText.getText().toString();
            String confirmPassword = mConfirmPasswordEditText.getText().toString();
            String dateOfBirth = getDateOfBirthFromDatePicker(mDatePicker);

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidPassword(password)) {
                Toast.makeText(this, "Please enter a valid password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isDateValid(dateOfBirth)) {
                signUp(email, password, confirmPassword, dateOfBirth);
            } else {
                Toast.makeText(SignUpActivity.this, "Please enter a valid date of birth in the format dd/MM/yyyy.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    private boolean isDateValid(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateFormat.setLenient(false);

        try {
            Date parsedDate = dateFormat.parse(date);
            return parsedDate != null;
        } catch (ParseException e) {
            return false;
        }
    }

    private String getDateOfBirthFromDatePicker(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1; // month is 0 -month based in DatePicker
        int year = datePicker.getYear();
        return String.format(Locale.getDefault(), "%02d/%02d/%d", day, month, year);
    }

    private void signUp(String email, String password, String confirmPassword, String dateOfBirth) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isEmailValid(email)) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign up success, automatically sign in and redirect to MainActivity
                        FirebaseUser user = mAuth.getCurrentUser();
                        sendVerificationEmail(user);
                        saveDateOfBirth(dateOfBirth);
                        startApp();
                    } else {
                        // If sign up fails, display a message to the user.
                        Toast.makeText(SignUpActivity.this, "Failed to create account", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    }
                });
    }

    private void saveDateOfBirth(String dateOfBirth) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(DOB_KEY, getDateInMillis(dateOfBirth));
        editor.apply();
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void sendVerificationEmail(FirebaseUser user) {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private long getDateInMillis(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateFormat.setLenient(false);

        try {
            Date parsedDate = dateFormat.parse(date);
            return parsedDate.getTime();
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date", e);
            return -1;
        }
    }


    private void startApp() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.isEmailVerified()) {
            if (emailVerificationHandler != null && emailVerificationRunnable != null) {
                emailVerificationHandler.removeCallbacks(emailVerificationRunnable);
            }
            Intent intent = new Intent(this, TimeLineActivity.class);
            startActivity(intent);
            finish();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please verify your email address before continuing.")
                    .setPositiveButton("Resend email", (dialog, id) -> {
                        // Resend verification email
                        sendVerificationEmail(user);
                    })
                    .setNegativeButton("Close", (dialog, id) -> {
                        // Close the dialog
                        if (emailVerificationHandler != null && emailVerificationRunnable != null) {
                            emailVerificationHandler.removeCallbacks(emailVerificationRunnable);
                        }
                        mAuth.signOut();
                    });
            builder.create().show();

            emailVerificationHandler = new Handler(Looper.getMainLooper());
            emailVerificationRunnable = new Runnable() {
                @Override
                public void run() {
                    user.reload().addOnCompleteListener(task -> {
                        if (user.isEmailVerified()) {
                            startApp();
                        } else {
                            // Check again in 5 seconds
                            emailVerificationHandler.postDelayed(this, 5000);
                        }
                    });
                }
            };
            // Start checking for email verification
            emailVerificationHandler.post(emailVerificationRunnable);
        }
    }


}
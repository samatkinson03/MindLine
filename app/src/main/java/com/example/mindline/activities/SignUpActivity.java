package com.example.mindline.activities;

import static android.service.controls.ControlsProviderService.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mindline.R;
import com.example.mindline.activities.TimeLineActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "user_data";
    private static final String DOB_KEY = "date_of_birth";

    private FirebaseAuth mAuth;
    private DatePicker mDatePicker;

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton mGoogleSignInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
        mDatePicker = findViewById(R.id.date_picker);
        mGoogleSignInButton = findViewById(R.id.google_sign_in_button);
        mGoogleSignInButton.setOnClickListener(view -> signIn());
    }

    private void signIn() {
        String dateOfBirthString = getDateOfBirthFromDatePicker(mDatePicker);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        dateFormat.setLenient(false);

        try {
            Date dateOfBirth = dateFormat.parse(dateOfBirthString);
            if (isDateValid(dateOfBirth)) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            } else {
                Toast.makeText(SignUpActivity.this, "Please enter a valid date of birth.", Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            Toast.makeText(SignUpActivity.this, "Please enter a valid date of birth.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(SignUpActivity.this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        mAuth.signInWithCredential(GoogleAuthProvider.getCredential(acct.getIdToken(), null)).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success");
                FirebaseUser user = mAuth.getCurrentUser();
                String dateOfBirth = getDateOfBirthFromDatePicker(mDatePicker);
                saveDateOfBirth(dateOfBirth);
                startApp();
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithCredential:failure", task.getException());
                Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean isDateValid(Date dateOfBirth) {
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        // Check if the date is not in the future
        if (dateOfBirth.after(currentDate)) {
            return false;
        }

        // Check if the date is not more than 110 years old
        calendar.add(Calendar.YEAR, -110);
        Date oldestAllowedDate = calendar.getTime();
        if (dateOfBirth.before(oldestAllowedDate)) {
            return false;
        }

        return true;
    }


    private String getDateOfBirthFromDatePicker(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1; // month is 0 -month based in DatePicker
        int year = datePicker.getYear();
        return String.format(Locale.getDefault(), "%02d/%02d/%d", day, month, year);
    }

    private void saveDateOfBirth(String dateOfBirth) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(DOB_KEY, getDateInMillis(dateOfBirth));
        editor.apply();
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
        if (user != null) {
            Intent intent = new Intent(this, TimeLineActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(SignUpActivity.this, "Please sign in to continue.", Toast.LENGTH_SHORT).show();
        }
    }

}

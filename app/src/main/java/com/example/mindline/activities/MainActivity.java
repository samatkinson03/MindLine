package com.example.mindline.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mindline.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "user_data";
    private static final String DOB_KEY = "date_of_birth";

    private static final int REQUEST_CODE_SIGN_IN = 1001;

    private MaterialButton mSignInButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        mSignInButton = findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        // Check if the user is already signed in
        // If so, start the app
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long dateOfBirthInMillis = sharedPreferences.getLong(DOB_KEY, -1);
        if (dateOfBirthInMillis != -1) {
            startApp();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if the user is signed in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            startApp();
        }
    }

    private void startApp() {
        // Check if the user is signed in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(this, TimeLineActivity.class);
            startActivity(intent);
            finish();
        } else {
            // If the user is not signed in, redirect to the sign in page
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN && resultCode == RESULT_OK) {
            // Sign in successful, start the app
            startApp();
        }
    }
}

package com.example.mindline.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mindline.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class SignInActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "user_data";
    private static final String DOB_KEY = "date_of_birth";

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> signInActivityResultLauncher;
    private ActivityResultLauncher<Intent> pickDobActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        // Configure sign-in to request the user's ID, email address, and basic profile.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestServerAuthCode(getString(R.string.default_web_client_id), true) // Add "true" as the second parameter
                .requestScopes(new Scope("https://www.googleapis.com/auth/photoslibrary"))
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Register activity result launcher for Google Sign-In
        signInActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                            if (task.isSuccessful()) {
                                GoogleSignInAccount account = task.getResult();
                                if (account != null) {
                                    handleSignInResult(account);
                                }
                            } else {
                                Toast.makeText(this, "Google sign in failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        // Register activity result launcher for picking date of birth
        pickDobActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            long dateOfBirthInMillis = data.getLongExtra("date_of_birth", -1);
                            if (dateOfBirthInMillis != -1) {
                                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putLong(DOB_KEY, dateOfBirthInMillis);
                                editor.apply();
                            }
                            startMainActivity();
                        }
                    }
                });

        // Link the sign-in button to the sign-in process.
        com.google.android.gms.common.SignInButton signInButton = findViewById(R.id.google_sign_in_button);
        signInButton.setOnClickListener(view -> signIn());

        // Check for existing Google Sign-In account, if the user is already signed in.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // If the user was previously logged in, sign the user out from GoogleSignInClient
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                // Use the Photos Library API client library to obtain an access token.
                handleSignInResult(account);
            });
        } else {
            // No existing Google Sign-In account, initiate the sign-in process.
            signIn();
        }
    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInActivityResultLauncher.launch(signInIntent);
    }

    private void handleSignInResult(GoogleSignInAccount account) {
        // Proceed with Firebase authentication.
        firebaseAuthWithGoogle(account);

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                        long dateOfBirthInMillis = sharedPreferences.getLong(DOB_KEY, -1);

                        if (dateOfBirthInMillis != -1) {
                            startMainActivity();
                        } else {
                            requestDateOfBirth();
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("SignInActivity", "signInWithCredential:failure", task.getException());
                        Toast.makeText(SignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }




    private void requestDateOfBirth() {
        Intent intent = new Intent(SignInActivity.this, DatePickerActivity.class);
        pickDobActivityResultLauncher.launch(intent);
    }

    private void startMainActivity() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(this, TimeLineActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(SignInActivity.this, "Please sign in to continue.", Toast.LENGTH_SHORT).show();
        }
    }
}


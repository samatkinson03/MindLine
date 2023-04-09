package com.example.mindline.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mindline.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private static final int REQUEST_CODE_PICK_DOB = 9002;
    private static final String PREFS_NAME = "user_data";
    private static final String DOB_KEY = "date_of_birth";

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

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

        // Check for existing Google Sign-In account, if the user is already signed in.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // Use the Photos Library API client library to obtain an access token.
            handleSignInResult(account);
        } else {
            // No existing Google Sign-In account, initiate the sign-in process.
            signIn();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                GoogleSignInAccount account = task.getResult();
                if (account != null) {
                    handleSignInResult(account);
                }
            } else {
                Toast.makeText(this, "Google sign in failed.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_PICK_DOB && resultCode == RESULT_OK) {
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
        startActivityForResult(intent, REQUEST_CODE_PICK_DOB);
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

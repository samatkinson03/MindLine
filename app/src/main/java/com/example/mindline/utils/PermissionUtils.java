//package com.example.mindline.utils;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.os.Build;
//import com.google.photos.library.v1.GooglePhotosLibraryApi;
//
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentActivity;
//
//import com.example.mindline.R;
//import com.google.android.gms.auth.api.signin.GoogleSignIn;
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
//import com.squareup.okhttp.Callback;
//import com.squareup.okhttp.MediaType;
//import com.squareup.okhttp.OkHttpClient;
//import com.squareup.okhttp.Request;
//import com.squareup.okhttp.RequestBody;
//import com.squareup.okhttp.Response;
//
//import org.json.JSONObject;
//
//import java.net.ProtocolException;
//
//public class PermissionUtils {
//
//    public static void obtainAccessToken(Context context, String authCode, OnTokenReceivedListener listener) {
//        // Use the Google Photos Library API client library to obtain an access token.
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
//        if (account != null) {
//            GooglePhotosLibraryApi client = GooglePhotosLibraryApiFactory.create(context, account);
//            client.initialize(() -> {
//                client.authorize(new Callback<String>() {
//                    @Override
//                    public void onSuccess(String accessToken) {
//                        listener.onTokenReceived(accessToken);
//                    }
//
//                    @Override
//                    public void onFailure(Throwable t) {
//                        listener.onTokenReceived(null);
//                    }
//                });
//            });
//        } else {
//            listener.onTokenReceived(null);
//        }
//    }
//}
//
//    public static void saveAccessToken(Context context, String accessToken) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("access_token", accessToken);
//        editor.apply();
//    }
//
//    private static void saveRefreshToken(Context context, String refreshToken) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("refresh_token", refreshToken);
//        editor.apply(); // Add this line
//    }
//
//
//    public static void saveToSharedPreferences(Context context, String accessToken, String refreshToken, long dateOfBirthInMillis) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//
//        if (accessToken != null) {
//            editor.putString("access_token", accessToken);
//        }
//
//        if (refreshToken != null) {
//            editor.putString("refresh_token", refreshToken);
//        }
//
//        if (dateOfBirthInMillis > 0) {
//            editor.putLong("date_of_birth", dateOfBirthInMillis);
//        }
//
//        editor.apply();
//    }
//
//    public static boolean hasPermission(Context context, String permission) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
//        } else {
//            return true;
//        }
//    }
//
//    public static void requestPermission(android.app.Activity activity, String permission, int requestCode) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
//        }
//    }
//}
//
//
//

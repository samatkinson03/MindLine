package com.example.mindline.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.mindline.R;
import com.example.mindline.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        if ("logout".equals(key)) {
            // Perform logout action here
            FirebaseAuth.getInstance().signOut();
            // Navigate back to login screen or main activity
            Intent intent = new Intent(requireActivity(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
        }
        return super.onPreferenceTreeClick(preference);
    }

}


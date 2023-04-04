package com.example.mindline.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mindline.R;

import java.util.Calendar;

public class DateOfBirthActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "user_data";
    private static final String DOB_KEY = "date_of_birth";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_of_birth);

        final DatePicker datePicker = findViewById(R.id.datePicker);
        Button saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth();
                int year = datePicker.getYear();

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);
                long dateOfBirthInMillis = calendar.getTimeInMillis();

                // Save date of birth to SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(DOB_KEY, dateOfBirthInMillis);
                editor.apply();

                // Proceed to the main app
                startApp();
            }
        });
    }

    private void startApp() {
        // Start your main app activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

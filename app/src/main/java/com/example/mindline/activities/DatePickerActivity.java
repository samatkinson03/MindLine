package com.example.mindline.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.example.mindline.R;

import java.util.Calendar;

public class DatePickerActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_PICK_DOB = 1001;

    private static final String PREFS_NAME = "user_data";
    private static final String DOB_KEY = "date_of_birth";

    private DatePicker mDatePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_picker);

        mDatePicker = findViewById(R.id.date_picker);
        Button mConfirmButton = findViewById(R.id.confirm_button);
        mConfirmButton.setOnClickListener(view -> {
            int day = mDatePicker.getDayOfMonth();
            int month = mDatePicker.getMonth();
            int year = mDatePicker.getYear();
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);

            if (isValidDate(calendar)) {
                saveDateOfBirth(calendar.getTimeInMillis());
                startApp();
            } else {
                Toast.makeText(this, "Please enter a valid date of birth.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidDate(Calendar dateOfBirth) {
        Calendar currentDate = Calendar.getInstance();

        // Check if the date is not in the future
        if (dateOfBirth.after(currentDate)) {
            return false;
        }

        // Check if the date is not more than 110 years old
        currentDate.add(Calendar.YEAR, -110);
        if (dateOfBirth.before(currentDate)) {
            return false;
        }

        return true;
    }

    private void saveDateOfBirth(long dateOfBirthInMillis) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(DOB_KEY, dateOfBirthInMillis);
        editor.apply();
    }

    private void startApp() {
        Intent intent = new Intent(this, TimeLineActivity.class);
        startActivity(intent);
        finish();
    }
}

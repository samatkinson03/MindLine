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

    //Constants that specify the name of the shared preferences file and the key for the date of birth value
    private static final String PREFS_NAME = "user_data";
    private static final String DOB_KEY = "date_of_birth";
// private instance variable mDatePicker of type DatePicker
    private DatePicker mDatePicker;

    // The onCreate() method initializes the DatePicker, sets an onClickListener for the confirm button, and handles the saving of date of birth.
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

    //Method that checks the validity of the date the user is entering
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

    //Method that saves te entered date of birth to shared preferences
    private void saveDateOfBirth(long dateOfBirthInMillis) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(DOB_KEY, dateOfBirthInMillis);
        editor.apply();
    }

    //Method that starts the key activity of the application
    private void startApp() {
        Intent intent = new Intent(this, TimeLineActivity.class);
        startActivity(intent);
        finish();
    }
}

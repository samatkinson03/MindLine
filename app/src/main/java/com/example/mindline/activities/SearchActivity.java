package com.example.mindline.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mindline.R;
import com.example.mindline.fragments.SearchFragment;

// The SearchActivity displays the search results to the user
public class SearchActivity extends AppCompatActivity {

    // Called when the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Extract the search query from the intent
        String searchQuery = getIntent().getStringExtra("search_query");

        // Create a new SearchFragment and pass the search query to it
        if (savedInstanceState == null) {
            SearchFragment searchFragment = new SearchFragment();
            Bundle args = new Bundle();
            args.putString("search_query", searchQuery);
            searchFragment.setArguments(args);

            // Add the SearchFragment to the activity
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.search_fragment_container, searchFragment)
                    .commit();
        }
    }

    // Inflate the search menu layout and add it to the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }

    // Handle when the user presses a menu item
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // If the back button is pressed, log a message and call onBackPressed()
        if (item.getItemId() == R.id.action_back) {
            Log.d("SearchActivity", "Back button pressed");
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

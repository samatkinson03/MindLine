package com.example.mindline.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mindline.R;
import com.example.mindline.fragments.SearchFragment;

public class SearchActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        String searchQuery = getIntent().getStringExtra("search_query");


        if (savedInstanceState == null) {
            SearchFragment searchFragment = new SearchFragment();
            Bundle args = new Bundle();
            args.putString("search_query", searchQuery);
            searchFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.search_fragment_container, searchFragment)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_back) {
            Log.d("SearchActivity", "Back button pressed");
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

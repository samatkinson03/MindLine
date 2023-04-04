package com.example.mindline.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mindline.R;
import com.example.mindline.fragments.MemoryDetailFragment;

public class MemoryDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_detail);

        // Get memoryId from the Intent
        long memoryId = getIntent().getLongExtra("memory_id", -1);

        // Pass the memoryId to the MemoryDetailFragment
        Bundle args = new Bundle();
        args.putLong("memory_id", memoryId);

        // Load MemoryDetailFragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        MemoryDetailFragment memoryDetailFragment = new MemoryDetailFragment();
        memoryDetailFragment.setArguments(args);
        fragmentTransaction.replace(R.id.memory_detail_container, memoryDetailFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        Log.d("MemoryDetailActivity", "Back button pressed 2");
        super.onBackPressed();
    }
}

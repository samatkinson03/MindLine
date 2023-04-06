package com.example.mindline.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mindline.R;
import com.example.mindline.fragments.EditMemoryFragment;

public class EditMemoryActivity extends AppCompatActivity {




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_memory);
        // Get memoryId from the Intent
        long memoryId = getIntent().getLongExtra("memory_id", -1);

        // Pass the memoryId to the EditMemoryFragment
        Bundle args = new Bundle();
        args.putLong("memory_id", memoryId);

        // Load EditMemoryFragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        EditMemoryFragment editMemoryFragment = new EditMemoryFragment();
        editMemoryFragment.setArguments(args);
        fragmentTransaction.replace(R.id.edit_memory_container, editMemoryFragment);
        fragmentTransaction.commit();
    }


}

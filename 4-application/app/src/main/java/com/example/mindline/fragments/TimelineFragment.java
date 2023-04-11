package com.example.mindline.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindline.R;
import com.example.mindline.activities.MemoryDetailActivity;
import com.example.mindline.activities.TimeLineItemDecoration;
import com.example.mindline.adapters.TimelineAdapter;
import com.example.mindline.models.Memory;
import com.example.mindline.models.MemoryViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimelineFragment extends Fragment {
    private MemoryViewModel memoryViewModel;
    private TimelineAdapter timelineAdapter;
    private FloatingActionButton addMemoryButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        addMemoryButton = view.findViewById(R.id.add_memory_button);
        addMemoryButton.setOnClickListener(v -> {
            // Handle the click event here, navigate to the AddMemoryFragment
            NavHostFragment.findNavController(this).navigate(R.id.action_timelineFragment_to_addMemoryFragment);
        });


        RecyclerView recyclerView = view.findViewById(R.id.timeline_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setPadding(getResources().getDimensionPixelSize(R.dimen.timeline_padding), 0, getResources().getDimensionPixelSize(R.dimen.timeline_padding), 0);
        recyclerView.setClipToPadding(false);

        timelineAdapter = new TimelineAdapter(null, getBirthYear(), this::onMemoryItemClick);
        recyclerView.setAdapter(timelineAdapter);

        recyclerView.addItemDecoration(new TimeLineItemDecoration(getContext()));
        memoryViewModel = new ViewModelProvider(requireActivity()).get(MemoryViewModel.class);
        memoryViewModel.getAllMemories().observe(getViewLifecycleOwner(), this::setMemories);

        return view;
    }

    private void setMemories(List<Memory> memories) {
        int birthYear = getBirthYear();
        if (memories == null || memories.isEmpty()) {
            // if the memories list is null or empty, set an empty list to the adapter
            timelineAdapter.setMemoryList(new ArrayList<>(), birthYear);
        } else {
            timelineAdapter.setMemoryList(memories, birthYear);
        }
    }


    private int getBirthYear() {
        Calendar calendar = Calendar.getInstance();
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE);
        long dobInMillis = sharedPreferences.getLong("date_of_birth", -1);
        if (dobInMillis == -1) {
            return calendar.get(Calendar.YEAR);
        }
        calendar.setTimeInMillis(dobInMillis);
        return calendar.get(Calendar.YEAR);
    }


    private void onMemoryItemClick(long memoryId) {
        Intent intent = new Intent(requireActivity(), MemoryDetailActivity.class);
        intent.putExtra("memory_id", memoryId);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            addMemoryButton.setVisibility(View.GONE);
            Log.d("TimelineFragment", "User not signed in");
        } else {
            addMemoryButton.setVisibility(View.VISIBLE);
            Log.d("TimelineFragment", "User signed in: " + currentUser.getEmail());
        }
    }
}

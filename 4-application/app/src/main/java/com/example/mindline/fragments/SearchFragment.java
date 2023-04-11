package com.example.mindline.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindline.R;
import com.example.mindline.activities.MemoryDetailActivity;
import com.example.mindline.adapters.TimelineAdapter;
import com.example.mindline.models.Memory;
import com.example.mindline.models.MemoryViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView searchRecyclerView;
    private TimelineAdapter timelineAdapter;
    private List<Memory> memoryList;
    private MemoryViewModel memoryViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        String searchQuery = getArguments().getString("search_query");
        memoryViewModel = new ViewModelProvider(this).get(MemoryViewModel.class);
        searchRecyclerView = view.findViewById(R.id.search_recycler_view);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        memoryList = new ArrayList<>();
        timelineAdapter = new TimelineAdapter(memoryList, 0, (memoryId) -> {
            // Handle item click
            // Navigate to the MemoryDetailActivity
            Intent intent = new Intent(requireActivity(), MemoryDetailActivity.class);
            intent.putExtra("memory_id", memoryId);
            startActivity(intent);
        });
        searchRecyclerView.setAdapter(timelineAdapter);

        performSearch(searchQuery);

        return view;
    }

    private void performSearch(String query) {
        memoryViewModel.searchMemories(query).observe(getViewLifecycleOwner(), memories -> {
            memoryList.clear();
            memoryList.addAll(memories);
            timelineAdapter.setSearchResultList(memoryList);
        });
    }
}
package com.example.mindline.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindline.R;
import com.example.mindline.models.Memory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_YEAR = 0;
    private static final int VIEW_TYPE_MEMORY = 1;

    private List<Object> timelineItems;
    private OnMemoryClickListener onMemoryClickListener;

    public interface OnMemoryClickListener {
        void onMemoryClick(long memoryId);
    }

    public TimelineAdapter(List<Memory> memories, int birthYear, OnMemoryClickListener onMemoryClickListener) {
        this.timelineItems = new ArrayList<>();
        this.onMemoryClickListener = onMemoryClickListener;
        setMemoryList(memories, birthYear);
    }

    public void setMemoryList(List<Memory> memories, int birthYear) {
        timelineItems.clear();

        if (memories != null) {
            // Sort the memories list by their dates in descending order
            memories.sort(Comparator.comparing(Memory::getDate).reversed());
        }
        // Create a list of years from the current year to the birth year
        Calendar calendar = Calendar.getInstance();
        int currentYearInt = calendar.get(Calendar.YEAR);
        List<String> years = new ArrayList<>();
        for (int year = currentYearInt; year >= birthYear; year--) {
            years.add(Integer.toString(year));
        }

        // For each year, insert the year into the timeline
        for (String year : years) {
            timelineItems.add(year);

            // If there are memories for a particular year, insert the memories after the year into the timeline
            if (memories != null) {
                for (int i = 0; i < memories.size(); i++) {
                    Memory memory = memories.get(i);
                    String date = memory.getDate();
                    if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        Log.e("InvalidDateFormat", "Memory with invalid date format found: " + date);
                        continue;
                    }
                    String memoryYear = date.substring(0, 4);

                    if (memoryYear.equals(year)) {
                        timelineItems.add(memory);
                        memories.remove(i);
                        i--; // Decrement the index since we've removed an item from the list
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_YEAR) {
            View view = inflater.inflate(R.layout.timeline_year_item, parent, false);
            return new YearViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.timeline_memory_item, parent, false);
            return new MemoryViewHolder(view, onMemoryClickListener);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object timelineItem = timelineItems.get(position);
        if (holder instanceof YearViewHolder) {
            // Check if the timelineItem is a String (year) and bind it
            if (timelineItem instanceof String) {
                ((YearViewHolder) holder).bind((String) timelineItems.get(position));
            }
        } else if (holder instanceof MemoryViewHolder) {
            // Check if the timelineItem is a Memory and bind it
            if (timelineItem instanceof Memory) {
                ((MemoryViewHolder) holder).bind((Memory) timelineItems.get(position));
            }
        }
    }


    @Override
    public int getItemCount() {
        return timelineItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return timelineItems.get(position) instanceof String ? VIEW_TYPE_YEAR : VIEW_TYPE_MEMORY;
    }

    public List<Object> getTimelineItems() {
        return timelineItems;
    }


    public void setSearchResultList(List<Memory> searchResults) {
        timelineItems.clear();

        if (searchResults != null) {
            // Sort the search results by their dates in descending order
            searchResults.sort(Comparator.comparing(Memory::getDate).reversed());

            // Add the search results to the timelineItems list
            timelineItems.addAll(searchResults);
        }

        notifyDataSetChanged();
    }


    static class YearViewHolder extends RecyclerView.ViewHolder {

        private final TextView yearTextView;

        YearViewHolder(@NonNull View itemView) {
            super(itemView);
            yearTextView = itemView.findViewById(R.id.year_text_view);
        }

        void bind(String year) {
            yearTextView.setText(year);
        }
    }

    static class MemoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView memoryTitleTextView;
        private final TextView memoryDateTextView;
        private final OnMemoryClickListener onMemoryClickListener;
        private long memoryId;

        MemoryViewHolder(@NonNull View itemView, OnMemoryClickListener onMemoryClickListener) {
            super(itemView);
            itemView.setOnClickListener(this);
            memoryTitleTextView = itemView.findViewById(R.id.memory_title_text_view);
            memoryDateTextView = itemView.findViewById(R.id.memory_date_text_view);
            this.onMemoryClickListener = onMemoryClickListener;
        }

        void bind(Memory memory) {
            memoryTitleTextView.setText(memory.getTitle());
            memoryDateTextView.setText(memory.getDate());
            memoryId = memory.getId();
        }

        @Override
        public void onClick(View v) {
            onMemoryClickListener.onMemoryClick(memoryId);
        }
    }
}


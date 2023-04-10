package com.example.mindline.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindline.R;
import com.example.mindline.adapters.ImageAdapter;
import com.example.mindline.models.Memory;
import com.example.mindline.models.MemoryViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;

public class EditMemoryFragment extends Fragment {
    private static final String TAG = "EditMemoryFragment";

    private EditText memoryTitle;
    private EditText memoryDescription;
    private DatePicker memoryDatePicker;
    private ImageView imageView;
    private Button addImageButton;
    private Button saveMemoryButton;

    private Memory observedMemory;
    private ArrayList<Uri> imageUris;
    private ImageAdapter imageAdapter;
    private MemoryViewModel memoryViewModel;
    private long memoryId;

    public EditMemoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            memoryId = getArguments().getLong("memory_id", -1);
            System.out.println("this is the memoryid" + memoryId);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_memory, container, false);
    }

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
            Intent data = result.getData();
            if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri imageUri = clipData.getItemAt(i).getUri();
                    imageUris.add(imageUri);
                }
            } else if (data.getData() != null) {
                Uri imageUri = data.getData();
                imageUris.add(imageUri);
            }
            displaySelectedImages(); // Update the RecyclerView with the new images
        }
    });

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        memoryTitle = view.findViewById(R.id.memoryTitle);
        memoryDescription = view.findViewById(R.id.memoryDescription);
        memoryDatePicker = view.findViewById(R.id.memory_date_picker);
        imageView = view.findViewById(R.id.memory_image_view);
        addImageButton = view.findViewById(R.id.addImageButton);
        saveMemoryButton = view.findViewById(R.id.saveMemoryButton);

        memoryViewModel = new ViewModelProvider(requireActivity()).get(MemoryViewModel.class);

        imageUris = new ArrayList<>();
        imageAdapter = new ImageAdapter(requireContext(), imageUris, position -> {
            imageUris.remove(position);
            imageAdapter.notifyItemRemoved(position);
        }, true);
        RecyclerView selectedImagesRecyclerView = view.findViewById(R.id.selectedImagesRecyclerView);
        selectedImagesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        selectedImagesRecyclerView.setAdapter(imageAdapter);
        addImageButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            pickImageLauncher.launch(Intent.createChooser(intent, "Select Images"));
        });

        memoryViewModel.getMemoryById(memoryId).observe(getViewLifecycleOwner(), memory -> {
            if (memory != null) {
                observedMemory = memory;
                memoryTitle.setText(memory.getTitle());
                memoryDescription.setText(memory.getDescription());
                String dateString = memory.getDate();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date;
                try {
                    date = dateFormat.parse(dateString);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                memoryDatePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                if (memory.getImageUris() != null && !memory.getImageUris().isEmpty()) {
                    imageUris.clear();
                    imageUris.addAll(memory.getImageUris().stream().map(Uri::parse).collect(Collectors.toList()));
                    displaySelectedImages();
                }
            }
        });

        saveMemoryButton.setOnClickListener(v -> {
            if (updateMemory()) {
                requireActivity().onBackPressed();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void displaySelectedImages() {
        imageAdapter.updateImageUris(imageUris);
        imageAdapter.notifyDataSetChanged();
    }

    private boolean updateMemory() {
        if (observedMemory == null) {
            return false;
        }
        String title = memoryTitle.getText().toString();
        String description = memoryDescription.getText().toString();
        int day = memoryDatePicker.getDayOfMonth();
        int month = memoryDatePicker.getMonth();
        int year = memoryDatePicker.getYear();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        @SuppressLint("DefaultLocale") String date = year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", day);
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, day);
        Calendar currentDate = Calendar.getInstance();

        if (selectedDate.after(currentDate)) {
            Toast.makeText(requireContext(), "Please select a date in the past or today", Toast.LENGTH_SHORT).show();
            return false;
        }
        long dobInMillis = getDoBInMillis();

        if (selectedDate.getTimeInMillis() < dobInMillis) {
            Toast.makeText(requireContext(), "Please select a date after your Date of Birth", Toast.LENGTH_SHORT).show();
            return false;
        }
        Memory memory = new Memory(observedMemory.getId(), title, date, description, observedMemory.getAlbumId(), new ArrayList<>(imageUris.stream().map(Uri::toString).collect(Collectors.toList())));
        int rowsUpdated = memoryViewModel.updateMemory(memory);

        if (rowsUpdated > 0) {
            Toast.makeText(requireContext(), "Memory updated successfully", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return true;
        } else {
            Toast.makeText(requireContext(), "Failed to update the memory", Toast.LENGTH_SHORT).show();
            return false;
        }
    }


    private long getDoBInMillis() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        String dobStr = sharedPreferences.getString("dob", "1900-01-01");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dateFormat.setLenient(false);

        try {
            Date parsedDate = dateFormat.parse(dobStr);
            return parsedDate.getTime();
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing DoB", e);
            return -1;
        }
    }
}

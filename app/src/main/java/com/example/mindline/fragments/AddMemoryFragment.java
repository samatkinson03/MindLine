package com.example.mindline.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindline.R;
import com.example.mindline.adapters.ImageAdapter;
import com.example.mindline.models.Memory;
import com.example.mindline.models.MemoryViewModel;
import com.example.mindline.utils.ImageUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AddMemoryFragment extends Fragment {

    private MemoryViewModel memoryViewModel;
    private EditText memoryTitleEditText;
    private EditText memoryDescriptionEditText;

    private NavController navController;

    private static final int PICK_IMAGE_REQUEST = 1;
    private ArrayList<Uri> imageUris = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_memory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        memoryViewModel = new ViewModelProvider(requireActivity()).get(MemoryViewModel.class);
        navController = NavHostFragment.findNavController(this);
        memoryTitleEditText = view.findViewById(R.id.memory_title_edit_text);
        memoryDescriptionEditText = view.findViewById(R.id.memory_description_edit_text);
        ImageButton backButton = view.findViewById(R.id.back_button);
        Button saveButton = view.findViewById(R.id.save_memory_button);

        backButton.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        saveButton.setOnClickListener(v -> saveMemory());
        ImageButton addImageButton = view.findViewById(R.id.add_image_button);
        addImageButton.setOnClickListener(v -> openImagePicker());

    }

    private void saveMemory() {
        String title = memoryTitleEditText.getText().toString().trim();
        String description = memoryDescriptionEditText.getText().toString().trim();
        DatePicker datePicker = getView().findViewById(R.id.memory_date_picker);
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1; // DatePicker months are 0-indexed
        int year = datePicker.getYear();
        String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        long selectedDateInMillis = getDateInMillis(dateStr);
        long dobInMillis = getDoBInMillis();
        if (selectedDateInMillis > System.currentTimeMillis()) {
            Toast.makeText(requireContext(), "Please select a date in the past or today", Toast.LENGTH_SHORT).show();
            return;
        } else if (selectedDateInMillis < dobInMillis) {
            Toast.makeText(requireContext(), "Please select a date after your Date of Birth", Toast.LENGTH_SHORT).show();
            return;
        }

        Memory memory = new Memory(title, description, dateStr);
        List<String> imageUrisAsString = imageUris.stream().map(Uri::toString).collect(Collectors.toList());
        memory.setImageUris(new ArrayList<>(imageUrisAsString));

        // Persist the images to Google Photos
        GooglePhotosUtils.persistImagesToGooglePhotos(requireContext(), imageUris, title, description);

        memoryViewModel.insert(memory);
        NavController navController = Navigation.findNavController(requireView());
        navController.popBackStack();
    }

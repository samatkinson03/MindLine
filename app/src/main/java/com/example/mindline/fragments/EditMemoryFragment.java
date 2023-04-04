package com.example.mindline.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mindline.R;
import com.example.mindline.adapters.ImageAdapter;
import com.example.mindline.models.Memory;
import com.example.mindline.models.MemoryViewModel;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EditMemoryFragment extends Fragment {
    private static final int PICK_IMAGE = 1;

    private EditText memoryTitle;
    private EditText memoryDescription;
    private DatePicker memoryDatePicker;
    private ImageView imageView;
    private Button addImageButton;
    private Button saveMemoryButton;

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
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_memory, container, false);
    }

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

        addImageButton.setOnClickListener(v -> openImagePicker());

        memoryViewModel.getMemoryById(memoryId).observe(getViewLifecycleOwner(), memory -> {
            if (memory != null) {
                memoryTitle.setText(memory.getTitle());
                memoryDescription.setText(memory.getDescription());

                String dateString = memory.getDate(); // Assuming dateString is in the format of "yyyy-MM-dd"
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date;
                try {
                    date = dateFormat.parse(dateString);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }


                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                memoryDatePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                memoryDatePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                List<String> imageUrisAsString = memory.getImageUris();
                if (imageUrisAsString != null && !imageUrisAsString.isEmpty()) {
                    for (String uriString : imageUrisAsString) {
                        imageUris.add(Uri.parse(uriString));
                    }
                    imageAdapter.notifyDataSetChanged();
                }

                if (!imageUris.isEmpty()) {
                    Glide.with(requireContext()).load(imageUris.get(0)).into((ImageView) requireView().findViewById(R.id.memory_image_view));
                } else {
                    requireView().findViewById(R.id.memory_image_view).setVisibility(View.GONE);
                }
            }
        });

        saveMemoryButton.setOnClickListener(v -> {
            if (updateMemory()) {
                requireActivity().onBackPressed();
            }
        });


    }


    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    /**
     * Handle the result of selecting an image from the image picker
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == requireActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                imageUris.add(imageUri);
                imageAdapter.notifyDataSetChanged();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Failed to load the image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Update the memory with the user input
     */
    private boolean updateMemory() {
        // Retrieve user input
        String title = memoryTitle.getText().toString();
        String description = memoryDescription.getText().toString();
        int day = memoryDatePicker.getDayOfMonth();
        int month = memoryDatePicker.getMonth();
        int year = memoryDatePicker.getYear();

        // Validate input
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        String date = year + "-" + String.format("%02d", month + 1) + "-" + String.format("%02d", day);

        // Update memory in the database
        Memory memory = new Memory(title, description, date);
        memory.setId(memoryId);
        memory.setImageUris(imageAdapter.getImageUris());
        int rowsUpdated = memoryViewModel.updateMemory(memory);
        if (rowsUpdated > 0) {
            return true;
        } else {
            return false;
        }
    }




}
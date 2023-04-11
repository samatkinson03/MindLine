package com.example.mindline.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.example.mindline.utils.GooglePhotosUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public class AddMemoryFragment extends Fragment {

    private MemoryViewModel memoryViewModel;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private DatePicker dateTextView;
    private RecyclerView selectedImagesRecyclerView;
    private ImageAdapter imageAdapter;
    private ArrayList<Uri> imageUris = new ArrayList<>();
    private static final int PICK_IMAGE_REQUEST = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_memory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        memoryViewModel = new ViewModelProvider(this).get(MemoryViewModel.class);

        titleEditText = view.findViewById(R.id.memory_title_edit_text);
        descriptionEditText = view.findViewById(R.id.memory_description_edit_text);
        dateTextView = view.findViewById(R.id.memory_date_picker);
        Button addMemoryButton = view.findViewById(R.id.save_memory_button);
        ImageButton addPhotosButton = view.findViewById(R.id.add_image_button);

        addMemoryButton.setOnClickListener(v -> {
            try {
                addMemory();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        selectedImagesRecyclerView = view.findViewById(R.id.selected_images_recycler_view);
        selectedImagesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        imageAdapter = new ImageAdapter(getContext(), imageUris, position -> {
            imageUris.remove(position);
            imageAdapter.updateImageUris(imageUris);
        }, true);
        selectedImagesRecyclerView.setAdapter(imageAdapter);
        addPhotosButton.setOnClickListener(v -> openImagePicker());

        dateTextView.init(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), (datePicker, year, month, day) -> {
            String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
            // Save the formatted date in the tag of the DatePicker
            dateTextView.setTag(dateStr);
        });
        dateTextView.setTag(String.format(Locale.getDefault(), "%04d-%02d-%02d", Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH) + 1, Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
    }


    private void addMemory() throws IOException {
        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String dateStr = (String) dateTextView.getTag();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        long selectedDateInMillis = getDateInMillis(dateStr);
        long dobInMillis = getDoBInMillis();
        System.out.println(dobInMillis);
        if (selectedDateInMillis > System.currentTimeMillis()) {
            Toast.makeText(requireContext(), "Please select a date in the past or today", Toast.LENGTH_SHORT).show();
            return;
        } else if (selectedDateInMillis <= dobInMillis) {
            Toast.makeText(requireContext(), "Please select a date after your Date of Birth", Toast.LENGTH_SHORT).show();
            return;
        }

        String albumId = UUID.randomUUID().toString();
        Memory memory = new Memory(title, description, dateStr, albumId);
        List<String> imageUrisAsString = imageUris.stream().map(Uri::toString).collect(Collectors.toList());
        memory.setImageUris(new ArrayList<>(imageUrisAsString));

        // Get the GoogleSignInAccount instance
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (googleSignInAccount == null) {
            Toast.makeText(requireContext(), "Unable to access Google Photos. Please sign in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        memoryViewModel.insert(memory);
        Toast.makeText(requireContext(), "Memory added successfully", Toast.LENGTH_SHORT).show();

        // Navigate back to the previous fragment
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }


    private ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    handleImagePickerResult(result.getData());
                }
            }
    );

    // ...

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Images"));
    }

    private void handleImagePickerResult(Intent data) {
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
        imageAdapter.updateImageUris(imageUris);
    }


    private long getDateInMillis(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = sdf.parse(dateStr);
            if (date != null) {
                return date.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private long getDoBInMillis() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_data", Context.MODE_PRIVATE);
        long dobInMillis = sharedPreferences.getLong("date_of_birth", -1);

        if (dobInMillis == -1) {
            Calendar calendar = Calendar.getInstance();
            dobInMillis = calendar.getTimeInMillis();
        }

        return dobInMillis;
    }

}


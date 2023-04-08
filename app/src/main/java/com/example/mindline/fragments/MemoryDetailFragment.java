package com.example.mindline.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mindline.R;
import com.example.mindline.activities.EditMemoryActivity;
import com.example.mindline.adapters.ImageAdapter;
import com.example.mindline.models.Memory;
import com.example.mindline.models.MemoryViewModel;
import com.example.mindline.utils.GooglePhotosUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MemoryDetailFragment extends Fragment {

    private TextView memoryTitleTextView;
    private TextView memoryDateTextView;
    private TextView memoryDescriptionLabel;
    private TextView memoryDescriptionTextView;
    private TextView memoryImagesLabel;
    private RecyclerView memoryImagesRecyclerView;
    private MemoryViewModel memoryViewModel;


    public MemoryDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_memory_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        memoryTitleTextView = view.findViewById(R.id.memory_title);
        memoryDateTextView = view.findViewById(R.id.memory_date);
        memoryDescriptionLabel = view.findViewById(R.id.memory_description_label);
        memoryDescriptionTextView = view.findViewById(R.id.memory_description);
        memoryImagesLabel = view.findViewById(R.id.memory_images_label);
        memoryImagesRecyclerView = view.findViewById(R.id.memory_images_recycler_view);

        memoryViewModel = new ViewModelProvider(requireActivity()).get(MemoryViewModel.class);
        Toolbar toolbar = view.findViewById(R.id.memory_detail_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        toolbar.inflateMenu(R.menu.memory_detail_menu);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_memory) {
                editMemory();
                return true;
            } else if (item.getItemId() == R.id.action_delete_memory) {
                deleteMemory();
                return true;
            }
            return false;
        });
        if (getArguments() != null) {
            long memoryId = getArguments().getLong("memory_id");
            memoryViewModel.getMemoryById(memoryId).observe(getViewLifecycleOwner(), this::displayMemoryDetails);
        }
    }

    private void displayMemoryDetails(Memory memory) {
        Log.d("test", "this is being ran");
        if (memory != null) {
            memoryTitleTextView.setText(memory.getTitle());
            memoryDateTextView.setText(memory.getDate());
            Log.d("test", "this is also being ran");
            if (!memory.getDescription().isEmpty()) {
                memoryDescriptionLabel.setVisibility(View.VISIBLE);
                memoryDescriptionTextView.setVisibility(View.VISIBLE);
                memoryDescriptionTextView.setText(memory.getDescription());
            }

            List<String> imageUris = new ArrayList<>(memory.getImageUris());
            List<Uri> imageUrisToDisplay = new ArrayList<>();
            System.out.println("Image URIs from Memory: " + imageUris);
            // Add local image Uris to list of image Uris to display
            for (String imageUriString : imageUris) {
                Uri imageUri = Uri.parse(imageUriString);
                System.out.println("Checking URI: " + imageUri);
                if (GooglePhotosUtils.isLocalFileUri(requireContext(), imageUri)) {
                    System.out.println("Adding URI: " + imageUri);
                    imageUrisToDisplay.add(imageUri);
                    System.out.printf("URI should be in list: ", imageUrisToDisplay.size());
                }
            }
            Log.d("Uh oh", "Number of images retrieved from database: " + imageUrisToDisplay.size());

            // Initialize the adapter with local images (if any)
            setupMemoryImagesAdapter(imageUrisToDisplay);

            // Add image Uris from Google Photos to list of image Uris to display
            String accessToken = getAccessToken();
            if (accessToken != null) {
                System.out.println("access token isnt null");
                GooglePhotosUtils.getImagesFromGooglePhotos(requireContext(), accessToken, memory.getAlbumId(), mediaItems -> {
                    List<Uri> imageUrisFromGoogle = mediaItems.stream()
                            .filter(mediaItem -> imageUris.contains(mediaItem.id))
                            .map(mediaItem -> Uri.parse(mediaItem.baseUrl))
                            .collect(Collectors.toList());
                    System.out.println(imageUrisFromGoogle);
                    imageUrisToDisplay.addAll(imageUrisFromGoogle);
                    System.out.println(imageUrisToDisplay.size());
                    // Update the adapter with local images and Google Photos images
                    updateMemoryImagesAdapter(imageUrisToDisplay);
                });
            } else {
                // Handle the case where the access token is missing
            }
        }
    }

    private void updateMemoryImagesAdapter(List<Uri> imageUris) {
        if (imageUris != null && !imageUris.isEmpty()) {
            memoryImagesLabel.setVisibility(View.VISIBLE);
            memoryImagesRecyclerView.setVisibility(View.VISIBLE);
            ImageAdapter memoryImagesAdapter = (ImageAdapter) memoryImagesRecyclerView.getAdapter();
            if (memoryImagesAdapter != null) {
                memoryImagesAdapter.updateImageUris((ArrayList<Uri>) imageUris);
            } else {
                setupMemoryImagesAdapter(imageUris);
            }
        }
    }


    private void setupMemoryImagesAdapter(List<Uri> imageUris) {
        if (imageUris != null && !imageUris.isEmpty()) {
            memoryImagesLabel.setVisibility(View.VISIBLE);
            memoryImagesRecyclerView.setVisibility(View.VISIBLE);
            memoryImagesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

            ImageAdapter memoryImagesAdapter = new ImageAdapter(requireContext(), (ArrayList<Uri>) imageUris);
            memoryImagesRecyclerView.setAdapter(memoryImagesAdapter);
        }
    }




    private String getAccessToken() {
        System.out.println("ayo");
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_preferences", Context.MODE_PRIVATE);
        return sharedPreferences.getString("access_token", null);
    }


    private void showImageInFullScreen(Uri imageUri) {
        Dialog fullScreenImageDialog = new Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        fullScreenImageDialog.setContentView(R.layout.full_screen_image_dialog);

        ImageView fullScreenImageView = fullScreenImageDialog.findViewById(R.id.full_screen_image_view);
        Glide.with(requireContext()).load(imageUri).into(fullScreenImageView);

        fullScreenImageView.setOnClickListener(v -> fullScreenImageDialog.dismiss());

        fullScreenImageDialog.show();
    }

    private void editMemory() {
        if (getArguments() != null) {
            long memoryId = getArguments().getLong("memory_id");
            Intent intent = new Intent(requireActivity(), EditMemoryActivity.class);
            intent.putExtra("memory_id", memoryId);
            startActivity(intent);
        }
    }

    private void deleteMemory() {
        if (getContext() != null) {
            new AlertDialog.Builder(getContext()).setTitle(R.string.delete_memory).setMessage(R.string.delete_memory_confirmation).setPositiveButton(R.string.yes, (dialog, which) -> {
                if (getArguments() != null) {
                    long memoryId = getArguments().getLong("memory_id");
                    memoryViewModel.deleteMemoryById(memoryId);
                    requireActivity().onBackPressed();
                }
            }).setNegativeButton(R.string.no, null).show();
        }

    }
}
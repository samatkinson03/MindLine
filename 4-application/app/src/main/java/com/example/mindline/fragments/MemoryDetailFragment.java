package com.example.mindline.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindline.R;
import com.example.mindline.activities.EditMemoryActivity;
import com.example.mindline.adapters.ImageAdapter;
import com.example.mindline.models.Memory;
import com.example.mindline.models.MemoryViewModel;
import com.example.mindline.utils.GooglePhotosUtils;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

    private List<Uri> imageUris;
    private List<String> imageUriStrings;
    private List<Uri> imageUrisToDisplay;

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
        if (memory != null) {
            memoryTitleTextView.setText(memory.getTitle());
            memoryDateTextView.setText(memory.getDate());

            if (!memory.getDescription().isEmpty()) {
                memoryDescriptionLabel.setVisibility(View.VISIBLE);
                memoryDescriptionTextView.setVisibility(View.VISIBLE);
                memoryDescriptionTextView.setText(memory.getDescription());
            }

            imageUriStrings = new ArrayList<>(memory.getImageUris());
            imageUris = new ArrayList<>();
            imageUrisToDisplay = new ArrayList<>();

            // Add local image Uris to list of image Uris to display
            for (String imageUriString : imageUriStrings) {
                Uri imageUri = Uri.parse(imageUriString);
                if (GooglePhotosUtils.isLocalFileUri(requireContext(), imageUri)) {
                    imageUris.add(imageUri);
                }
            }

            // Initialize the adapter with local images (if any)
            setupMemoryImagesAdapter(imageUris);

            // Add image Uris from Google Photos to list of image Uris to display
            GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(requireActivity());
            if (googleSignInAccount != null) {
                GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(requireContext(), Collections.singleton("https://www.googleapis.com/auth/photoslibrary"));
                credential.setSelectedAccount(googleSignInAccount.getAccount());
                new GetAccessTokenTask().execute(credential);
            }
        }
    }

    private class GetAccessTokenTask extends AsyncTask<GoogleAccountCredential, Void, String> {
        @Override
        protected String doInBackground(GoogleAccountCredential... credentials) {
            try {
                return credentials[0].getToken();
            } catch (IOException | GoogleAuthException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String accessToken) {
            if (accessToken != null) {
                // Code that uses the access token for Google Photos API calls
                GooglePhotosUtils.getImagesFromGooglePhotos(requireContext(), accessToken, imageUriStrings, mediaItems -> {
                    List<Uri> imageUrisFromGoogle = mediaItems.stream()
                            .filter(mediaItem -> imageUriStrings.contains(mediaItem.id))
                            .map(mediaItem -> Uri.parse(mediaItem.baseUrl))
                            .collect(Collectors.toList());
                    imageUrisToDisplay.addAll(imageUrisFromGoogle);

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


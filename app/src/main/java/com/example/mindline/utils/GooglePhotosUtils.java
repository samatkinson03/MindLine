package com.example.mindline.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;
import com.google.photos.library.v1.proto.BatchCreateMediaItemsResponse;
import com.google.photos.library.v1.proto.NewMediaItem;
import com.google.photos.library.v1.proto.NewMediaItemResult;
import com.google.photos.library.v1.proto.SearchMediaItemsResponse;
import com.google.photos.library.v1.upload.UploadMediaItemRequest;
import com.google.photos.library.v1.upload.UploadMediaItemResponse;
import com.google.photos.library.v1.util.NewMediaItemFactory;
import com.google.photos.types.proto.MediaItem;
import com.google.rpc.Code;
import com.google.rpc.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class GooglePhotosUtils {
    private static final String TAG = "GooglePhotosUtils";


    public static void getImagesFromGooglePhotos(Context context, String albumId, OnImagesFetchedListener listener) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<MediaItem>> future = executorService.submit(() -> {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleCredentials credential = getUserCredentials(context);
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory((HttpRequestInitializer) credential);
            List<MediaItem> mediaItems = new ArrayList<>();

            try {
                String nextPageToken = "";
                while (true) {
                    String url = "https://photoslibrary.googleapis.com/v1/mediaItems:search";
                    HttpRequest request = requestFactory.buildPostRequest(new GenericUrl(url),
                            ByteArrayContent.fromString("application/json",
                                    "{"
                                            + "\"albumId\":\"" + albumId + "\","
                                            + "\"pageSize\":50,"
                                            + "\"pageToken\":\"" + nextPageToken + "\""
                                            + "}"));
                    HttpResponse response = request.execute();

                    String responseBody = response.parseAsString();
                    Gson gson = new Gson();
                    Type type = new TypeToken<SearchMediaItemsResponse>() {
                    }.getType();
                    SearchMediaItemsResponse searchMediaItemsResponse = gson.fromJson(responseBody, type);

                    if (searchMediaItemsResponse.mediaItems.isEmpty()) {
                        break;
                    }
                    mediaItems.addAll(searchMediaItemsResponse.mediaItems);
                    if (searchMediaItemsResponse.nextPageToken == null) {
                        break;
                    } else {
                        nextPageToken = searchMediaItemsResponse.nextPageToken;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error retrieving images from Google Photos", e);
            }
            return mediaItems;
        });

        // handle result
        try {
            List<MediaItem> mediaItems = future.get();
            if (listener != null) {
                listener.onImagesFetched(mediaItems);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting result from getImagesFromGooglePhotos task", e);
        } finally {
            executorService.shutdown();
        }
    }

    public static void persistImagesToGooglePhotos(Context context, ArrayList<Uri> imageUris, String title, String description) throws IOException {
        // Set up the Photos Library Client
        PhotosLibrarySettings settings = PhotosLibrarySettings.newBuilder()
                .setCredentialsProvider(() -> getUserCredentials(context))

                .build();

        try (PhotosLibraryClient photosLibraryClient = PhotosLibraryClient.initialize(settings)) {
            List<String> uploadTokens = new ArrayList<>();

            // Step 1: Uploading bytes
            for (Uri uri : imageUris) {
                try (RandomAccessFile inputStream = new RandomAccessFile(new File(uri.getPath()), "r")) {
                    String mimeType = context.getContentResolver().getType(uri);
                    UploadMediaItemRequest uploadRequest = UploadMediaItemRequest.newBuilder()
                            .setMimeType(mimeType)
                            .setDataFile(inputStream)
                            .build();

                    UploadMediaItemResponse uploadResponse = photosLibraryClient.uploadMediaItem(uploadRequest);
                    if (uploadResponse.getError().isPresent()) {
                        // Handle error
                    } else {
                        String uploadToken = uploadResponse.getUploadToken().get();
                        uploadTokens.add(uploadToken);
                    }
                } catch (FileNotFoundException e) {
                    // Handle error
                } catch (IOException e) {
                    // Handle error
                }
            }

            // Step 2: Creating media items
            List<NewMediaItem> newItems = uploadTokens.stream()
                    .map(uploadToken -> NewMediaItemFactory.createNewMediaItem(uploadToken, title, description))
                    .collect(Collectors.toList());

            BatchCreateMediaItemsResponse response = photosLibraryClient.batchCreateMediaItems(newItems);

            for (NewMediaItemResult result : response.getNewMediaItemResultsList()) {
                Status status = result.getStatus();
                if (status.getCode() == Code.OK_VALUE) {
                    com.google.photos.types.proto.MediaItem createdItem = result.getMediaItem();
                } else {
                    // Handle error
                }
            }
        } catch (IOException e) {
            // Handle error
        }
    }

    public interface OnImagesFetchedListener {
        void onImagesFetched(List<MediaItem> mediaItems);
    }


        private static GoogleCredentials getUserCredentials(Context context) {
            String jsonKeyFilePath = "credentials.json";

            try (InputStream inputStream = context.getAssets().open(jsonKeyFilePath)) {
                return GoogleCredentials.fromStream(inputStream)
                        .createScoped(Collections.singletonList("https://www.googleapis.com/auth/photoslibrary"));
            } catch (IOException e) {
                Log.e(TAG, "Error loading credentials from JSON key file", e);
                return null;
            }
        }


    public static class MediaItem {
        public String id;
        public String filename;
        public String mimeType;
        public String mediaMetadata;
        public String baseUrl;
    }

    public static class SearchMediaItemsResponse {
        public List<MediaItem> mediaItems;
        public String nextPageToken;
    }
}



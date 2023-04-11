package com.example.mindline.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GooglePhotosUtils {
    private static final String TAG = "GooglePhotosUtils";


    public static void getImagesFromGooglePhotos(Context context, String accessToken, List<String> imageUriStrings, OnImagesFetchedListener listener) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<List<MediaItem>> future = executorService.submit(() -> {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleCredentials credential = getUserCredentials(accessToken);
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory((HttpRequestInitializer) credential);
            List<MediaItem> mediaItems = new ArrayList<>();
            try {
                String nextPageToken = "";
                while (true) {
                    String url = "https://photoslibrary.googleapis.com/v1/mediaItems:search";
                    HttpRequest request = requestFactory.buildPostRequest(new GenericUrl(url), ByteArrayContent.fromString("application/json", "{" + "\"pageSize\":50," + "\"pageToken\":\"" + nextPageToken + "\"" + "}"));
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


    public static boolean isLocalFileUri(Context context, Uri uri) {
        return ContentResolver.SCHEME_FILE.equals(uri.getScheme())
                || (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())
                && context != null
                && "com.android.providers.media.documents".equals(uri.getAuthority()));
    }

    private static GoogleCredentials getUserCredentials(String accessToken) {
        return GoogleCredentials.create(new AccessToken(accessToken, null));
    }


    public interface OnImagesFetchedListener {
        void onImagesFetched(List<MediaItem> mediaItems);
    }

    public static class MediaItem {
        public String id;
        public String baseUrl;
    }

    public static class SearchMediaItemsResponse {
        public List<MediaItem> mediaItems;
        public String nextPageToken;
    }
}



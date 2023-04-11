package com.example.mindline.converters;

import android.net.Uri;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class UriListConverter {

    // Create a Gson object with a custom deserializer for the Uri class.
    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Uri.class, new UriDeserialiser())
            .create();

    // Create a Type object representing a list of Uri objects.
    private static Type type = new TypeToken<List<Uri>>() {}.getType();

    // Convert a string value to a list of Uri objects.
    @TypeConverter
    public static List<Uri> toUriList(String value) {
        // If the value is null, return null.
        if (value == null) {
            return null;
        }
        // Use the Gson object to parse the JSON string and return a list of Uri objects.
        return gson.fromJson(value, type);
    }

    // Convert a list of Uri objects to a string value.
    @TypeConverter
    public static String fromUriList(List<Uri> list) {
        // If the list is null, return null.
        if (list == null) {
            return null;
        }
        // Use the Gson object to serialize the list of Uri objects to a JSON string.
        return gson.toJson(list, type);
    }
}

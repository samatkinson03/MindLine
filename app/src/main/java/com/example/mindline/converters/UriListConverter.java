package com.example.mindline.converters;

import android.net.Uri;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class UriListConverter {
    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Uri.class, new UriDeserialiser())
            .create();

    private static Type type = new TypeToken<List<Uri>>() {}.getType();

    @TypeConverter
    public static List<Uri> toUriList(String value) {
        if (value == null) {
            return null;
        }
        return gson.fromJson(value, type);
    }

    @TypeConverter
    public static String fromUriList(List<Uri> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list, type);
    }
}

package com.example.mindline.converters;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class StringListConverter {

    @TypeConverter
    public static ArrayList<String> toStringList(String value) {
        if (value == null) {
            return null;
        }
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromStringList(ArrayList<String> list) {
        if (list == null) {
            return null;
        }
        return new Gson().toJson(list);
    }
}

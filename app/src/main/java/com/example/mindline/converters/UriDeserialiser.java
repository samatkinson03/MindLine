package com.example.mindline.converters;

import android.net.Uri;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class UriDeserialiser implements JsonDeserializer<Uri> {
    @Override
    public Uri deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String uriAsString = json.isJsonPrimitive() ? json.getAsString() : json.toString();
        return Uri.parse(uriAsString);
    }
}

package com.example.mindline.converters;

import android.net.Uri;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class UriDeserialiser implements JsonDeserializer<Uri> {

    // This method converts a JSON element representing a URI to an actual Uri object.
    @Override
    public Uri deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // Get the URI string representation from the JSON element.
        String uriAsString = json.isJsonPrimitive() ? json.getAsString() : json.toString();

        // Create and return a new Uri object from the URI string representation.
        return Uri.parse(uriAsString);
    }
}

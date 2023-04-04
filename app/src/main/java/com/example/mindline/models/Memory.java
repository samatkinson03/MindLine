package com.example.mindline.models;

import android.net.Uri;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.mindline.converters.StringListConverter;
import com.example.mindline.converters.UriListConverter;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Memory {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private String date;
    private String description;
    @TypeConverters(UriListConverter.class)
    private List<Uri> images;
    @TypeConverters(StringListConverter.class)
    private ArrayList<String> imageUris;

    @Ignore
    public Memory(long id, String title, String date, String description, List<Uri> images, ArrayList<String> imageUris) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.description = description;
        this.images = images;
        this.imageUris = imageUris;
    }

    public Memory(String title, String description, String date) {
        this.title = title;
        this.description = description;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Uri> getImages() {
        return images;
    }

    public void setImages(List<Uri> images) {
        this.images = images;
    }

    public ArrayList<String> getImageUris() {
        return imageUris;
    }

    public void setImageUris(ArrayList<String> imageUris) {
        this.imageUris = imageUris;
    }

    // ... Remaining getters and setters
}


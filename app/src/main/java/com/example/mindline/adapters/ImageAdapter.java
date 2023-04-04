package com.example.mindline.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mindline.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Uri> imageUris;
    private OnImageRemoveListener onImageRemoveListener;

    private boolean enableDelete;

    public ImageAdapter(Context context, ArrayList<Uri> imageUris, OnImageRemoveListener onImageRemoveListener, boolean enableDelete) {
        this.context = context;
        this.imageUris = imageUris;
        this.onImageRemoveListener = onImageRemoveListener;
        this.enableDelete = enableDelete;
    }
    public ImageAdapter(Context context, ArrayList<Uri> imageUris) {
        this.context = context;
        this.imageUris = imageUris;
        this.onImageRemoveListener = null;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), imageUri);
                Drawable drawable = ImageDecoder.decodeDrawable(source);
                holder.imageView.setImageDrawable(drawable);
            } else {
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                holder.imageView.setImageBitmap(bitmap);
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView deleteImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            deleteImageView = itemView.findViewById(R.id.delete_image_view);
            if (enableDelete) {
                deleteImageView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onImageRemoveListener.onImageRemove(position);
                    }
                });
            } else {
                deleteImageView.setVisibility(View.GONE);
            }
        }
    }
    public interface OnImageRemoveListener {
        void onImageRemove(int position);
    }

    public ArrayList<String> getImageUris() {
        ArrayList<String> imageUrisAsString = new ArrayList<>();
        for (Uri uri : imageUris) {
            imageUrisAsString.add(uri.toString());
        }
        return imageUrisAsString;
    }
}

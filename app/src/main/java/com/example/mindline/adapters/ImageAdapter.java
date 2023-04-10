package com.example.mindline.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
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

// This adapter is responsible for displaying a list of images in a RecyclerView.
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Uri> imageUris;
    private OnImageRemoveListener onImageRemoveListener;

    // This flag determines whether to enable the delete functionality or not.
    private boolean enableDelete;

    // Constructor for the adapter that takes in a list of image URIs and a listener for remove events.
    public ImageAdapter(Context context, ArrayList<Uri> imageUris, OnImageRemoveListener onImageRemoveListener, boolean enableDelete) {
        this.context = context;
        this.imageUris = imageUris;
        this.onImageRemoveListener = onImageRemoveListener;
        this.enableDelete = enableDelete;
    }

    // Constructor for the adapter that takes in a list of image URIs without any listener for remove events.
    public ImageAdapter(Context context, ArrayList<Uri> imageUris) {
        this.context = context;
        this.imageUris = imageUris;
        this.onImageRemoveListener = null;
    }

    // This method inflates the layout for each item in the RecyclerView.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new ViewHolder(view);
    }

    // This method binds the data of an image URI to the corresponding view holder in the RecyclerView.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri imageUri = imageUris.get(position);
        try {
            // Use ImageDecoder to decode the image URI and set the drawable to the image view.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), imageUri);
                context.grantUriPermission(context.getPackageName(), imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Drawable drawable = ImageDecoder.decodeDrawable(source);
                holder.imageView.setImageDrawable(drawable);
            } else {
                // If running on lower API level, use BitmapFactory to decode the image URI and set the bitmap to the image view.
                InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                holder.imageView.setImageBitmap(bitmap);
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            holder.imageView.setImageResource(R.drawable.default_image);
        }
    }

    // This method returns the number of items in the list of image URIs.
    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    // This inner class defines the view holder for each item in the RecyclerView.
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView deleteImageView;

        // Constructor for the view holder that sets the image view and delete image view.
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            deleteImageView = itemView.findViewById(R.id.delete_image_view);
            if (enableDelete) {
                // If delete functionality is enabled, set a click listener for the delete image view.
                deleteImageView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onImageRemoveListener.onImageRemove(position);
                    }
                });
            } else {
                // If delete functionality is disabled, hide the delete image view.
                deleteImageView.setVisibility(View.GONE);
            }
        }
    }


    public interface OnImageRemoveListener {
        void onImageRemove(int position);
    }


    public void updateImageUris(ArrayList<Uri> imageUris) {
        this.imageUris = imageUris;
        notifyDataSetChanged();
    }
}

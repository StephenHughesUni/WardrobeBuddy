package com.example.wardrobebuddy.com.firebaseauth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CollectionOverviewAdapter extends RecyclerView.Adapter<CollectionOverviewAdapter.ViewHolder> {
    private Context context;
    private List<Collection> collections;

    public CollectionOverviewAdapter(Context context, List<Collection> collections) {
        this.context = context;
        this.collections = collections;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.collection_overview_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Collection collection = collections.get(position);
        holder.collectionNameTextView.setText(collection.getName());

        // Set the number of items in the collection
        holder.itemCountTextView.setText(String.format("%d items", collection.getItems().size()));

        // Assume we use the first item's image in the collection as the display image
        if (!collection.getItems().isEmpty() && collection.getItems().get(0).getProductImageUrl() != null) {
            String imageUrl = collection.getItems().get(0).getProductImageUrl();
            Glide.with(context)
                    .load(imageUrl)
                    .into(holder.collectionImageView);
        } else {
            // Load a default image or leave it blank
        }
    }

    @Override
    public int getItemCount() {
        return collections != null ? collections.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView collectionNameTextView, itemCountTextView;
        ImageView collectionImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            collectionNameTextView = itemView.findViewById(R.id.collectionNameTextView);
            itemCountTextView = itemView.findViewById(R.id.itemCountTextView); // Make sure you have this TextView in your XML
            collectionImageView = itemView.findViewById(R.id.collectionImageView);
        }
    }
}

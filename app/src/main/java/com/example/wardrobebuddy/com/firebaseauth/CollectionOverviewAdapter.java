package com.example.wardrobebuddy.com.firebaseauth;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

        // Set delete button listener
        holder.deleteButton.setOnClickListener(v -> {
            removeCollection(collection.getName(), position); // Using getname as the unique ID
        });
    }

    private void removeCollection(String collectionId, int position) {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://finalyearprojectapp-29b81-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("collections")
                .child(collectionId);
        ref.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Remove the item from the list and notify the adapter
                collections.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Collection deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete collection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return collections != null ? collections.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView collectionNameTextView, itemCountTextView;
        ImageView collectionImageView;

        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views
            collectionNameTextView = itemView.findViewById(R.id.collectionNameTextView);
            itemCountTextView = itemView.findViewById(R.id.itemCountTextView);
            collectionImageView = itemView.findViewById(R.id.collectionImageView);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Collection clickedCollection = collections.get(position);
                        Intent intent = new Intent(view.getContext(), CollectionDetailActivity.class);
                        intent.putExtra("collectionName", clickedCollection.getName());
                        view.getContext().startActivity(intent);
                    }
                }
            });
        }

    }
}

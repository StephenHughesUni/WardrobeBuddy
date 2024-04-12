package com.example.wardrobebuddy.com.firebaseauth;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder> {
    private Context context;
    private List<CollectionItem> items;
    private DatabaseReference scannedItemsReference;

    public CollectionAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<>();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Make sure you're using the correct region URL for the database if you intend to change
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://finalyearprojectapp-29b81-default-rtdb.europe-west1.firebasedatabase.app");
            scannedItemsReference = database.getReference().child("users").child(userId).child("scannedItems");

            fetchCollectionItems();
        }

    }

    private void fetchCollectionItems() {
        // Fetch scanned items
        scannedItemsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                items.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CollectionItem item = snapshot.getValue(CollectionItem.class);
                    if (item != null) {
                        items.add(item);
                        // Pass the current position of the item in the list
                        int position = items.size() - 1; // Since item is added at the end of the list
                        fetchProductInfo(item.getArticleNumber().replace("/", "_"), position);
                    }
                }
                notifyDataSetChanged();
                Log.d("CollectionAdapter", "Scanned items fetched: " + items.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle potential errors
            }
        });
    }

    private void fetchProductInfo(final String articleNumber, final int position) {
        // Make sure you're using the correct region URL for the database if you intend to change
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://finalyearprojectapp-29b81-default-rtdb.europe-west1.firebasedatabase.app");

        // Get the user ID from the FirebaseUser object
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Use the database object to get a reference to the product info of the specific article number under the user ID
        DatabaseReference productInfoRef = database.getReference()
                .child("users").child(userId)
                .child("productInfo").child(articleNumber);

        productInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ProductInfo productInfo = dataSnapshot.getValue(ProductInfo.class);
                if (productInfo != null) {
                    Log.d("CollectionAdapter", "Product info fetched: " + productInfo.toString());
                    // Assuming your ProductInfo class has a getter for the imageUrl
                    String imageUrl = productInfo.getImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        // Update the corresponding CollectionItem with this imageUrl
                        CollectionItem item = items.get(position);
                        item.setProductImageUrl(imageUrl); // Make sure you have this setter in the CollectionItem class
                        notifyItemChanged(position); // Notify to update the item at this position
                    }
                } else {
                    Log.d("CollectionAdapter", "Product info not found for articleNumber: " + articleNumber);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("CollectionAdapter", "Failed to fetch product info: " + error.toException());
            }
        });
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.collection_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollectionItem item = items.get(position);

        // Set text on the TextViews
        holder.brandTextView.setText(item.getBrand());
        holder.priceTextView.setText(item.getPrice());
        holder.sizeTextView.setText(item.getSize());
        holder.articleTextView.setText(item.getArticleNumber());

        // Load the image using Glide
        String productImageUrl = item.getProductImageUrl();
        if (productImageUrl != null && !productImageUrl.isEmpty()) {
            Glide.with(context)
                    .load(productImageUrl)
                    .override(200, 200)
                    .centerCrop()
                    .into(holder.productImageView);
        } else {
            Log.w("CollectionAdapter", "ProductImageUrl is null or empty for item: " + item);
        }

        // Set the checkbox state based on whether the item is selected
        holder.checkBox.setChecked(item.isSelected());

        // Set up the click listener for the checkbox to toggle the selection state
        holder.checkBox.setOnClickListener(v -> {
            boolean isSelected = !item.isSelected();
            item.setSelected(isSelected); // Toggle the item's selected state
            notifyItemChanged(position); // Notify any registered observers that the item at position has changed.
        });

        // Optionally,  might want to handle the item click to toggle the selection state as well
        holder.itemView.setOnClickListener(v -> {
            boolean isSelected = !item.isSelected();
            item.setSelected(isSelected); // Toggle the item's selected state
            holder.checkBox.setChecked(isSelected); // Update the checkbox to reflect the new state
            notifyItemChanged(position); // Notify any registered observers that the item at position has changed.
        });
    }


    // To get all CollectionItem objects
    public List<CollectionItem> getItems() {
        return items;
    }

    public List<CollectionItem> getSelectedItems() {
        List<CollectionItem> selectedItems = new ArrayList<>();
        for (CollectionItem item : items) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }
        return selectedItems;
    }


    // To clear all selections
    public void clearSelections() {
        for (CollectionItem item : items) {
            item.setSelected(false);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView brandTextView, priceTextView, sizeTextView, articleTextView;
        CheckBox checkBox; // Checkbox to indicate selection
        ImageView productImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            brandTextView = itemView.findViewById(R.id.brandTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            sizeTextView = itemView.findViewById(R.id.sizeTextView);
            articleTextView = itemView.findViewById(R.id.articleTextView);
            productImageView = itemView.findViewById(R.id.productImageView);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}

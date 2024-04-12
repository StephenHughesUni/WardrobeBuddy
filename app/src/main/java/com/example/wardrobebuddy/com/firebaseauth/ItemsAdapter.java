package com.example.wardrobebuddy.com.firebaseauth;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemViewHolder> {
    private Context context;
    private List<CollectionItem> items; // Your CollectionItem model

    private String collectionName; // Add this line
    private TextView totalPriceTextView;
    private double currentTotalPrice;

    public ItemsAdapter(Context context, List<CollectionItem> items, String collectionName, double currentTotalPrice, TextView totalPriceTextView) {
        this.context = context;
        this.items = items;
        this.collectionName = collectionName;
        this.currentTotalPrice = currentTotalPrice;
        this.totalPriceTextView = totalPriceTextView;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_collection_detail, parent, false);
        return new ItemViewHolder(view);
    }

    public void removeItemFromCollection(CollectionItem item, int position) {
        if (item.getKey() == null) {
            Log.e("ItemsAdapter", "Key is null, can't remove item");
            return;
        }

        DatabaseReference itemRef = FirebaseDatabase.getInstance("https://finalyearprojectapp-29b81-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("collections")
                .child(collectionName)
                .child("items")
                .child(item.getKey());

        itemRef.removeValue().addOnSuccessListener(aVoid -> {
            double priceOfRemovedItem = parsePrice(item.getPrice());
            items.remove(position);
            notifyItemRemoved(position);
            updateTotalPrice(-priceOfRemovedItem);
        }).addOnFailureListener(e -> {
            Log.e("ItemsAdapter", "Failed to remove item: ", e);
        });
    }

    private double parsePrice(String priceStr) {
        try {
            return Double.parseDouble(priceStr.replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            Log.e("ItemsAdapter", "Failed to parse price: ", e);
            return 0.0;
        }
    }

    private void updateTotalPrice(double priceChange) {
        currentTotalPrice += priceChange;
        totalPriceTextView.setText(String.format("Total Price: €%.2f", currentTotalPrice));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        CollectionItem item = items.get(position);

        // Set the image from the product URL
        Glide.with(context)
                .load(item.getProductImageUrl())
                .into(holder.itemImageView);

        // Set the article number and size
        String articleAndSize = "Article: " + item.getArticleNumber() + " - Size: " + item.getSize();
        holder.itemArticleTextView.setText(articleAndSize);

        // Set the price
        holder.itemPriceTextView.setText("Price: " + item.getPrice());

        holder.deleteItemButton.setOnClickListener(v -> {
            // Call method to handle deletion
            removeItemFromCollection(item, position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImageView;
        TextView itemArticleTextView, itemPriceTextView;

        ImageButton deleteItemButton;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.itemImageView);
            itemArticleTextView = itemView.findViewById(R.id.itemArticleTextView);
            itemPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
            deleteItemButton = itemView.findViewById(R.id.deleteItemButton);
        }
    }
}

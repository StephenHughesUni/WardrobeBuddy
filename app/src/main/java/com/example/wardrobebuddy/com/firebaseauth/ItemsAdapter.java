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

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ItemViewHolder> {
    private Context context;
    private List<CollectionItem> items; // Your CollectionItem model

    public ItemsAdapter(Context context, List<CollectionItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_collection_detail, parent, false);
        return new ItemViewHolder(view);
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
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImageView;
        TextView itemArticleTextView, itemPriceTextView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.itemImageView);
            itemArticleTextView = itemView.findViewById(R.id.itemArticleTextView);
            itemPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
        }
    }
}

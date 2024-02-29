package com.example.wardrobebuddy.com.firebaseauth;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ScannedItemsAdapter extends RecyclerView.Adapter<ScannedItemsAdapter.ViewHolder> {
    private List<ScannedItem> scannedItems;
    private Context context;

    public ScannedItemsAdapter(Context context, List<ScannedItem> scannedItems) {
        this.context = context;
        this.scannedItems = scannedItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scanned_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScannedItem item = scannedItems.get(position);
        holder.sizeTextView.setText(item.getSize());
        holder.priceTextView.setText(item.getPrice());
        holder.articleNumberTextView.setText(item.getArticleNumber());

        // Use Glide to load the image from the URI
        // Make sure the imageUri in ScannedItem is stored as a String and converted to Uri here
        if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
            Glide.with(context)
                    .load(Uri.parse(item.getImageUri())) // Convert the String URI back to Uri
                    .into(holder.imageView);
        }
    }


    @Override
    public int getItemCount() {
        return scannedItems.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView sizeTextView, priceTextView, articleNumberTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            sizeTextView = itemView.findViewById(R.id.item_size); // Ensure you have this ID in your layout
            priceTextView = itemView.findViewById(R.id.item_price); // Ensure you have this ID in your layout
            articleNumberTextView = itemView.findViewById(R.id.item_article_number); // Ensure this too
        }
    }

}

package com.example.wardrobebuddy.com.firebaseauth;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ScannedItemsAdapter extends RecyclerView.Adapter<ScannedItemsAdapter.ViewHolder> {
    private List<ScannedItem> scannedItems;
    private Context context;
    private OnItemDeleteClickListener deleteClickListener;

    public interface OnItemDeleteClickListener {
        void onItemDeleteClick(int position);
    }

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

        // Load the image using Glide
        Glide.with(context).load(Uri.parse(item.getImageUri())).into(holder.imageView);

        // Set the brand name and article number
        holder.brandTextView.setText(item.getBrand());
        holder.scanDateTextView.setText(item.getDateTimeScanned());

        // Click listener for the view that shows item details and offers an option to view online
        holder.itemView.setOnClickListener(view -> {
            // AlertDialog to show details
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Item Details")
                    .setMessage("Brand: " + item.getBrand() + "\nSize: " + item.getSize() + "\nPrice: " + item.getPrice() + "\nArticle Number: " + item.getArticleNumber())
                    .setPositiveButton("OK", null)
                    .setNegativeButton("View Online", (dialog, which) -> {
                        // Use UrlBuilder to get the URL for viewing online
                        String url = UrlBuilder.getUrl(item.getBrand(), item.getArticleNumber());
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        context.startActivity(browserIntent);
                    })
                    .show();
        });

        // Set up click listener for delete icon
        holder.deleteIcon.setOnClickListener(view -> {
            if (deleteClickListener != null) {
                deleteClickListener.onItemDeleteClick(position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return scannedItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView deleteIcon;
        TextView brandTextView; // TextView for the brand
        TextView scanDateTextView; // TextView for the scan date


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
            brandTextView = itemView.findViewById(R.id.textView_brand); // Make sure this ID matches your layout
            scanDateTextView = itemView.findViewById(R.id.scan_date_text_view); // Make sure this ID matches your layout
        }
    }

    public void setOnItemDeleteClickListener(OnItemDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    public List<ScannedItem> getScannedItems() {
        return scannedItems;
    }
}

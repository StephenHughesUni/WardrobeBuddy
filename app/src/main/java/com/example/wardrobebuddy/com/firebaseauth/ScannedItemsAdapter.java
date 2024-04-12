package com.example.wardrobebuddy.com.firebaseauth;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class ScannedItemsAdapter extends RecyclerView.Adapter<ScannedItemsAdapter.ViewHolder> {
    private List<ScannedItem> scannedItems;
    private Context context;
    private OnItemDeleteClickListener deleteClickListener;
    private FirebaseUser user;
    private DatabaseReference databaseReference;

    public ScannedItemsAdapter(Context context, List<ScannedItem> scannedItems) {
        this.context = context;
        this.scannedItems = scannedItems;
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://finalyearprojectapp-29b81-default-rtdb.europe-west1.firebasedatabase.app");
        databaseReference = database.getReference("users").child(user.getUid()).child("scannedItems");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scanned_item, parent, false);
        return new ViewHolder(view);
    }

    public void deleteItem(int position) {
        scannedItems.remove(position);
        notifyItemRemoved(position);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Item Details")
                    .setMessage("Brand: " + item.getBrand() + "\nSize: " + item.getSize() + "\nPrice: " + item.getPrice() + "\nArticle Number: " + item.getArticleNumber())
                    .setPositiveButton("OK", null)
                    .setNegativeButton("View Online", (dialog, which) -> {
                        String url = UrlBuilder.getUrl(item.getBrand(), item.getArticleNumber(), item.getCategory());
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        context.startActivity(browserIntent);
                    })
                    .setNeutralButton("More Info", (dialog, which) -> {
                        Intent detailIntent = new Intent(context, ItemDetailActivity.class);
                        detailIntent.putExtra("brand", item.getBrand());
                        detailIntent.putExtra("size", item.getSize());
                        detailIntent.putExtra("price", item.getPrice());
                        detailIntent.putExtra("articleNumber", item.getArticleNumber());
                        detailIntent.putExtra("dateTimeScanned", item.getDateTimeScanned());
                        // Use UrlBuilder to dynamically generate the product URL
                        String productUrl = UrlBuilder.getUrl(item.getBrand(), item.getArticleNumber(), item.getCategory());
                        detailIntent.putExtra("productUrl", productUrl); // Pass the dynamically generated URL

                        context.startActivity(detailIntent);
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

    public void setOnItemDeleteClickListener(OnItemDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return scannedItems.size();
    }

    public interface OnItemDeleteClickListener {
        void onItemDeleteClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView deleteIcon;
        TextView brandTextView;
        TextView scanDateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.item_image);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
            brandTextView = itemView.findViewById(R.id.textView_brand);
            scanDateTextView = itemView.findViewById(R.id.scan_date_text_view);

            deleteIcon.setOnClickListener(view -> {
                if (deleteClickListener != null) {
                    deleteClickListener.onItemDeleteClick(getAdapterPosition());
                }
            });
        }
    }
}

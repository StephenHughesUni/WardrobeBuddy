package com.example.wardrobebuddy.com.firebaseauth; // Adjust this package name as necessary

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SizeAdapter extends ArrayAdapter<String> {
    private Context context;
    private String[] sizes;
    private boolean[] availableSizes; // Boolean array to store availability

    public SizeAdapter(@NonNull Context context, int resource, String[] sizes) {
        super(context, resource, sizes);
        this.context = context;
        this.sizes = sizes;
        this.availableSizes = new boolean[sizes.length]; // Initially, all sizes are unavailable
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_size, parent, false);
        TextView sizeTextView = view.findViewById(R.id.size_text);
        sizeTextView.setText(sizes[position]);
        view.setBackgroundResource(availableSizes[position] ? R.drawable.available_background : R.drawable.unavailable_background); // Set background based on availability
        return view;
    }

    public void setAvailableSizes(boolean[] availableSizes) {
        this.availableSizes = availableSizes;
        notifyDataSetChanged(); // Notify the adapter to refresh the views
    }
}

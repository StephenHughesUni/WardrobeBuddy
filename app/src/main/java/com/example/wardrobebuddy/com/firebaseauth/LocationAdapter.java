package com.example.wardrobebuddy.com.firebaseauth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
    private List<LocationInfo> locationInfoList;

    public LocationAdapter(List<LocationInfo> locationInfoList) {
        this.locationInfoList = locationInfoList;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_recycler, parent, false);
        return new LocationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationInfo locationInfo = locationInfoList.get(position);
        holder.bind(locationInfo);
    }

    @Override
    public int getItemCount() {
        return locationInfoList.size();
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        private TextView areaTextView;
        private TextView titleTextView;
        private TextView messageTextView;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            areaTextView = itemView.findViewById(R.id.location_area);
            titleTextView = itemView.findViewById(R.id.location_title);
            messageTextView = itemView.findViewById(R.id.location_message);
        }

        public void bind(LocationInfo locationInfo) {
            areaTextView.setText(locationInfo.getArea());
            titleTextView.setText(locationInfo.getTitle());
            messageTextView.setText(locationInfo.getMessage());
        }
    }
}

package com.example.wardrobebuddy.com.firebaseauth;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ItemDetailActivity extends AppCompatActivity {
    private APIServiceInterface apiService;
    private LinearLayout sizeContainer; // Container for displaying size boxes
    private Map<String, Boolean> sizeAvailabilityMap; // Store fetched product information
    private List<LocationInfo> locationInfoList; // Store fetched location information

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // Initialize Retrofit and OkHttpClient
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://16.171.44.250:80")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        apiService = retrofit.create(APIServiceInterface.class);

        // Extract details from Intent
        String brand = getIntent().getStringExtra("brand");
        String price = getIntent().getStringExtra("price");
        String articleNumber = getIntent().getStringExtra("articleNumber");
        String productUrl = getIntent().getStringExtra("productUrl");

        // Set TextViews with the details passed from the previous activity
        ((TextView) findViewById(R.id.detail_brand)).setText(brand);
        ((TextView) findViewById(R.id.detail_price)).setText(price);
        ((TextView) findViewById(R.id.detail_articleNumber)).setText(articleNumber);

        // Initialize sizeContainer
        sizeContainer = findViewById(R.id.size_container);

        // Fetch product information
        if (productUrl != null && !productUrl.isEmpty()) {
            fetchProductInfo(productUrl, brand);
        } else {
            Log.e("ItemDetailActivity", "Product URL is missing or not provided.");
        }

        // Set click listener for "Check Available Sizes" button
        findViewById(R.id.button_check_available_sizes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show sizeContainer and display available sizes
                sizeContainer.setVisibility(View.VISIBLE);
                displayAvailableSizes();

                // Display location information
                if (locationInfoList != null) {
                    for (LocationInfo locationInfo : locationInfoList) {
                        Log.d("ItemDetailActivity", "Area: " + locationInfo.getArea());
                        Log.d("ItemDetailActivity", "Title: " + locationInfo.getTitle());
                        Log.d("ItemDetailActivity", "Message: " + locationInfo.getMessage());
                    }
                }
            }
        });

        // Set up RecyclerView for location information
        RecyclerView recyclerView = findViewById(R.id.location_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set click listener for "Check In-Store Availability" button
        findViewById(R.id.button_check_in_store_availability).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show RecyclerView and display location information
                if (locationInfoList != null) {
                    recyclerView.setAdapter(new LocationAdapter(locationInfoList));
                } else {
                    Log.e("ItemDetailActivity", "Location information not available.");
                }
            }
        });
    }

    private void fetchProductInfo(String productUrl, String brand) {
        apiService.fetchProductInfo(productUrl, brand).enqueue(new Callback<ProductInfoResponse>() {
            @Override
            public void onResponse(Call<ProductInfoResponse> call, Response<ProductInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProductInfoResponse productInfoResponse = response.body();
                    Log.d("ItemDetailActivity", "API call successful. Fetched Product Info: " + productInfoResponse);

                    // Store available sizes and location information from the response
                    if (productInfoResponse.getProductInfo() != null) {
                        sizeAvailabilityMap = productInfoResponse.getProductInfo().getSizeAvailability();
                        locationInfoList = productInfoResponse.getProductInfo().getLocationInfo();
                        // Load and display the image
                        String imageUrl = productInfoResponse.getProductInfo().getImageUrl();
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            ImageView imageView = findViewById(R.id.image_placeholder);
                            RequestOptions requestOptions = new RequestOptions()
                                    .placeholder(R.color.gray) // Placeholder while loading
                                    .error(R.color.gray) // Placeholder if loading fails
                                    .diskCacheStrategy(DiskCacheStrategy.ALL); // Cache image
                            Glide.with(ItemDetailActivity.this)
                                    .load(imageUrl)
                                    .apply(requestOptions)
                                    .into(imageView);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE); // Center the image
                            imageView.setAdjustViewBounds(true); // Adjust bounds to maintain aspect ratio
                            imageView.setPadding(16, 16, 16, 16); // Add padding around the image
                        } else {
                            Log.e("ItemDetailActivity", "Image URL is null or empty.");
                        }
                    } else {
                        Log.e("ItemDetailActivity", "Product info is null in the response.");
                    }
                } else {
                    Log.e("ItemDetailActivity", "API call successful but response parsing failed.");
                }
            }

            @Override
            public void onFailure(Call<ProductInfoResponse> call, Throwable t) {
                Log.e("ItemDetailActivity", "API call failed: " + t.getMessage());
            }
        });
    }

    private void displayAvailableSizes() {
        if (sizeAvailabilityMap != null) {
            // Clear previous size views
            sizeContainer.removeAllViews();

            // Display available sizes
            for (Map.Entry<String, Boolean> entry : sizeAvailabilityMap.entrySet()) {
                String size = entry.getKey();
                boolean isAvailable = entry.getValue();
                Log.d("ItemDetailActivity", size + ": " + (isAvailable ? "Available" : "Not Available"));

                // Create a TextView for each size and set its properties
                TextView textView = new TextView(ItemDetailActivity.this);
                textView.setText(size);
                textView.setPadding(16, 8, 16, 8);
                textView.setTextColor(Color.WHITE);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                textView.setGravity(Gravity.CENTER);

                // Set background color based on availability
                textView.setBackgroundResource(isAvailable ? R.drawable.available_background : R.drawable.unavailable_background);

                // Add the TextView to sizeContainer
                sizeContainer.addView(textView);
            }
        } else {
            Log.e("ItemDetailActivity", "Size availability information not available.");
        }
    }
}
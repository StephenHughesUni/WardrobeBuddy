package com.example.wardrobebuddy.com.firebaseauth;

import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private LinearLayout sizeContainer;
    private Map<String, Boolean> sizeAvailabilityMap;
    private List<LocationInfo> locationInfoList;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private TextToSpeech textToSpeech;
    private boolean isTextToSpeechEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);


        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8000")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        apiService = retrofit.create(APIServiceInterface.class);

        String brand = getIntent().getStringExtra("brand");
        String price = getIntent().getStringExtra("price");
        String articleNumber = getIntent().getStringExtra("articleNumber");
        String productUrl = getIntent().getStringExtra("productUrl");

        TextView brandTextView = findViewById(R.id.detail_brand);
        brandTextView.setText(brand);
        brandTextView.setOnClickListener(v -> onBrandClicked(brand));

        TextView priceTextView = findViewById(R.id.detail_price);
        priceTextView.setText(price);
        priceTextView.setOnClickListener(v -> onPriceClicked(price));

        TextView articleNumberTextView = findViewById(R.id.detail_articleNumber);
        articleNumberTextView.setText(articleNumber);
        articleNumberTextView.setOnClickListener(v -> onArticleNumberClicked(articleNumber));

        sizeContainer = findViewById(R.id.size_container);
        textToSpeech = new TextToSpeech(this, status -> {
            if (status != TextToSpeech.ERROR) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        if (productUrl != null && !productUrl.isEmpty()) {
            fetchProductInfo(productUrl, brand, articleNumber);
        } else {
            Log.e("ItemDetailActivity", "Product URL is missing or not provided.");
        }

        RecyclerView recyclerView = findViewById(R.id.location_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.button_check_available_sizes).setOnClickListener(v -> {
            toggleAvailableSizesVisibility();
        });

        findViewById(R.id.button_check_in_store_availability).setOnClickListener(v -> {
            toggleInStoreInfoVisibility();
        });

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the toolbar title icon programmatically
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide the default title
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logo_toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        displayUserEmail(menu); // Call to display user email
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            // Handle settings action
            return true;
        } else if (id == R.id.menu_user_email) {
            // Handle user email action
            return true;
        } else if (id == R.id.menu_enable_text_to_speech) {
            // Enable text-to-speech
            isTextToSpeechEnabled = true;
            return true;
        } else if (id == R.id.menu_disable_text_to_speech) {
            // Disable text-to-speech
            isTextToSpeechEnabled = false;
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void toggleAvailableSizesVisibility() {
        if (sizeContainer.getVisibility() == View.VISIBLE) {
            sizeContainer.setVisibility(View.GONE);
        } else {
            sizeContainer.setVisibility(View.VISIBLE);
            displayAvailableSizes();
            speakAvailableSizes();
        }
    }

    private void toggleInStoreInfoVisibility() {
        RecyclerView recyclerView = findViewById(R.id.location_recycler_view);
        if (recyclerView.getVisibility() == View.VISIBLE) {
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            if (locationInfoList != null) {
                recyclerView.setAdapter(new LocationAdapter(locationInfoList));
                speakLocationInfo(locationInfoList);
            } else {
                Log.e("ItemDetailActivity", "Location information not available.");
            }
        }
    }

    private void displayUserEmail(Menu menu) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userEmail = user.getEmail();
            MenuItem userEmailItem = menu.findItem(R.id.menu_user_email);
            if (userEmailItem != null) {
                userEmailItem.setTitle(userEmail);
            }
        }
    }

    private void speakAvailableSizes() {
        if (sizeAvailabilityMap != null && isTextToSpeechEnabled) {
            StringBuilder availableSizes = new StringBuilder();
            for (String size : sizeAvailabilityMap.keySet()) {
                availableSizes.append(size).append(", ");
            }
            String textToSpeak = "Available sizes are " + availableSizes.toString();
            textToSpeech.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void speakProductInfo(String info) {
        if (isTextToSpeechEnabled) {
            textToSpeech.speak(info, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void speakLocationInfo(List<LocationInfo> locationInfoList) {
        if (isTextToSpeechEnabled) {
            StringBuilder textToSpeak = new StringBuilder("In-store availability information. ");
            for (LocationInfo locationInfo : locationInfoList) {
                String area = locationInfo.getArea();
                String title = locationInfo.getTitle();
                String message = locationInfo.getMessage();
                textToSpeak.append(area).append(". ").append(title).append(". ").append(message).append(". ");
            }
            textToSpeech.speak(textToSpeak.toString(), TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void fetchProductInfo(String productUrl, String brand, String articleNumber) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://finalyearprojectapp-29b81-default-rtdb.europe-west1.firebasedatabase.app");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        String productNode = articleNumber.replaceAll("/", "_"); // Convert '/' to '_' for Firebase compatibility
        DatabaseReference ref = database.getReference("users").child(user.getUid()).child("productInfo").child(productNode);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> productInfoMap = (Map<String, Object>) dataSnapshot.getValue();
                    sizeAvailabilityMap = (Map<String, Boolean>) productInfoMap.get("sizeAvailability");
                    locationInfoList = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.child("locationInfo").getChildren()) {
                        LocationInfo locationInfo = snapshot.getValue(LocationInfo.class);
                        locationInfoList.add(locationInfo);
                    }
                    String imageUrl = (String) productInfoMap.get("imageUrl");
                    loadAndSetImage(imageUrl);
                    displayAvailableSizes();
                } else {
                    apiService.fetchProductInfo(productUrl, brand).enqueue(new Callback<ProductInfoResponse>() {
                        @Override
                        public void onResponse(Call<ProductInfoResponse> call, Response<ProductInfoResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ProductInfoResponse productInfoResponse = response.body();
                                Log.d("ItemDetailActivity", "API call successful. Fetched Product Info: " + productInfoResponse);

                                if (productInfoResponse.getProductInfo() != null) {
                                    sizeAvailabilityMap = productInfoResponse.getProductInfo().getSizeAvailability();
                                    locationInfoList = productInfoResponse.getProductInfo().getLocationInfo();
                                    DatabaseReference newRef = ref;
                                    HashMap<String, Object> productInfoMap = new HashMap<>();
                                    productInfoMap.put("sizeAvailability", sizeAvailabilityMap);
                                    productInfoMap.put("imageUrl", productInfoResponse.getProductInfo().getImageUrl());
                                    List<Map<String, String>> locationMapList = new ArrayList<>();
                                    for (LocationInfo locationInfo : locationInfoList) {
                                        Map<String, String> locationMap = new HashMap<>();
                                        locationMap.put("area", locationInfo.getArea());
                                        locationMap.put("title", locationInfo.getTitle());
                                        locationMap.put("message", locationInfo.getMessage());
                                        locationMapList.add(locationMap);
                                    }
                                    productInfoMap.put("locationInfo", locationMapList);
                                    newRef.setValue(productInfoMap)
                                            .addOnSuccessListener(aVoid -> Log.d("Firebase", "Product info saved successfully."))
                                            .addOnFailureListener(e -> Log.e("Firebase", "Failed to save product info.", e));
                                    loadAndSetImage(productInfoResponse.getProductInfo().getImageUrl());
                                    displayAvailableSizes();
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ItemDetailActivity", "Database operation cancelled: " + databaseError.getMessage());
            }
        });
    }


    private void loadAndSetImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ImageView imageView = findViewById(R.id.image_placeholder);
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.color.gray)
                    .error(R.color.gray)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
            Glide.with(ItemDetailActivity.this)
                    .load(imageUrl)
                    .apply(requestOptions)
                    .into(imageView);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setAdjustViewBounds(true);
            imageView.setPadding(16, 16, 16, 16);
        } else {
            Log.e("ItemDetailActivity", "Image URL is null or empty.");
        }
    }

    private void displayAvailableSizes() {
        if (sizeAvailabilityMap != null) {
            sizeContainer.removeAllViews();
            for (Map.Entry<String, Boolean> entry : sizeAvailabilityMap.entrySet()) {
                String size = entry.getKey();
                boolean isAvailable = entry.getValue();
                Log.d("ItemDetailActivity", size + ": " + (isAvailable ? "Available" : "Not Available"));
                TextView textView = new TextView(ItemDetailActivity.this);
                textView.setText(size);
                textView.setPadding(16, 8, 16, 8);
                textView.setTextColor(Color.WHITE);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                textView.setGravity(Gravity.CENTER);
                textView.setBackgroundResource(isAvailable ? R.drawable.available_background : R.drawable.unavailable_background);
                sizeContainer.addView(textView);
            }
        } else {
            Log.e("ItemDetailActivity", "Size availability information not available.");
        }
    }

    private void onBrandClicked(String brand) {
        speakProductInfo("Brand: " + brand);
    }

    private void onArticleNumberClicked(String articleNumber) {
        speakProductInfo("Article Number: " + articleNumber);
    }

    private void onPriceClicked(String price) {
        speakProductInfo("Price: " + price);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}

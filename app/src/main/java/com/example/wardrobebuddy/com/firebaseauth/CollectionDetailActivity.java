package com.example.wardrobebuddy.com.firebaseauth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CollectionDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ItemsAdapter adapter;
    private TextView collectionNameTextView;
    private TextView totalPriceTextView;

    private String collectionName; // The collection name
    private FirebaseAuth auth; // Firebase authentication instance

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_detail_recycler);

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // Hide the default title
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setLogo(R.drawable.logo_toolbar);
        }

        recyclerView = findViewById(R.id.collectionItemsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        collectionName = getIntent().getStringExtra("collectionName");

        collectionNameTextView = findViewById(R.id.collectionNameTextView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);

        // Check for null to avoid runtime exceptions
        if (collectionNameTextView != null && totalPriceTextView != null) {
            collectionNameTextView.setText(collectionName); // Set the collection name
            fetchCollectionItems(); // Fetch the items for the collection
        } else {
            Log.e("CollectionDetailActivity", "TextViews not found in layout");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        MenuItem userEmailItem = menu.findItem(R.id.menu_user_email);
        if (auth.getCurrentUser() != null) {
            userEmailItem.setTitle(auth.getCurrentUser().getEmail());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_logout) {
            auth.signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void fetchCollectionItems() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://finalyearprojectapp-29b81-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference itemsRef = database.getReference("users")
                .child(auth.getCurrentUser().getUid())
                .child("collections")
                .child(collectionName)
                .child("items");

        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<CollectionItem> itemsList = new ArrayList<>();
                double total = 0.0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CollectionItem item = snapshot.getValue(CollectionItem.class);
                    if (item != null) {
                        itemsList.add(item);
                        total += parsePrice(item.getPrice());
                    }
                }
                adapter = new ItemsAdapter(CollectionDetailActivity.this, itemsList);
                recyclerView.setAdapter(adapter);

                String totalFormatted = String.format("Total Price: €%.2f", total);
                totalPriceTextView.setText(totalFormatted);
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("CollectionDetailActivity", "Database error", databaseError.toException());
            }
        });
    }

    @SuppressLint("LongLogTag")
    private double parsePrice(String priceStr) {
        try {
            priceStr = priceStr.replaceAll("[^\\d.]", "");
            return Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Log.e("CollectionDetailActivity", "Failed to parse price from string: " + priceStr, e);
            return 0.0;
        }
    }
}

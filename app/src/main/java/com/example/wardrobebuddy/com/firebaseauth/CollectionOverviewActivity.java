package com.example.wardrobebuddy.com.firebaseauth;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

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

public class CollectionOverviewActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CollectionOverviewAdapter adapter;
    private Button makeNewWardrobeButton;
    private List<Collection> collectionList = new ArrayList<>(); // Initialize the list here
    private FirebaseAuth auth; // Firebase authentication instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_overview);

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

        recyclerView = findViewById(R.id.collection_overview_recyclerView);
        makeNewWardrobeButton = findViewById(R.id.makeNewWardrobeButton); // Replace with actual button ID in your layout

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        fetchCollections(); // This will fetch the collections and set the adapter

        // Set click listener for the button
        makeNewWardrobeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open a new page (replace MyOutfitsActivity.class with your desired activity)
                Intent intent = new Intent(CollectionOverviewActivity.this, MyOutfitsActivity.class);
                startActivity(intent);
            }
        });
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

    private void fetchCollections() {
        // Use the specified database instance with the correct URL
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://finalyearprojectapp-29b81-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference collectionsRef = database.getReference("users")
                .child(auth.getCurrentUser().getUid()) // Use auth.getCurrentUser() to get the current user
                .child("collections");

        collectionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                collectionList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Collection collection = snapshot.getValue(Collection.class);
                    collectionList.add(collection);
                }
                adapter = new CollectionOverviewAdapter(CollectionOverviewActivity.this, collectionList);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
            }
        });
    }
}

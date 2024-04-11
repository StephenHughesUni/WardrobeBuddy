package com.example.wardrobebuddy.com.firebaseauth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_overview);

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

    private void fetchCollections() {
        // Use the specified database instance with the correct URL
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://finalyearprojectapp-29b81-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference collectionsRef = database.getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
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

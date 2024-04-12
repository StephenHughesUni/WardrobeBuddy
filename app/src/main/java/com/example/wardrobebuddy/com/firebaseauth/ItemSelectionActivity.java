package com.example.wardrobebuddy.com.firebaseauth;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

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

public class ItemSelectionActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CollectionAdapter adapter;
    private Button addButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_select_layout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false); // Hide the default title
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setLogo(R.drawable.logo_toolbar);
        }

        recyclerView = findViewById(R.id.itemsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CollectionAdapter(this); // Ensure CollectionAdapter can handle selection logic
        recyclerView.setAdapter(adapter);

        addButton = findViewById(R.id.confirmAdditionButton);
        addButton.setOnClickListener(v -> addSelectedItemsToCollection());
    }


    private void addSelectedItemsToCollection() {
        List<CollectionItem> selectedItems = adapter.getSelectedItems(); // Ensure this method is implemented in CollectionAdapter
        String collectionName = getIntent().getStringExtra("collectionName");

        // Get a reference to the collection
        DatabaseReference collectionRef = FirebaseDatabase.getInstance("https://finalyearprojectapp-29b81-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("collections")
                .child(collectionName);

        // Retrieve the current itemCount
        collectionRef.child("itemCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer itemCount = dataSnapshot.getValue(Integer.class);
                if (itemCount == null) {
                    itemCount = 0; // default to 0 if itemCount does not exist
                }

                // Update the items and itemCount
                for (CollectionItem item : selectedItems) {
                    int newItemIndex = itemCount++;
                    collectionRef.child("items").child(String.valueOf(newItemIndex)).setValue(item);
                }

                collectionRef.child("itemCount").setValue(itemCount).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ItemSelectionActivity.this, "Items added to collection", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ItemSelectionActivity.this, "Failed to update item count", Toast.LENGTH_SHORT).show();
                    }
                    finish(); // Optionally close the activity after adding items
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AddItem", "Failed to retrieve itemCount", databaseError.toException());
            }
        });
    }
}


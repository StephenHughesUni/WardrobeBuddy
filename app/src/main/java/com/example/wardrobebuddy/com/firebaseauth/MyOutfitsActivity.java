package com.example.wardrobebuddy.com.firebaseauth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyOutfitsActivity extends AppCompatActivity {

    private EditText etCollectionName;
    private Button btnCreateCollection;
    private RecyclerView recyclerView;
    private CollectionAdapter collectionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_outfits);

        etCollectionName = findViewById(R.id.etCollectionName);
        btnCreateCollection = findViewById(R.id.btnCreateCollection);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        collectionAdapter = new CollectionAdapter(this);
        recyclerView.setAdapter(collectionAdapter);

        btnCreateCollection.setOnClickListener(v -> {
            String collectionName = etCollectionName.getText().toString().trim();
            if (!collectionName.isEmpty()) {
                Collection newCollection = new Collection(collectionName);
                boolean atLeastOneSelected = false;

                for (CollectionItem item : collectionAdapter.getItems()) {
                    if (item.isSelected()) {
                        newCollection.addItem(item);
                        item.setSelected(false); // Reset selection
                        atLeastOneSelected = true;
                    }
                }

                if (atLeastOneSelected) {
                    saveCollectionToFirebase(newCollection);
                    etCollectionName.setText("");
                    collectionAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MyOutfitsActivity.this, "Please select at least one item.", Toast.LENGTH_SHORT).show();
                }

            } else {
                etCollectionName.setError("Please enter a collection name");
            }
        });
    }

    private void saveCollectionToFirebase(Collection collection) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            // Use the correct Firebase URL
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://finalyearprojectapp-29b81-default-rtdb.europe-west1.firebasedatabase.app");
            DatabaseReference collectionsRef = database.getReference()
                    .child("users").child(userId).child("collections").child(collection.getName());

            collectionsRef.setValue(collection)
                    .addOnSuccessListener(aVoid -> {
                        // Provide feedback to the user that the collection was successfully saved
                        Toast.makeText(MyOutfitsActivity.this, "Collection saved!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Provide feedback to the user that there was an error saving the collection
                        Toast.makeText(MyOutfitsActivity.this, "Failed to save collection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Handle the case where the currentUser is null
            Toast.makeText(MyOutfitsActivity.this, "User not signed in", Toast.LENGTH_SHORT).show();
        }
    }

}

package com.example.wardrobebuddy.com.firebaseauth;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;

    // Request code for selecting an image from the gallery
    private static final int PICK_IMAGE_REQUEST = 104;
    private RecyclerView recyclerView;
    private ScannedItemsAdapter adapter;
    private List<ScannedItem> scannedItems;

    // Request codes for camera permission and capturing an image (commented for future use)
    // private static final int REQUEST_CAMERA_PERMISSION = 201;
    // private static final int REQUEST_IMAGE_CAPTURE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        scannedItems = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ScannedItemsAdapter(this, scannedItems);
        recyclerView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Call loadUserItems here to load the data as soon as the user is confirmed to be logged in
            loadUserItems();
        }

        Button btnScanNow = findViewById(R.id.btnScanNow);
        btnScanNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    // Code to check camera permission and open the camera (commented for future use)
    /*
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                // Handle the case where the user denies the permission.
            }
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            processImage(selectedImageUri);
        }
    }
    private String encodeImageToBase64(Uri imageUri) {
        try {
            InputStream imageStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void processImage(Uri imageUri) {
        String base64Image = encodeImageToBase64(imageUri);
        if (base64Image != null) {
            new Thread(() -> {
                OpenAiHelper openAIHelper = new OpenAiHelper();
                String prompt = "Please categorize the following information from the label and do not include other information:\n" +
                        "- Size (e.g., S, XS, M, L)\n" +
                        "- Price (e.g., $39.90)\n" +
                        "- Article Number (e.g., 2398/028/800)";
                String response = openAIHelper.getAIResponse(prompt, base64Image);
                runOnUiThread(() -> {
                    if (response != null && !response.isEmpty()) {
                        // Assuming response format is "Size: S\nPrice: $39.90\nArticle Number: 2398/028/800"
                        // You may need to adjust parsing logic based on actual response format
                        String[] parts = response.split("\n");
                        String size = parts.length > 0 ? parts[0].split(": ")[1] : ""; // Adjusted to extract value
                        String price = parts.length > 1 ? parts[1].split(": ")[1] : ""; // Adjusted to extract value
                        String articleNumber = parts.length > 2 ? parts[2].split(": ")[1] : ""; // Adjusted to extract value

                        // Convert Uri to String before passing to ScannedItem constructor
                        String imageUriString = imageUri.toString();
                        ScannedItem newItem = new ScannedItem(imageUriString, size, price, articleNumber);
                        saveItemToRealtimeDatabase(newItem); // Corrected comment to indicate saving to Realtime Database
                        Toast.makeText(MainActivity.this, "Item added to database", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to process image", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        } else {
            Toast.makeText(MainActivity.this, "Failed to encode image", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveItemToRealtimeDatabase(ScannedItem item) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://finalyearprojectapp-29b81-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference ref = database.getReference("users").child(user.getUid()).child("scannedItems");

        // Convert ScannedItem to a map or use a model class that Firebase can serialize directly
        Map<String, Object> itemMap = new HashMap<>();
        itemMap.put("size", item.getSize());
        itemMap.put("price", item.getPrice());
        itemMap.put("articleNumber", item.getArticleNumber());
        itemMap.put("imageUri", item.getImageUri().toString());

        // Push creates a unique ID for each new child
        ref.push().setValue(itemMap)
                .addOnSuccessListener(aVoid -> Log.d("RealtimeDatabase", "Item saved successfully"))
                .addOnFailureListener(e -> Log.w("RealtimeDatabase", "Error saving item", e));
    }


    private void loadUserItems() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://finalyearprojectapp-29b81-default-rtdb.europe-west1.firebasedatabase.app");
        DatabaseReference ref = database.getReference("users").child(user.getUid()).child("scannedItems");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                scannedItems.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ScannedItem item = snapshot.getValue(ScannedItem.class); // Make sure ScannedItem has a no-arg constructor and setters
                    scannedItems.add(item);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("RealtimeDatabase", "loadUserItems:onCancelled", databaseError.toException());
            }
        });
    }




    private String convertTextToJson(Text texts) {
        try {
            JSONObject jsonRoot = new JSONObject();
            JSONArray jsonTextBlocks = new JSONArray();

            for (Text.TextBlock block : texts.getTextBlocks()) {
                JSONObject jsonBlock = new JSONObject();
                JSONArray jsonLines = new JSONArray();

                for (Text.Line line : block.getLines()) {
                    JSONObject jsonLine = new JSONObject();
                    jsonLine.put("text", line.getText());
                    jsonLines.put(jsonLine);
                }

                jsonBlock.put("lines", jsonLines);
                jsonTextBlocks.put(jsonBlock);
            }

            jsonRoot.put("textBlocks", jsonTextBlocks);
            return jsonRoot.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveJsonToFile(String jsonString, String fileName) {
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(jsonString.getBytes());
            outputStream.close();

            // Get the file's path
            String filePath = getFilesDir().getAbsolutePath() + "/" + fileName;
            Log.d("MainActivity", "JSON saved to: " + filePath);
            Toast.makeText(this, "JSON saved to " + filePath, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving JSON", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        MenuItem userEmailItem = menu.findItem(R.id.menu_user_email);
        if (user != null) {
            userEmailItem.setTitle(user.getEmail());
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
}

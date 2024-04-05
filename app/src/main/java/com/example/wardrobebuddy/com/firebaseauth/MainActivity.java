package com.example.wardrobebuddy.com.firebaseauth;

import android.content.Context;
import android.content.DialogInterface;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.text.Text;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
                showCategorySelectionDialog();
            }
        });
    }

    private void showCategorySelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Category");

        String[] categories = {"Men", "Women", "Kids"};
        builder.setItems(categories, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedCategory = categories[which];
                openGallery(selectedCategory);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openGallery(String category) {
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
                String prompt = "Please extract/categorize and list only the following details from the label, ignoring all other information. Please return it in order of Brand, Size, Price, Article Number. :\n" +
                        "- Size (MAKE SURE TO CHECK for the one in bold, larger print or a box around it, ONLY provide one size do not list multiple sizes  e.g., 'Size: 28' or 'Size: M' or 'One-Size')\n" +
                        "- Price (if there is a sale, list the original price followed by the sale price in parentheses, e.g., 'Price: 22.99 (10.00 Sale)'). If no sale, just list the current price. Please prioritize euro.\n" +
                        "- Article Number (Please first check the brand found then follow the structure of that brands article number templating from that Brand for example H&M., 'Article Number: 1939/1 75 248179'). and Zara 'Article Number: 2398/028/800'\n" +
                        "- Brand (Use the article code to figure out the brand as each brand has its own pattern for article numbers. Check for logos or other similar patterns on it too, to check brand.  e.g., 'Brand: Zara' or 'Brand: H&M)";

                String response = openAIHelper.getAIResponse(prompt, base64Image);
                runOnUiThread(() -> {
                    if (response != null && !response.isEmpty()) {
                        // Split by line breaks to get each part of the response
                        String[] parts = response.split("\n");

                        // Temporary variables to hold each part
                        String brand = "", size = "", price = "", articleNumber = "";

                        // Iterate through each part to assign the values correctly based on a known identifier
                        for (String part : parts) {
                            if (part.startsWith("Brand:")) {
                                brand = part.split(":")[1].trim(); // Adjusted to trim whitespace
                            } else if (part.startsWith("Size:")) {
                                size = part.split(":")[1].trim();
                            } else if (part.startsWith("Price:")) {
                                price = part.split(":")[1].trim();
                            } else if (part.startsWith("Article Number:")) {
                                articleNumber = part.split(":")[1].trim();
                            }
                        }

                        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                        String imageUriString = imageUri.toString();
                        ScannedItem newItem = new ScannedItem(imageUriString, brand, size, price, articleNumber, currentDateTime);
                        saveItemToRealtimeDatabase(newItem);
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
        itemMap.put("dateTimeScanned", item.getDateTimeScanned()); // This line should add the dateTimeScanned
        itemMap.put("brand", item.getBrand()); // Add this line to include the brand


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

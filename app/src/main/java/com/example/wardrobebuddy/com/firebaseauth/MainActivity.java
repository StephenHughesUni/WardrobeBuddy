package com.example.wardrobebuddy.com.firebaseauth;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.Manifest;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

public class MainActivity extends AppCompatActivity implements ScannedItemsAdapter.OnItemDeleteClickListener {

    FirebaseAuth auth;
    FirebaseUser user;

    // Request code for selecting an image from the gallery
    private static final int PICK_IMAGE_REQUEST = 104;
    private RecyclerView recyclerView;
    private ScannedItemsAdapter adapter;
    private List<ScannedItem> scannedItems;

    private String selectedCategory;

    // Request codes for camera permission and capturing an image (commented for future use)
     private static final int REQUEST_CAMERA_PERMISSION = 201;
     private static final int REQUEST_IMAGE_CAPTURE = 102;

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
        adapter.setOnItemDeleteClickListener(this);

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

        Button btnOutfitCollection = findViewById(R.id.btnOutfitCollection);
        btnOutfitCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MyOutfitsActivity.class);
                startActivity(intent);
            }
        });


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
                selectedCategory = categories[which];
                showImageSourceDialog();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image Source");
        String[] imageSources = {"Camera", "Gallery"};
        builder.setItems(imageSources, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    // User chose Camera
                    // Check for camera permission and open the camera
                    checkCameraPermission();
                } else {
                    // User chose Gallery
                    openGallery(selectedCategory);
                }
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
                String prompt = "Analyze the clothing label image and identify the specific details listed below. The extracted information must be presented in the exact order and format as follows: 'Brand: [brand]', 'Size: [size]', 'Price: [price] EUR', 'Article Number: [article number]'. Use these guidelines very strictly:\n" +
                        "First please search for popular clothing tags online even if its old content and examine any information on the structure of them such as COS, ZARA, H&M. Example being GB will be on H&M labels while DX is on COS labels. COS usually also contains COS.com on the label sideways"+
                        "- Size: Determine the size that is prominently displayed on the label, which could be in bold, larger print, or within a box. Provide only this size.\n" +
                        "- Price: Locate the price in euros. Absent a clear indication of a sale, such as a sale sticker or strikethrough, list only the current euro price.\n" +
                        "- Article Number: Different brands have unique article number formats. For H&M, this is '1234567 003' which is 7 numbers a space and three colour code numbers '1234567 001', strictly only 7 digits then space then three digits. For Zara, it looks like '2398/028/800', with 4 digits a / then 3 digits / then 3 more digits for colour code in total 10 digits. Use these formats to identify the article number also remember the structure of each to determine the brand.\n" +
                        "- Brand: Based on the article number format and any visible logos or distinctive features of the label design, determine the brand. As we know H&M does not use slashes while Zara does\n" +
                        "The response must only contain these four pieces of information in the structure provided, ignoring all other data on the label, such as additional currencies or non-relevant numbers." +
                        "Only provide article number with 10 digits never more. Never more. Never more. Only 10. Stop giving me more than 10 digits. Zara will only contain slash's in its article code so if the item does not have slashes it wont be Zara";
                String response = openAIHelper.getAIResponse(prompt, base64Image);
                runOnUiThread(() -> {
                    if (response != null && !response.isEmpty()) {
                        // Split by line breaks to get each part of the response
                        String[] parts = response.split("\n");

                        // Temporary variables to hold each part
                        String brand = "", size = "", price = "", articleNumber = "";

                        // Flags to check if brand or article code was found
                        boolean brandFound = false, articleCodeFound = false;

                        // Iterate through each part to assign the values correctly based on a known identifier
                        for (String part : parts) {
                            if (part.startsWith("Brand:")) {
                                brand = part.split(":")[1].trim(); // Adjusted to trim whitespace
                                brandFound = true;
                            } else if (part.startsWith("Size:")) {
                                size = part.split(":")[1].trim();
                            } else if (part.startsWith("Price:")) {
                                price = part.split(":")[1].trim();
                            } else if (part.startsWith("Article Number:")) {
                                articleNumber = part.split(":")[1].trim();
                                articleCodeFound = true;
                            }
                        }

                        if (brandFound && articleCodeFound) {
                            String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                            String imageUriString = imageUri.toString();
                            ScannedItem newItem = new ScannedItem(imageUriString, brand, size, price, articleNumber, currentDateTime, selectedCategory);
                            saveItemToRealtimeDatabase(newItem, selectedCategory);
                            Toast.makeText(MainActivity.this, "Item added to database", Toast.LENGTH_LONG).show();
                        } else {
                            // Prompt user that it's not a proper image of a clothing tag
                            Toast.makeText(MainActivity.this, "Either brand or article code not found", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // Prompt user that it's not a proper image of a clothing tag
                        Toast.makeText(MainActivity.this, "Item is not a clothing tag", Toast.LENGTH_LONG).show();
                    }
                });
            }).start();
        } else {
            Toast.makeText(MainActivity.this, "Failed to encode image", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onItemDeleteClick(int position) {
        ScannedItem itemToDelete = scannedItems.get(position);
        deleteItemFromDatabase(itemToDelete, position);
    }

    private void deleteItemFromDatabase(ScannedItem item, int position) {
        // Specify the Firebase database instance with URL
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://finalyearprojectapp-29b81-default-rtdb.europe-west1.firebasedatabase.app");
        // Use the specified database instance to get a reference
        DatabaseReference ref = database.getReference("users").child(user.getUid()).child("scannedItems");

        // First, attempt to delete the ProductInfo with a callback to then delete the ScannedItem
        deleteProductInfoFromDatabase(item.getArticleNumber().replace("/", "_"), new Runnable() {
            @Override
            public void run() {
                // Once ProductInfo is successfully deleted, proceed to delete the ScannedItem
                ref.orderByChild("dateTimeScanned").equalTo(item.getDateTimeScanned()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            snapshot.getRef().removeValue(); // Deletes the ScannedItem
                            break; // Assuming dateTimeScanned is unique
                        }

                        // Update UI after deletion
                        scannedItems.remove(position);
                        adapter.notifyItemRemoved(position);
                        Toast.makeText(MainActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("MainActivity", "Deletion cancelled", databaseError.toException());
                    }
                });
            }
        });
    }

    // Modified to include the database instance with URL in deleteProductInfoFromDatabase method
    private void deleteProductInfoFromDatabase(String formattedArticleNumber, Runnable onSuccess) {
        // Specify the Firebase database instance with URL
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://finalyearprojectapp-29b81-default-rtdb.europe-west1.firebasedatabase.app");
        // Use the specified database instance to get a reference
        DatabaseReference productInfoRef = database.getReference("users")
                .child(user.getUid()).child("productInfo").child(formattedArticleNumber);

        productInfoRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d("MainActivity", "Product info deleted successfully");
                    onSuccess.run(); // Callback to indicate successful deletion
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Error deleting product info", e));
    }


    private void saveItemToRealtimeDatabase(ScannedItem item, String category) {
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
        itemMap.put("category", category); // Add the category to the map


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

package com.example.wardrobebuddy.com.firebaseauth;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ItemDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        // Extract the details passed through the Intent
        String brand = getIntent().getStringExtra("brand");
        String size = getIntent().getStringExtra("size");
        String price = getIntent().getStringExtra("price");
        String articleNumber = getIntent().getStringExtra("articleNumber");
        String dateTimeScanned = getIntent().getStringExtra("dateTimeScanned");

        // Assume you have TextViews for each detail in your layout
        ((TextView) findViewById(R.id.detail_brand)).setText(brand);
        ((TextView) findViewById(R.id.detail_size)).setText(size);
        ((TextView) findViewById(R.id.detail_price)).setText(price);
        ((TextView) findViewById(R.id.detail_articleNumber)).setText(articleNumber);
        ((TextView) findViewById(R.id.detail_dateTimeScanned)).setText(dateTimeScanned);
    }
}

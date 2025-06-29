package com.thanhhuyen.evocasaadmin;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set custom font for EVOCASA text
        TextView titleText = findViewById(R.id.titleText);
        Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/ZenOldMincho-Black.ttf");
        titleText.setTypeface(customFont);

        // Find the Products card view
        CardView productCard = findViewById(R.id.productCard);

        // Set click listener
        productCard.setOnClickListener(v -> {
            // Start ProductActivity
            Intent intent = new Intent(MainActivity.this, ProductActivity.class);
            startActivity(intent);
        });
    }
}
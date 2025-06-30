package com.thanhhuyen.evocasaadmin;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.cardview.widget.CardView;
import com.thanhhuyen.utils.AdminSessionManager;
import com.thanhhuyen.utils.FontUtils;

public class MainActivity extends AppCompatActivity {
    private TextView txtAdminName;
    private ImageView btnLogOut;
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
        txtAdminName = findViewById(R.id.txtAdminName);
        btnLogOut = findViewById(R.id.btnLogOut);
        AdminSessionManager session = new AdminSessionManager(this);
        txtAdminName.setTypeface(FontUtils.getItalic(this));
        txtAdminName.setText("Hi, " + capitalizeFirstLetter(session.getShortName()));

        btnLogOut.setOnClickListener(v -> {
            session.clearSession();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        // Find the Products card view
        CardView productCard = findViewById(R.id.productCard);

        // Set click listener
        productCard.setOnClickListener(v -> {
            // Start ProductActivity
            Intent intent = new Intent(MainActivity.this, ProductActivity.class);
            startActivity(intent);
        });

        //Find linearLayoutCustomer and set click listener
        View linearLayoutCustomer = findViewById(R.id.linearLayoutCustomer);
        linearLayoutCustomer.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CustomerManagementActivity.class);
            startActivity(intent);
        });

        //Find linearLayoutOrder and set click listener
        View linearLayoutOrder = findViewById(R.id.linearLayoutOrder);
        linearLayoutOrder.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OrderManagementActivity.class);
            startActivity(intent);
        });
    }
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) return "";
        return input.substring(0,1).toUpperCase() + input.substring(1).toLowerCase();
    }
}
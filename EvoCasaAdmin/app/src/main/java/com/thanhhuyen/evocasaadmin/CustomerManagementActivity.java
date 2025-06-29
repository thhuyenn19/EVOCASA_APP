package com.thanhhuyen.evocasaadmin;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.thanhhuyen.untils.FontUtils;

public class CustomerManagementActivity extends AppCompatActivity {

    private RecyclerView customerRecyclerView;
    private TextView txtTitle, txtSearchHint;
    private ImageView imgBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_management);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
        initViews();
    }

    private void initViews() {
        // Back button trong topbar
        imgBack = findViewById(R.id.imgBack);
        if (imgBack != null) imgBack.setOnClickListener(v -> finish());

        // Font setup
        txtTitle = findViewById(R.id.txtTitle);
        if (txtTitle != null) {
            FontUtils.setBoldFont(this, txtTitle);
        }
    }
}
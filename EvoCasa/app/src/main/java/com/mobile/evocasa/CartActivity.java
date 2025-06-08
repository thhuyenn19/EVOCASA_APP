package com.mobile.evocasa;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mobile.utils.FontUtils;

public class CartActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        // Gán sự kiện nút quay lại
        ImageView imgCartBack = findViewById(R.id.imgCartBack);
        if (imgCartBack != null) {
            imgCartBack.setOnClickListener(v -> finish()); // chỉ cần finish để quay lại HomeFragment trong NavBarActivity
        }

        // Set font cho tiêu đề
        TextView txtTitle = findViewById(R.id.txtTitle);
        if (txtTitle != null) {
            FontUtils.setZboldFont(this, txtTitle);
        }


    }
}
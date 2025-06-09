package com.mobile.evocasa;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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

        //Set font cho All products
        TextView txtAllProducts = findViewById(R.id.txtAllProducts);
        if (txtTitle != null) {
            FontUtils.setRegularFont(this, txtAllProducts);
        }

        //Set font cho footer
        TextView txtSubtotal = findViewById(R.id.txtSubtotal);
        if (txtTitle != null) {
            FontUtils.setRegularFont(this, txtSubtotal);
        }
        TextView txtSubtotalAmount = findViewById(R.id.txtSubtotalAmount);
        if (txtTitle != null) {
            FontUtils.setRegularFont(this, txtSubtotalAmount);
        }
        TextView txtUseVoucher = findViewById(R.id.txtUseVoucher);
        if (txtTitle != null) {
            FontUtils.setMediumFont(this, txtUseVoucher);
        }
        TextView txtTotalCart = findViewById(R.id.txtTotalCart);
        if (txtTitle != null) {
            FontUtils.setBoldFont(this, txtTotalCart);
        }
        TextView txtTotalCartAmount = findViewById(R.id.txtTotalCartAmount);
        if (txtTitle != null) {
            FontUtils.setBoldFont(this, txtTotalCartAmount);
        }



    }
}
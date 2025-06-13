package com.mobile.evocasa;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.adapters.CartProductAdapter;
import com.mobile.evocasa.payment.PaymentActivity;
import com.mobile.models.CartProduct;
import com.mobile.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;

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

        // Setup RecyclerView cho Cart Products
        RecyclerView recyclerViewCartProducts = findViewById(R.id.recyclerViewCartProduct);
        recyclerViewCartProducts.setLayoutManager(new LinearLayoutManager(this));

        List<CartProduct> cartProductList = new ArrayList<>();
        cartProductList.add(new CartProduct("Travertine Table Lamp", 3500, R.mipmap.ic_cart_product, 1));
        cartProductList.add(new CartProduct("Travertine Table Lamp", 3500, R.mipmap.ic_cart_product, 1));
        cartProductList.add(new CartProduct("Travertine Table Lamp", 3500, R.mipmap.ic_cart_product, 1));
        cartProductList.add(new CartProduct("Travertine Table Lamp", 3500, R.mipmap.ic_cart_product, 1));
        cartProductList.add(new CartProduct("Travertine Table Lamp", 3500, R.mipmap.ic_cart_product, 1));
        cartProductList.add(new CartProduct("Travertine Table Lamp", 3500, R.mipmap.ic_cart_product, 1));


        CartProductAdapter cartProductAdapter = new CartProductAdapter(cartProductList);
        recyclerViewCartProducts.setAdapter(cartProductAdapter);


        CheckBox checkboxAllProducts = findViewById(R.id.checkboxAllProducts);

        // Gán listener cho checkbox "All Products"
        checkboxAllProducts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Cập nhật trạng thái tất cả sản phẩm
            for (CartProduct product : cartProductList) {
                product.setSelected(isChecked);
            }
            // Thông báo adapter cập nhật UI
            cartProductAdapter.notifyDataSetChanged();
        });


        CheckBox checkboxAll = findViewById(R.id.checkboxAllProducts);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            checkboxAll.setButtonTintList(null);
        }



        // Nút "Check Out"
        Button btnCheckOut = findViewById(R.id.btnCheckOut);
        if (btnCheckOut != null) {
            btnCheckOut.setOnClickListener(v -> {
                Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
                startActivity(intent);
            });
        }



    }
}
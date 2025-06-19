package com.mobile.evocasa;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mobile.adapters.CartProductAdapter;
import com.mobile.evocasa.payment.PaymentActivity;
import com.mobile.models.CartProduct;
import com.mobile.utils.FontUtils;
import com.mobile.utils.UserSessionManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CartActivity extends AppCompatActivity {
    private static final String TAG = "CartActivity";

    private RecyclerView recyclerViewCartProducts;
    private List<CartProduct> cartProductList = new ArrayList<>();
    private List<CartProduct> selectedProducts = new ArrayList<>();
    private LinearLayout checkoutLayout;
    private TextView txtTotalCartAmount;
    private TextView txtSubtotalAmount;
    private CartProductAdapter cartProductAdapter;
    private CheckBox checkboxAllProducts;

    private LinearLayout emptyCartLayout;
    private ImageView imgEmpty;
    private TextView txtEmpty, txtEmptyDesc;
    private Button btnBackShop, btnCheckOut;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        setupRecyclerView();
        setupListeners();
        loadCartProducts();
    }

    private void initViews() {
        ImageView imgCartBack = findViewById(R.id.imgCartBack);
        if (imgCartBack != null) imgCartBack.setOnClickListener(v -> finish());

        // Font setup
        FontUtils.setZboldFont(this, findViewById(R.id.txtTitle));
        FontUtils.setRegularFont(this, findViewById(R.id.txtAllProducts));
        FontUtils.setRegularFont(this, findViewById(R.id.txtSubtotal));
        FontUtils.setRegularFont(this, findViewById(R.id.txtSubtotalAmount));
        FontUtils.setMediumFont(this, findViewById(R.id.txtUseVoucher));
        FontUtils.setBoldFont(this, findViewById(R.id.txtTotalCart));
        txtTotalCartAmount = findViewById(R.id.txtTotalCartAmount);
        txtSubtotalAmount = findViewById(R.id.txtSubtotalAmount);
        FontUtils.setBoldFont(this, txtTotalCartAmount);

        // Main views
        recyclerViewCartProducts = findViewById(R.id.recyclerViewCartProduct);
        checkoutLayout = findViewById(R.id.bg_total_cart);
        checkboxAllProducts = findViewById(R.id.checkboxAllProducts);


        // Empty cart views
        emptyCartLayout = findViewById(R.id.emptyCartLayout);
        imgEmpty = findViewById(R.id.imgCartEmpty);
        txtEmpty = findViewById(R.id.txtCartEmptyTitle);
        txtEmptyDesc = findViewById(R.id.txtCartDescription);
        btnBackShop = findViewById(R.id.btnBackShop);
        btnCheckOut = findViewById(R.id.btnCheckOut);

        // Initially hide checkout layout
        checkoutLayout.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        recyclerViewCartProducts.setLayoutManager(new LinearLayoutManager(this));

        cartProductAdapter = new CartProductAdapter(cartProductList, new CartProductAdapter.OnProductCheckedChangeListener() {
            @Override
            public void onCheckedChanged(List<CartProduct> selected) {
                selectedProducts = new ArrayList<>(selected);
                updateCheckboxAllProducts();
                updateCheckoutLayout();
            }

            @Override
            public void onCartUpdated() {
                Log.d(TAG, "Cart updated. Current size: " + cartProductList.size());

                // Cập nhật Firebase
                updateCartOnFirebase();

                if (cartProductList.isEmpty()) {
                    showEmptyCart();
                } else {
                    // Cập nhật lại danh sách selected products
                    selectedProducts.clear();
                    for (CartProduct p : cartProductList) {
                        if (p.isSelected()) {
                            selectedProducts.add(p);
                        }
                    }
                    updateCheckboxAllProducts();
                    updateCheckoutLayout();
                }
            }

            @Override
            public void onQuantityChanged(CartProduct product, int newQuantity) {
                Log.d(TAG, "Quantity changed for product: " + product.getId() + ", new quantity: " + newQuantity);
                // Cập nhật Firebase ngay lập tức khi quantity thay đổi
                updateSingleProductQuantityOnFirebase(product.getId(), newQuantity);
            }

            @Override
            public void onProductRemoved(String productId) {
                Log.d(TAG, "Product removed: " + productId);
                // Xóa sản phẩm khỏi Firebase
                removeProductFromFirebase(productId);
            }
        });

        recyclerViewCartProducts.setAdapter(cartProductAdapter);
    }

    private void setupListeners() {
        checkboxAllProducts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.d(TAG, "Select all checkbox changed: " + isChecked);

            for (CartProduct product : cartProductList) {
                product.setSelected(isChecked);
            }
            cartProductAdapter.notifyDataSetChanged();

            // Cập nhật selectedProducts
            selectedProducts.clear();
            if (isChecked) {
                selectedProducts.addAll(cartProductList);
            }
            updateCheckoutLayout();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            checkboxAllProducts.setButtonTintList(null);
        }
//        Chuyển mảng selectedProducts sang JSON bằng Gson, Đưa JSON đó vào Intent để truyền sang PaymentActivity
        Button btnCheckOut = findViewById(R.id.btnCheckOut);
        btnCheckOut.setOnClickListener(v -> {
            if (!selectedProducts.isEmpty()) {
                Log.d(TAG, "Selected cartPayment: " + new Gson().toJson(selectedProducts));
                Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
                intent.putExtra("cartPayment", new Gson().toJson(selectedProducts));
                startActivity(intent);
            }
        });

        btnBackShop.setOnClickListener(v -> finish());
    }

    private void updateCheckboxAllProducts() {
        if (cartProductList.isEmpty()) {
            checkboxAllProducts.setOnCheckedChangeListener(null);
            checkboxAllProducts.setChecked(false);
            checkboxAllProducts.setOnCheckedChangeListener((buttonView, isChecked) -> {
                for (CartProduct product : cartProductList) {
                    product.setSelected(isChecked);
                }
                cartProductAdapter.notifyDataSetChanged();
                selectedProducts.clear();
                if (isChecked) {
                    selectedProducts.addAll(cartProductList);
                }
                updateCheckoutLayout();
            });
            return;
        }

        boolean allSelected = true;
        for (CartProduct product : cartProductList) {
            if (!product.isSelected()) {
                allSelected = false;
                break;
            }
        }

        // Tạm thời remove listener để tránh trigger
        checkboxAllProducts.setOnCheckedChangeListener(null);
        checkboxAllProducts.setChecked(allSelected);

        // Restore listener
        checkboxAllProducts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for (CartProduct product : cartProductList) {
                product.setSelected(isChecked);
            }
            cartProductAdapter.notifyDataSetChanged();
            selectedProducts.clear();
            if (isChecked) {
                selectedProducts.addAll(cartProductList);
            }
            updateCheckoutLayout();
        });
    }

    private void updateCheckoutLayout() {
        if (selectedProducts.isEmpty()) {
            checkoutLayout.setVisibility(View.GONE);
        } else {
            checkoutLayout.setVisibility(View.VISIBLE);

            double subtotal = 0;
            for (CartProduct p : selectedProducts) {
                subtotal += p.getPrice() * p.getQuantity();
            }

            double discount = 0.0; // Tạm thời chưa có giảm giá
            double total = subtotal - discount;

            // Format số: ví dụ 3,500
            DecimalFormat formatter = new DecimalFormat("#,###");

            txtSubtotalAmount.setText("$" + formatter.format(subtotal));
            txtTotalCartAmount.setText("$" + formatter.format(total));
        }
    }


    private void showEmptyCart() {
        Log.d(TAG, "Showing empty cart");

        emptyCartLayout.setVisibility(View.VISIBLE);
        checkboxAllProducts.setVisibility(View.GONE);
        recyclerViewCartProducts.setVisibility(View.GONE);
        checkoutLayout.setVisibility(View.GONE);
    }

    private void showCartWithProducts() {
        Log.d(TAG, "Showing cart with products. Count: " + cartProductList.size());

        emptyCartLayout.setVisibility(View.GONE);
        checkboxAllProducts.setVisibility(View.VISIBLE);
        recyclerViewCartProducts.setVisibility(View.VISIBLE);

        // Notify adapter about data change
        cartProductAdapter.notifyDataSetChanged();

        // Update UI state
        updateCheckboxAllProducts();
        updateCheckoutLayout();
    }

    private void loadCartProducts() {
        Log.d(TAG, "Bắt đầu load cart products");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        UserSessionManager sessionManager = new UserSessionManager(this);
        String uid = sessionManager.getUid();

        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "UID null hoặc rỗng");
            showEmptyCart();
            return;
        }

        Log.d(TAG, "Loading cart for UID: " + uid);

        db.collection("Customers").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "Document snapshot exists: " + documentSnapshot.exists());

                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> cartList = (List<Map<String, Object>>) documentSnapshot.get("Cart");
                        Log.d(TAG, "Cart list: " + cartList);

                        if (cartList == null || cartList.isEmpty()) {
                            Log.d(TAG, "Cart trống, hiển thị empty layout");
                            showEmptyCart();
                            return;
                        }

                        // Clear existing data
                        cartProductList.clear();
                        selectedProducts.clear();

                        // Sử dụng AtomicInteger để đếm số lượng sản phẩm đã load
                        AtomicInteger loadedCount = new AtomicInteger(0);
                        int totalProducts = cartList.size();

                        for (Map<String, Object> item : cartList) {
                            String productId = (String) item.get("productId");
                            Long quantity = (Long) item.get("cartQuantity");

                            Log.d(TAG, "Loading product: " + productId + ", quantity: " + quantity);

                            if (productId != null) {
                                db.collection("Product").document(productId)
                                        .get()
                                        .addOnSuccessListener(productSnapshot -> {
                                            if (productSnapshot.exists()) {
                                                CartProduct cp = new CartProduct();
                                                cp.setId(productId);
                                                cp.setQuantity(quantity != null ? quantity.intValue() : 1);
                                                cp.setName(productSnapshot.getString("Name"));
                                                cp.setSelected(false); // Mặc định không chọn

                                                // Xử lý price an toàn
                                                Double price = productSnapshot.getDouble("Price");
                                                cp.setPrice(price != null ? price : 0.0);

                                                // Xử lý imageUrls an toàn
                                                String imageRaw = productSnapshot.getString("Image");
                                                List<String> imageUrls = new ArrayList<>();

                                                if (imageRaw != null && imageRaw.startsWith("[")) {
                                                    try {
                                                        imageUrls = new Gson().fromJson(imageRaw, new TypeToken<List<String>>(){}.getType());
                                                    } catch (Exception e) {
                                                        Log.e("CartActivity", "Lỗi parse JSON từ Image field", e);
                                                    }
                                                }
                                                cp.setImageUrls(imageUrls);

                                                // Add to list
                                                cartProductList.add(cp);

                                                // Kiểm tra xem đã load hết chưa
                                                if (loadedCount.incrementAndGet() == totalProducts) {
                                                    Log.d(TAG, "Đã load xong tất cả sản phẩm: " + cartProductList.size());

                                                    runOnUiThread(() -> {
                                                        if (!cartProductList.isEmpty()) {
                                                            showCartWithProducts();
                                                        } else {
                                                            showEmptyCart();
                                                        }
                                                    });
                                                }
                                            } else {
                                                Log.w(TAG, "Product không tồn tại: " + productId);
                                                // Vẫn tăng counter để tránh bị treo
                                                if (loadedCount.incrementAndGet() == totalProducts) {
                                                    runOnUiThread(() -> {
                                                        if (!cartProductList.isEmpty()) {
                                                            showCartWithProducts();
                                                        } else {
                                                            showEmptyCart();
                                                        }
                                                    });
                                                }
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Lỗi khi load product: " + productId, e);
                                            // Vẫn tăng counter để tránh bị treo
                                            if (loadedCount.incrementAndGet() == totalProducts) {
                                                runOnUiThread(() -> {
                                                    if (!cartProductList.isEmpty()) {
                                                        showCartWithProducts();
                                                    } else {
                                                        showEmptyCart();
                                                    }
                                                });
                                            }
                                        });
                            } else {
                                Log.w(TAG, "ProductId null trong cart item");
                                // Tăng counter cho item null
                                if (loadedCount.incrementAndGet() == totalProducts) {
                                    runOnUiThread(() -> {
                                        if (!cartProductList.isEmpty()) {
                                            showCartWithProducts();
                                        } else {
                                            showEmptyCart();
                                        }
                                    });
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "Document không tồn tại");
                        showEmptyCart();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi khi load cart", e);
                    showEmptyCart();
                });
    }

    private void updateCartOnFirebase() {
        UserSessionManager sessionManager = new UserSessionManager(this);
        String uid = sessionManager.getUid();

        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "Cannot update cart: UID is null or empty");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Chuyển đổi cartProductList thành format phù hợp với Firebase
        List<Map<String, Object>> cartList = new ArrayList<>();

        for (CartProduct product : cartProductList) {
            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("productId", product.getId());
            cartItem.put("cartQuantity", product.getQuantity());
            cartList.add(cartItem);
        }

        // Cập nhật lên Firebase
        db.collection("Customers").document(uid)
                .update("Cart", cartList)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Cart updated successfully on Firebase");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update cart on Firebase", e);
                });
    }

    private void updateSingleProductQuantityOnFirebase(String productId, int newQuantity) {
        UserSessionManager sessionManager = new UserSessionManager(this);
        String uid = sessionManager.getUid();

        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "Cannot update product quantity: UID is null or empty");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Lấy cart hiện tại và cập nhật quantity cho product cụ thể
        db.collection("Customers").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> cartList = (List<Map<String, Object>>) documentSnapshot.get("Cart");

                        if (cartList != null) {
                            // Tìm và cập nhật product
                            boolean found = false;
                            for (Map<String, Object> item : cartList) {
                                if (productId.equals(item.get("productId"))) {
                                    item.put("cartQuantity", newQuantity);
                                    found = true;
                                    break;
                                }
                            }

                            if (found) {
                                // Cập nhật lại cart với quantity mới
                                db.collection("Customers").document(uid)
                                        .update("Cart", cartList)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Product quantity updated successfully on Firebase");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to update product quantity on Firebase", e);
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get cart for quantity update", e);
                });
    }

    private void removeProductFromFirebase(String productId) {
        UserSessionManager sessionManager = new UserSessionManager(this);
        String uid = sessionManager.getUid();

        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "Cannot remove product: UID is null or empty");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Lấy cart hiện tại và xóa product
        db.collection("Customers").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> cartList = (List<Map<String, Object>>) documentSnapshot.get("Cart");

                        if (cartList != null) {
                            // Xóa product khỏi cart
                            cartList.removeIf(item -> productId.equals(item.get("productId")));

                            // Cập nhật lại cart
                            db.collection("Customers").document(uid)
                                    .update("Cart", cartList)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Product removed successfully from Firebase");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to remove product from Firebase", e);
                                    });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get cart for product removal", e);
                });
    }
}
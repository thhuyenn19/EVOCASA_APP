
package com.mobile.evocasa;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mobile.adapters.CartProductAdapter;
import com.mobile.adapters.VoucherAdapter;
import com.mobile.evocasa.payment.PaymentActivity;
import com.mobile.models.CartProduct;
import com.mobile.models.Voucher;
import com.mobile.utils.FontUtils;
import com.mobile.utils.UserSessionManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
    private TextView txtAllProducts;
    private View overlayBackground;

    private CartProductAdapter cartProductAdapter;
    private CheckBox checkboxAllProducts;

    private LinearLayout emptyCartLayout;
    private boolean isEditing = false;
    private ImageView imgEmpty;
    private TextView txtEmpty, txtEmptyDesc;
    private Button btnBackShop, btnCheckOut;

    private LinearLayout btnCartEdit;
    private LinearLayout editOptionsLayout;
    private LinearLayout layoutUseVoucher, voucherOptionsLayout;
    private RecyclerView recyclerVoucher;
    private Voucher selectedVoucher = null;
    private List<Voucher> allVouchers = new ArrayList<>();
    private TextView txtDeleteSelected;
    private TextView txtAddToWishlist, txtCancelEdit;

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
        txtAllProducts = findViewById(R.id.txtAllProducts);

        layoutUseVoucher = findViewById(R.id.layoutUseVoucher);
        voucherOptionsLayout = findViewById(R.id.voucherOptionsLayout);
        recyclerVoucher = findViewById(R.id.recyclerVoucher);
        overlayBackground = findViewById(R.id.overlayBackground);

        // Edit button and options layout
        btnCartEdit = findViewById(R.id.btnCartEdit);
        editOptionsLayout = findViewById(R.id.editOptionsLayout);
        txtDeleteSelected = findViewById(R.id.txtDeleteSelected);
        txtAddToWishlist = findViewById(R.id.txtAddToWishlist);
        txtCancelEdit = findViewById(R.id.txtCancelEdit);

        // Empty cart views
        emptyCartLayout = findViewById(R.id.emptyCartLayout);
        imgEmpty = findViewById(R.id.imgCartEmpty);
        txtEmpty = findViewById(R.id.txtCartEmptyTitle);
        txtEmptyDesc = findViewById(R.id.txtCartDescription);
        btnBackShop = findViewById(R.id.btnBackShop);
        btnCheckOut = findViewById(R.id.btnCheckOut);

        // Initially hide checkout layout
        checkoutLayout.setVisibility(View.GONE);
        btnCartEdit.setVisibility(View.GONE);
        editOptionsLayout.setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        // Initialize adapter here with proper callbacks
        cartProductAdapter = new CartProductAdapter(cartProductList, new CartProductAdapter.OnProductCheckedChangeListener() {
            @Override
            public void onCheckedChanged(List<CartProduct> selected) {
                // Use runOnUiThread to ensure UI updates happen on main thread
                runOnUiThread(() -> {
                    selectedProducts.clear();
                    selectedProducts.addAll(selected);
                    updateCheckboxAllProducts();
                    updateCheckoutLayout();
                    updateEditButtonVisibility();
                });
            }

            @Override
            public void onCartUpdated() {
                Log.d(TAG, "Cart updated. Current size: " + cartProductList.size());

                runOnUiThread(() -> {
                    // Update Firebase
                    updateCartOnFirebase();

                    if (cartProductList.isEmpty()) {
                        showEmptyCart();
                    } else {
                        // Update selected products list
                        selectedProducts.clear();
                        for (CartProduct p : cartProductList) {
                            if (p.isSelected()) {
                                selectedProducts.add(p);
                            }
                        }
                        updateCheckboxAllProducts();
                        updateCheckoutLayout();
                        updateEditButtonVisibility();
                    }
                });
            }

            @Override
            public void onQuantityChanged(CartProduct product, int newQuantity) {
                Log.d(TAG, "Quantity changed for product: " + product.getId() + ", new quantity: " + newQuantity);

                // Validate quantity first
                if (newQuantity <= 0) {
                    Log.w(TAG, "Invalid quantity: " + newQuantity + ". Removing product.");
                    onProductRemoved(product.getId());
                    return;
                }

                // Update local data first
                runOnUiThread(() -> {
                    // Find and update the product in the list
                    for (CartProduct p : cartProductList) {
                        if (p.getId().equals(product.getId())) {
                            p.setQuantity(newQuantity);
                            break;
                        }
                    }

                    // Update UI
                    updateCheckoutLayout();

                    // Update Firebase
                    updateSingleProductQuantityOnFirebase(product.getId(), newQuantity);
                });
            }

            @Override
            public void onProductRemoved(String productId) {
                Log.d(TAG, "Product removed: " + productId);

                runOnUiThread(() -> {
                    // Remove from local lists
                    cartProductList.removeIf(p -> p.getId().equals(productId));
                    selectedProducts.removeIf(p -> p.getId().equals(productId));

                    // Notify adapter
                    cartProductAdapter.notifyDataSetChanged();

                    // Update UI
                    updateCheckboxAllProducts();
                    updateCheckoutLayout();
                    updateEditButtonVisibility();

                    // Check if cart is empty
                    if (cartProductList.isEmpty()) {
                        showEmptyCart();
                    }

                    // Remove from Firebase
                    removeProductFromFirebase(productId);
                });
            }
        });

        recyclerViewCartProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCartProducts.setAdapter(cartProductAdapter);
    }

    private void setupListeners() {
        checkboxAllProducts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isEditing = false;
            for (CartProduct product : cartProductList) {
                product.setSelected(isChecked);
            }
            cartProductAdapter.notifyDataSetChanged();

            selectedProducts.clear();
            if (isChecked) {
                selectedProducts.addAll(cartProductList);
            }
            updateCheckoutLayout();
            updateEditButtonVisibility();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            checkboxAllProducts.setButtonTintList(null);
        }

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

        // Edit button listeners
        btnCartEdit.setOnClickListener(v -> {
            isEditing = true;
            editOptionsLayout.setVisibility(View.VISIBLE);
            checkoutLayout.setVisibility(View.GONE);
        });

        txtDeleteSelected.setOnClickListener(v -> {
            deleteSelectedProducts();
            editOptionsLayout.setVisibility(View.GONE);
            isEditing = false;
            updateCheckoutLayout();
        });

        txtAddToWishlist.setOnClickListener(v -> {
            addSelectedToWishlist();
            editOptionsLayout.setVisibility(View.GONE);
            isEditing = false;
            updateCheckoutLayout();
        });

        txtCancelEdit.setOnClickListener(v -> {
            isEditing = false;
            editOptionsLayout.setVisibility(View.GONE);
            updateCheckoutLayout();
        });

        // Voucher layout listeners
        voucherOptionsLayout.setOnClickListener(v -> {});

        ImageView btnCloseVoucherLayout = findViewById(R.id.btnCloseVoucherLayout);
        btnCloseVoucherLayout.setOnClickListener(v -> {
            voucherOptionsLayout.setVisibility(View.GONE);
            overlayBackground.setVisibility(View.GONE);
            updateCheckoutLayout();
        });

        layoutUseVoucher.setOnClickListener(v -> {
            if (voucherOptionsLayout.getVisibility() == View.VISIBLE) {
                voucherOptionsLayout.setVisibility(View.GONE);
                overlayBackground.setVisibility(View.GONE);
                updateCheckoutLayout();
            } else {
                overlayBackground.setVisibility(View.VISIBLE);
                voucherOptionsLayout.setVisibility(View.VISIBLE);
                checkoutLayout.setVisibility(View.GONE);
                loadAndDisplayVouchers();
            }
        });

        overlayBackground.setOnClickListener(v -> {
            voucherOptionsLayout.setVisibility(View.GONE);
            overlayBackground.setVisibility(View.GONE);
            updateCheckoutLayout();
        });
    }

    // Reset voucher when conditions change
    private void resetVoucherIfInvalid() {
        if (selectedVoucher != null) {
            double currentSubtotal = calculateSubtotal();
            if (!selectedVoucher.isValid(currentSubtotal)) {
                selectedVoucher = null;
                Log.d(TAG, "Voucher reset because it's no longer valid");
            }
        }
    }

    private void loadAndDisplayVouchers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        UserSessionManager sessionManager = new UserSessionManager(this);
        String uid = sessionManager.getUid();

        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "Cannot load vouchers: UID is null or empty");
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Customers").document(uid)
                .get()
                .addOnSuccessListener(customerDoc -> {
                    if (!customerDoc.exists()) {
                        Log.d(TAG, "Customer document not found");
                        displayEmptyVoucherList();
                        return;
                    }

                    List<Map<String, Object>> customerVouchers = (List<Map<String, Object>>) customerDoc.get("Voucher");

                    if (customerVouchers == null || customerVouchers.isEmpty()) {
                        Log.d(TAG, "Customer has no vouchers");
                        displayEmptyVoucherList();
                        return;
                    }

                    List<String> voucherIds = new ArrayList<>();
                    for (Map<String, Object> voucherItem : customerVouchers) {
                        String voucherId = (String) voucherItem.get("VoucherId");
                        if (voucherId != null && !voucherId.isEmpty()) {
                            voucherIds.add(voucherId);
                        }
                    }

                    if (voucherIds.isEmpty()) {
                        Log.d(TAG, "No valid voucher IDs found");
                        displayEmptyVoucherList();
                        return;
                    }

                    loadVoucherDetails(voucherIds);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load customer vouchers", e);
                    Toast.makeText(this, "Failed to load vouchers", Toast.LENGTH_SHORT).show();
                    displayEmptyVoucherList();
                });
    }

    private void displayEmptyVoucherList() {
        VoucherAdapter adapter = new VoucherAdapter(new ArrayList<>(), calculateSubtotal(), voucher -> {
            selectedVoucher = voucher;
            updateCheckoutLayout();
            voucherOptionsLayout.setVisibility(View.GONE);
            overlayBackground.setVisibility(View.GONE);
        });
        runOnUiThread(() -> {
            recyclerVoucher.setLayoutManager(new LinearLayoutManager(this));
            recyclerVoucher.setAdapter(adapter);
        });
    }

    private void loadVoucherDetails(List<String> voucherIds) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Voucher> customerVouchers = new ArrayList<>();
        AtomicInteger loadedCount = new AtomicInteger(0);
        int totalVouchers = voucherIds.size();

        for (String voucherId : voucherIds) {
            db.collection("Voucher").document(voucherId)
                    .get()
                    .addOnSuccessListener(voucherDoc -> {
                        if (voucherDoc.exists()) {
                            Voucher voucher = voucherDoc.toObject(Voucher.class);
                            if (voucher != null) {
                                voucher.setId(voucherDoc.getId());
                                customerVouchers.add(voucher);
                                Log.d(TAG, "Loaded voucher: " + voucher.getName());
                            }
                        } else {
                            Log.w(TAG, "Voucher not found: " + voucherId);
                        }

                        if (loadedCount.incrementAndGet() == totalVouchers) {
                            displayVouchers(customerVouchers);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load voucher: " + voucherId, e);
                        if (loadedCount.incrementAndGet() == totalVouchers) {
                            displayVouchers(customerVouchers);
                        }
                    });
        }
    }

    private void displayVouchers(List<Voucher> vouchers) {
        runOnUiThread(() -> {
            double subtotal = calculateSubtotal();
            List<Voucher> validVouchers = new ArrayList<>();
            long currentTime = System.currentTimeMillis();

            for (Voucher voucher : vouchers) {
                if (voucher.getExpireDate() != null &&
                        voucher.getExpireDate().toDate().getTime() > currentTime) {
                    validVouchers.add(voucher);
                } else {
                    Log.d(TAG, "Filtered out expired voucher: " + voucher.getName());
                }
            }

            Log.d(TAG, "Displaying " + validVouchers.size() + " valid vouchers out of " + vouchers.size() + " total");

            VoucherAdapter adapter = new VoucherAdapter(validVouchers, subtotal, voucher -> {
                selectedVoucher = voucher;
                updateCheckoutLayout();
                Log.d(TAG, "Voucher selected: " + voucher.getName());
            }, selectedVoucher);

            recyclerVoucher.setLayoutManager(new LinearLayoutManager(this));
            recyclerVoucher.setAdapter(adapter);

            if (validVouchers.isEmpty()) {
                Toast.makeText(this, "No available vouchers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCheckoutLayout() {
        if (isEditing) {
            checkoutLayout.setVisibility(View.GONE);
            return;
        }

        if (selectedProducts.isEmpty()) {
            checkoutLayout.setVisibility(View.GONE);
            selectedVoucher = null;
        } else {
            checkoutLayout.setVisibility(View.VISIBLE);

            double subtotal = calculateSubtotal();
            resetVoucherIfInvalid();

            double discount = 0.0;
            if (selectedVoucher != null && selectedVoucher.isValid(subtotal)) {
                discount = subtotal * (selectedVoucher.getDiscountPercent() / 100.0);
                if (discount > selectedVoucher.getMaxDiscount()) {
                    discount = selectedVoucher.getMaxDiscount();
                }
            }
            double total = subtotal - discount;

            txtSubtotalAmount.setText("$" + String.format("%.2f", subtotal));
            txtTotalCartAmount.setText("$" + String.format("%.2f", total));

            updateVoucherDisplayText();
        }
    }

    private void updateVoucherDisplayText() {
        TextView txtUseVoucher = findViewById(R.id.txtUseVoucher);
        if (selectedVoucher != null) {
            txtUseVoucher.setText("Voucher: " + selectedVoucher.getName());
            txtUseVoucher.setTextColor(getResources().getColor(R.color.color_FF6600));
        } else {
            txtUseVoucher.setText(getString(R.string.title_use_voucher));
            txtUseVoucher.setTextColor(getResources().getColor(R.color.color_3F2305));
        }
    }

    private double calculateSubtotal() {
        double subtotal = 0;
        for (CartProduct p : selectedProducts) {
            subtotal += p.getPrice() * p.getQuantity();
        }
        return subtotal;
    }

    private void updateEditButtonVisibility() {
        Log.d("CartActivity", "selectedProducts size: " + selectedProducts.size());
        if (selectedProducts.isEmpty()) {
            btnCartEdit.setVisibility(View.GONE);
            editOptionsLayout.setVisibility(View.GONE);
            isEditing = false;
            updateCheckoutLayout();
        } else {
            btnCartEdit.setVisibility(View.VISIBLE);
        }
    }

    private void deleteSelectedProducts() {
        if (selectedProducts.isEmpty()) return;

        List<String> productIdsToRemove = new ArrayList<>();
        for (CartProduct product : selectedProducts) {
            productIdsToRemove.add(product.getId());
        }

        // Remove from local list
        cartProductList.removeAll(selectedProducts);
        selectedProducts.clear();

        // Update adapter
        cartProductAdapter.notifyDataSetChanged();

        // Remove from Firebase
        for (String productId : productIdsToRemove) {
            removeProductFromFirebase(productId);
        }

        // Update UI
        updateCheckboxAllProducts();
        updateCheckoutLayout();
        updateEditButtonVisibility();

        // Check if cart is empty
        if (cartProductList.isEmpty()) {
            showEmptyCart();
        }

        if (productIdsToRemove.size() > 0) {
            Toast.makeText(this,
                    "Deleted " + productIdsToRemove.size() + " selected product(s)!",
                    Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, "Deleted " + productIdsToRemove.size() + " selected products");
    }

    private void addSelectedToWishlist() {
        if (selectedProducts.isEmpty()) {
            Toast.makeText(this, "No products selected", Toast.LENGTH_SHORT).show();
            return;
        }

        UserSessionManager sessionManager = new UserSessionManager(this);
        String uid = sessionManager.getUid();

        if (uid == null || uid.isEmpty()) {
            Log.e(TAG, "Cannot add to wishlist: UID is null or empty");
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Wishlist")
                .whereEqualTo("Customer_id", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> currentWishlist = new ArrayList<>();
                    String wishlistDocId = null;

                    if (!querySnapshot.isEmpty()) {
                        for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            wishlistDocId = doc.getId();
                            List<String> existingProductIds = (List<String>) doc.get("Productid");
                            if (existingProductIds != null) {
                                currentWishlist.addAll(existingProductIds);
                            }
                            break;
                        }
                    }

                    List<String> productsToAdd = new ArrayList<>();
                    int duplicateCount = 0;

                    for (CartProduct product : selectedProducts) {
                        if (!currentWishlist.contains(product.getId())) {
                            currentWishlist.add(product.getId());
                            productsToAdd.add(product.getId());
                        } else {
                            duplicateCount++;
                        }
                    }

                    if (productsToAdd.isEmpty()) {
                        Log.d(TAG, "All selected products are already in wishlist");
                        Toast.makeText(CartActivity.this,
                                "All selected products are already in wishlist",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (wishlistDocId != null) {
                        int finalDuplicateCount = duplicateCount;
                        db.collection("Wishlist").document(wishlistDocId)
                                .update("Productid", currentWishlist)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Added " + productsToAdd.size() + " product(s) to existing wishlist");
                                    String message = productsToAdd.size() + " product(s) added to wishlist";
                                    if (finalDuplicateCount > 0) {
                                        message += " (" + finalDuplicateCount + " already existed)";
                                    }
                                    Toast.makeText(CartActivity.this, message, Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to update wishlist", e);
                                    Toast.makeText(CartActivity.this,
                                            "Failed to add to wishlist",
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Map<String, Object> newWishlist = new HashMap<>();
                        newWishlist.put("Customer_id", uid);
                        newWishlist.put("Productid", currentWishlist);
                        newWishlist.put("CreatedAt", Timestamp.now());

                        int finalDuplicateCount1 = duplicateCount;
                        db.collection("Wishlist")
                                .add(newWishlist)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d(TAG, "Created new wishlist with " + productsToAdd.size() + " products");
                                    String message = productsToAdd.size() + " product(s) added to wishlist";
                                    if (finalDuplicateCount1 > 0) {
                                        message += " (" + finalDuplicateCount1 + " already existed)";
                                    }
                                    Toast.makeText(CartActivity.this, message, Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to create wishlist", e);
                                    Toast.makeText(CartActivity.this,
                                            "Failed to add to wishlist",
                                            Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get wishlist", e);
                    Toast.makeText(CartActivity.this,
                            "Failed to connect to server",
                            Toast.LENGTH_SHORT).show();
                });
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
                updateEditButtonVisibility();
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

        checkboxAllProducts.setOnCheckedChangeListener(null);
        checkboxAllProducts.setChecked(allSelected);

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
            updateEditButtonVisibility();
        });
    }

    private void showEmptyCart() {
        Log.d(TAG, "Showing empty cart");
        runOnUiThread(() -> {
            emptyCartLayout.setVisibility(View.VISIBLE);
            txtAllProducts.setVisibility(View.GONE);
            checkboxAllProducts.setVisibility(View.GONE);
            recyclerViewCartProducts.setVisibility(View.GONE);
            checkoutLayout.setVisibility(View.GONE);
            btnCartEdit.setVisibility(View.GONE);
            editOptionsLayout.setVisibility(View.GONE);
        });
    }

    private void showCartWithProducts() {
        Log.d(TAG, "Showing cart with products. Count: " + cartProductList.size());
        runOnUiThread(() -> {
            emptyCartLayout.setVisibility(View.GONE);
            checkboxAllProducts.setVisibility(View.VISIBLE);
            txtAllProducts.setVisibility(View.VISIBLE);
            recyclerViewCartProducts.setVisibility(View.VISIBLE);

            cartProductAdapter.notifyDataSetChanged();
            updateCheckboxAllProducts();
            updateCheckoutLayout();
        });
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
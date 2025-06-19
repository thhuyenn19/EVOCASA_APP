package com.mobile.evocasa;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mobile.models.OrderGroup;
import com.mobile.models.OrderItem;
import com.mobile.utils.CustomTypefaceSpan;
import com.mobile.utils.FontUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LeaveReviewActivity extends AppCompatActivity {
    private LinearLayout itemContainer;
    private TextView txtTotal, btnViewMore;
    private AppCompatButton btnAction;
    private LinearLayout btnViewMoreContainer;
    private ImageView iconArrow;
    private boolean isExpanded = false;

    private OrderGroup orderGroup;
    private String orderId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_review);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        applyCustomFonts();

        // Lấy các view con từ layout include
        View orderGroupView = findViewById(R.id.orderGroupReview);
        itemContainer = orderGroupView.findViewById(R.id.itemContainer);
        txtTotal = orderGroupView.findViewById(R.id.txtTotal);
        btnAction = orderGroupView.findViewById(R.id.btnAction);
        btnViewMoreContainer = orderGroupView.findViewById(R.id.btnViewMoreContainer);
        btnViewMore = orderGroupView.findViewById(R.id.btnViewMore);
        iconArrow = orderGroupView.findViewById(R.id.iconArrow);

        LinearLayout btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v ->{
            finish();
        });
        orderId = getIntent().getStringExtra("orderId");
        if (orderId != null) {
            loadOrderFromFirestore(orderId);
        }
    }

    private void loadOrderFromFirestore(String orderId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Product")
                .get()
                .addOnSuccessListener(productSnapshots -> {
                    Map<String, String> productNameMap = new HashMap<>();
                    Map<String, Long> productPriceMap = new HashMap<>();
                    Map<String, String> productImageMap = new HashMap<>();

                    for (QueryDocumentSnapshot doc : productSnapshots) {
                        String id = doc.getId();
                        String name = doc.getString("Name");
                        Long price = doc.getLong("Price");

                        Object rawImageData = doc.get("Image");
                        List<String> imageList = new ArrayList<>();

                        if (rawImageData instanceof List) {
                            imageList = (List<String>) rawImageData;
                        } else if (rawImageData instanceof String) {
                            try {
                                imageList = new com.google.gson.Gson().fromJson((String) rawImageData, new com.google.gson.reflect.TypeToken<List<String>>(){}.getType());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (id != null && name != null && price != null) {
                            productNameMap.put(id, name);
                            productPriceMap.put(id, price);
                            if (!imageList.isEmpty()) {
                                productImageMap.put(id, imageList.get(0));
                            }
                        }
                    }

                    db.collection("Order").document(orderId)
                            .get()
                            .addOnSuccessListener(orderDoc -> {
                                if (orderDoc.exists()) {
                                    String status = orderDoc.getString("Status");
                                    Long totalPrice = orderDoc.getLong("TotalPrice");
                                    Map<String, Object> orderProduct = (Map<String, Object>) orderDoc.get("OrderProduct");

                                    if (orderProduct != null && orderProduct.get("id") != null) {
                                        Map<String, Object> productIdMap = (Map<String, Object>) orderProduct.get("id");
                                        String productId = (String) productIdMap.get("$oid");

                                        if (productId != null && productNameMap.containsKey(productId)) {
                                            String productName = productNameMap.get(productId);
                                            Long priceEach = productPriceMap.get(productId);
                                            Long quantity = (Long) orderProduct.get("Quantity");
                                            String imageUrl = productImageMap.get(productId);

                                            int qty = quantity != null ? quantity.intValue() : 1;
                                            int unitPrice = priceEach != null ? priceEach.intValue() : 0;

                                            OrderItem item = new OrderItem(
                                                    imageUrl,
                                                    productName,
                                                    unitPrice,
                                                    qty
                                            );

                                            List<OrderItem> itemList = new ArrayList<>();
                                            itemList.add(item);

                                            orderGroup = new OrderGroup(status, itemList);
                                            orderGroup.setExpanded(isExpanded);
                                            orderGroup.setTotal(totalPrice != null ? totalPrice : 0);

                                            renderOrderGroup(orderGroup);
                                        }
                                    }
                                }
                            });
                });
    }

    private void renderOrderGroup(OrderGroup group) {
        itemContainer.removeAllViews();
        List<OrderItem> items = group.getItems();

        int showCount = group.isExpanded() ? items.size() : Math.min(1, items.size());

        for (int i = 0; i < showCount; i++) {
            OrderItem item = items.get(i);
            View productView = LayoutInflater.from(this).inflate(R.layout.item_order_product, itemContainer, false);

            ImageView img = productView.findViewById(R.id.imgProduct);
            TextView title = productView.findViewById(R.id.txtTitle);
            TextView price = productView.findViewById(R.id.txtPrice);
            TextView qty = productView.findViewById(R.id.txtQuantity);

            Glide.with(this)
                    .load(item.getImageUrl())
                    .placeholder(R.mipmap.ic_cart_product)
                    .into(img);

            title.setText(item.getTitle());
            price.setText("$" + NumberFormat.getNumberInstance(Locale.US).format(item.getPrice()));
            qty.setText("Quantity: " + item.getQuantity());

            FontUtils.setZboldFont(this, title);
            FontUtils.setZboldFont(this, price);
            FontUtils.setRegularFont(this, qty);

            itemContainer.addView(productView);
        }

        String boldPart = "Total";
        String normalPart = " (" + items.size() + " items): $" + NumberFormat.getNumberInstance(Locale.US).format(group.getTotal());
        String fullText = boldPart + normalPart;

        SpannableString spannable = new SpannableString(fullText);
        Typeface boldFont = FontUtils.getSemiBold(this);
        Typeface regularFont = FontUtils.getRegular(this);
        spannable.setSpan(new CustomTypefaceSpan(boldFont), 0, boldPart.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new CustomTypefaceSpan(regularFont), boldPart.length(), fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtTotal.setText(spannable);

        if (items.size() > 1) {
            btnViewMoreContainer.setVisibility(View.VISIBLE);
            btnViewMore.setText(group.isExpanded() ? "View Less" : "View More");
            FontUtils.setMediumFont(this, btnViewMore);
            iconArrow.setRotation(group.isExpanded() ? 270 : 90);

            btnViewMoreContainer.setOnClickListener(v -> {
                isExpanded = !group.isExpanded();
                group.setExpanded(isExpanded);
                renderOrderGroup(group);
            });
        } else {
            btnViewMoreContainer.setVisibility(View.GONE);
        }

        btnAction.setText("Buy Again");
        btnAction.setEnabled(true);
        btnAction.setOnClickListener(v -> {
            // TODO: xử lý mua lại
        });
    }

    private void applyCustomFonts() {
        int[] textViewIds = {
                R.id.txtLeaveComment,
                R.id.txtAddPhotos,
                R.id.txtHowYourOrder
        };
        for (int id : textViewIds) {
            TextView textView = findViewById(id);
            if (textView != null) {
                FontUtils.setSemiBoldFont(this, textView);
            }
        }
        int[] textViewRegularIds = {
                R.id.txtYourOverallRating,
        };
        for (int id : textViewRegularIds) {
            TextView textView = findViewById(id);
            if (textView != null) {
                FontUtils.setRegularFont(this, textView);
            }
        }

    }
}
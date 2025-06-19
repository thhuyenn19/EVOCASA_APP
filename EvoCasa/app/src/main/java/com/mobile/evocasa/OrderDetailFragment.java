package com.mobile.evocasa;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mobile.models.OrderGroup;
import com.mobile.models.OrderItem;
import com.mobile.utils.CustomTypefaceSpan;
import com.mobile.utils.FontUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OrderDetailFragment extends Fragment {
    private LinearLayout itemContainer;
    private TextView txtTotalSummary;
    private LinearLayout btnViewMoreContainer;
    private TextView btnViewMore;
    private ImageView iconArrow;
    private boolean isExpanded = false;

    private Button btnTrackOrder;


    public OrderDetailFragment() {
        // Required empty public constructor
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout đã tạo (fragment_order_detail.xml)
        return inflater.inflate(R.layout.fragment_order_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context ctx = requireContext();
        // 1. Bind các view liên quan đến Order Details
        View orderGroupView = view.findViewById(R.id.orderGroupRoot);
        // Trong fragment_order_detail.xml, phần Order Details group chính là một LinearLayout,
        // bạn có thể wrap nó với id="orderGroupRoot" hoặc bind trực tiếp:
        itemContainer         = view.findViewById(R.id.itemContainer);
        txtTotalSummary       = view.findViewById(R.id.txtTotalSummary);
        btnViewMoreContainer  = view.findViewById(R.id.btnViewMoreContainer);
        btnViewMore           = view.findViewById(R.id.btnViewMore);
        iconArrow             = view.findViewById(R.id.iconArrow);
        btnTrackOrder = view.findViewById(R.id.btnTrackOrders);

        String orderId = getArguments() != null ? getArguments().getString("orderId") : null;
        if (orderId != null) {
            loadOrderDetailFromFirestore(orderId);
        }
        btnTrackOrder.setOnClickListener(v -> {
            Intent it = new Intent(getActivity(), TrackOrderActivity.class);
            it.putExtra("orderId", orderId);
            startActivity(it);
        });

        int[] boldTextIds = {
                R.id.txtShippingInfoLabel,
                R.id.txtOrderDetailsLabel,
                R.id.txtPaymentDetails
        };

        // Lặp và setTypeface cho từng cái
        Typeface boldFont = FontUtils.getBold(ctx);
        for (int id : boldTextIds) {
            TextView tv = view.findViewById(id);
            if (tv != null) {
                tv.setTypeface(boldFont);
            }
        }
        int[] sboldTextIds = {
                R.id.txtShippingMethod,
                R.id.txtPaymentMethod,
                R.id.txtMessageForShopLabel
        };

        // Lặp và setTypeface cho từng cái
        Typeface sboldFont = FontUtils.getBold(ctx);
        for (int id : sboldTextIds) {
            TextView tv = view.findViewById(id);
            if (tv != null) {
                tv.setTypeface(sboldFont);
            }
        }
        int[] italicTextIds = {
                R.id.txtShippingMethodValue,
                R.id.txtPaymentMethodValue,
        };

        // Lặp và setTypeface cho từng cái
        Typeface italicFont = FontUtils.getItalic(ctx);
        for (int id : italicTextIds) {
            TextView tv = view.findViewById(id);
            if (tv != null) {
                tv.setTypeface(italicFont);
            }
        }
        int[] litalicTextIds = {
                R.id.txtMessageForShopValue,
        };

        // Lặp và setTypeface cho từng cái
        Typeface litalicFont = FontUtils.getItalic(ctx);
        for (int id : litalicTextIds) {
            TextView tv = view.findViewById(id);
            if (tv != null) {
                tv.setTypeface(litalicFont);
            }
        }
    }
    private void loadOrderDetailFromFirestore(String orderId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Product").get().addOnSuccessListener(productSnapshots -> {
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

            db.collection("Order").document(orderId).get().addOnSuccessListener(orderDoc -> {
                if (!orderDoc.exists()) return;

                try {
                    String status = orderDoc.getString("Status");
                    Long totalPrice = orderDoc.getLong("TotalPrice");
                    Map<String, Object> orderProduct = (Map<String, Object>) orderDoc.get("OrderProduct");

                    if (orderProduct == null || orderProduct.get("id") == null) return;

                    Map<String, Object> productIdMap = (Map<String, Object>) orderProduct.get("id");
                    String productId = (String) productIdMap.get("$oid");

                    if (productId == null || !productNameMap.containsKey(productId)) return;

                    String productName = productNameMap.get(productId);
                    Long priceEach = productPriceMap.get(productId);
                    String imageUrl = productImageMap.get(productId);
                    Long quantity = (Long) orderProduct.get("Quantity");

                    int qty = quantity != null ? quantity.intValue() : 1;
                    int unitPrice = priceEach != null ? priceEach.intValue() : 0;

                    OrderItem item = new OrderItem(imageUrl, productName, unitPrice, qty);
                    List<OrderItem> items = new ArrayList<>();
                    items.add(item);

                    OrderGroup group = new OrderGroup(status, items);
                    group.setOrderId(orderId);
                    group.setTotal(totalPrice != null ? totalPrice : 0);

                    renderOrderGroup(group);

                } catch (Exception e) {
                    Log.e("OrderDetailFragment", "Lỗi khi parse đơn hàng: " + e.getMessage());
                }
            });
        });
    }

    private void renderOrderGroup(OrderGroup group) {
        itemContainer.removeAllViews();
        List<OrderItem> items = group.getItems();
        int showCount = group.isExpanded() ? items.size() : Math.min(1, items.size());

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (int i = 0; i < showCount; i++) {
            OrderItem item = items.get(i);
            View productView = inflater.inflate(R.layout.item_order_product, itemContainer, false);

            ImageView img = productView.findViewById(R.id.imgProduct);
            TextView title = productView.findViewById(R.id.txtTitle);
            TextView price = productView.findViewById(R.id.txtPrice);
            TextView qty = productView.findViewById(R.id.txtQuantity);

            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(requireContext())
                        .load(item.getImageUrl())
                        .placeholder(R.mipmap.ic_cart_product)
                        .error(R.mipmap.ic_cart_product)
                        .into(img);
            } else {
                img.setImageResource(R.mipmap.ic_cart_product);
            }


            title.setText(item.getTitle());
            price.setText("$" + item.getPrice());
            qty.setText("Quantity: " + item.getQuantity());

            FontUtils.setZboldFont(getContext(), title);
            FontUtils.setZboldFont(getContext(), price);
            FontUtils.setRegularFont(getContext(), qty);

            itemContainer.addView(productView);
        }

        String boldPart = "Total";
        String normalPart = " (" + items.size() + " items): $" + group.getTotal();
        String fullText = boldPart + normalPart;
        SpannableString span = new SpannableString(fullText);
        span.setSpan(new CustomTypefaceSpan(FontUtils.getSemiBold(getContext())), 0, boldPart.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new CustomTypefaceSpan(FontUtils.getRegular(getContext())), boldPart.length(), fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtTotalSummary.setText(span);

        if (items.size() > 1) {
            btnViewMoreContainer.setVisibility(View.VISIBLE);
            btnViewMore.setText(group.isExpanded() ? "View Less" : "View More");
            iconArrow.setRotation(group.isExpanded() ? 270 : 90);
            btnViewMoreContainer.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                group.setExpanded(isExpanded);
                renderOrderGroup(group);
            });
        } else {
            btnViewMoreContainer.setVisibility(View.GONE);
        }
    }
}
package com.mobile.evocasa;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class OrderDetailFragment extends Fragment {
    private LinearLayout itemContainer;
    private TextView txtTotalSummary;
    private LinearLayout btnViewMoreContainer;
    private TextView btnViewMore;
    private ImageView iconArrow;
    private boolean isExpanded = false;

    private Button btnTrackOrder;
    private TextView txtShippingMethodValue,txtPaymentMethodValue,  txtMessageForShopValue;
    private TextView txtTotalPrice, txtShippingFee, txtDiscount, txtTotalPayment, txtYouSave;
    private TextView txtCustomerName, txtCustomerPhone, txtCustomerAddress;

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
        itemContainer = view.findViewById(R.id.itemContainer);
        txtTotalSummary = view.findViewById(R.id.txtTotalSummary);
        btnViewMoreContainer  = view.findViewById(R.id.btnViewMoreContainer);
        btnViewMore = view.findViewById(R.id.btnViewMore);
        iconArrow = view.findViewById(R.id.iconArrow);
        btnTrackOrder = view.findViewById(R.id.btnTrackOrders);

        txtShippingMethodValue = view.findViewById(R.id.txtShippingMethodValue);
        txtPaymentMethodValue = view.findViewById(R.id.txtPaymentMethodValue);
        txtMessageForShopValue = view.findViewById(R.id.txtMessageForShopValue);

        txtTotalPrice = view.findViewById(R.id.txtTotalPrice);
        txtShippingFee = view.findViewById(R.id.txtShippingFee);
        txtDiscount = view.findViewById(R.id.txtDiscount);
        txtTotalPayment = view.findViewById(R.id.txtTotalPayment);
        txtYouSave = view.findViewById(R.id.txtYouSave);

        txtCustomerName = view.findViewById(R.id.txtCustomerName);
        txtCustomerPhone = view.findViewById(R.id.txtCustomerPhone);
        txtCustomerAddress = view.findViewById(R.id.txtCustomerAddress);

        String orderId = getArguments() != null ? getArguments().getString("orderId") : null;
        if (orderId != null) {
            loadOrderDetailFromFirestore(orderId);
        }
        btnTrackOrder.setOnClickListener(v -> {
            Intent it = new Intent(getActivity(), TrackOrderActivity.class);
            it.putExtra("orderId", orderId);
            startActivity(it);
        });
        LinearLayout btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
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

        db.collection("Product").
                get().
                addOnSuccessListener(productSnapshots -> {
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
                Map<String, Object> shippingAddress = (Map<String, Object>) orderDoc.get("ShippingAddresses");
                if (shippingAddress != null) {
                    String name = (String) shippingAddress.get("Name");
                    String phone = (String) shippingAddress.get("Phone");
                    String address = (String) shippingAddress.get("Address");

                    txtCustomerName.setText(name != null ? name : "N/A");
                    txtCustomerPhone.setText(phone != null ? phone : "N/A");
                    txtCustomerAddress.setText(address != null ? address : "N/A");
                }

// NEW: Get shippingMethod, paymentMethod, and note from Firestore
                String shippingMethod = orderDoc.getString("ShippingMethod");
                String paymentMethod = orderDoc.getString("PaymentMethod");
                String note = orderDoc.getString("Note");

// Shipping Method
                if (shippingMethod != null && !shippingMethod.trim().isEmpty()) {
                    txtShippingMethodValue.setText(shippingMethod);
                } else {
                    txtShippingMethodValue.setText("N/A");
                }

// Payment Method
                if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
                    txtPaymentMethodValue.setText(paymentMethod);
                } else {
                    txtPaymentMethodValue.setText("N/A");
                }

// Note
                if (note != null && !note.trim().isEmpty()) {
                    txtMessageForShopValue.setText(note);
                } else {
                    txtMessageForShopValue.setText("No message");
                    txtMessageForShopValue.setTypeface(null, Typeface.ITALIC);
                    txtMessageForShopValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_5E4C3E));
                }

                try {
                    String status = orderDoc.getString("Status");

                    List<Map<String, Object>> orderProducts = (List<Map<String, Object>>) orderDoc.get("OrderProduct");
                    if (orderProducts == null || orderProducts.isEmpty()) return;
                    List<OrderItem> items = new ArrayList<>();
                    for (Map<String, Object> orderProduct : orderProducts) {
                        Map<String, Object> productIdMap = (Map<String, Object>) orderProduct.get("id");
                        if (productIdMap == null) continue;

                        String productId = (String) productIdMap.get("$oid");
                        if (productId == null || !productNameMap.containsKey(productId)) continue;

                        String productName = productNameMap.get(productId);
                        Long priceEach = productPriceMap.get(productId);
                        String imageUrl = productImageMap.get(productId);
                        Long quantity = (Long) orderProduct.get("Quantity");

                        int qty = quantity != null ? quantity.intValue() : 1;
                        int unitPrice = priceEach != null ? priceEach.intValue() : 0;

                        items.add(new OrderItem(imageUrl, productName, unitPrice, qty));
                    }
                    // ✅ Tính tổng giá sản phẩm
                    int totalProductPrice = 0;
                    for (OrderItem item : items) {
                        totalProductPrice += item.getPrice() * item.getQuantity();
                    }

// ✅ Delivery fee
                    int deliveryFee = 0;
                    Long rawDeliveryFee = orderDoc.getLong("DeliveryFee");
                    if (rawDeliveryFee != null) {
                        deliveryFee = rawDeliveryFee.intValue();
                    }

// ✅ Discount từ phần trăm
                    int discountPercent = 0;
                    Map<String, Object> voucher = (Map<String, Object>) orderDoc.get("Voucher");
                    if (voucher != null && voucher.get("DiscountPercent") != null) {
                        discountPercent = ((Long) voucher.get("DiscountPercent")).intValue();
                    }
                    int discountAmount = (totalProductPrice + deliveryFee) * discountPercent / 100;

// ✅ Tổng thanh toán cuối cùng
                    int finalTotal = totalProductPrice + deliveryFee - discountAmount;

// ✅ Set vào UI
                    txtTotalPrice.setText("$" +  NumberFormat.getNumberInstance(Locale.US).format(totalProductPrice));
                    txtShippingFee.setText("$" + deliveryFee);
                    TextView txtDiscount = requireView().findViewById(R.id.txtDiscount); // nếu đã có id
                    txtDiscount.setText("-$" +  NumberFormat.getNumberInstance(Locale.US).format(discountAmount));
                    TextView txtTotalPayment = requireView().findViewById(R.id.txtTotalPayment); // nếu đã có id
                    txtTotalPayment.setText("$" +  NumberFormat.getNumberInstance(Locale.US).format(finalTotal));
                    TextView txtYouSave = requireView().findViewById(R.id.txtYouSave); // nếu có
                    txtYouSave.setText("You save $" + discountAmount + " on this order");


                    // ✅ Tạo nhóm đơn hàng
                    OrderGroup group = new OrderGroup(status, items);
                    group.setOrderId(orderId);
                    group.setTotal(finalTotal); // dùng tổng đã tính thủ công


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
            price.setText("$" + NumberFormat.getNumberInstance(Locale.US).format(item.getPrice()));
            qty.setText("Quantity: " + item.getQuantity());

            FontUtils.setZboldFont(getContext(), title);
            FontUtils.setZboldFont(getContext(), price);
            FontUtils.setRegularFont(getContext(), qty);

            itemContainer.addView(productView);
        }
        int totalQuantity = 0;
        for (OrderItem item : items) {
            totalQuantity += item.getQuantity();
        }
        String boldPart = "Total";
        String normalPart = " (" + totalQuantity + " items): $" +NumberFormat.getNumberInstance(Locale.US).format(group.getTotal());
        String fullText = boldPart + normalPart;
        SpannableString span = new SpannableString(fullText);
        span.setSpan(new CustomTypefaceSpan(FontUtils.getSemiBold(getContext())), 0, boldPart.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new CustomTypefaceSpan(FontUtils.getRegular(getContext())), boldPart.length(), fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtTotalSummary.setText(span);

        if (items.size() > 1) {
            btnViewMoreContainer.setVisibility(View.VISIBLE);
            btnViewMore.setText(group.isExpanded() ? "View Less" : "View More");
            FontUtils.setMediumFont(getContext(), btnViewMore);
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
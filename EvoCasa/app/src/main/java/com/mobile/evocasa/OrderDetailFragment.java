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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mobile.models.OrderGroup;
import com.mobile.models.OrderItem;
import com.mobile.utils.CustomTypefaceSpan;
import com.mobile.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;


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

        // 2. Load mock data và render lần đầu
        OrderGroup mock = createMockOrderGroup();
        renderOrderGroup(mock);

        // 3. Toggle View More / View Less
        btnViewMoreContainer.setOnClickListener(v -> {
            isExpanded = !isExpanded;
            mock.setExpanded(isExpanded);
            renderOrderGroup(mock);
        });

        // 4. Bind nút Track Order
        btnTrackOrder = view.findViewById(R.id.btnTrackOrders);
        btnTrackOrder.setOnClickListener(v -> {
            Intent it = new Intent(getActivity(), TrackOrderActivity.class);
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

    private void renderOrderGroup(OrderGroup orderGroup) {
        itemContainer.removeAllViews();

        List<OrderItem> items = orderGroup.getItems();
        // Tính tổng full price
        int totalPrice = 0;
        for (OrderItem it : items) {
            totalPrice += it.getPrice() * it.getQuantity();
        }

        // Chọn số item hiển thị
        int showCount = orderGroup.isExpanded()
                ? items.size()
                : Math.min(1, items.size());

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < showCount; i++) {
            OrderItem item = items.get(i);
            View productView = inflater.inflate(
                    R.layout.item_order_product, itemContainer, false);

            ImageView img   = productView.findViewById(R.id.imgProduct);
            TextView title  = productView.findViewById(R.id.txtTitle);
            TextView price  = productView.findViewById(R.id.txtPrice);
            TextView qty    = productView.findViewById(R.id.txtQuantity);

            img.setImageResource(item.getImageResId());
            title.setText(item.getTitle());
            price.setText("$" + item.getPrice());
            qty.setText("Quantity: " + item.getQuantity());

            // Giữ font nhất quán với app
            FontUtils.setZboldFont(getContext(), title);
            FontUtils.setZboldFont(getContext(), price);
            FontUtils.setRegularFont(getContext(), qty);

            itemContainer.addView(productView);
        }
        itemContainer.requestLayout();
        itemContainer.invalidate();

        // Render total với Spannable (in đậm "Total")
        String boldPart = "Total";
        String normalPart = " (" + items.size() + " items): $" + totalPrice;
        String fullText = boldPart + normalPart;
        SpannableString span = new SpannableString(fullText);
        Typeface boldFont    = FontUtils.getSemiBold(getContext());
        Typeface regularFont = FontUtils.getRegular(getContext());
        span.setSpan(new CustomTypefaceSpan(boldFont),
                0, boldPart.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new CustomTypefaceSpan(regularFont),
                boldPart.length(), fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtTotalSummary.setText(span);

        // Show / hide View More
        if (items.size() > 1) {
            btnViewMoreContainer.setVisibility(View.VISIBLE);
            FontUtils.setMediumFont(getContext(), btnViewMore);
            btnViewMore.setText(orderGroup.isExpanded() ? "View Less" : "View More");
            iconArrow.setRotation(orderGroup.isExpanded() ? 270 : 90);
        } else {
            btnViewMoreContainer.setVisibility(View.GONE);
        }
    }

    private OrderGroup createMockOrderGroup() {
        List<OrderItem> list = new ArrayList<>();
        list.add(new OrderItem(R.mipmap.ic_cart_product, "Product A", 1000, 1));
        list.add(new OrderItem(R.mipmap.ic_cart_product, "Product B", 1500, 2));
        OrderGroup g = new OrderGroup("Pending", list);
        return g;
    }
}
package com.mobile.evocasa;

import android.os.Bundle;
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

import com.mobile.models.OrderGroup;
import com.mobile.models.OrderItem;
import com.mobile.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;

public class LeaveReviewActivity extends AppCompatActivity {
    private LinearLayout itemContainer;
    private TextView txtTotal, btnViewMore;
    private AppCompatButton btnAction;
    private LinearLayout btnViewMoreContainer;
    private boolean isExpanded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_review);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Ẩn thanh cuộn ScrollView
        ScrollView scrollView = findViewById(R.id.main).findViewById(R.id.scrollView);
        if (scrollView != null) {
            scrollView.setVerticalScrollBarEnabled(false);
        }
        applyCustomFonts();
        // Lấy các view con từ layout include
        View orderGroupView = findViewById(R.id.orderGroupReview);
        itemContainer = orderGroupView.findViewById(R.id.itemContainer);
        txtTotal = orderGroupView.findViewById(R.id.txtTotal);
        btnAction = orderGroupView.findViewById(R.id.btnAction);
        btnViewMoreContainer = orderGroupView.findViewById(R.id.btnViewMoreContainer);
        btnViewMore = orderGroupView.findViewById(R.id.btnViewMore);

        OrderGroup mockGroup = createMockOrderGroup();
        renderOrderGroup(mockGroup);
    }

    private void renderOrderGroup(OrderGroup orderGroup) {
        itemContainer.removeAllViews();
        int totalPrice = 0;

        List<OrderItem> items = orderGroup.getItems();
        List<OrderItem> showList = isExpanded ? items : items.subList(0, Math.min(1, items.size()));

        for (OrderItem item : showList) {
            totalPrice += item.getPrice() * item.getQuantity();

            View productView = LayoutInflater.from(this)
                    .inflate(R.layout.item_order_product, itemContainer, false);

            ImageView img = productView.findViewById(R.id.imgProduct);
            TextView title = productView.findViewById(R.id.txtTitle);
            TextView price = productView.findViewById(R.id.txtPrice);
            TextView qty = productView.findViewById(R.id.txtQuantity);

            img.setImageResource(item.getImageResId());
            title.setText(item.getTitle());
            price.setText("$" + item.getPrice());
            qty.setText("Quantity: " + item.getQuantity());

            FontUtils.setZboldFont(this, title);
            FontUtils.setZboldFont(this, price);
            FontUtils.setRegularFont(this, qty);

            itemContainer.addView(productView);
        }

        txtTotal.setText("Total (" + items.size() + " items): $" + totalPrice);

        // ✅ Show View More nếu có nhiều hơn 1 item và chưa mở rộng
        if (items.size() > 1 && !isExpanded) {
            btnViewMoreContainer.setVisibility(View.VISIBLE);
            FontUtils.setMediumFont(this, btnViewMore); // ✅ set font Medium
            btnViewMore.setOnClickListener(v -> {
                isExpanded = true;
                renderOrderGroup(orderGroup); // re-render
            });
        } else {
            btnViewMoreContainer.setVisibility(View.GONE);
        }

        // Nút hành động
        btnAction.setText("Buy Again");
        btnAction.setEnabled(true);
        btnAction.setOnClickListener(v -> {
            // TODO: xử lý mua lại
        });
    }

    private OrderGroup createMockOrderGroup() {
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem(R.mipmap.ic_cart_product,  "Product A", 1000, 1));
        items.add(new OrderItem(R.mipmap.ic_cart_product,  "Product B", 1000, 2));

        OrderGroup mock = new OrderGroup();
        mock.setItems(items);
        mock.setStatus("Completed");
        return mock;
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
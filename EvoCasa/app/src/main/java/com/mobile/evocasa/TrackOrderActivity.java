package com.mobile.evocasa;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.adapters.TimelineAdapter;
import com.mobile.models.EventItem;
import com.mobile.models.HeaderItem;
import com.mobile.models.TimelineItem;

import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;

public class TrackOrderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_order);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
// 1. Chuẩn bị dữ liệu timeline
        // 1. Chuẩn bị dữ liệu timeline
        List<TimelineItem> timelineItems = new ArrayList<>();
        timelineItems.add(new HeaderItem("23rd, 2025"));
        timelineItems.add(new EventItem("16:30", "Your order has arrived at the delivery station", true));
        timelineItems.add(new EventItem("09:30", "Your order has left the sorting facility", false));
        timelineItems.add(new HeaderItem("23rd, 2025"));
        timelineItems.add(new EventItem("16:30", "Your order has arrived at the delivery station", true));
        timelineItems.add(new EventItem("09:30", "Your order has left the sorting facility", false));
        // ... thêm các header và event khác tương tự

        // 2. Khởi tạo RecyclerView và Adapter
        RecyclerView rv = findViewById(R.id.rvTimeline);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new TimelineAdapter(this, timelineItems));

    }
}
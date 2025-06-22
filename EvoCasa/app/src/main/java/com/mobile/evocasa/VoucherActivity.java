package com.mobile.evocasa;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.mobile.adapters.VoucherProfileAdapter;
import com.mobile.models.Voucher;
import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class VoucherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_voucher);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup RecyclerView & Adapter
        RecyclerView recyclerView = findViewById(R.id.recyclerVoucher);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Voucher> sampleVouchers = createSampleVouchers();
        VoucherProfileAdapter adapter = new VoucherProfileAdapter(sampleVouchers, voucher -> {
            // TODO: xử lý khi click voucher
        });
        recyclerView.setAdapter(adapter);

        // Handle back button in topbar
        LinearLayout btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private List<Voucher> createSampleVouchers() {
        List<Voucher> list = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            Voucher v = new Voucher();
            v.setName("LOVEEVOCASA" + (i+1));
            v.setDiscountPercent(15);
            v.setMaxDiscount(50);
            v.setMinOrderValue(300);

            Calendar cal = Calendar.getInstance();
            cal.set(2025, Calendar.JUNE, 30);
            v.setExpireDate(new Timestamp(cal.getTime()));
            list.add(v);
        }
        return list;
    }
}
package com.mobile.evocasa.payment;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.mobile.evocasa.R;

public class PaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment); // layout chứa FrameLayout với id: fragment_container

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, new MainPaymentFragment());
            transaction.commit();
        }
        String cartJson = getIntent().getStringExtra("cartPayment");
        String voucherJson = getIntent().getStringExtra("selectedVoucher");

        Bundle args = new Bundle();
        args.putString("cartPayment", cartJson);
        if (voucherJson != null) {
            args.putString("selectedVoucher", voucherJson);
        }

        MainPaymentFragment fragment = new MainPaymentFragment();
        fragment.setArguments(args);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)  // fragment_container được định nghĩa trong activity_payment.xml:contentReference[oaicite:0]{index=0}
                .commit();
    }
}
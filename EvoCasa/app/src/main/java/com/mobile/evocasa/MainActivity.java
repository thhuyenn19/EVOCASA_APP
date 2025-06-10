package com.mobile.evocasa;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.mobile.evocasa.auth.SignInFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Lắng nghe WindowInsets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Kiểm tra xem lần đầu tiên Activity được khởi tạo
        if (savedInstanceState == null) {
            // Tạo một instance của SignInFragment
            SignInFragment signInFragment = new SignInFragment();

            // Bắt đầu một giao dịch Fragment
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // Thay thế fragment hiện tại bằng SignInFragment
            transaction.replace(R.id.fragment_container, signInFragment);
            // Thêm giao dịch vào back stack (nếu muốn quay lại trước đó)
            transaction.addToBackStack(null);
            // Cam kết giao dịch fragment
            transaction.commit();
        }
    }
}
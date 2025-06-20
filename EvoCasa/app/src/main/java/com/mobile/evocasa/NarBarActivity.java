package com.mobile.evocasa;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.mobile.evocasa.category.ShopFragment;
import com.mobile.evocasa.payment.FinishPaymentFragment;
import com.mobile.evocasa.profile.ProfileFragment;

public class NarBarActivity extends AppCompatActivity implements BottomNavFragment.OnBottomNavSelectedListener {

    private BottomNavFragment bottomNavFragment; // Khai báo như instance variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nar_bar);

        int tabPos = getIntent().getIntExtra("tab_pos", 0);

        // Load nội dung chính (fragment tương ứng)
        showFragment(tabPos);

        // Truyền tabPos vào BottomNavFragment và lưu reference
        bottomNavFragment = BottomNavFragment.newInstance(tabPos);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottom_nav_container, bottomNavFragment)
                .commit();

        // Tạo và hiển thị popup với callback
        PopupDialog popupDialog = new PopupDialog();
        popupDialog.setOnShopClickListener(new PopupDialog.OnShopClickListener() {
            @Override
            public void onShopClick() {
                // Chuyển đến tab Shop (position 1)
                navigateToShop();
            }
        });
        popupDialog.show(getSupportFragmentManager(), "popup");
    }

    private void navigateToShop() {
        // Chuyển đến ShopFragment
        showFragment(1);

        // Cập nhật bottom navigation để highlight tab Shop
        if (bottomNavFragment != null) {
            bottomNavFragment.setSelectedPosition(1);
        }
    }

    @Override
    public void onBottomNavSelected(int position) {
        showFragment(position);
    }

    private void showFragment(int pos) {
        Fragment frag;
        switch (pos) {
            case 1:
                frag = new ShopFragment();
                break;
            case 2:
                frag = new NotificationFragment();
                break;
            case 3:
                frag = new ProfileFragment();
                break;
            case 4:
                frag = new FinishPaymentFragment();
                break;
            default:
                frag = new HomeFragment();
                break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, frag)
                .commit();
    }

}
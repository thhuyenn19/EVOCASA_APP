package com.mobile.evocasa;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mobile.adapters.CategoryShopAdapter;
import com.mobile.evocasa.category.ProductPreloadManager;
import com.mobile.evocasa.category.ShopFragment;
import com.mobile.evocasa.order.OrderDetailFragment;
import com.mobile.evocasa.payment.FinishPaymentFragment;
import com.mobile.evocasa.profile.ProfileFragment;
import com.mobile.models.Category;
import com.mobile.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NarBarActivity extends AppCompatActivity implements BottomNavFragment.OnBottomNavSelectedListener {
    public static List<Category> preloadedCategories = new ArrayList<>();
    public static CategoryShopAdapter categoryAdapter;


    private BottomNavFragment bottomNavFragment; // Khai báo như instance variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nar_bar);
        preloadProductDataInBackground();

        int tabPos = getIntent().getIntExtra("tab_pos", 0);

        String orderId = getIntent().getStringExtra("orderId");
        boolean fromDirectIntent = getIntent().getBooleanExtra("from_direct", false); // flag từ FinishPayment

        if (orderId != null) {
            // Mở OrderDetailFragment nếu có orderId
            OrderDetailFragment orderDetailFragment = new OrderDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString("orderId", orderId);
            orderDetailFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, orderDetailFragment)
                    .commit();

            tabPos = 5; // Chọn tab tương ứng
        } else {
            // Nếu không có orderId thì load tab thông thường
            showFragment(tabPos);

            if (!fromDirectIntent) {
                PopupDialog popupDialog = new PopupDialog();
                popupDialog.setOnShopClickListener(this::navigateToShop);
                popupDialog.show(getSupportFragmentManager(), "popup");
            }
        }


        // Truyền tabPos vào BottomNavFragment và lưu reference
        bottomNavFragment = BottomNavFragment.newInstance(tabPos);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.bottom_nav_container, bottomNavFragment)
                .commit();


    }

    private void preloadProductDataInBackground() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            ProductPreloadManager preloadManager = ProductPreloadManager.getInstance();
            preloadManager.preloadAllCategoryDataBlocking(); // ← preload toàn bộ categories
            Log.d("NarBarActivity", "Preload toàn bộ category xong");
        });
        executor.execute(() -> {
            String userId = new UserSessionManager(this).getUid();
            if (userId != null) {
                ProductPreloadManager.getInstance().preloadWishlistForUser(userId, () -> {
                    Log.d("Wishlist", "Wishlist cache loaded: " + ProductPreloadManager.getInstance().getCachedWishlist().size());
                });
            }
        });
    }
    public static List<Category> getStaticCategoryList() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category(R.mipmap.ic_category_furniture, "Shop All"));
        categories.add(new Category(R.mipmap.ic_category_furniture_shop, "Furniture"));
        categories.add(new Category(R.mipmap.ic_category_decor, "Decor"));
        categories.add(new Category(R.mipmap.ic_category_softgoods, "Soft Goods"));
        categories.add(new Category(R.mipmap.ic_category_lighting, "Lighting"));
        categories.add(new Category(R.mipmap.ic_category_art, "Art"));
        categories.add(new Category(R.mipmap.ic_category_dining, "Dining & Entertaining"));
        return categories;
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
                ShopFragment shopFragment = new ShopFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("preloadedCategories", new ArrayList<>(getStaticCategoryList())); // truyền list vào
                shopFragment.setArguments(bundle);
                frag = shopFragment;
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
            case 5:
                frag = new OrderDetailFragment();
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
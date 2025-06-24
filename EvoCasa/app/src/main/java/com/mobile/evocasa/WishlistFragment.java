package com.mobile.evocasa;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.adapters.HotProductsAdapter;
import com.mobile.adapters.WishProductAdapter;
import com.mobile.evocasa.profile.ProfileFragment;
import com.mobile.models.HotProducts;
import com.mobile.models.WishProduct;
import com.mobile.utils.FontUtils;
import com.mobile.utils.UserSessionManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WishlistFragment extends Fragment {

    private RecyclerView recyclerView;
    private View view;
    private TextView btnAll, btnSale, btnLowStock, btnOutOfStock;
    private List<TextView> allTabs;
    private ImageView imgWishlistBack;
    private WishProductAdapter wishProductAdapter;
    private FirebaseFirestore db;
    private List<WishProduct> wishProductList;
    private List<HotProducts> hotProductList;
    private HotProductsAdapter hotProductsAdapter;
    private RecyclerView recyclerViewHotProducts;
    private String currentCustomerId;
    private String currentFilter = "all";

    // Lưu trữ trạng thái sản phẩm cho từng tab
    private Map<String, List<WishProduct>> tabProductsMap = new HashMap<>();
    private boolean isTabDataLoaded = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_wishlist, container, false);

        db = FirebaseFirestore.getInstance();
        currentCustomerId = new UserSessionManager(getContext()).getUid();

        RecyclerView recyclerViewWishProduct = view.findViewById(R.id.recyclerViewWishProduct);
        recyclerViewWishProduct.setLayoutManager(new GridLayoutManager(getContext(), 2));

        wishProductList = new ArrayList<>();
        wishProductAdapter = new WishProductAdapter(wishProductList, position -> {
            // Lấy product để remove khỏi Firestore
            WishProduct productToRemove = wishProductList.get(position);

            // Remove từ Firestore trước
            removeProductFromWishlist(productToRemove, () -> {
                // Callback sau khi remove thành công từ Firestore
                // Remove khỏi UI và từ tabProductsMap
                wishProductList.remove(position);
                wishProductAdapter.notifyItemRemoved(position);

                // Update positions cho các item còn lại
                wishProductAdapter.notifyItemRangeChanged(position, wishProductList.size());

                // Cập nhật tabProductsMap để lưu trạng thái
                tabProductsMap.put(currentFilter, new ArrayList<>(wishProductList));

                // Reload Hot Products vì có thể có sản phẩm mới available
                loadHotProducts();
            });
        });

        // Add the OnProductItemClickListener to open Product Details Activity
        wishProductAdapter.setOnProductItemClickListener(product -> {
            Intent intent = new Intent(requireContext(), com.mobile.evocasa.productdetails.ProductDetailsActivity.class);
            intent.putExtra("productId", product.getId());  // Pass the productId to the details activity
            startActivity(intent);
        });

        recyclerViewWishProduct.setAdapter(wishProductAdapter);

        // Khởi tạo tab products map
        initializeTabProductsMap();

        TextView txtViewRcm = view.findViewById(R.id.txtViewRcm);
        FontUtils.setZboldFont(requireContext(), txtViewRcm);
        TextView txtTitle = view.findViewById(R.id.txtTitle);
        FontUtils.setZboldFont(requireContext(), txtTitle);

        btnAll = view.findViewById(R.id.btnAll);
        btnSale = view.findViewById(R.id.btnSale);
        btnLowStock = view.findViewById(R.id.btnLowStock);
        btnOutOfStock = view.findViewById(R.id.btnOutOfStock);

        FontUtils.setMediumFont(requireContext(), btnAll);
        FontUtils.setMediumFont(requireContext(), btnSale);
        FontUtils.setMediumFont(requireContext(), btnLowStock);
        FontUtils.setMediumFont(requireContext(), btnOutOfStock);

        allTabs = Arrays.asList(btnAll, btnSale, btnLowStock, btnOutOfStock);
        setActiveTab(btnAll);

        for (TextView tab : allTabs) {
            tab.setOnClickListener(v -> {
                setActiveTab(tab);
                String filter = "all";
                if (tab == btnSale) filter = "sale";
                else if (tab == btnLowStock) filter = "lowStock";
                else if (tab == btnOutOfStock) filter = "outOfStock";

                currentFilter = filter;
                displayTabProducts(filter);
            });
        }

        imgWishlistBack = view.findViewById(R.id.imgWishlistBack);
        imgWishlistBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        /* Hot Products */
        recyclerViewHotProducts = view.findViewById(R.id.recyclerViewHotProducts);
        recyclerViewHotProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        hotProductList = new ArrayList<>();
        hotProductsAdapter = new HotProductsAdapter(hotProductList, (product, position) -> {
            // Thêm vào Wishlist trong Firestore
            addProductToWishlist(product, () -> {
                // Callback sau khi add thành công
                // Remove từ Hot Products UI
                hotProductList.remove(product);
                hotProductsAdapter.notifyDataSetChanged();

                // Reset tab data để load lại từ Firestore
                isTabDataLoaded = false;
                initializeTabProductsMap();

                // Nếu hết sản phẩm hot, load thêm
                if (hotProductList.isEmpty()) {
                    loadHotProducts();
                }
            });
        });

        hotProductsAdapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(requireContext(), com.mobile.evocasa.productdetails.ProductDetailsActivity.class);
            intent.putExtra("productId", product.getId());
            startActivity(intent);
        });

        recyclerViewHotProducts.setAdapter(hotProductsAdapter);
        loadHotProducts();

        return view;
    }

    private void initializeTabProductsMap() {
        if (!isTabDataLoaded) {
            loadWishProductFromFirestore();
        } else {
            displayTabProducts(currentFilter);
        }
    }

    private void displayTabProducts(String filter) {
        wishProductAdapter.setCurrentTab(filter);

        if (tabProductsMap.containsKey(filter)) {
            // Hiển thị từ cache
            wishProductList.clear();
            wishProductList.addAll(tabProductsMap.get(filter));
            wishProductAdapter.notifyDataSetChanged();
        } else {
            // Nếu chưa có data cho tab này, hiển thị rỗng
            wishProductList.clear();
            wishProductAdapter.notifyDataSetChanged();
        }
    }

    private void loadWishProductFromFirestore() {
        db.collection("Wishlist")
                .whereEqualTo("Customer_id", currentCustomerId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        List<String> productIds = (List<String>) querySnapshot.getDocuments().get(0).get("Productid");
                        if (productIds == null || productIds.isEmpty()) {
                            // Wishlist trống
                            tabProductsMap.clear();
                            isTabDataLoaded = true;
                            displayTabProducts(currentFilter);
                            return;
                        }

                        // Shuffle để random sản phẩm
                        Collections.shuffle(productIds);

                        // Load tất cả sản phẩm trước
                        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                        for (String productId : productIds) {
                            tasks.add(db.collection("Product").document(productId).get());
                        }

                        Tasks.whenAllSuccess(tasks)
                                .addOnSuccessListener(results -> {
                                    List<WishProduct> allProducts = new ArrayList<>();
                                    for (Object obj : results) {
                                        DocumentSnapshot productDoc = (DocumentSnapshot) obj;
                                        if (productDoc.exists()) {
                                            WishProduct product = productDoc.toObject(WishProduct.class);
                                            if (product != null) {
                                                product.setId(productDoc.getId());
                                                allProducts.add(product);
                                            }
                                        }
                                    }

                                    // Phân chia sản phẩm cho từng tab
                                    tabProductsMap.put("all", new ArrayList<>(allProducts));

                                    // Sale: 4 sản phẩm đầu
                                    int saleCount = Math.min(4, allProducts.size());
                                    tabProductsMap.put("sale", new ArrayList<>(allProducts.subList(0, saleCount)));

                                    // Low Stock: 3 sản phẩm tiếp theo (hoặc từ đầu nếu không đủ)
                                    int lowStockStart = Math.min(saleCount, allProducts.size());
                                    int lowStockEnd = Math.min(lowStockStart + 3, allProducts.size());
                                    if (lowStockStart >= allProducts.size()) {
                                        lowStockStart = 0;
                                        lowStockEnd = Math.min(3, allProducts.size());
                                    }
                                    tabProductsMap.put("lowStock", new ArrayList<>(allProducts.subList(lowStockStart, lowStockEnd)));

                                    // Out of Stock: 2 sản phẩm tiếp theo
                                    int outOfStockStart = Math.min(lowStockEnd, allProducts.size());
                                    int outOfStockEnd = Math.min(outOfStockStart + 2, allProducts.size());
                                    if (outOfStockStart >= allProducts.size()) {
                                        outOfStockStart = 0;
                                        outOfStockEnd = Math.min(2, allProducts.size());
                                    }
                                    tabProductsMap.put("outOfStock", new ArrayList<>(allProducts.subList(outOfStockStart, outOfStockEnd)));

                                    isTabDataLoaded = true;
                                    displayTabProducts(currentFilter);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Wishlist", "Error loading products", e);
                                    tabProductsMap.clear();
                                    isTabDataLoaded = true;
                                    displayTabProducts(currentFilter);
                                });
                    } else {
                        // Không có wishlist nào cho customer này
                        tabProductsMap.clear();
                        isTabDataLoaded = true;
                        displayTabProducts(currentFilter);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Wishlist", "Error loading wishlist", e);
                    tabProductsMap.clear();
                    isTabDataLoaded = true;
                    displayTabProducts(currentFilter);
                });
    }

    private void addProductToWishlist(HotProducts product, Runnable onSuccess) {
        // Tìm product ID từ Firestore
        db.collection("Product")
                .whereEqualTo("Name", product.getName())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String productId = querySnapshot.getDocuments().get(0).getId();

                        // Thêm vào Wishlist collection
                        db.collection("Wishlist")
                                .whereEqualTo("Customer_id", currentCustomerId)
                                .get()
                                .addOnSuccessListener(wishlistQuery -> {
                                    if (!wishlistQuery.isEmpty()) {
                                        // Customer đã có wishlist, thêm product vào
                                        String wishlistDocId = wishlistQuery.getDocuments().get(0).getId();
                                        db.collection("Wishlist").document(wishlistDocId)
                                                .update("Productid", FieldValue.arrayUnion(productId))
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("Wishlist", "Product added to existing wishlist");
                                                    if (onSuccess != null) {
                                                        onSuccess.run();
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Wishlist", "Error adding to wishlist", e);
                                                });
                                    } else {
                                        // Tạo wishlist mới cho customer
                                        List<String> productIds = new ArrayList<>();
                                        productIds.add(productId);

                                        WishlistDocument newWishlist = new WishlistDocument();
                                        newWishlist.setCustomer_id(currentCustomerId);
                                        newWishlist.setProductid(productIds);

                                        db.collection("Wishlist")
                                                .add(newWishlist)
                                                .addOnSuccessListener(documentReference -> {
                                                    Log.d("Wishlist", "New wishlist created");
                                                    if (onSuccess != null) {
                                                        onSuccess.run();
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Wishlist", "Error creating wishlist", e);
                                                });
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Wishlist", "Error finding product", e);
                });
    }

    private void removeProductFromWishlist(WishProduct product, Runnable onSuccess) {
        // Tìm product ID từ Firestore
        db.collection("Product")
                .whereEqualTo("Name", product.getName())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String productId = querySnapshot.getDocuments().get(0).getId();

                        // Remove từ Wishlist collection
                        db.collection("Wishlist")
                                .whereEqualTo("Customer_id", currentCustomerId)
                                .get()
                                .addOnSuccessListener(wishlistQuery -> {
                                    if (!wishlistQuery.isEmpty()) {
                                        String wishlistDocId = wishlistQuery.getDocuments().get(0).getId();
                                        db.collection("Wishlist").document(wishlistDocId)
                                                .update("Productid", FieldValue.arrayRemove(productId))
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d("Wishlist", "Product removed from wishlist");
                                                    if (onSuccess != null) {
                                                        onSuccess.run();
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Wishlist", "Error removing from wishlist", e);
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Wishlist", "Error finding wishlist", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Wishlist", "Error finding product", e);
                });
    }

    private void setActiveTab(TextView selectedTab) {
        for (TextView tab : allTabs) {
            tab.setBackgroundResource(tab == selectedTab ? R.drawable.filter_button_selector_choose : R.drawable.filter_button_selector);
        }
    }

    private void loadHotProducts() {
        db.collection("Product")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    List<DocumentSnapshot> allDocs = querySnapshots.getDocuments();

                    // Lọc ra những sản phẩm chưa có trong wishlist
                    db.collection("Wishlist")
                            .whereEqualTo("Customer_id", currentCustomerId)
                            .get()
                            .addOnSuccessListener(wishlistQuery -> {
                                List<String> wishlistProductIds = new ArrayList<>();
                                if (!wishlistQuery.isEmpty()) {
                                    List<String> productIds = (List<String>) wishlistQuery.getDocuments().get(0).get("Productid");
                                    if (productIds != null) {
                                        wishlistProductIds = productIds;
                                    }
                                }

                                List<DocumentSnapshot> availableDocs = new ArrayList<>();
                                for (DocumentSnapshot doc : allDocs) {
                                    if (!wishlistProductIds.contains(doc.getId())) {
                                        availableDocs.add(doc);
                                    }
                                }

                                hotProductList.clear();
                                Collections.shuffle(availableDocs);

                                int limit = Math.min(4, availableDocs.size());
                                for (int i = 0; i < limit; i++) {
                                    HotProducts product = availableDocs.get(i).toObject(HotProducts.class);
                                    if (product != null) {
                                        product.setId(availableDocs.get(i).getId());  // Setting the product ID
                                        hotProductList.add(product);
                                    }
                                }


                                hotProductsAdapter.notifyDataSetChanged();
                                recyclerViewHotProducts.setVisibility(hotProductList.isEmpty() ? View.GONE : View.VISIBLE);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Error loading wishlist for hot products", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Lỗi khi load Hot Products", e);
                });
    }

    // Helper class cho Wishlist document structure
    public static class WishlistDocument {
        private String Customer_id;
        private List<String> Productid;

        public WishlistDocument() {}

        public String getCustomer_id() { return Customer_id; }
        public void setCustomer_id(String customer_id) { Customer_id = customer_id; }

        public List<String> getProductid() { return Productid; }
        public void setProductid(List<String> productid) { Productid = productid; }
    }
}
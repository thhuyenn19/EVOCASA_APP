package com.mobile.evocasa.category;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.mobile.adapters.CategoryShopAdapter;
import com.mobile.evocasa.CartActivity;
import com.mobile.evocasa.R;
import com.mobile.models.Category;
import com.mobile.utils.FontUtils;
import com.mobile.utils.GridSpacingItemDecoration;
import com.mobile.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopFragment extends Fragment {
    private static final String TAG = "ShopFragment";
    private RecyclerView recyclerViewCategories;
    private CategoryShopAdapter adapter;
    private View view;
    private List<Category> categoryList;
    private FirebaseFirestore db;
    private Map<String, String> categoryNameToIdMap;
    private Handler handler;
    private TextView txtCartBadge;
    private ImageView imgCart;
    private ListenerRegistration cartListener;
    private UserSessionManager sessionManager;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_shop, container, false);
        db = FirebaseFirestore.getInstance();
        txtCartBadge = view.findViewById(R.id.txtCartBadge);
        imgCart = view.findViewById(R.id.imgCart);
        sessionManager = new UserSessionManager(requireContext());
        categoryNameToIdMap = new HashMap<>();
        handler = new Handler(Looper.getMainLooper());

        applyCustomFonts();
        fetchCategoryIds();
        if (imgCart != null) {
            imgCart.setOnClickListener(v -> {
                if (isAdded() && getActivity() != null) {
                    Intent intent = new Intent(requireContext(), CartActivity.class);
                    startActivity(intent);
                }
            });
        }
        return view;
    }

    private void setupRecyclerView() {
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        categoryList = new ArrayList<>();
        categoryList.add(new Category(R.mipmap.ic_category_furniture, "Shop All"));
        categoryList.add(new Category(R.mipmap.ic_category_furniture_shop, "Furniture"));
        categoryList.add(new Category(R.mipmap.ic_category_decor, "Decor"));
        categoryList.add(new Category(R.mipmap.ic_category_softgoods, "Soft Goods"));
        categoryList.add(new Category(R.mipmap.ic_category_lighting, "Lighting"));
        categoryList.add(new Category(R.mipmap.ic_category_art, "Art"));
        categoryList.add(new Category(R.mipmap.ic_category_dining, "Dining & Entertaining"));

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return (position == categoryList.size() - 1) ? 2 : 1;
            }
        });
        recyclerViewCategories.setLayoutManager(layoutManager);

        int spacingInPixels = (int) (17 * getResources().getDisplayMetrics().density);
        recyclerViewCategories.addItemDecoration(new GridSpacingItemDecoration(2, spacingInPixels, true));

        recyclerViewCategories.setClipToPadding(false);
        recyclerViewCategories.setHasFixedSize(true);

        adapter = new CategoryShopAdapter(getContext(), categoryList, category -> {
            CategoryFragment categoryFragment = new CategoryFragment();
            Bundle bundle = new Bundle();
            bundle.putString("selectedCategory", category.getName());
            if (!category.getName().equals("Shop All")) {
                String categoryId = categoryNameToIdMap.get(category.getName().toLowerCase());
                if (categoryId != null) {
                    bundle.putString("categoryId", categoryId);
                    Log.d(TAG, "Navigating to CategoryFragment with category: " + category.getName() + ", ID: " + categoryId);
                } else {
                    Log.w(TAG, "No category ID found for: " + category.getName());
                }
            } else {
                Log.d(TAG, "Navigating to CategoryFragment for Shop All");
            }
            categoryFragment.setArguments(bundle);

            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, categoryFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });
        recyclerViewCategories.setAdapter(adapter);
    }

    private void fetchCategoryIds() {
        db.collection("Category")
                .whereEqualTo("ParentCategory", null)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var doc : queryDocumentSnapshots) {
                        String name = doc.getString("Name");
                        String id = doc.getId();
                        if (name != null) {
                            categoryNameToIdMap.put(name.toLowerCase(), id);
                            Log.d(TAG, "Mapped category: " + name + " to ID: " + id);
                        }
                    }
                    Log.d(TAG, "Category ID mapping: " + categoryNameToIdMap.toString());
                    // Setup RecyclerView after IDs are fetched
                    handler.post(() -> {
                        if (!isAdded()) {
                            Log.w(TAG, "Fragment not attached, skip setting up RecyclerView");
                            return;
                        }
                        setupRecyclerView();
                    });                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch category IDs: ", e);
                    // Setup RecyclerView even if query fails to ensure UI loads
                    handler.post(() -> {
                        if (!isAdded()) {
                            Log.w(TAG, "Fragment not attached, skip setting up RecyclerView");
                            return;
                        }
                        setupRecyclerView();
                    });                });
    }

    private void applyCustomFonts() {
        TextView txtCategoryShop = view.findViewById(R.id.txtCategoryShop);
        if (txtCategoryShop != null) {
            FontUtils.setZboldFont(getContext(), txtCategoryShop);
        }

        TextView txtDescriptionShop = view.findViewById(R.id.txtDescriptionShop);
        if (txtDescriptionShop != null) {
            FontUtils.setLightitalicFont(getContext(), txtDescriptionShop);
        }

        applyZboldFontToAllTextViews(view);
    }

    private void applyZboldFontToAllTextViews(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                applyZboldFontToAllTextViews(viewGroup.getChildAt(i));
            }
        } else if (view instanceof TextView) {
            TextView textView = (TextView) view;
            if (textView.getId() != R.id.txtCategoryShop && textView.getId() != R.id.txtDescriptionShop) {
                FontUtils.setZboldFont(getContext(), textView);
            }
        }
    }
    // CartBadge
    /**
     * Start listening for cart changes and update badge
     */
    private void startCartBadgeListener() {
        String uid = sessionManager.getUid();

        if (uid == null || uid.isEmpty()) {
            Log.d("CartBadge", "User not logged in, hiding badge");
            if (txtCartBadge != null) {
                txtCartBadge.setVisibility(View.GONE);
            }
            return;
        }

        // Remove existing listener before creating new one
        if (cartListener != null) {
            cartListener.remove();
            cartListener = null;
        }

        cartListener = FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .addSnapshotListener((documentSnapshot, e) -> {
                    // CRITICAL: Check if fragment is still attached AND context is not null
                    if (!isAdded() || getContext() == null || getActivity() == null) {
                        Log.d("CartBadge", "Fragment not attached, ignoring listener callback");
                        return;
                    }

                    if (e != null) {
                        Log.w("CartBadge", "Listen failed.", e);
                        // Safe update with lifecycle check
                        safeUpdateCartBadge(0);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        List<Map<String, Object>> cartList = (List<Map<String, Object>>) documentSnapshot.get("Cart");
                        int totalQuantity = 0;

                        if (cartList != null) {
                            for (Map<String, Object> item : cartList) {
                                Object qtyObj = item.get("cartQuantity");
                                if (qtyObj instanceof Number) {
                                    totalQuantity += ((Number) qtyObj).intValue();
                                }
                            }
                        }

                        safeUpdateCartBadge(totalQuantity);
                    } else {
                        Log.d("CartBadge", "No customer document found");
                        safeUpdateCartBadge(0);
                    }
                });
    }

    private void safeUpdateCartBadge(int totalQuantity) {
        // First check: Fragment lifecycle
        if (!isAdded() || getContext() == null || getActivity() == null) {
            Log.w("CartBadge", "Fragment not attached, cannot update badge");
            return;
        }

        // Second check: View availability
        if (txtCartBadge == null) {
            Log.w("CartBadge", "Cart badge view is null, cannot update");
            return;
        }

        // Use Handler with additional safety check
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> {
            // Triple check: Ensure fragment is still attached when handler executes
            if (!isAdded() || getContext() == null || getActivity() == null || txtCartBadge == null) {
                Log.w("CartBadge", "Fragment detached during handler execution, skip update");
                return;
            }

            try {
                if (totalQuantity > 0) {
                    txtCartBadge.setVisibility(View.VISIBLE);
                    String displayText = totalQuantity >= 100 ? "99+" : String.valueOf(totalQuantity);
                    txtCartBadge.setText(displayText);
                    Log.d("CartBadge", "Badge updated: " + displayText);
                } else {
                    txtCartBadge.setVisibility(View.GONE);
                    Log.d("CartBadge", "Badge hidden (quantity = 0)");
                }
            } catch (Exception ex) {
                Log.e("CartBadge", "Error updating cart badge UI", ex);
            }
        });
    }


    /**
     * Update cart badge display
     */
    private void updateCartBadge(int totalQuantity) {
        // Add comprehensive lifecycle checks
        if (!isAdded() || getContext() == null || getActivity() == null || txtCartBadge == null) {
            Log.w("CartBadge", "Fragment not attached or views null, skip update");
            return;
        }

        // Post to main thread with additional safety check
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            // Double-check lifecycle state in the posted runnable
            if (!isAdded() || getContext() == null || getActivity() == null || txtCartBadge == null) {
                Log.w("CartBadge", "Fragment detached during handler post, skip update");
                return;
            }

            if (totalQuantity > 0) {
                txtCartBadge.setVisibility(View.VISIBLE);
                String displayText = totalQuantity >= 100 ? "99+" : String.valueOf(totalQuantity);
                txtCartBadge.setText(displayText);
                Log.d("CartBadge", "Badge updated: " + displayText);
            } else {
                txtCartBadge.setVisibility(View.GONE);
                Log.d("CartBadge", "Badge hidden (quantity = 0)");
            }
        });
    }

    /**
     * Public method for fragments to refresh cart badge
     */
    public void refreshCartBadge() {
        if (!isAdded() || getContext() == null || getActivity() == null) {
            Log.d("CartBadge", "Cannot refresh badge - fragment not properly attached");
            return;
        }

        String uid = sessionManager.getUid();
        if (uid == null || uid.isEmpty()) {
            Log.d("CartBadge", "Cannot refresh badge - user not logged in");
            safeUpdateCartBadge(0);
            return;
        }

        Log.d("CartBadge", "Manually refreshing cart badge");

        FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Check if fragment is still attached when callback returns
                    if (!isAdded() || getContext() == null || getActivity() == null) {
                        Log.d("CartBadge", "Fragment detached during refresh, ignoring result");
                        return;
                    }

                    int totalQuantity = 0;
                    if (documentSnapshot.exists()) {
                        List<Map<String, Object>> cartList = (List<Map<String, Object>>) documentSnapshot.get("Cart");
                        if (cartList != null) {
                            for (Map<String, Object> item : cartList) {
                                Object qtyObj = item.get("cartQuantity");
                                if (qtyObj instanceof Number) {
                                    totalQuantity += ((Number) qtyObj).intValue();
                                }
                            }
                        }
                    }
                    safeUpdateCartBadge(totalQuantity);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null || getActivity() == null) {
                        Log.d("CartBadge", "Fragment detached during refresh error, ignoring");
                        return;
                    }
                    Log.e("CartBadge", "Error refreshing cart badge", e);
                    safeUpdateCartBadge(0);
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("CartBadge", "Fragment onStart()");
        if (sessionManager != null && txtCartBadge != null && isAdded()) {
            startCartBadgeListener();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("CartBadge", "Fragment onResume()");
        // Only start listener if we don't already have one and fragment is properly attached
        if (cartListener == null && isAdded() && getContext() != null &&
                sessionManager != null && txtCartBadge != null) {
            startCartBadgeListener();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("CartBadge", "Fragment onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("CartBadge", "Fragment onStop()");
        cleanupCartListener();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("CartBadge", "Fragment onDestroyView()");
        cleanupCartListener();
        txtCartBadge = null; // Clear view reference
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("CartBadge", "Fragment onDestroy()");
        cleanupCartListener();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("CartBadge", "Fragment onDetach()");
        cleanupCartListener();
    }

    private void cleanupCartListener() {
        if (cartListener != null) {
            Log.d("CartBadge", "Removing cart listener");
            cartListener.remove();
            cartListener = null;
        }
    }
}
package com.mobile.evocasa.profile;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.mobile.evocasa.CartActivity;
import com.mobile.evocasa.MainActivity;
import com.mobile.evocasa.SettingFragment;
import com.mobile.utils.BehaviorLogger;
import com.mobile.utils.UserSessionManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.adapters.SuggestedProductAdapter;
import com.mobile.evocasa.BlogFragment;
import com.mobile.evocasa.ChatActivity;
import com.mobile.evocasa.order.OrdersFragment;
import com.mobile.evocasa.R;
import com.mobile.evocasa.WishlistFragment;
import com.mobile.evocasa.helpcenter.HelpCenterFragment;
import com.mobile.models.ProductItem;
import com.mobile.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private TextView txtName, txtLogOut, txtLogin, txtRegister, txtCartBadge;
    private ImageView imgAvatar, imgCart;
    private ImageButton btnEditAvatar;
    private RecyclerView recyclerView;
    private View view;
    private ListenerRegistration cartListener;
    private UserSessionManager sessionManager;
    private FirebaseFirestore db;
    private LinearLayout containerLoginRegister;
    private TextView badgePending, badgePickUp, badgeTransit, badgeReview;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        applyCustomFonts(view);

        db = FirebaseFirestore.getInstance();

        txtName = view.findViewById(R.id.txtName);
        txtCartBadge = view.findViewById(R.id.txtCartBadge);
        imgCart = view.findViewById(R.id.imgCart);
        sessionManager = new UserSessionManager(requireContext());
        imgAvatar = view.findViewById(R.id.img_avatar);
        btnEditAvatar = view.findViewById(R.id.btn_edit_avatar);

        badgePending = view.findViewById(R.id.badge_pending);
        badgePickUp = view.findViewById(R.id.badge_pick_up);
        badgeTransit = view.findViewById(R.id.badge_transit);
        badgeReview = view.findViewById(R.id.badge_review);

        // Initialize login/register views
        txtLogin = view.findViewById(R.id.txtLogin);
        txtRegister = view.findViewById(R.id.txtRegister);
        containerLoginRegister = view.findViewById(R.id.containerLoginRegister);

        loadCustomerInformation();
        setupSuggestedProducts();
        setupClickListeners();
        startCartBadgeListener();
        loadOrderCounts();

        return view;
    }

    private void loadOrderCounts() {
        String uid = sessionManager.getUid();
        if (uid == null || uid.isEmpty()) {
            updateBadge(badgePending, 0);
            updateBadge(badgePickUp, 0);
            updateBadge(badgeTransit, 0);
            updateBadge(badgeReview, 0);
            return;
        }

        db.collection("Order")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int pendingCount = 0;
                    int pickUpCount = 0;
                    int inTransitCount = 0;
                    int reviewCount = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Map<String, Object> customerMap = (Map<String, Object>) doc.get("Customer_id");
                        if (customerMap == null) continue;
                        String orderUid = (String) customerMap.get("$oid");
                        if (!uid.equals(orderUid)) continue;

                        String status = doc.getString("Status");
                        if (status == null) continue;
                        switch (status) {
                            case "Pending":
                                pendingCount++;
                                break;
                            case "Pick Up":
                                pickUpCount++;
                                break;
                            case "In Transit":
                                inTransitCount++;
                                break;
                            case "Review":
                                reviewCount++;
                                break;
                        }
                    }

                    updateBadge(badgePending, pendingCount);
                    updateBadge(badgePickUp, pickUpCount);
                    updateBadge(badgeTransit, inTransitCount);
                    updateBadge(badgeReview, reviewCount);
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileFragment", "loadOrderCounts failed", e);
                });
    }

    private void updateBadge(TextView badge, int count) {
        if (count > 0) {
            badge.setVisibility(View.VISIBLE);
            badge.setText(String.valueOf(count));
        } else {
            badge.setVisibility(View.GONE);
        }
    }

    private void setupSuggestedProducts() {
        RecyclerView recyclerViewSuggestedProducts = view.findViewById(R.id.recyclerViewSuggestedProducts);
        recyclerViewSuggestedProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        List<ProductItem> productList = new ArrayList<>();
        SuggestedProductAdapter suggestedProductsAdapter = new SuggestedProductAdapter(productList, requireContext());
        recyclerViewSuggestedProducts.setAdapter(suggestedProductsAdapter);

        suggestedProductsAdapter.setOnItemClickListener(product -> {
            String uid = new UserSessionManager(requireContext()).getUid(); // hoặc từ SharedPreferences nếu bạn không dùng FirebaseAuth
            String productId = product.getId(); // hoặc product.get_id()

            // Ghi hành vi click
            BehaviorLogger.record(
                    uid,
                    productId,
                    "click",
                    "profile_page",
                    null
            );
            Intent intent = new Intent(requireContext(), com.mobile.evocasa.productdetails.ProductDetailsActivity.class);
            intent.putExtra("productId", product.getId());
            startActivity(intent);
        });

        db.collection("Product")
                .limit(4)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        ProductItem product = new ProductItem();
                        product.setId(doc.getId());
                        product.setName(doc.getString("Name"));
                        product.setPrice(doc.getDouble("Price") != null ? doc.getDouble("Price") : 0.0);
                        product.setImage(doc.getString("Image"));
                        product.setDescription(doc.getString("Description"));
                        product.setDimensions(doc.getString("Dimensions"));
                        ProductItem.Ratings ratings = new ProductItem.Ratings();
                        Object ratingsObj = doc.get("Ratings");
                        if (ratingsObj instanceof Map) {
                            Map<String, Object> ratingsMap = (Map<String, Object>) ratingsObj;
                            Object averageObj = ratingsMap.get("Average");
                            if (averageObj instanceof Number) {
                                ratings.setAverage(((Number) averageObj).doubleValue());
                            }
                        }
                        product.setRatings(ratings);
                        Object categoryIdObj = doc.get("category_id");
                        if (categoryIdObj instanceof Map) {
                            Map<String, Object> categoryIdMap = (Map<String, Object>) categoryIdObj;
                            product.setCategoryId(categoryIdMap);
                        }
                        productList.add(product);
                    }
                    suggestedProductsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileFragment", "Failed to load suggested products", e);
                });
    }

    private void setupClickListeners() {
        if (txtLogin != null) {
            txtLogin.setOnClickListener(v -> {
                if (isAdded() && getActivity() != null) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("openFragment", "SignIn");
                    startActivity(intent);
                }
            });
        }

        if (txtRegister != null) {
            txtRegister.setOnClickListener(v -> {
                if (isAdded() && getActivity() != null) {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("openFragment", "SignUp");
                    startActivity(intent);
                }
            });
        }

        View txtEvoCasaBlog = view.findViewById(R.id.txtEvoCasaBlog);
        txtEvoCasaBlog.setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new BlogFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        View txtWishlist = view.findViewById(R.id.txtWishlist);
        txtWishlist.setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new WishlistFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        View imgArrowWish = view.findViewById(R.id.imgArrowWish);
        imgArrowWish.setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new WishlistFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        View imgHelpCenter = view.findViewById(R.id.imgHelpCenter);
        imgHelpCenter.setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new HelpCenterFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        TextView txtHelpCenter = view.findViewById(R.id.txtHelpCenter);
        txtHelpCenter.setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new HelpCenterFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        ImageView imgAvatar = view.findViewById(R.id.img_avatar);
        ImageButton btnEditAvatar = view.findViewById(R.id.btn_edit_avatar);

        imgAvatar.setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                if (sessionManager.isLoggedIn()) {
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, new ProfileDetailFragment())
                            .addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(requireContext(), "You need to log in to view your profile.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnEditAvatar.setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ProfileDetailFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        txtLogOut = view.findViewById(R.id.txtLogOut);
        txtLogOut.setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                new UserSessionManager(requireContext()).clearSession();
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .commit();
            }
        });

        LinearLayout containerSeeAll = view.findViewById(R.id.containerSeeAll);
        containerSeeAll.setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new OrdersFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        LinearLayout itemPending = view.findViewById(R.id.containerPending);
        LinearLayout itemPickup = view.findViewById(R.id.containerPickup);
        LinearLayout itemTransit = view.findViewById(R.id.containerInTransit);
        LinearLayout itemReview = view.findViewById(R.id.containerReview);

        View.OnClickListener goToOrders = v -> {
            if (!isAdded() || getActivity() == null) return;

            String status = "";
            if (v.getId() == R.id.containerPending) status = "Pending";
            else if (v.getId() == R.id.containerPickup) status = "Pick Up";
            else if (v.getId() == R.id.containerInTransit) status = "In Transit";
            else if (v.getId() == R.id.containerReview) status = "Review";

            OrdersFragment ordersFragment = new OrdersFragment();
            Bundle bundle = new Bundle();
            bundle.putString("selectedStatus", status);
            ordersFragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, ordersFragment)
                    .addToBackStack(null)
                    .commit();
        };

        itemPending.setOnClickListener(goToOrders);
        itemPickup.setOnClickListener(goToOrders);
        itemTransit.setOnClickListener(goToOrders);
        itemReview.setOnClickListener(goToOrders);

        TextView txtChat = view.findViewById(R.id.txtChat);
        ImageView icChat = view.findViewById(R.id.icChat);

        View.OnClickListener openChatActivity = v -> {
            if (isAdded() && getActivity() != null) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                startActivity(intent);
            }
        };

        if (txtChat != null) {
            txtChat.setOnClickListener(openChatActivity);
        }
        if (icChat != null) {
            icChat.setOnClickListener(openChatActivity);
        }

        if (imgCart != null) {
            imgCart.setOnClickListener(v -> {
                if (isAdded() && getActivity() != null) {
                    Intent intent = new Intent(requireContext(), CartActivity.class);
                    startActivity(intent);
                }
            });
        }

        View btnSetting = view.findViewById(R.id.btnSetting);
        if (btnSetting != null) {
            btnSetting.setOnClickListener(v -> {
                if (isAdded() && getActivity() != null) {
                    if (sessionManager.isLoggedIn()) {
                        Log.d("DEBUG", "btnSetting clicked – ready to open SettingFragment");
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_container, new SettingFragment())
                                .addToBackStack(null)
                                .commit();
                    } else {
                        Toast.makeText(requireContext(), "You need to log in to access settings.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        // Vouchers
        View txtVouchers = view.findViewById(R.id.txtVouchers);
        txtVouchers.setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                Intent intent = new Intent(getActivity(), com.mobile.evocasa.VoucherActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadCustomerInformation() {
        if (!isAdded() || getContext() == null) return;

        String uid = sessionManager.getUid();

        if (uid != null && !uid.isEmpty()) {
            showUserInfo();
            FirebaseFirestore.getInstance()
                    .collection("Customers")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!isAdded() || getContext() == null) return;

                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("Name");
                            if (txtName != null) {
                                if (name != null && !name.isEmpty()) {
                                    txtName.setText(name);
                                } else {
                                    txtName.setText("No Name");
                                }
                            }

                            String avatarUrl = documentSnapshot.getString("Image");
                            if (imgAvatar != null && avatarUrl != null && !avatarUrl.isEmpty()) {
                                Glide.with(requireContext())
                                        .load(avatarUrl)
                                        .placeholder(R.mipmap.sample_avt)
                                        .circleCrop()
                                        .into(imgAvatar);
                            }
                        } else {
                            if (txtName != null) txtName.setText("No Profile Found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (!isAdded() || getContext() == null) return;
                        if (txtName != null) txtName.setText("Error");
                    });
        } else {
            showLoginRegisterButtons();
        }
    }

    private void showLoginRegisterButtons() {
        if (txtName != null) txtName.setVisibility(View.GONE);
        if (imgAvatar != null) imgAvatar.setVisibility(View.VISIBLE);
        if (btnEditAvatar != null) btnEditAvatar.setVisibility(View.GONE);
        if (containerLoginRegister != null) containerLoginRegister.setVisibility(View.VISIBLE);
    }

    private void showUserInfo() {
        if (txtName != null) txtName.setVisibility(View.VISIBLE);
        if (imgAvatar != null) imgAvatar.setVisibility(View.VISIBLE);
        if (btnEditAvatar != null) btnEditAvatar.setVisibility(View.VISIBLE);
        if (containerLoginRegister != null) containerLoginRegister.setVisibility(View.GONE);
    }

    private void applyCustomFonts(View view) {
        TextView txtName = view.findViewById(R.id.txtName);
        TextView txtOrders = view.findViewById(R.id.txtOrders);
        TextView txtMyFeatures = view.findViewById(R.id.txtMyFeatures);
        TextView txtSupport = view.findViewById(R.id.txtSupport);
        TextView txtSuggestedForYou = view.findViewById(R.id.txtSuggestedForYou);

        if (txtName != null && getContext() != null) {
            FontUtils.setZboldFont(getContext(), txtName);
        }
        if (txtOrders != null && getContext() != null) {
            FontUtils.setZblackFont(getContext(), txtOrders);
        }
        if (txtMyFeatures != null && getContext() != null) {
            FontUtils.setZblackFont(getContext(), txtMyFeatures);
        }
        if (txtSupport != null && getContext() != null) {
            FontUtils.setZblackFont(getContext(), txtSupport);
        }
        if (txtSuggestedForYou != null && getContext() != null) {
            FontUtils.setZregularFont(getContext(), txtSuggestedForYou);
        }

        int[] textViewIds = {
                R.id.txtSeeAll, R.id.txtViewMore, R.id.txtPending, R.id.txtPickUp,
                R.id.txtTransit, R.id.txtReview, R.id.txtWishlist, R.id.txtVouchers,
                R.id.txtCoin, R.id.txtFlashSale, R.id.txtHelpCenter, R.id.txtChat,
                R.id.txtEvoCasaBlog, R.id.txtLogOut
        };

        for (int id : textViewIds) {
            TextView textView = view.findViewById(id);
            if (textView != null && getContext() != null) {
                FontUtils.setRegularFont(getContext(), textView);
            }
        }
    }

    private void startCartBadgeListener() {
        String uid = sessionManager.getUid();

        if (uid == null || uid.isEmpty()) {
            Log.d("CartBadge", "User not logged in, hiding badge");
            if (txtCartBadge != null) {
                txtCartBadge.setVisibility(View.GONE);
            }
            return;
        }

        if (cartListener != null) {
            cartListener.remove();
            cartListener = null;
        }

        cartListener = FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (!isAdded() || getContext() == null || getActivity() == null) {
                        Log.d("CartBadge", "Fragment not attached, ignoring listener callback");
                        return;
                    }

                    if (e != null) {
                        Log.w("CartBadge", "Listen failed.", e);
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
        if (!isAdded() || getContext() == null || getActivity() == null) {
            Log.w("CartBadge", "Fragment not attached, cannot update badge");
            return;
        }

        if (txtCartBadge == null) {
            Log.w("CartBadge", "Cart badge view is null, cannot update");
            return;
        }

        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> {
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
        txtCartBadge = null;
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
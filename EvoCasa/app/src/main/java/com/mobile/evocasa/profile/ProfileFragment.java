package com.mobile.evocasa.profile;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.mobile.evocasa.CartActivity;
import com.mobile.evocasa.MainActivity;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.adapters.SuggestedProductAdapter;
import com.mobile.evocasa.BlogFragment;
import com.mobile.evocasa.ChatActivity;
import com.mobile.evocasa.OrdersFragment;
import com.mobile.evocasa.R;
import com.mobile.evocasa.WishlistFragment;
import com.mobile.evocasa.helpcenter.HelpCenterFragment;
import com.mobile.models.SuggestedProducts;
import com.mobile.utils.FontUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private TextView txtName, txtLogOut, txtCartBadge;
    private ImageView imgAvatar, imgCart;
    ImageButton btnEditAvatar;
    private RecyclerView recyclerView;
    private View view;

    // CartBadge
    private ListenerRegistration cartListener;
    private UserSessionManager sessionManager;

    //
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        applyCustomFonts(view);

        // Initialize views
        txtName = view.findViewById(R.id.txtName);

       // CartBadge
        txtCartBadge = view.findViewById(R.id.txtCartBadge);
        imgCart = view.findViewById(R.id.imgCart);
        sessionManager = new UserSessionManager(requireContext());
        imgAvatar = view.findViewById(R.id.img_avatar);
        btnEditAvatar = view.findViewById(R.id.btn_edit_avatar);
        txtName = view.findViewById(R.id.txtName);

        //
        loadCustomerInformation();
        setupSuggestedProducts();
        setupClickListeners();
        startCartBadgeListener();

        return view;
    }

    private void setupSuggestedProducts() {
        RecyclerView recyclerViewSuggestedProducts = view.findViewById(R.id.recyclerViewSuggestedProducts);
        recyclerViewSuggestedProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));

        List<SuggestedProducts> suggestedProductsList = new ArrayList<>();
        suggestedProductsList.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        suggestedProductsList.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        suggestedProductsList.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));
        suggestedProductsList.add(new SuggestedProducts(R.mipmap.ic_lighting_brasslamp, "MCM Brass Lamp", "$109", "$85", "-22%", 5.0f));

        SuggestedProductAdapter suggestedProductsAdapter = new SuggestedProductAdapter(suggestedProductsList);
        recyclerViewSuggestedProducts.setAdapter(suggestedProductsAdapter);
    }

    private void setupClickListeners() {
        // Blog Fragment
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

        // Wishlist Fragment
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

        // Help Center
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

        // Profile Detail
        ImageView imgAvatar = view.findViewById(R.id.img_avatar);
        ImageButton btnEditAvatar = view.findViewById(R.id.btn_edit_avatar);

        imgAvatar.setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ProfileDetailFragment())
                        .addToBackStack(null)
                        .commit();
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

        // Logout
        TextView txtLogOut = view.findViewById(R.id.txtLogOut);
        txtLogOut.setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                new UserSessionManager(requireContext()).clearSession();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        // Orders
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

        // Order Status Items
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

        // Chat
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

        // Cart
        if (imgCart != null) {
            imgCart.setOnClickListener(v -> {
                if (isAdded() && getActivity() != null) {
                    Intent intent = new Intent(requireContext(), CartActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void loadCustomerInformation() {
        if (!isAdded() || getContext() == null) return;

        String uid = sessionManager.getUid();

        if (uid != null) {
            FirebaseFirestore.getInstance()
                    .collection("Customers")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!isAdded() || getContext() == null) return;

                        if (documentSnapshot.exists()) {
                            // Gán tên người dùng
                            String name = documentSnapshot.getString("Name");
                            if (txtName != null) {
                                if (name != null && !name.isEmpty()) {
                                    txtName.setText(name);
                                } else {
                                    txtName.setText("No Name");
                                }
                            }

                            // Gán ảnh avatar
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
            if (txtName != null) {
                txtName.setText("Not logged in");
            }
        }
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
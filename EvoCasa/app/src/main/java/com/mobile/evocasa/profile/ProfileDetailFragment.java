package com.mobile.evocasa.profile;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.ListenerRegistration;
import com.mobile.adapters.ProfileInfoAdapter;
import com.mobile.adapters.ShippingAddressAdapter;
import com.mobile.evocasa.R;
import com.mobile.models.Customer;
import com.mobile.models.ProfileInfo;
import com.mobile.models.ShippingAddress;
import com.mobile.utils.FontUtils;
import com.mobile.utils.UserSessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


public class ProfileDetailFragment extends Fragment {
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private Uri selectedImageUri;
    private View view;
    private TextView txtName, txtLogOut, txtCartBadge;
    private ImageView imgAvatar, imgCart;
    ImageButton btnEditAvatar;
    private ListenerRegistration cartListener;
    private UserSessionManager sessionManager;

    private ProfileInfoAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile_detail, container, false);
        applyCustomFonts(view);
        txtCartBadge = view.findViewById(R.id.txtCartBadge);
        imgCart = view.findViewById(R.id.imgCart);
        imgAvatar = view.findViewById(R.id.img_avatar);
        btnEditAvatar = view.findViewById(R.id.btn_edit_avatar);
        txtName = view.findViewById(R.id.txtName);
        sessionManager = new UserSessionManager(requireContext());

        loadCustomerInformation();
        return view;
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


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvProfileInfo = view.findViewById(R.id.rv_profile_info);
        rvProfileInfo.setLayoutManager(new LinearLayoutManager(getContext()));
        List<ProfileInfo> data = new ArrayList<>();
        ProfileInfoAdapter adapter = new ProfileInfoAdapter(data);
        rvProfileInfo.setAdapter(adapter);

        loadCustomerData();
        
        RecyclerView rvShipping = view.findViewById(R.id.rv_shipping_address);
        rvShipping.setLayoutManager(new LinearLayoutManager(getContext()));

// Danh sách giả định
        List<ShippingAddress> addresses = new ArrayList<>();
        addresses.add(new ShippingAddress(
                "John Anthony", "(+84) 123 456 789",
                "669 Do Muoi, Linh Xuan, Thu Duc, HCMC, Vietnam", true));
        addresses.add(new ShippingAddress(
                "Jessica Nguyen", "(+84) 456 789 123",
                "Thuan Giao, Thuan An, Binh Duong, Vietnam", false));

//        ShippingAddressAdapter shipadapter = new ShippingAddressAdapter(addresses);
//        rvShipping.setAdapter(shipadapter);

        ShippingAddressAdapter shipadapter = new ShippingAddressAdapter(addresses, address -> {
            // Mở EditShippingFragment và truyền dữ liệu qua Bundle
            Bundle bundle = new Bundle();
            bundle.putSerializable("shippingAddress", address);

            EditShippingFragment fragment = new EditShippingFragment();
            fragment.setArguments(bundle);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
        rvShipping.setAdapter(shipadapter);


        LinearLayout btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });


        //Mở edit infor
        TextView txtEdit = view.findViewById(R.id.txtEdit);
        txtEdit.setOnClickListener(v -> {
            // Chuyển sang EditPersonalFragment
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new EditPersonalFragment()) // ID của container chứa fragment
                    .addToBackStack(null) // Cho phép quay lại bằng nút back
                    .commit();
        });

        ImageView imgEditProfile = view.findViewById(R.id.imgEditProfile);
        imgEditProfile.setOnClickListener(v -> {
            // Chuyển sang EditPersonalFragment
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new EditPersonalFragment()) // ID của container chứa fragment
                    .addToBackStack(null) // Cho phép quay lại bằng nút back
                    .commit();
        });

        ImageView iconEditName = view.findViewById(R.id.iconEditName);
        iconEditName.setOnClickListener(v -> {
            // Chuyển sang EditPersonalFragment
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new EditPersonalFragment()) // ID của container chứa fragment
                    .addToBackStack(null) // Cho phép quay lại bằng nút back
                    .commit();
        });



        //Mở popup đổi avatar
        ImageButton btnEditAvatar = view.findViewById(R.id.btn_edit_avatar);
        btnEditAvatar.setOnClickListener(v -> showBottomSheetDialog());

        ImageView btnEditAvatarImage = view.findViewById(R.id.img_avatar);
        btnEditAvatarImage.setOnClickListener(v -> showBottomSheetDialog());



    }

    private void loadCustomerData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        DocumentReference documentReference = FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(userId);

        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                try {
                    // Use toObject() - this will now work with the updated Customer class
                    Customer customer = documentSnapshot.toObject(Customer.class);
                    if (customer != null) {
                        updateProfileInfo(customer);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error converting document to Customer object", e);
                    // Fallback to manual extraction if needed
                    handleManualExtraction(documentSnapshot);
                }
            } else {
                Log.d(TAG, "No customer document found");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading customer data", e);
        });
    }

    private void handleManualExtraction(com.google.firebase.firestore.DocumentSnapshot documentSnapshot) {
        // Fallback method if toObject() still fails
        Customer customer = new Customer();
        customer.setName(documentSnapshot.getString("Name"));
        customer.setGender(documentSnapshot.getString("Gender"));
        customer.setMail(documentSnapshot.getString("Mail"));
        customer.setPhone(documentSnapshot.getString("Phone"));
        customer.setAddress(documentSnapshot.getString("Address"));
        customer.setImage(documentSnapshot.getString("Image"));
        customer.setDOB(documentSnapshot.get("DOB")); // This handles the map automatically

        updateProfileInfo(customer);
    }

    private void updateProfileInfo(Customer customer) {
        List<ProfileInfo> profileInfoList = new ArrayList<>();

        // Add profile information


        if (customer.getMail() != null && !customer.getMail().isEmpty()) {
            profileInfoList.add(new ProfileInfo("Email", customer.getMail(), R.drawable.ic_email));
        }

        if (customer.getPhone() != null && !customer.getPhone().isEmpty()) {
            profileInfoList.add(new ProfileInfo("Phone", customer.getPhone(), R.drawable.ic_phone));
        }

        // Use the new getDOBString() method
        String dobString = customer.getDOBString();
        if (!dobString.isEmpty()) {
            profileInfoList.add(new ProfileInfo("Date of Birth", dobString, R.drawable.ic_calendar));
        }

        if (customer.getAddress() != null && !customer.getAddress().isEmpty()) {
            profileInfoList.add(new ProfileInfo("Location", customer.getAddress(), R.drawable.ic_location));
        }

        // Update adapter
        adapter.setData(profileInfoList);
    }


    private void showBottomSheetDialog() {
        View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_image_options, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetDialog.setContentView(bottomSheetView);

        Button btnViewImage = bottomSheetView.findViewById(R.id.btn_view_image);
        Button btnUploadImage = bottomSheetView.findViewById(R.id.btn_upload_image);

        btnViewImage.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            showAvatarDialog();
        });

        btnUploadImage.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            openImagePicker();
        });

        bottomSheetDialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private void showAvatarDialog() {
        if (!isAdded() || getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Avatar");
        ImageView imageView = new ImageView(requireContext());
        imageView.setPadding(20, 20, 20, 20);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // Gán ảnh từ avatar hiện tại
        if (imgAvatar != null && imgAvatar.getDrawable() != null) {
            imageView.setImageDrawable(imgAvatar.getDrawable());
        } else {
            imageView.setImageResource(R.mipmap.sample_avt); // fallback
        }

        builder.setView(imageView);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            Button closeButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (closeButton != null) {
                closeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_5E4C3E)); // màu nâu
            }
        });

        dialog.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            uploadImageToFirebase(selectedImageUri);
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri == null || !isAdded() || getContext() == null) return;

        String uid = sessionManager.getUid();
        if (uid == null) return;

        String fileName = "avatars/" + uid + "_" + System.currentTimeMillis() + ".jpg";

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(fileName);

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            if (!isAdded() || getContext() == null) return; // Additional safety check

                            String downloadUrl = uri.toString();

                            // Update Firestore with the new image URL
                            FirebaseFirestore.getInstance()
                                    .collection("Customers")
                                    .document(uid)
                                    .update("Image", downloadUrl)
                                    .addOnSuccessListener(unused -> {
                                        if (!isAdded() || getContext() == null) return; // Safety check

                                        // Update the UI with the new image
                                        Glide.with(requireContext())
                                                .load(downloadUrl)
                                                .placeholder(R.mipmap.sample_avt)
                                                .circleCrop()
                                                .into(imgAvatar);

                                        Toast.makeText(getContext(), "Avatar updated successfully!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        if (!isAdded() || getContext() == null) return; // Safety check
                                        Toast.makeText(getContext(), "Failed to update Firestore.", Toast.LENGTH_SHORT).show();
                                        Log.e("ProfileDetail", "Failed to update Firestore", e);
                                    });
                        })
                        .addOnFailureListener(e -> {
                            if (!isAdded() || getContext() == null) return; // Safety check
                            Toast.makeText(getContext(), "Failed to get download URL.", Toast.LENGTH_SHORT).show();
                            Log.e("ProfileDetail", "Failed to get download URL", e);
                        }))
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) return; // Safety check
                    Toast.makeText(getContext(), "Failed to upload image.", Toast.LENGTH_SHORT).show();
                    Log.e("ProfileDetail", "Failed to upload image", e);
                });
    }

    private void applyCustomFonts(View view) {
        TextView txtName = view.findViewById(R.id.txtName);
        if (txtName != null) {
            FontUtils.setZboldFont(getContext(), txtName);
        }
        TextView txtPersonalInformation = view.findViewById(R.id.txtPersonalInformation);
        TextView txtShippingAddress = view.findViewById(R.id.txtShippingAddress);
        TextView txtEdit= view.findViewById(R.id.txtEdit);
        if (txtPersonalInformation != null) {
            FontUtils.setZblackFont(getContext(), txtPersonalInformation);
        }
        if (txtShippingAddress != null) {
            FontUtils.setZblackFont(getContext(), txtShippingAddress);
        }
        if (txtEdit != null) {
            FontUtils.setZblackFont(getContext(), txtEdit);
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
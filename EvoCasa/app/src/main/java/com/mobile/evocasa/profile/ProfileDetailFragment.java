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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileDetailFragment extends Fragment {
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;

    private View view;
    private TextView txtName, txtCartBadge, txtNoShippingAddress;
    private ImageView imgAvatar, imgCart, iconEditName;
    private LinearLayout layoutEditProfile,layoutAddShipping, name_with_icon;
    private ImageButton btnEditAvatar;
    private ListenerRegistration cartListener;
    private UserSessionManager sessionManager;
    private ProfileInfoAdapter adapter;
    private ShippingAddressAdapter shippingAdapter;
    private Uri selectedImageUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sessionManager = new UserSessionManager(requireContext());
        view = inflater.inflate(R.layout.fragment_profile_detail, container, false);

        initializeViews();
        setupRecyclerViews();
        setupClickListeners();
        applyCustomFonts(view);
        loadCustomerInformation();

        return view;
    }

    private void initializeViews() {
        txtCartBadge = view.findViewById(R.id.txtCartBadge);
        imgCart = view.findViewById(R.id.imgCart);
        imgAvatar = view.findViewById(R.id.img_avatar);
        btnEditAvatar = view.findViewById(R.id.btn_edit_avatar);
        txtName = view.findViewById(R.id.txtName);
        layoutEditProfile = view.findViewById(R.id.layoutEditProfile);
        layoutAddShipping = view.findViewById(R.id.layoutAddShipping);
        name_with_icon = view.findViewById(R.id.name_with_icon);
        txtNoShippingAddress = view.findViewById(R.id.txtNoShippingAddress);
    }

    private void setupRecyclerViews() {
        // Profile Info RecyclerView
        RecyclerView rvProfileInfo = view.findViewById(R.id.rv_profile_info);
        if (rvProfileInfo.getAdapter() == null) {
            rvProfileInfo.setLayoutManager(new LinearLayoutManager(getContext()));
            List<ProfileInfo> profileData = new ArrayList<>();
            adapter = new ProfileInfoAdapter(profileData);
            rvProfileInfo.setAdapter(adapter);
        }

        // Shipping Address RecyclerView - chỉ setup một lần
        RecyclerView rvShipping = view.findViewById(R.id.rv_shipping_address);
        if (rvShipping.getLayoutManager() == null) {
            rvShipping.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        // Load shipping addresses from Firebase
        loadShippingAddresses();
    }

    private void setupClickListeners() {
        // Back button
        LinearLayout btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });
        // Edit profile buttons

        ImageView iconEditName = view.findViewById(R.id.iconEditName);

        // Avatar edit buttons
        btnEditAvatar.setOnClickListener(v -> showBottomSheetDialog());
        imgAvatar.setOnClickListener(v -> showBottomSheetDialog());
        // EditShippingFragment khi click vào layoutAddShipping
        layoutAddShipping.setOnClickListener(v -> {
            requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new EditShippingFragment())
                .addToBackStack(null)
                .commit();
        });
    }

    private void openEditPersonalFragment(Customer customer) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("customer", customer);

        EditPersonalFragment fragment = new EditPersonalFragment();
        fragment.setArguments(bundle);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
    private void loadCustomerInformation() {
        if (!isAdded() || getContext() == null) return;

        String userId = sessionManager.getUid();
        if (userId == null || userId.isEmpty()) {
            Log.d(TAG, "User not logged in");
            initializeEmptyProfile();
            return;
        }

        DocumentReference documentReference = FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(userId);

        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            if (!isAdded() || getContext() == null) return;

            if (documentSnapshot.exists()) {
                try {
                    Customer customer = documentSnapshot.toObject(Customer.class);
                    if (customer != null) {
                        updateProfileInfo(customer);
                        updateUserName(customer);
                        updateAvatar(customer);
                        layoutEditProfile.setOnClickListener(v -> {
                            if (customer != null) openEditPersonalFragment(customer);
                        });
                        name_with_icon.setOnClickListener(v -> {
                            if (customer != null) openEditPersonalFragment(customer);
                        });
                    } else {
                        handleManualExtraction(documentSnapshot);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error converting document to Customer object", e);
                    handleManualExtraction(documentSnapshot);
                }
            } else {
                Log.d(TAG, "No customer document found");
                initializeEmptyProfile();
            }
        }).addOnFailureListener(e -> {
            if (!isAdded() || getContext() == null) return;
            Log.e(TAG, "Error loading customer data", e);
            initializeEmptyProfile();
        });
    }

    private void handleManualExtraction(com.google.firebase.firestore.DocumentSnapshot documentSnapshot) {
        Customer customer = new Customer();
        customer.setName(documentSnapshot.getString("Name"));
        customer.setGender(documentSnapshot.getString("Gender"));
        customer.setMail(documentSnapshot.getString("Mail"));
        customer.setPhone(documentSnapshot.getString("Phone"));
        customer.setAddress(documentSnapshot.getString("Address"));
        customer.setImage(documentSnapshot.getString("Image"));
        customer.setDOB(documentSnapshot.get("DOB"));

        updateProfileInfo(customer);
        updateUserName(customer);
        updateAvatar(customer);
    }

    private void updateProfileInfo(Customer customer) {
        List<ProfileInfo> profileInfoList = new ArrayList<>();

        // Email
        String email = (customer.getMail() == null || customer.getMail().trim().isEmpty()) ? "" : customer.getMail();
        profileInfoList.add(new ProfileInfo("Email", email, R.drawable.ic_email));

        // Phone
        String phone = (customer.getPhone() == null || customer.getPhone().trim().isEmpty()) ? "" : customer.getPhone();
        profileInfoList.add(new ProfileInfo("Phone", phone, R.drawable.ic_phone));

        // Date of Birth
        String dobString = customer.getDOBString();
        dobString = (dobString == null || dobString.trim().isEmpty()) ? "" : dobString;
        profileInfoList.add(new ProfileInfo("Date of Birth", dobString, R.drawable.ic_calendar));

        // Address
        String address = (customer.getAddress() == null || customer.getAddress().trim().isEmpty()) ? "" : customer.getAddress();
        profileInfoList.add(new ProfileInfo("Location", address, R.drawable.ic_location));

        // Update adapter
        if (adapter != null) {
            adapter.setData(profileInfoList);
        }
    }

    private void updateUserName(Customer customer) {
        if (txtName != null) {
            if (customer.getName() != null && !customer.getName().trim().isEmpty()) {
                txtName.setText(customer.getName());
            } else {
                txtName.setText(""); // Để trống nếu null
            }
        }
    }

    private void updateAvatar(Customer customer) {
        if (imgAvatar != null && getContext() != null) {
            if (customer.getImage() != null && !customer.getImage().trim().isEmpty()) {
                Glide.with(requireContext())
                        .load(customer.getImage())
                        .placeholder(R.mipmap.sample_avt)
                        .error(R.mipmap.sample_avt)
                        .circleCrop()
                        .into(imgAvatar);
            } else {
                // Sử dụng avatar mặc định nếu không có ảnh
                imgAvatar.setImageResource(R.mipmap.sample_avt);
            }
        }
    }

    private void initializeEmptyProfile() {
        if (adapter != null) {
            adapter.setData(new ArrayList<>());
        }
        if (txtName != null) {
            txtName.setText("");
        }
        if (imgAvatar != null) {
            imgAvatar.setImageResource(R.mipmap.sample_avt);
        }
    }

    private void loadShippingAddresses() {
        if (!isAdded() || getContext() == null) {
            Log.d("Shipping", "Fragment not attached, skip loading");
            return;
        }

        String uid = sessionManager.getUid();
        if (uid == null) {
            Log.e("Shipping", "User ID is null");
            updateShippingUI(new ArrayList<>());
            return;
        }

        Log.d("Shipping", "Loading shipping addresses for user: " + uid);

        FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || getContext() == null) {
                        Log.d("Shipping", "Fragment detached during load, ignoring result");
                        return;
                    }

                    if (!documentSnapshot.exists()) {
                        Log.d("Shipping", "Customer document not found");
                        updateShippingUI(new ArrayList<>());
                        return;
                    }

                    List<Map<String, Object>> rawList = (List<Map<String, Object>>) documentSnapshot.get("ShippingAddresses");

                    if (rawList == null || rawList.isEmpty()) {
                        Log.d("Shipping", "No shipping addresses found");
                        updateShippingUI(new ArrayList<>());
                        return;
                    }

                    Log.d("Shipping", "Found " + rawList.size() + " shipping addresses");

                    List<ShippingAddress> shippingAddresses = new ArrayList<>();
                    for (Map<String, Object> item : rawList) {
                        String name = item.get("Name") != null ? item.get("Name").toString() : "";
                        String phone = item.get("Phone") != null ? item.get("Phone").toString() : "";
                        String address = item.get("Address") != null ? item.get("Address").toString() : "";
                        boolean isDefault = item.get("IsDefault") != null && (Boolean) item.get("IsDefault");

                        shippingAddresses.add(new ShippingAddress(name, phone, address, isDefault));
                        Log.d("Shipping", "Added address: " + name + " - " + address);
                    }

                    updateShippingUI(shippingAddresses);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) {
                        Log.d("Shipping", "Fragment detached during error, ignoring");
                        return;
                    }
                    Log.e("Shipping", "Failed to load shipping addresses", e);
                    updateShippingUI(new ArrayList<>());
                });
    }
    private void setRecyclerViewHeight(RecyclerView recyclerView) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter == null) return;

        int totalHeight = 160;
        for (int i = 0; i < adapter.getItemCount(); i++) {
            RecyclerView.ViewHolder holder = adapter.createViewHolder(recyclerView, adapter.getItemViewType(i));
            adapter.onBindViewHolder(holder, i);
            holder.itemView.measure(
                    View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.UNSPECIFIED
            );
            totalHeight += holder.itemView.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        params.height = totalHeight;
        recyclerView.setLayoutParams(params);
    }
    private void updateShippingUI(List<ShippingAddress> shippingAddresses) {
        if (!isAdded() || getContext() == null) {
            return;
        }

        RecyclerView rvShipping = view.findViewById(R.id.rv_shipping_address);

        if (shippingAddresses.isEmpty()) {
            txtNoShippingAddress.setVisibility(View.VISIBLE);
            rvShipping.setVisibility(View.GONE);
            Log.d("Shipping", "No addresses to display");
        } else {
            txtNoShippingAddress.setVisibility(View.GONE);
            rvShipping.setVisibility(View.VISIBLE);

            Log.d("Shipping", "Displaying " + shippingAddresses.size() + " addresses");

            // Tạo adapter mới hoặc cập nhật adapter hiện tại
            ShippingAddressAdapter adapter = new ShippingAddressAdapter(shippingAddresses, address -> {
                // Mở EditShippingFragment khi click
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

            rvShipping.setAdapter(adapter);
            rvShipping.post(() -> setRecyclerViewHeight(rvShipping));

            // Log từng địa chỉ để debug
            for (int i = 0; i < shippingAddresses.size(); i++) {
                ShippingAddress addr = shippingAddresses.get(i);
                Log.d("Shipping", "Address " + i + ": " + addr.getName() + " - " + addr.getAddress());
            }
        }
    }


    private void openEditShippingFragment(ShippingAddress address) {
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

        if (imgAvatar != null && imgAvatar.getDrawable() != null) {
            imageView.setImageDrawable(imgAvatar.getDrawable());
        } else {
            imageView.setImageResource(R.mipmap.sample_avt);
        }

        builder.setView(imageView);
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dlg -> {
            Button closeButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (closeButton != null) {
                closeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_5E4C3E));
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
                            if (!isAdded() || getContext() == null) return;

                            String downloadUrl = uri.toString();
                            FirebaseFirestore.getInstance()
                                    .collection("Customers")
                                    .document(uid)
                                    .update("Image", downloadUrl)
                                    .addOnSuccessListener(unused -> {
                                        if (!isAdded() || getContext() == null) return;

                                        Glide.with(requireContext())
                                                .load(downloadUrl)
                                                .placeholder(R.mipmap.sample_avt)
                                                .circleCrop()
                                                .into(imgAvatar);

                                        Toast.makeText(getContext(), "Avatar updated successfully!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        if (!isAdded() || getContext() == null) return;
                                        Toast.makeText(getContext(), "Failed to update avatar.", Toast.LENGTH_SHORT).show();
                                        Log.e("ProfileDetail", "Failed to update Firestore", e);
                                    });
                        }))
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) return;
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
        TextView txtEdit = view.findViewById(R.id.txtEdit);
        TextView txtAddShipping = view.findViewById(R.id.txtAddShipping);
        if (txtPersonalInformation != null) {
            FontUtils.setZblackFont(getContext(), txtPersonalInformation);
        }
        if (txtShippingAddress != null) {
            FontUtils.setZblackFont(getContext(), txtShippingAddress);
        }
        if (txtEdit != null) {
            FontUtils.setZblackFont(getContext(), txtEdit);
        }
        if (txtAddShipping != null) {
            FontUtils.setZblackFont(getContext(), txtAddShipping);
        }
    }

    // Cart Badge Management
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
        if (!isAdded() || getContext() == null || getActivity() == null || txtCartBadge == null) {
            Log.w("CartBadge", "Fragment not attached, cannot update badge");
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
        Log.d("ProfileDetail", "onResume() called");

        // Chỉ tải lại dữ liệu, không tạo lại RecyclerView
        loadShippingAddresses();
        loadCustomerInformation(); // Tải lại thông tin customer nếu cần

        if (cartListener == null && isAdded() && getContext() != null &&
                sessionManager != null && txtCartBadge != null) {
            startCartBadgeListener();
        }
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
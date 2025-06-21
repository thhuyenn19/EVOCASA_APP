package com.mobile.evocasa.profile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.evocasa.R;
import com.mobile.models.ShippingAddress;
import com.mobile.utils.FontUtils;
import com.mobile.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditShippingFragment extends Fragment {

    private EditText edtName, edtPhone, edtAddress;
    private SwitchCompat switchDefault;
    LinearLayout btnDeleteEditShip;
    private ShippingAddress originalAddress;

    public EditShippingFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_shipping, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtName = view.findViewById(R.id.edtNameShip);
        edtPhone = view.findViewById(R.id.edtPhoneShip);
        edtAddress = view.findViewById(R.id.edtAddressShip);
        switchDefault = view.findViewById(R.id.switchDefault);
        btnDeleteEditShip = view.findViewById(R.id.btnDeleteEditShip);
        Button btnSave = view.findViewById(R.id.btnSave);
        ImageView btnBack = view.findViewById(R.id.imgProfileDetailsBack);

        // Nhận dữ liệu nếu có (chế độ sửa)
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("shippingAddress")) {
            originalAddress = (ShippingAddress) bundle.getSerializable("shippingAddress");
            if (originalAddress != null) {
                edtName.setText(originalAddress.getName());
                edtPhone.setText(originalAddress.getPhone());
                edtAddress.setText(originalAddress.getAddress());
                switchDefault.setChecked(originalAddress.isDefault());
            }
        } else {
            // Nếu là chế độ thêm mới, khởi tạo object rỗng để không bị null khi lưu
            originalAddress = null;
        }

        btnSave.setOnClickListener(v -> {
            if (originalAddress == null) {
                saveNewShipping(); // Thêm mới
            } else {
                saveUpdatedShipping(); // Sửa
            }
        });
        btnDeleteEditShip.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Confirm delete")
                    .setMessage("Are you sure you want to delete this address?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteShippingAddress())
                    .setNegativeButton("No", null)
                    .show();
        });
        btnBack.setOnClickListener(v -> showExitDialog());

        // Mở fragment chọn địa chỉ nếu cần
        edtAddress.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("currentShippingAddress", edtAddress.getText().toString());

            EditAddressFragment fragment = new EditAddressFragment();
            fragment.setArguments(b);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        requireActivity().getSupportFragmentManager().setFragmentResultListener(
                "shippingAddressUpdated",
                this,
                (requestKey, res) -> {
                    if (res != null) {
                        String newAddress = res.getString("selectedAddress", "");
                        edtAddress.setText(newAddress);
                    }
                });
        if (bundle != null && bundle.containsKey("shippingAddress")) {
            originalAddress = (ShippingAddress) bundle.getSerializable("shippingAddress");
            if (originalAddress != null) {
                edtName.setText(originalAddress.getName());
                edtPhone.setText(originalAddress.getPhone());
                edtAddress.setText(originalAddress.getAddress());
                switchDefault.setChecked(originalAddress.isDefault());

                btnDeleteEditShip.setVisibility(View.VISIBLE); // HIỆN nút xoá khi đang sửa
            }
        } else {
            originalAddress = null;
            btnDeleteEditShip.setVisibility(View.GONE); // ẨN nút xoá khi thêm mới
        }
    }

    private void deleteShippingAddress() {
        String uid = new UserSessionManager(requireContext()).getUid();
        if (uid == null || originalAddress == null) return;

        FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    List<Map<String, Object>> list = (List<Map<String, Object>>) doc.get("ShippingAddresses");
                    if (list == null) return;

                    // Tìm và xoá địa chỉ trùng (so sánh theo Address)
                    for (int i = 0; i < list.size(); i++) {
                        Map<String, Object> item = list.get(i);
                        if (item.get("Address").equals(originalAddress.getAddress())) {
                            list.remove(i);
                            break;
                        }
                    }

                    FirebaseFirestore.getInstance()
                            .collection("Customers")
                            .document(uid)
                            .update("ShippingAddresses", list)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(), "Deleted successfully", Toast.LENGTH_SHORT).show();
                                requireActivity().getSupportFragmentManager().popBackStack();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to delete address", Toast.LENGTH_SHORT).show();
                            });
                });
    }


    private void saveNewShipping() {
        String uid = new UserSessionManager(requireContext()).getUid();
        if (uid == null) return;

        Map<String, Object> newAddress = new HashMap<>();
        newAddress.put("Name", edtName.getText().toString().trim());
        newAddress.put("Phone", edtPhone.getText().toString().trim());
        newAddress.put("Address", edtAddress.getText().toString().trim());
        newAddress.put("IsDefault", switchDefault.isChecked());

        FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    List<Map<String, Object>> list = (List<Map<String, Object>>) doc.get("ShippingAddresses");
                    if (list == null) list = new ArrayList<>();

                    list.add(newAddress);

                    FirebaseFirestore.getInstance()
                            .collection("Customers")
                            .document(uid)
                            .update("ShippingAddresses", list)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(), "Address added successfully", Toast.LENGTH_SHORT).show();
                                requireActivity().getSupportFragmentManager().popBackStack();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to add address", Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void saveUpdatedShipping() {
        String uid = new UserSessionManager(requireContext()).getUid();
        if (uid == null || originalAddress == null) return;

        Map<String, Object> updated = new HashMap<>();
        updated.put("Name", edtName.getText().toString().trim());
        updated.put("Phone", edtPhone.getText().toString().trim());
        updated.put("Address", edtAddress.getText().toString().trim());
        updated.put("IsDefault", switchDefault.isChecked());

        FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    List<Map<String, Object>> list = (List<Map<String, Object>>) doc.get("ShippingAddresses");
                    if (list == null) list = new ArrayList<>();

                    for (int i = 0; i < list.size(); i++) {
                        Map<String, Object> item = list.get(i);
                        if (item.get("Address").equals(originalAddress.getAddress())) {
                            list.set(i, updated);
                            break;
                        }
                    }

                    FirebaseFirestore.getInstance()
                            .collection("Customers")
                            .document(uid)
                            .update("ShippingAddresses", list)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(getContext(), "Address updated successfully", Toast.LENGTH_SHORT).show();
                                requireActivity().getSupportFragmentManager().popBackStack();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to update address", Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void showExitDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.custom_exit_dialog);
        dialog.setCancelable(true);

        Button btnExit = dialog.findViewById(R.id.btn_exit);
        Button btnSave = dialog.findViewById(R.id.btn_save);
        ImageView btnClose = dialog.findViewById(R.id.btn_close_icon);

        btnExit.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
            dialog.dismiss();
        });

        btnSave.setOnClickListener(v -> {
            saveUpdatedShipping();
            saveNewShipping();
            dialog.dismiss();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }
}
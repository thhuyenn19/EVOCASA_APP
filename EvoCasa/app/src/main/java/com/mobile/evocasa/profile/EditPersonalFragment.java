package com.mobile.evocasa.profile;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.evocasa.R;
import com.mobile.models.Customer;
import com.mobile.utils.UserSessionManager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditPersonalFragment extends Fragment {

    private EditText edtName, edtEmail, edtPhone, edtBirthday, edtLocation;
    private AutoCompleteTextView genderDropdown;
    private ImageView imgProfileDetailsBack;
    private Customer customer;
    private View view;
    private UserSessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit_personal, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new UserSessionManager(requireContext());

        edtName = view.findViewById(R.id.edtName);
        edtEmail = view.findViewById(R.id.edtEmail);
        edtPhone = view.findViewById(R.id.edtPhone);
        edtBirthday = view.findViewById(R.id.edtBirthday);
        edtLocation = view.findViewById(R.id.edtLocation);
        genderDropdown = view.findViewById(R.id.autoGender);
        Button btnSave = view.findViewById(R.id.btnSave);

        // Nhận dữ liệu Customer từ arguments
        customer = (Customer) getArguments().getSerializable("customer");
        if (customer != null) {
            edtName.setText(customer.getName());
            edtEmail.setText(customer.getMail());
            edtPhone.setText(customer.getPhone());
            edtBirthday.setText(customer.getDOBString());
            genderDropdown.setText(customer.getGender(), false);
            edtLocation.setText(customer.getAddress());
        }

        // Dropdown giới tính
        String[] genderOptions = {"Male", "Female"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(requireContext(), R.layout.item_dropdown_gender, genderOptions);
        genderDropdown.setAdapter(genderAdapter);
        genderDropdown.setOnClickListener(v -> genderDropdown.showDropDown());

        // DatePicker cho ngày sinh
        edtBirthday.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(
                    requireContext(),
                    R.style.DatePickerDialogTheme,
                    (datePicker, year, month, day) -> {
                        String selected = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
                        edtBirthday.setText(selected);
                    },
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Mở fragment chọn địa chỉ nếu cần
        edtLocation.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("currentAddress", edtLocation.getText().toString());

            EditLocationFragment fragment = new EditLocationFragment();
            fragment.setArguments(bundle);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        getParentFragmentManager().setFragmentResultListener("addressUpdated", this, (requestKey, bundle) -> {
            String newAddress = bundle.getString("selectedAddress", "");
            edtLocation.setText(newAddress);
        });

        // Sự kiện nút Save
        btnSave.setOnClickListener(v -> saveUpdatedInfo());
        imgProfileDetailsBack = view.findViewById(R.id.imgProfileDetailsBack);
        imgProfileDetailsBack.setOnClickListener(v -> {
            Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(R.layout.custom_exit_dialog);
            dialog.setCancelable(true);

            Button btnExit = dialog.findViewById(R.id.btn_exit);
            Button btnSaveInDialog = dialog.findViewById(R.id.btn_save); // đổi tên tránh trùng với btnSave ở trên
            ImageView btnExitIcon = dialog.findViewById(R.id.btn_close_icon);

            // Nhấn EXIT: quay lại mà không cập nhật
            btnExit.setOnClickListener(confirmView -> {
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ProfileDetailFragment())
                        .addToBackStack(null)
                        .commit();
                dialog.dismiss();
            });

            // Nhấn SAVE: cập nhật rồi quay lại
            btnSaveInDialog.setOnClickListener(saveView -> {
                saveUpdatedInfo(); // sẽ tự popBackStack sau khi cập nhật
                dialog.dismiss();
            });

            // Nhấn icon X: chỉ đóng dialog
            btnExitIcon.setOnClickListener(xView -> {
                dialog.dismiss();
            });

            dialog.show();
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        });


    }

    private void saveUpdatedInfo() {
        String uid = sessionManager.getUid();
        if (uid == null || uid.isEmpty()) {
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("Name", edtName.getText().toString().trim());
        updates.put("Mail", edtEmail.getText().toString().trim());
        updates.put("Phone", edtPhone.getText().toString().trim());
        updates.put("Gender", genderDropdown.getText().toString().trim());
        updates.put("Address", edtLocation.getText().toString().trim());
        updates.put("DOB", edtBirthday.getText().toString().trim());

        FirebaseFirestore.getInstance()
                .collection("Customers")
                .document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                });
    }
}


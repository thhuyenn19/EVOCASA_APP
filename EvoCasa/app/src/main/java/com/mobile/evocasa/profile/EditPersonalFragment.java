package com.mobile.evocasa.profile;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.mobile.evocasa.HomeFragment;
import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;

import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditPersonalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditPersonalFragment extends Fragment {

    private View view;

    private ImageView imgProfileDetailsBack;

    private Button btnProfileDetailsBack;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EditPersonalFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditPersonalFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditPersonalFragment newInstance(String param1, String param2) {
        EditPersonalFragment fragment = new EditPersonalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("WrongViewCast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_edit_personal, container, false);
        view = inflater.inflate(R.layout.fragment_edit_personal, container, false);

        //set font
        TextView txtTitle = view.findViewById(R.id.txtTitle);
        FontUtils.setZboldFont(requireContext(), txtTitle);

//        // Gán sự kiện quay lại ProfilDetailsFragment
//        imgProfileDetailsBack = view.findViewById(R.id.imgProfileDetailsBack);
//        imgProfileDetailsBack.setOnClickListener(v -> {
//            requireActivity()
//                    .getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.fragment_container, new ProfileDetailFragment())
//                    .addToBackStack(null)
//                    .commit();
//        });

        imgProfileDetailsBack = view.findViewById(R.id.imgProfileDetailsBack);
        imgProfileDetailsBack.setOnClickListener(v -> {
            // Tạo và hiển thị custom dialog
            Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(R.layout.custom_exit_dialog);
            dialog.setCancelable(true); // Hoặc false nếu bạn không muốn người dùng bấm ra ngoài để đóng

            // Ánh xạ các nút trong custom_exit_dialog (ví dụ: Confirm và Cancel)
            Button btnExit = dialog.findViewById(R.id.btn_exit);
            Button btnSave = dialog.findViewById(R.id.btn_save);

            btnExit.setOnClickListener(confirmView -> {
                // Xử lý khi người dùng chọn xác nhận (ví dụ: thoát Fragment, hoặc thoát Activity)
                requireActivity().finish(); // hoặc popBackStack(), hoặc hành động khác
                dialog.dismiss();
            });

            btnSave.setOnClickListener(cancelView -> {
                // Đóng dialog nếu người dùng huỷ
                dialog.dismiss();
            });

            dialog.show();

            // Cài đặt lại kích thước và nền trong suốt cho dialog
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        });


        return view;
    }

    //Set up cho Male/Female & Birthday
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gender dropdown setup
        AutoCompleteTextView genderDropdown = view.findViewById(R.id.autoGender);
        String[] genderOptions = {"Male", "Female"};

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                genderOptions
        );
        genderDropdown.setAdapter(genderAdapter);

        // Show dropdown when clicking the field
        genderDropdown.setOnClickListener(v -> genderDropdown.showDropDown());

        // Birthday DatePicker setup
        EditText edtBirthday = view.findViewById(R.id.edtBirthday);
        edtBirthday.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (datePicker, year, month, day) -> {
                String selected = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
                edtBirthday.setText(selected);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        //Address
        EditText edtLocation = view.findViewById(R.id.edtLocation);
        edtLocation.setOnClickListener(v -> {
            // Chuyển sang EditAddressFragment
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new EditAddressFragment())
                    .addToBackStack(null)
                    .commit();
        });

    }
}
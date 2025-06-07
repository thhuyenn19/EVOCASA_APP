package com.mobile.evocasa.auth;

import android.graphics.Typeface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.mobile.evocasa.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Forgot4Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Forgot4Fragment extends Fragment {

    private Button btnSignIn;
    private TextView txtSuccess, txtDescription;

    public Forgot4Fragment() {
        // Required empty public constructor
    }

    public static Forgot4Fragment newInstance(String param1, String param2) {
        Forgot4Fragment fragment = new Forgot4Fragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Lấy dữ liệu từ bundle nếu có
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_forgot4, container, false);

        // Lấy tham chiếu đến các TextView và Button
        btnSignIn = rootView.findViewById(R.id.btnSignIn);
        txtSuccess = rootView.findViewById(R.id.txtSucess);
        txtDescription = rootView.findViewById(R.id.txtDescription);

        // Tạo Typeface từ font trong assets
        Typeface customFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Inter-Regular.otf");

        // Áp dụng font cho các TextView và Button
        txtSuccess.setTypeface(customFont);
        txtDescription.setTypeface(customFont);
        btnSignIn.setTypeface(customFont);

        // Thiết lập sự kiện click cho nút
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Khi bấm vào nút, chuyển sang SignInFragment
                SignInFragment signInFragment = new SignInFragment();

                // Thực hiện thay đổi fragment
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, signInFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return rootView;
    }
}

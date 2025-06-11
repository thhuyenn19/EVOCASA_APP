package com.mobile.evocasa.auth;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUp5Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUp5Fragment extends Fragment {

    private Button btnSignIn;
    private TextView txtSuccess, txtDescription;

    public SignUp5Fragment() {
        // Required empty public constructor
    }

    public static SignUp5Fragment newInstance(String param1, String param2) {
        SignUp5Fragment fragment = new SignUp5Fragment();
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
        View rootView = inflater.inflate(R.layout.fragment_sign_up5, container, false);

        // Tham chiếu UI
        btnSignIn = rootView.findViewById(R.id.btnSignIn);
        txtSuccess = rootView.findViewById(R.id.txtSucess); // hoặc R.id.txtTitle nếu bạn nhầm tên
        txtDescription = rootView.findViewById(R.id.txtDescription);

        // Áp dụng font
        FontUtils.setBoldFont(getContext(), txtSuccess);
        txtDescription.setTypeface(FontUtils.getMediumitalic(getContext()));
        btnSignIn.setTypeface(FontUtils.getMedium(getContext()));

        // Xử lý sự kiện
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInFragment signInFragment = new SignInFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, signInFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return rootView;
    }
}

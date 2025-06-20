package com.mobile.evocasa;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mobile.evocasa.helpcenter.HelpCenterFragment;
import com.mobile.evocasa.profile.ProfileDetailFragment;
import com.mobile.evocasa.R;

public class SettingFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout btnAccountInfo = view.findViewById(R.id.btnAccountInfo);
        LinearLayout btnHelpCenter = view.findViewById(R.id.btnHelpCenter);
        LinearLayout btnChangeLanguage = view.findViewById(R.id.btnChangeLanguage);
        LinearLayout btnChangePassword = view.findViewById(R.id.btnChangePassword);
        LinearLayout btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);

        btnAccountInfo.setOnClickListener(v -> openFragment(new ProfileDetailFragment()));
        btnHelpCenter.setOnClickListener(v -> openFragment(new HelpCenterFragment()));
        btnChangeLanguage.setOnClickListener(v ->
                Toast.makeText(getContext(), "Coming soon: Language Selection", Toast.LENGTH_SHORT).show());
        btnChangePassword.setOnClickListener(v ->
                Toast.makeText(getContext(), "Coming soon: Password Change", Toast.LENGTH_SHORT).show());
        btnDeleteAccount.setOnClickListener(v ->
                Toast.makeText(getContext(), "Feature not available yet.", Toast.LENGTH_SHORT).show());
    }

    private void openFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}

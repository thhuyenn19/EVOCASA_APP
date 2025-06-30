package com.mobile.evocasa;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.evocasa.helpcenter.HelpCenterFragment;
import com.mobile.evocasa.profile.ProfileDetailFragment;
import com.mobile.utils.UserSessionManager;
import com.mobile.evocasa.R;

import java.util.Locale;

public class SettingFragment extends Fragment {

    private static final String TAG = "SettingFragment";
    private static final String PREFS_NAME = "LanguagePrefs";
    private static final String KEY_LANGUAGE = "language";

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

        btnAccountInfo.setOnClickListener(v -> ((NarBarActivity) requireActivity()).switchToFragment(new ProfileDetailFragment()));
        btnHelpCenter.setOnClickListener(v -> ((NarBarActivity) requireActivity()).switchToFragment(new HelpCenterFragment()));
        btnChangeLanguage.setOnClickListener(v -> showLanguageDialog());
        btnChangePassword.setOnClickListener(v ->
                Toast.makeText(getContext(), getString(R.string.coming_soon, getString(R.string.password_change)), Toast.LENGTH_SHORT).show());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void showLanguageDialog() {
        if (!isAdded() || getActivity() == null) {
            Toast.makeText(getContext(), R.string.cannot_show_dialog, Toast.LENGTH_SHORT).show();
            return;
        }

        String[] languages = {"English", "Tiếng Việt"};
        String[] languageCodes = {"en", "vi"};

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String currentLanguage = prefs.getString(KEY_LANGUAGE, "en");
        int selectedPosition = 0;
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLanguage)) {
                selectedPosition = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.language_selection);
        builder.setSingleChoiceItems(languages, selectedPosition, (dialog, which) -> {
            String selectedLanguageCode = languageCodes[which];
            if (!selectedLanguageCode.equals(currentLanguage)) {
                setLocale(selectedLanguageCode);
                Toast.makeText(getContext(), "Language changed. Please restart the app.", Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();
        });

        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setCancelable(true);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setLocale(String languageCode) {
        try {
            // Lưu ngôn ngữ đã chọn vào SharedPreferences
            SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_LANGUAGE, languageCode);
            editor.apply();

            // Áp dụng ngôn ngữ mới mà không recreate
            Locale locale = new Locale(languageCode);
            Locale.setDefault(locale);

            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.setLocale(locale);
            res.updateConfiguration(conf, dm);

        } catch (Exception e) {
            Log.e(TAG, "Error setting locale", e);
            Toast.makeText(getContext(), "Error changing language", Toast.LENGTH_SHORT).show();
        }
    }
    private void showDeleteAccountDialog() {
        if (!isAdded() || getActivity() == null) {
            Toast.makeText(getContext(), R.string.cannot_show_dialog, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_account_title)
                .setMessage(R.string.delete_account_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> deleteAccount())
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .create()
                .show();
    }

    private void deleteAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserSessionManager sessionManager = new UserSessionManager(requireContext());
        String uid = sessionManager.getUid();

        if (user == null || uid == null || uid.isEmpty()) {
            Toast.makeText(getContext(), R.string.no_user, Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Account")
                .document(uid)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully deleted account data from Account collection");

                    user.delete()
                            .addOnSuccessListener(aVoid1 -> {
                                sessionManager.clearSession();
                                Toast.makeText(getContext(), R.string.delete_success, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra("openFragment", "SignIn");
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to delete auth account", e);
                                Toast.makeText(getContext(), getString(R.string.delete_failure, e.getMessage()), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete account data", e);
                    Toast.makeText(getContext(), getString(R.string.delete_failure, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Không cần kiểm tra và áp dụng ngôn ngữ ở đây nữa
    }
}
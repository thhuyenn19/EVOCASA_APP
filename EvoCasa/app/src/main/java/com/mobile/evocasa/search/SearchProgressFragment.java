package com.mobile.evocasa.search;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.adapters.SearchHistoryAdapter;
import com.mobile.evocasa.R;
import com.mobile.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SearchProgressFragment extends Fragment {

    private EditText edtSearch;
    private ImageView imgSearch, imgMic, btnBack;
    private TextView txtClearHistory;
    private RecyclerView recyclerView;

    private SearchHistoryAdapter adapter;
    private List<String> fullHistory = new ArrayList<>();

    private static final String PREF_NAME = "search_history";
    private static final String KEY_PREFIX = "history_";

    private final ActivityResultLauncher<Intent> voiceInputLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        String voiceText = matches.get(0);
                        edtSearch.setText(voiceText);
                        edtSearch.setSelection(voiceText.length());
                        showKeyboard();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_progress, container, false);

        // Bind View
        edtSearch = view.findViewById(R.id.edtSearch);
        imgSearch = view.findViewById(R.id.imgSearch);
        imgMic = view.findViewById(R.id.imgMic);
        btnBack = view.findViewById(R.id.imgBack);
        txtClearHistory = view.findViewById(R.id.txtClearHistory);
        recyclerView = view.findViewById(R.id.recyclerHistory);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchHistoryAdapter();
        recyclerView.setAdapter(adapter);

        String userId = new UserSessionManager(requireContext()).getUid();
        clearOldFormatHistoryIfNeeded(requireContext(), userId);  // ✅ Thêm dòng này
        updateHistory(userId);

        // Sự kiện xoá lịch sử
        txtClearHistory.setOnClickListener(v -> {
            SearchHistoryManager.clearHistory(requireContext(), userId);
            updateHistory(userId);
        });

        // Sự kiện tìm kiếm
        imgSearch.setOnClickListener(v -> performSearch());

        // Sự kiện bàn phím
        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                performSearch();
                return true;
            }
            return false;
        });

        // Sự kiện mic
        imgMic.setOnClickListener(v -> startVoiceInput());

        // Quay lại
        btnBack.setOnClickListener(v -> requireActivity().finish());

        // Nhận keyword nếu quay từ SearchResultFragment
        Bundle args = getArguments();
        if (args != null) {
            String keyword = args.getString("keywordFromVoice", "");
            if (keyword.isEmpty()) {
                keyword = args.getString("keywordFromSearchResult", "");
            }
            if (!keyword.isEmpty()) {
                edtSearch.setText(keyword);
                edtSearch.setSelection(keyword.length());
                showKeyboard();
                updateHistory(userId);
            }
        }

        // Auto focus bàn phím (lúc mở fragment)
        edtSearch.postDelayed(() -> {
            edtSearch.requestFocus();
            edtSearch.setSelection(edtSearch.getText().length());
            showKeyboard();
        }, 150);

        // Click item trong lịch sử
        adapter.setOnItemClickListener(keyword -> {
            edtSearch.setText(keyword);
            performSearch();
        });

        return view;
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hãy nói từ khóa tìm kiếm...");
        try {
            voiceInputLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Thiết bị không hỗ trợ giọng nói", Toast.LENGTH_SHORT).show();
        }
    }

    private void performSearch() {
        String keyword = edtSearch.getText().toString().trim();
        if (!keyword.isEmpty()) {
            String userId = new UserSessionManager(getContext()).getUid();
            SearchHistoryManager.saveSearch(getContext(), userId, keyword);

            FragmentTransaction ft = getParentFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, SearchResultFragment.newInstance(keyword, false));
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(edtSearch, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void updateHistory(String userId) {
        fullHistory = SearchHistoryManager.getHistory(requireContext(), userId);
        Log.d("SearchProgress", "Lịch sử lấy được: " + fullHistory.size() + " phần tử");

        adapter.setData(fullHistory);

        // 👉 Hiện/ẩn nút "Xoá lịch sử"
        if (fullHistory.isEmpty()) {
            txtClearHistory.setVisibility(View.GONE);
        } else {
            txtClearHistory.setVisibility(View.VISIBLE);
        }
    }
    public static void clearOldFormatHistoryIfNeeded(Context context, String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String key = KEY_PREFIX + userId;

        try {
            Object rawValue = prefs.getAll().get(key);
            if (rawValue instanceof Set) {
                Log.d("SearchHistoryFix", "Định dạng cũ (Set<String>) được phát hiện. Đang xoá...");
                prefs.edit().remove(key).apply();
            }
        } catch (Exception e) {
            Log.e("SearchHistoryFix", "Lỗi khi kiểm tra định dạng dữ liệu cũ", e);
        }
    }
}

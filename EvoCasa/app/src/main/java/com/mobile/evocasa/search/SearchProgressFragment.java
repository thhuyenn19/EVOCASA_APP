package com.mobile.evocasa.search;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mobile.adapters.SearchHistoryAdapter;
import com.mobile.adapters.SuggestionAdapter;
import com.mobile.evocasa.R;
import com.mobile.utils.UserSessionManager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SearchProgressFragment extends Fragment {
    private ImageView imgClearText; // Nút xóa text
    private EditText edtSearch;
    private ImageView imgSearch, imgMic, btnBack;
    private TextView txtClearHistory;
    private RecyclerView recyclerView;

    private SearchHistoryAdapter adapter;
    private List<String> fullHistory = new ArrayList<>();

    private static final String PREF_NAME = "search_history";
    private static final String KEY_PREFIX = "history_";
    private RecyclerView recyclerSuggestions;
    private SuggestionAdapter suggestionAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private LinearLayout layoutSearchHistory;

    private final ActivityResultLauncher<Intent> voiceInputLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        String voiceText = matches.get(0);
                        edtSearch.setText(voiceText);
                        fetchSuggestionsFromFirestore(voiceText.toLowerCase());
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
        layoutSearchHistory = view.findViewById(R.id.layoutSearchHistory);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchHistoryAdapter();
        recyclerView.setAdapter(adapter);
        recyclerSuggestions = view.findViewById(R.id.recyclerSuggestions);
        suggestionAdapter = new SuggestionAdapter(new ArrayList<>(), keyword -> {
            edtSearch.setText(keyword);
            edtSearch.setSelection(keyword.length());
            triggerSearch(keyword);
        });
        recyclerSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerSuggestions.setAdapter(suggestionAdapter);
        recyclerSuggestions.setVisibility(View.GONE);

        // ✅ TextWatcher được cải thiện hơn nữa
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                // Xóa debounce cũ
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                if (query.isEmpty()) {
                    // Khi chưa nhập gì -> hiện lịch sử
                    showSearchHistory();
                    recyclerSuggestions.setVisibility(View.GONE);
                } else if (query.length() >= 1) { // Bắt đầu gợi ý từ 1 ký tự
                    // Khi bắt đầu nhập -> ẩn lịch sử
                    hideSearchHistory();

                    // Debounce search với delay ngắn hơn cho responsive
                    searchRunnable = () -> {
                        // Kiểm tra lại query hiện tại để tránh race condition
                        String currentQuery = edtSearch.getText().toString().trim();
                        if (currentQuery.equals(query) && !currentQuery.isEmpty()) {
                            fetchSuggestionsFromFirestore(currentQuery.toLowerCase());
                        }
                    };
                    handler.postDelayed(searchRunnable, 200); // Giảm delay từ 300ms xuống 200ms
                }
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        String userId = new UserSessionManager(requireContext()).getUid();
        clearOldFormatHistoryIfNeeded(requireContext(), userId);
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

    // ✅ Method để hiện lịch sử tìm kiếm
    private void showSearchHistory() {
        layoutSearchHistory.setVisibility(View.VISIBLE);
        recyclerSuggestions.setVisibility(View.GONE);
        Log.d("SearchProgress", "Hiện lịch sử tìm kiếm");
    }

    // ✅ Method để ẩn lịch sử tìm kiếm
    private void hideSearchHistory() {
        layoutSearchHistory.setVisibility(View.GONE);
        // Chưa hiện suggestions ngay, chờ kết quả từ Firestore
        Log.d("SearchProgress", "Ẩn lịch sử tìm kiếm");
    }

    private void triggerSearch(String s) {
        String userId = new UserSessionManager(getContext()).getUid();
        SearchHistoryManager.saveSearch(getContext(), userId, s);

        hideSearchHistory();
        recyclerSuggestions.setVisibility(View.GONE);

        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, SearchResultFragment.newInstance(s, false));
        ft.addToBackStack(null);
        ft.commit();
    }

    // ✅ Cải thiện logic fetch suggestions
    private void fetchSuggestionsFromFirestore(String query) {
        Log.d("SearchProgress", "Đang tìm gợi ý cho: " + query);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // ✅ Tìm kiếm theo tên sản phẩm - Sử dụng uppercase để tìm kiếm chính xác hơn
        String queryUpper = query.toUpperCase();

        db.collection("Product")
                .whereGreaterThanOrEqualTo("Name", queryUpper)
                .whereLessThanOrEqualTo("Name", queryUpper + "\uf8ff")
                .limit(50) // Tăng limit để có nhiều dữ liệu hơn để lọc
                .get()
                .addOnSuccessListener(snapshot -> {
                    Set<String> suggestionsSet = new LinkedHashSet<>();

                    for (DocumentSnapshot doc : snapshot) {
                        String name = doc.getString("Name");
                        if (name != null && !name.trim().isEmpty()) {
                            String originalName = name; // Giữ tên gốc
                            String lowerName = name.toLowerCase();
                            String queryLower = query.toLowerCase();

                            // ✅ 1. Thêm tên sản phẩm đầy đủ nếu bắt đầu bằng query
                            if (lowerName.startsWith(queryLower)) {
                                suggestionsSet.add(originalName);
                            }

                            // ✅ 2. Thêm tên sản phẩm đầy đủ nếu chứa query
                            if (lowerName.contains(queryLower) && !lowerName.startsWith(queryLower)) {
                                suggestionsSet.add(originalName);
                            }

                            // ✅ 3. Thêm từng từ trong tên sản phẩm
                            String[] words = lowerName.split("\\s+");
                            for (String word : words) {
                                if (word.startsWith(queryLower) && word.length() > queryLower.length()) {
                                    // Capitalize first letter of word
                                    String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1);
                                    suggestionsSet.add(capitalizedWord);
                                }
                            }

                            // Giới hạn số lượng gợi ý
                            if (suggestionsSet.size() >= 8) break;
                        }
                    }

                    // ✅ Thêm tìm kiếm bổ sung nếu kết quả ít
                    if (suggestionsSet.size() < 5) {
                        searchWithContains(db, query, suggestionsSet);
                    } else {
                        updateSuggestionsUI(new ArrayList<>(suggestionsSet), query);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SearchProgress", "Lỗi khi tìm gợi ý: " + e.getMessage());
                    // Thử tìm kiếm bổ sung khi có lỗi
                    searchWithContains(db, query, new LinkedHashSet<>());
                });
    }

    // ✅ Phương pháp tìm kiếm bổ sung bằng cách lấy tất cả và filter
    private void searchWithContains(FirebaseFirestore db, String query, Set<String> existingSuggestions) {
        Log.d("SearchProgress", "Tìm kiếm bổ sung cho: " + query);

        db.collection("Product")
                .limit(100) // Lấy 100 sản phẩm đầu tiên
                .get()
                .addOnSuccessListener(snapshot -> {
                    Set<String> suggestionsSet = new LinkedHashSet<>(existingSuggestions);
                    String queryLower = query.toLowerCase();

                    for (DocumentSnapshot doc : snapshot) {
                        String name = doc.getString("Name");
                        if (name != null && !name.trim().isEmpty()) {
                            String lowerName = name.toLowerCase();

                            // Tìm các từ khóa chứa query
                            if (lowerName.contains(queryLower)) {
                                suggestionsSet.add(name);
                            }

                            // Tìm từng từ
                            String[] words = lowerName.split("\\s+");
                            for (String word : words) {
                                if (word.startsWith(queryLower) && word.length() > queryLower.length()) {
                                    String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1);
                                    suggestionsSet.add(capitalizedWord);
                                }
                            }

                            if (suggestionsSet.size() >= 10) break;
                        }
                    }

                    updateSuggestionsUI(new ArrayList<>(suggestionsSet), query);
                })
                .addOnFailureListener(e -> {
                    Log.e("SearchProgress", "Lỗi tìm kiếm bổ sung: " + e.getMessage());
                    updateSuggestionsUI(new ArrayList<>(existingSuggestions), query);
                });
    }

    // ✅ Cập nhật UI suggestions
    private void updateSuggestionsUI(List<String> suggestions, String query) {
        Log.d("SearchProgress", "Tìm được " + suggestions.size() + " gợi ý cho '" + query + "': " + suggestions);

        if (getActivity() != null && isAdded()) {
            // Kiểm tra xem user vẫn đang gõ cùng query không
            String currentQuery = edtSearch.getText().toString().trim();
            if (currentQuery.equalsIgnoreCase(query)) {
                if (!suggestions.isEmpty()) {
                    suggestionAdapter.updateSuggestions(suggestions);
                    recyclerSuggestions.setVisibility(View.VISIBLE);
                    Log.d("SearchProgress", "Hiển thị " + suggestions.size() + " suggestions");
                } else {
                    recyclerSuggestions.setVisibility(View.GONE);
                    Log.d("SearchProgress", "Không có suggestions");
                }
            } else {
                Log.d("SearchProgress", "Query đã thay đổi, bỏ qua kết quả cũ");
            }
        } else {
            Log.d("SearchProgress", "Activity null hoặc Fragment không attached");
        }
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

            hideSearchHistory();
            recyclerSuggestions.setVisibility(View.GONE);

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

        // Hiện/ẩn nút "Xoá lịch sử"
        if (fullHistory.isEmpty()) {
            layoutSearchHistory.setVisibility(View.GONE);
            txtClearHistory.setVisibility(View.GONE);
        } else {
            // Chỉ hiện khi EditText trống
            if (edtSearch.getText().toString().trim().isEmpty()) {
                layoutSearchHistory.setVisibility(View.VISIBLE);
                txtClearHistory.setVisibility(View.VISIBLE);
            }
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
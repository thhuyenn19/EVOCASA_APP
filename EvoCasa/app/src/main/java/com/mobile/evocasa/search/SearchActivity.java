package com.mobile.evocasa.search;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.mobile.evocasa.R;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Xử lý hiển thị SearchProgressFragment khi được yêu cầu
        boolean openProgress = getIntent().getBooleanExtra("openProgress", false);
        String voiceKeyword = getIntent().getStringExtra("voiceKeyword");

        if (savedInstanceState == null && openProgress) {
            SearchProgressFragment fragment = new SearchProgressFragment();

            if (voiceKeyword != null && !voiceKeyword.isEmpty()) {
                Bundle args = new Bundle();
                args.putString("keywordFromVoice", voiceKeyword);
                fragment.setArguments(args);
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}

package com.mobile.evocasa.onboarding;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;

public class Onboarding5Activity extends AppCompatActivity {

    private TextView txtViewOnboarding5;
    private TextView txtView3;
    private TextView txtHaveAccount;
    private Button btnCreateAccountOnboarding5;
    private Button btnLogIn;


    private void typeTextWithCursor(final TextView textView, final String fullText, final long charDelay, final Runnable onComplete) {
        final int[] index = {0};
        final String cursor = "|";
        final boolean[] showCursor = {true};

        textView.setText("");
        final Runnable[] cursorRunnable = new Runnable[1];

        cursorRunnable[0] = new Runnable() {
            @Override
            public void run() {
                if (index[0] <= fullText.length()) {
                    String visibleText = fullText.substring(0, index[0]);
                    textView.setText(visibleText + (showCursor[0] ? cursor : ""));
                    showCursor[0] = !showCursor[0];
                    textView.postDelayed(this, 500);
                }
            }
        };
        textView.post(cursorRunnable[0]);

        Runnable typingRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] < fullText.length()) {
                    index[0]++;
                    textView.postDelayed(this, charDelay);
                } else {
                    textView.removeCallbacks(cursorRunnable[0]);
                    textView.postDelayed(() -> {
                        textView.setText(fullText);
                        if (onComplete != null) onComplete.run();
                    }, 800);
                }
            }
        };
        textView.postDelayed(typingRunnable, 300);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_onboarding5);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        txtViewOnboarding5 = findViewById(R.id.txtViewOnboarding5);
        txtView3 = findViewById(R.id.txtView3);
        txtHaveAccount = findViewById(R.id.txtHaveAccount);
        btnCreateAccountOnboarding5 = findViewById(R.id.btnCreateAccountOnboarding5);
        btnLogIn = findViewById(R.id.btnLogIn);

// Gán font
        FontUtils.setZblackFont(this, txtViewOnboarding5);
        FontUtils.setItalicFont(this, txtView3);
        FontUtils.setRegularFont(this, txtHaveAccount);
        FontUtils.setBoldFont(this, btnCreateAccountOnboarding5);
        FontUtils.setMediumFont(this, btnLogIn);


        //Animation
        // Lấy chuỗi từ strings.xml
        String line1 = getString(R.string.title_onboarding5_line_1);
        String line2 = getString(R.string.title_onboarding5_description);

        // Ẩn ban đầu
        txtViewOnboarding5.setVisibility(View.INVISIBLE);
        txtView3.setVisibility(View.INVISIBLE);

        txtViewOnboarding5.setVisibility(View.VISIBLE);
        typeTextWithCursor(txtViewOnboarding5, line1, 60, () -> {
            txtView3.setVisibility(View.VISIBLE);
            typeTextWithCursor(txtView3, line2, 40, null);
        });


    }
}
package com.mobile.evocasa.onboarding;

import android.os.Bundle;
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


    }
}
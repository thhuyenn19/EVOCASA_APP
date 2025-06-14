package com.mobile.evocasa.productdetails;

import android.content.Context;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;

public class DimensionsFragment extends Fragment {

    public DimensionsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dimensions, container, false);
        TextView txtDimensions = view.findViewById(R.id.txtDimensions);
        txtDimensions.setText("• Height: 24.5 inches\n• Base Width: 8 inches\n• Cord Length: 60 inches\n• Shade Diameter: 16 inches\n• Weight: 3.2 kg");
        Context context = null;
        txtDimensions.setTypeface(FontUtils.getRegular(null));
        return view;
    }
}

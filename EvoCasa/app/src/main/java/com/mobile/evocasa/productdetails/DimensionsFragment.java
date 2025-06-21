package com.mobile.evocasa.productdetails;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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

    public static final String ARG_DIMENSIONS = "dimensions";

    public DimensionsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dimensions, container, false);

        TextView txtDimensions = view.findViewById(R.id.txtDimensions);
        Log.d("DimensionsFragment", "onCreateView called");
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_DIMENSIONS)) {
            String dimensions = args.getString(ARG_DIMENSIONS);
            txtDimensions.setText(formatDimensions(dimensions));
            Log.d("DimensionsFragment", "Received and set dimensions: " + dimensions);
        } else {
            txtDimensions.setText("No dimensions available.");
            Log.w("DimensionsFragment", "No dimensions data received");
        }

        txtDimensions.setTypeface(FontUtils.getRegular(getContext()));
        return view;
    }

    private String formatDimensions(String dimensions) {
        if (dimensions == null) return "";
        String[] dimLines = dimensions.split("\n");
        StringBuilder formatted = new StringBuilder();
        for (String line : dimLines) {
            formatted.append("• ").append(line).append("\n");
        }
        return formatted.toString().trim();
    }
}
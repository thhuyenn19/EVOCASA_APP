package com.mobile.evocasa.productdetails;

import android.content.Context;
import android.graphics.text.LineBreaker;
import android.os.Build;
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

public class DescriptionFragment extends Fragment {

    public static final String ARG_DESCRIPTION = "description";

    public DescriptionFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_description, container, false);

        TextView txtDescription = view.findViewById(R.id.txtDescription);
        Log.d("DescriptionFragment", "onCreateView called");
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_DESCRIPTION)) {
            String description = args.getString(ARG_DESCRIPTION);
            txtDescription.setText(description);
            Log.d("DescriptionFragment", "Received and set description: " + description);
        } else {
            txtDescription.setText("No description available.");
            Log.w("DescriptionFragment", "No description data received");
        }

        txtDescription.setTypeface(FontUtils.getRegular(getContext()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            txtDescription.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }
        return view;
    }
}
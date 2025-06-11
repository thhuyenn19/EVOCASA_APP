package com.mobile.evocasa.productdetails;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mobile.evocasa.R;

public class DescriptionFragment extends Fragment {

    public DescriptionFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_description, container, false);

        TextView txtDescription = view.findViewById(R.id.txtDescription);
        txtDescription.setText("Oversized travertine table lamp at 24.5\" tall, this Asian inspired sculptural lamp has a weighted travertine base and verdigris metal body. An empire style metal shade and finial completes each piece. Rewired to US standard with neutral double twist cord.");
        return view;
    }
}

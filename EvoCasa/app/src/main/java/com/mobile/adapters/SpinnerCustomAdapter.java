package com.mobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mobile.evocasa.R;

import java.util.List;

public class SpinnerCustomAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> items;

    public SpinnerCustomAdapter(Context context, List<String> items) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_spinner_dropdown, parent, false);
        TextView txtItem = view.findViewById(R.id.txtItem);
        txtItem.setText(items.get(position));
        return view;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false);
        TextView txtItem = view.findViewById(R.id.spinnerText);
        txtItem.setText(items.get(position));
        return view;
    }
}

package com.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.evocasa.R;
import com.mobile.models.ProfileInfo;
import com.mobile.utils.FontUtils;

import java.util.List;

public class ProfileInfoAdapter extends RecyclerView.Adapter<ProfileInfoAdapter.InfoViewHolder>{
    private List<ProfileInfo> infoList;

    public ProfileInfoAdapter(List<ProfileInfo> infoList) {
        this.infoList = infoList;
    }

    @NonNull
    @Override
    public InfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile_info, parent, false);
        return new InfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoViewHolder holder, int position) {
        ProfileInfo item = infoList.get(position);
        holder.label.setText(item.getLabel());
        holder.value.setText(item.getValue());
        holder.icon.setImageResource(item.getIconRes());
        // ✅ Set font tại đây
        FontUtils.setRegularFont(holder.itemView.getContext(), holder.label);
        FontUtils.setRegularFont(holder.itemView.getContext(), holder.value);
    }

    @Override
    public int getItemCount() {
        return infoList.size();
    }

    public static class InfoViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView label, value;

        public InfoViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            label = itemView.findViewById(R.id.label);
            value = itemView.findViewById(R.id.value);
        }
    }
}

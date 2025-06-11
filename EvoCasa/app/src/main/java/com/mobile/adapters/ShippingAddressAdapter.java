package com.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.evocasa.R;
import com.mobile.models.ShippingAddress;
import com.mobile.utils.FontUtils;

import java.util.List;

public class ShippingAddressAdapter extends RecyclerView.Adapter<ShippingAddressAdapter.ViewHolder> {

    private List<ShippingAddress> list;
    private OnEditClickListener editClickListener;


    public ShippingAddressAdapter(List<ShippingAddress> list, OnEditClickListener listener) {
        this.list = list;
        this.editClickListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtDefaultTag, txtPhone, txtAddress, txtEditLabel;

        public ViewHolder(View view) {
            super(view);
            txtName = view.findViewById(R.id.txtName);
            txtDefaultTag = view.findViewById(R.id.txtDefaultTag);
            txtPhone = view.findViewById(R.id.txtPhone);
            txtAddress = view.findViewById(R.id.txtAddress);
            txtEditLabel = view.findViewById(R.id.txtEditLabel);
        }
    }

    @NonNull
    @Override
    public ShippingAddressAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shipping_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShippingAddress item = list.get(position);
        // Gán dữ liệu
        holder.txtName.setText(item.getName());
        holder.txtPhone.setText(item.getPhone());
        holder.txtAddress.setText(item.getAddress());
        holder.txtEditLabel.setText("Edit");

        if (item.isDefault()) {
            holder.txtDefaultTag.setVisibility(View.VISIBLE);
        } else {
            holder.txtDefaultTag.setVisibility(View.GONE);
        }


        holder.txtEditLabel.setOnClickListener(v -> {
            if (editClickListener != null) {
                editClickListener.onEditClick(item);
            }
        });


        // Set font:
        FontUtils.setSemiBoldFont(holder.itemView.getContext(), holder.txtName);            // tên = semi-bold
        FontUtils.setRegularFont(holder.itemView.getContext(), holder.txtDefaultTag);       // default tag = regular
        FontUtils.setRegularFont(holder.itemView.getContext(), holder.txtPhone);            // số điện thoại = regular
        FontUtils.setRegularFont(holder.itemView.getContext(), holder.txtAddress);
        FontUtils.setZblackFont(holder.itemView.getContext(), holder.txtEditLabel);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }



    //Click mở edit shipping
    public interface OnEditClickListener {
        void onEditClick(ShippingAddress address);
    }


}

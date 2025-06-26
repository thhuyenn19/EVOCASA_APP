package com.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.evocasa.R;
import com.mobile.models.ShippingAddress;
import com.mobile.utils.FontUtils;

import java.util.List;

public class ShippingAddressPaymentAdapter extends RecyclerView.Adapter<ShippingAddressPaymentAdapter.ViewHolder> {

    private final List<ShippingAddress> list;
    private final OnEditClickListener editClickListener;
    private final OnSelectClickListener selectClickListener;
    private int selectedPosition = -1;
    private ShippingAddress selectedShipping;
    public ShippingAddressPaymentAdapter(
            List<ShippingAddress> list,
            OnEditClickListener editClickListener,
            OnSelectClickListener selectClickListener,ShippingAddress selectedShipping) {
        this.list = list;
        this.editClickListener = editClickListener;
        this.selectClickListener = selectClickListener;
        this.selectedShipping = selectedShipping;
    }

    public interface OnEditClickListener {
        void onEditClick(ShippingAddress address);
    }
    public interface OnSelectClickListener {
        void onSelectClick(ShippingAddress address);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtPhone, txtAddress, txtEditLabel, txtDefaultTag;
        RadioButton radioSelect;
        LinearLayout btnEdit;

        public ViewHolder(View view) {
            super(view);
            txtName = view.findViewById(R.id.txtName);
            txtPhone = view.findViewById(R.id.txtPhone);
            txtAddress = view.findViewById(R.id.txtAddress);
            txtEditLabel = view.findViewById(R.id.txtEditLabel);
            txtDefaultTag = view.findViewById(R.id.txtDefaultTag);
            radioSelect = view.findViewById(R.id.radioSelect);
            btnEdit = view.findViewById(R.id.btnEdit); // 👈 THÊM DÒNG NÀY
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_choose_shipping_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShippingAddress item = list.get(position);

        holder.txtName.setText(item.getName());
        holder.txtPhone.setText(item.getPhone());
        holder.txtAddress.setText(item.getAddress());
        holder.txtDefaultTag.setVisibility(item.isDefault() ? View.VISIBLE : View.GONE);

        // Chỉ hiển thị RadioButton được chọn
        holder.radioSelect.setChecked(position == selectedPosition);

        holder.radioSelect.setButtonTintList(
                ContextCompat.getColorStateList(holder.itemView.getContext(), R.color.color_5E4C3E)
        );

        View.OnClickListener selectListener = v -> {
            int oldPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPos);
            notifyItemChanged(selectedPosition);
            if (selectClickListener != null) {
                selectClickListener.onSelectClick(item);
            }
        };

        holder.itemView.setOnClickListener(selectListener);
        holder.radioSelect.setOnClickListener(selectListener);

        holder.btnEdit.setOnClickListener(v -> {
            if (editClickListener != null) {
                editClickListener.onEditClick(item);
            }
        });

        // Font setup
        FontUtils.setSemiBoldFont(holder.itemView.getContext(), holder.txtName);
        FontUtils.setRegularFont(holder.itemView.getContext(), holder.txtPhone);
        FontUtils.setRegularFont(holder.itemView.getContext(), holder.txtAddress);
        FontUtils.setRegularFont(holder.itemView.getContext(), holder.txtDefaultTag);
        FontUtils.setZblackFont(holder.itemView.getContext(), holder.txtEditLabel);

        boolean isSelected = selectedShipping != null
                && selectedShipping.getAddress().equals(item.getAddress())
                && selectedShipping.getPhone().equals(item.getPhone());

        holder.radioSelect.setChecked(isSelected);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public ShippingAddress getSelectedAddress() {
        if (selectedPosition >= 0 && selectedPosition < list.size()) {
            return list.get(selectedPosition);
        }
        return null;
    }
}

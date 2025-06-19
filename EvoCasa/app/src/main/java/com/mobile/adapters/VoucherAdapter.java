package com.mobile.adapters;

import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.models.Voucher;
import com.mobile.evocasa.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {
    public interface OnVoucherSelectedListener {
        void onVoucherSelected(Voucher voucher);
    }

    private final List<Voucher> voucherList;
    private final OnVoucherSelectedListener listener;
    private int selectedPosition = -1;
    private double cartTotal; // Current cart total to check conditions

    // Fixed constructor - now properly accepts cartTotal parameter
    public VoucherAdapter(List<Voucher> list, double cartTotal, OnVoucherSelectedListener listener) {
        this.voucherList = list;
        this.cartTotal = cartTotal;
        this.listener = listener;
    }

    // NEW: Constructor with pre-selected voucher
    public VoucherAdapter(List<Voucher> list, double cartTotal, OnVoucherSelectedListener listener, Voucher selectedVoucher) {
        this.voucherList = list;
        this.cartTotal = cartTotal;
        this.listener = listener;

        // Find the position of the selected voucher
        if (selectedVoucher != null) {
            for (int i = 0; i < voucherList.size(); i++) {
                if (voucherList.get(i).getId().equals(selectedVoucher.getId())) {
                    selectedPosition = i;
                    break;
                }
            }
        }
    }

    public void updateCartTotal(double newTotal) {
        this.cartTotal = newTotal;
        // Reset selection if current voucher is no longer valid
        if (selectedPosition >= 0 && selectedPosition < voucherList.size()) {
            Voucher currentVoucher = voucherList.get(selectedPosition);
            if (!currentVoucher.isValid(newTotal)) {
                selectedPosition = -1;
            }
        }
        notifyDataSetChanged();
    }

    // NEW: Method to set selected voucher from outside
    public void setSelectedVoucher(Voucher voucher) {
        selectedPosition = -1;
        if (voucher != null) {
            for (int i = 0; i < voucherList.size(); i++) {
                if (voucherList.get(i).getId().equals(voucher.getId())) {
                    selectedPosition = i;
                    break;
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voucher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);
        holder.txtVoucherName.setText(voucher.getName());
        holder.txtVoucherDesc.setText("Discount: "+voucher.getDiscountPercent()+"%");
        holder.txtVoucherMax.setText("Max discount: $" + String.format("%.2f", voucher.getMaxDiscount()));

        // FIX: Convert Firebase Timestamp to Date before formatting
        String expireText = "EXP: ";
        if (voucher.getExpireDate() != null) {
            try {
                Date expireDate = voucher.getExpireDate().toDate(); // Convert Timestamp to Date
                expireText += new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(expireDate);
            } catch (Exception e) {
                expireText += "Invalid Date";
            }
        } else {
            expireText += "No Expire Date";
        }
        holder.txtVoucherExpire.setText(expireText);

        // Check if voucher can be applied to current order
        boolean isUsable = voucher.isValid(cartTotal);

        // Radio logic: only one voucher can be selected
        holder.radioVoucher.setChecked(selectedPosition == position);
        holder.radioVoucher.setEnabled(isUsable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.radioVoucher.setButtonTintList(null);
        }

        // If not eligible, dim the item and disable click
        holder.itemView.setAlpha(isUsable ? 1f : 0.5f);
        holder.itemView.setEnabled(isUsable);

        // Change text color based on usability
        int textColor = isUsable ? Color.BLACK : Color.GRAY;
        int descColor = isUsable ? Color.DKGRAY : Color.LTGRAY;

        holder.txtVoucherName.setTextColor(textColor);
        holder.txtVoucherDesc.setTextColor(descColor);

        holder.itemView.setOnClickListener(v -> {
            if (!isUsable) {
                String reason;
                if (cartTotal < voucher.getMinOrderValue()) {
                    reason = "Minimum order value: $" + String.format("%.2f", voucher.getMinOrderValue());
                } else if (voucher.getExpireDate() != null &&
                        System.currentTimeMillis() >= voucher.getExpireDate().toDate().getTime()) {
                    reason = "This voucher has expired";
                } else {
                    reason = "Orders are not eligible to use this voucher";
                }

                Toast.makeText(holder.itemView.getContext(), reason, Toast.LENGTH_SHORT).show();
                return;
            }

            int prevPos = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            // Update UI
            if (prevPos >= 0) {
                notifyItemChanged(prevPos);
            }
            notifyItemChanged(selectedPosition);

            // Notify listener
            listener.onVoucherSelected(voucher);
        });

        holder.radioVoucher.setOnClickListener(v -> holder.itemView.performClick());
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    // Method to get currently selected voucher
    public Voucher getSelectedVoucher() {
        if (selectedPosition >= 0 && selectedPosition < voucherList.size()) {
            return voucherList.get(selectedPosition);
        }
        return null;
    }

    // Method to clear selection
    public void clearSelection() {
        int prevPos = selectedPosition;
        selectedPosition = -1;
        if (prevPos >= 0) {
            notifyItemChanged(prevPos);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtVoucherName, txtVoucherDesc, txtVoucherExpire, txtVoucherMax;
        RadioButton radioVoucher;

        ViewHolder(View v) {
            super(v);
            txtVoucherName = v.findViewById(R.id.txtVoucherName);
            txtVoucherDesc = v.findViewById(R.id.txtVoucherDesc);
            txtVoucherExpire = v.findViewById(R.id.txtVoucherExpire);
            txtVoucherMax = v.findViewById(R.id.txtVoucherMax);
            radioVoucher = v.findViewById(R.id.radioVoucher);
        }
    }
}
package com.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.evocasa.R;
import com.mobile.models.Voucher;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class VoucherProfileAdapter extends RecyclerView.Adapter<VoucherProfileAdapter.ViewHolder> {

    public interface OnVoucherClickListener {
        void onVoucherClicked(Voucher voucher);
    }

    private final List<Voucher> voucherList;
    private final OnVoucherClickListener listener;

    public VoucherProfileAdapter(List<Voucher> voucherList, OnVoucherClickListener listener) {
        this.voucherList = voucherList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voucher_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Voucher voucher = voucherList.get(position);
        holder.bind(voucher);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onVoucherClicked(voucher);
        });
    }

    @Override
    public int getItemCount() {
        return voucherList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCode, txtDesc1, txtDesc2, txtExpire;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCode = itemView.findViewById(R.id.txtVoucherCode);
            txtDesc1 = itemView.findViewById(R.id.txtVoucherDesc1);
            txtDesc2 = itemView.findViewById(R.id.txtVoucherDesc2);
            txtExpire = itemView.findViewById(R.id.txtVoucherExpire);
        }

        void bind(Voucher voucher) {
            txtCode.setText(voucher.getName());
            txtDesc1.setText(String.format(Locale.getDefault(), "%d%% off Capped at $%.0f", (int) voucher.getDiscountPercent(), voucher.getMaxDiscount()));
            txtDesc2.setText(String.format(Locale.getDefault(), "Min. Spend $%.0f", voucher.getMinOrderValue()));

            String expireStr = "";
            if (voucher.getExpireDate() != null) {
                expireStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(voucher.getExpireDate().toDate());
            }
            txtExpire.setText("Expired: " + expireStr);
        }
    }
} 
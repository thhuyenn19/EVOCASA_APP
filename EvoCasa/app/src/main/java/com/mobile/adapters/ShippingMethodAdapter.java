package com.mobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.evocasa.R;
import com.mobile.models.ShippingMethod;

import java.util.List;

public class ShippingMethodAdapter   extends RecyclerView.Adapter<ShippingMethodAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(ShippingMethod method);
    }

    private final List<ShippingMethod> list;
    private final Context ctx;
    private int selected = 0;                // default chọn item 0
    private OnItemClickListener listener;

    public ShippingMethodAdapter(List<ShippingMethod> list, Context ctx) {
        this.list = list;
        this.ctx  = ctx;
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx)
                .inflate(R.layout.item_shipping_method, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ShippingMethod m = list.get(pos);

        // 1) icon riêng
        h.imgIcon.setImageResource(m.getIconRes());
        // 2) text
        h.txtName .setText(m.getName());
        h.txtDesc .setText(m.getReceiveOn());
        h.txtPrice.setText("$" + (int)m.getPrice());

        // 3) highlight background nếu được chọn
        if (pos == selected) {
            h.root.setBackgroundResource(R.drawable.bg_shipping_selected);
        } else {
            h.root.setBackgroundResource(R.drawable.bg_shipping_normal);
        }

        h.root.setOnClickListener(v -> {
            int prev = selected;
            selected = h.getAdapterPosition();
            // refresh 2 vị trí để xóa và đánh dấu lại
            notifyItemChanged(prev);
            notifyItemChanged(selected);
            if (listener != null) listener.onItemClick(m);
        });
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final View root;
        final ImageView imgIcon;
        final TextView txtName, txtDesc, txtPrice;

        VH(@NonNull View v) {
            super(v);
            root     = v.findViewById(R.id.rootShippingItem);
            imgIcon  = v.findViewById(R.id.imgShippingIcon);
            txtName  = v.findViewById(R.id.txtShippingMethodName);
            txtDesc  = v.findViewById(R.id.txtShippingMethodDesc);
            txtPrice = v.findViewById(R.id.txtShippingPrice);
        }
    }
}
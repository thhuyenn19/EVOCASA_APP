package com.mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.models.Collection;
import com.mobile.evocasa.R;
import com.mobile.utils.FontUtils;

import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder> {

    private List<Collection> collectionList;
    private OnItemClickListener listener;

    // Interface for click events
    public interface OnItemClickListener {
        void onItemClick(Collection collection);
    }

    public CollectionAdapter(List<Collection> collectionList) {
        this.collectionList = collectionList;
    }

    // Method to set the click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection, parent, false);
        return new CollectionViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionViewHolder holder, int position) {
        Collection collection = collectionList.get(position);
        holder.imgCollection.setImageResource(collection.getImageResId());
        holder.txtName.setText(collection.getName());

        // ✅ Áp dụng font Zbold cho tên bộ sưu tập
        FontUtils.setZboldFont(holder.itemView.getContext(), holder.txtName);
    }

    @Override
    public int getItemCount() {
        return collectionList.size();
    }

    public class CollectionViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgCollection;
        TextView txtName;

        public CollectionViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            imgCollection = itemView.findViewById(R.id.imgCollection);
            txtName = itemView.findViewById(R.id.txtCollectionName);

            // Set click listener on the entire item view
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(collectionList.get(getAdapterPosition()));
                }
            });
        }
    }
}
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

import java.util.List;

public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder> {

    private List<Collection> collectionList;

    public CollectionAdapter(List<Collection> collectionList) {
        this.collectionList = collectionList;
    }

    @NonNull
    @Override
    public CollectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection, parent, false);
        return new CollectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectionViewHolder holder, int position) {
        Collection collection = collectionList.get(position);
        holder.imgCollection.setImageResource(collection.getImageResId());
        holder.txtName.setText(collection.getName());
    }

    @Override
    public int getItemCount() {
        return collectionList.size();
    }

    public static class CollectionViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imgCollection;
        TextView txtName;

        public CollectionViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCollection = itemView.findViewById(R.id.imgCollection);
            txtName = itemView.findViewById(R.id.txtCollectionName);
        }
    }
}

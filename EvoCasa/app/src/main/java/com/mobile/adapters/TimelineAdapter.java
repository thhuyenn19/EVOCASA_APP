package com.mobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.evocasa.R;
import com.mobile.models.EventItem;
import com.mobile.models.HeaderItem;
import com.mobile.models.TimelineItem;

import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_EVENT  = 1;

    private final Context context;
    private final List<TimelineItem> items;

    public TimelineAdapter(Context context, List<TimelineItem> items) {
        this.context = context;
        this.items   = items;
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof HeaderItem)
                ? TYPE_HEADER
                : TYPE_EVENT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(context);
        if (viewType == TYPE_HEADER) {
            View v = li.inflate(R.layout.item_timeline_header, parent, false);
            return new HeaderVH(v);
        } else {
            View v = li.inflate(R.layout.item_timeline_event, parent, false);
            return new EventVH(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TimelineItem item = items.get(position);
        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).bind((HeaderItem) item);
        } else {
            ((EventVH)  holder).bind((EventItem)  item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    // ViewHolder cho Header (ngày)
    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvDate;
        HeaderVH(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDateHeader);
        }
        void bind(HeaderItem h) {
            tvDate.setText(h.getDate());
        }
    }

    // ViewHolder cho Event (thời gian + mô tả)
    static class EventVH extends RecyclerView.ViewHolder {
        TextView tvTime, tvDesc;
        EventVH(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDesc = itemView.findViewById(R.id.tvDesc);
        }
        void bind(EventItem e) {
            tvTime.setText(e.getTime());
            tvDesc.setText(e.getDesc());
            // Tô màu mô tả nếu là active event
            int colorRes = e.isActive()
                    ? R.color.orange_active    // bạn define trong colors.xml
                    : R.color.gray_inactive;
            tvDesc.setTextColor(ContextCompat.getColor(itemView.getContext(), colorRes));
        }
    }
}
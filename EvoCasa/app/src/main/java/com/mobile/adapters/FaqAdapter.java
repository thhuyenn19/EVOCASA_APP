package com.mobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobile.evocasa.R;
import com.mobile.models.FaqItem;

import java.util.List;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.FaqViewHolder> {

    private List<FaqItem> faqList;
    private Context context;

    public FaqAdapter(Context context, List<FaqItem> faqList) {
        this.context = context;
        this.faqList = faqList;
    }

    @NonNull
    @Override
    public FaqAdapter.FaqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_faq, parent, false);
        return new FaqViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FaqAdapter.FaqViewHolder holder, int position) {
        FaqItem item = faqList.get(position);
        holder.tvQuestion.setText(item.getQuestion());
        holder.tvAnswer.setText(item.getAnswer());

        // Set visibility
        holder.tvAnswer.setVisibility(item.isExpanded() ? View.VISIBLE : View.GONE);
        holder.ivToggle.setImageResource(
                item.isExpanded() ? R.drawable.ic_minus_helpcenter : R.drawable.ic_plus_helpcenter
        );

        // Toggle on click
        holder.questionLayout.setOnClickListener(v -> {
            item.setExpanded(!item.isExpanded());
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return faqList.size();
    }

    static class FaqViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvAnswer;
        ImageView ivToggle;
        LinearLayout questionLayout;

        public FaqViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_question);
            tvAnswer = itemView.findViewById(R.id.tv_answer);
            ivToggle = itemView.findViewById(R.id.iv_toggle);
            questionLayout = itemView.findViewById(R.id.faq_question_layout);
        }
    }
}

package com.example.messmate.owner.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.common.models.MoreOption;

import java.util.List;

public class MoreAdapter extends RecyclerView.Adapter<MoreAdapter.MoreViewHolder> {

    private final List<MoreOption> optionList;
    private OnOptionClickListener listener;

    public interface OnOptionClickListener {
        void onOptionClick(MoreOption option);
    }

    public MoreAdapter(List<MoreOption> optionList) {
        this.optionList = optionList;
    }

    public void setOnOptionClickListener(OnOptionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public MoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_more_option, parent, false);
        return new MoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoreViewHolder holder, int position) {
        MoreOption option = optionList.get(position);

        holder.tvOptionTitle.setText(option.getTitle());
        holder.tvOptionSubtitle.setText(option.getSubtitle());
        holder.imgOptionIcon.setImageResource(option.getIconResId());

        // Highlight Logout item in Red color
        if ("Logout".equalsIgnoreCase(option.getTitle())) {
            holder.tvOptionTitle.setTextColor(Color.parseColor("#D32F2F"));
            holder.imgOptionIcon.setColorFilter(Color.parseColor("#D32F2F"));
        } else {
            holder.tvOptionTitle.setTextColor(Color.parseColor("#1A1A1A"));
            holder.imgOptionIcon.setColorFilter(Color.parseColor("#1B8E32"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOptionClick(option);
            }
        });
    }

    @Override
    public int getItemCount() {
        return optionList != null ? optionList.size() : 0;
    }

    public static class MoreViewHolder extends RecyclerView.ViewHolder {

        ImageView imgOptionIcon;
        TextView tvOptionTitle, tvOptionSubtitle;

        public MoreViewHolder(@NonNull View itemView) {
            super(itemView);
            imgOptionIcon = itemView.findViewById(R.id.imgOptionIcon);
            tvOptionTitle = itemView.findViewById(R.id.tvOptionTitle);
            tvOptionSubtitle = itemView.findViewById(R.id.tvOptionSubtitle);
        }
    }
}

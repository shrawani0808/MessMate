package com.example.messmate.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.models.MessModel;

import java.util.List;

public class MessAdapter extends RecyclerView.Adapter<MessAdapter.MessViewHolder> {

    private List<MessModel> messList;
    private final OnMessClickListener listener;

    public interface OnMessClickListener {
        void onMessClick(MessModel item);
    }

    public MessAdapter(List<MessModel> messList, OnMessClickListener listener) {
        this.messList = messList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mess_card, parent, false);
        return new MessViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessViewHolder holder, int position) {
        MessModel item = messList.get(position);
        holder.tvName.setText(item.getName());
        holder.tvRating.setText("★ " + item.getRating() + " (" + item.getRatingCount() + ")");
        holder.tvType.setText(item.getType());
        holder.tvPrice.setText("₹" + (int) item.getPricePerMonth() + " / month");

        holder.itemView.setOnClickListener(v -> listener.onMessClick(item));
    }

    @Override
    public int getItemCount() {
        return messList.size();
    }

    public void updateList(List<MessModel> newList) {
        this.messList = newList; // or this.messList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    static class MessViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRating, tvType, tvPrice;

        public MessViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMessName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvType = itemView.findViewById(R.id.tvType);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
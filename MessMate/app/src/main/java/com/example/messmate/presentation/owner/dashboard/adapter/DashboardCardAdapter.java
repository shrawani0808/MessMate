package com.example.messmate.presentation.owner.dashboard.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.presentation.owner.dashboard.model.DashboardCard;

import java.util.List;

public class DashboardCardAdapter extends RecyclerView.Adapter<DashboardCardAdapter.ViewHolder> {

    private final List<DashboardCard> list;

    public DashboardCardAdapter(List<DashboardCard> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_card, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        DashboardCard card = list.get(position);

        holder.icon.setImageResource(card.getIcon());
        holder.title.setText(card.getTitle());
        holder.value.setText(card.getValue());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView icon;
        TextView title;
        TextView value;

        ViewHolder(View itemView) {

            super(itemView);

            icon = itemView.findViewById(R.id.imgIcon);
            title = itemView.findViewById(R.id.txtTitle);
            value = itemView.findViewById(R.id.txtValue);

        }
    }
}
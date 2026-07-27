package com.example.messmate.user.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.common.models.OrderItem;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private Context context;
    private List<OrderItem> orderList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(OrderItem order);
    }

    public OrderAdapter(Context context, List<OrderItem> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem order = orderList.get(position);

        holder.tvMessName.setText(order.getMessName());
        holder.tvPlanType.setText(order.getPlanType());
        holder.tvMealType.setText(order.getMealType());
        holder.tvStatus.setText(order.getStatus());
        holder.tvOrderId.setText("ID: #" + order.getOrderId());
        holder.tvTime.setText(order.getTime());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateList(List<OrderItem> newList) {
        this.orderList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessName, tvPlanType, tvMealType, tvStatus, tvOrderId, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessName = itemView.findViewById(R.id.tvOrderMessName);
            tvPlanType = itemView.findViewById(R.id.tvOrderPlanType);
            tvMealType = itemView.findViewById(R.id.tvOrderMealType);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvTime = itemView.findViewById(R.id.tvOrderTime);
        }
    }
}
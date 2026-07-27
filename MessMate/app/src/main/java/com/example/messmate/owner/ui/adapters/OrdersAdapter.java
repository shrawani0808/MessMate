package com.example.messmate.owner.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.common.models.Order;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {

    private final List<Order> orderList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrdersAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    public void setOnOrderClickListener(OnOrderClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflating item_owner_order instead of item_order
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_owner_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvCustomerName.setText(order.getCustomerName() + " (" + order.getOrderId() + ")");
        holder.tvMealType.setText(order.getMealType());
        holder.tvOrderItems.setText(order.getItems());
        holder.tvOrderTime.setText(order.getTime());
        holder.tvOrderAmount.setText(order.getAmount());
        holder.tvOrderStatus.setText(order.getStatus());

        switch (order.getStatus().toLowerCase()) {
            case "completed":
                holder.tvOrderStatus.setTextColor(Color.parseColor("#2E7D32"));
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
                break;
            case "pending":
                holder.tvOrderStatus.setTextColor(Color.parseColor("#E65100"));
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#FFF3E0"));
                break;
            case "cancelled":
                holder.tvOrderStatus.setTextColor(Color.parseColor("#C62828"));
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#FFEBEE"));
                break;
            default:
                holder.tvOrderStatus.setTextColor(Color.parseColor("#757575"));
                holder.tvOrderStatus.setBackgroundColor(Color.parseColor("#F5F5F5"));
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView tvCustomerName, tvOrderStatus, tvMealType, tvOrderItems, tvOrderTime, tvOrderAmount;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvMealType = itemView.findViewById(R.id.tvMealType);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
            tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
            tvOrderAmount = itemView.findViewById(R.id.tvOrderAmount);
        }
    }
}
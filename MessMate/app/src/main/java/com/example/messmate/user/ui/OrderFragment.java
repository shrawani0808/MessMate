package com.example.messmate.user.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.user.ui.adapters.OrderAdapter;
import com.example.messmate.common.database.DatabaseHelper;
import com.example.messmate.common.models.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderFragment extends Fragment {

    private RecyclerView rvOrders;
    private TextView tvEmptyOrders;
    private OrderAdapter orderAdapter;
    private List<OrderItem> orderList = new ArrayList<>();
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_order, container, false);

        // 1. Initialize Layout Views
        rvOrders = view.findViewById(R.id.rvOrders);
        tvEmptyOrders = view.findViewById(R.id.tvEmptyOrders);
        dbHelper = new DatabaseHelper(requireContext());

        // 2. Setup RecyclerView & Adapter
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));

        orderAdapter = new OrderAdapter(requireContext(), orderList, order -> {
            Toast.makeText(requireContext(), "Order #" + order.getOrderId() + " - " + order.getMessName(), Toast.LENGTH_SHORT).show();
        });

        rvOrders.setAdapter(orderAdapter);

        // 3. Load Data from Database
        loadOrders();

        return view;
    }

    private void loadOrders() {
        orderList.clear();

        // Fetch orders from SQLite DB
        List<OrderItem> dbOrders = dbHelper.getAllOrders();

        if (dbOrders != null && !dbOrders.isEmpty()) {
            orderList.addAll(dbOrders);
            tvEmptyOrders.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        } else {
            // Show empty state if no orders exist
            tvEmptyOrders.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        }

        orderAdapter.notifyDataSetChanged();
    }
}
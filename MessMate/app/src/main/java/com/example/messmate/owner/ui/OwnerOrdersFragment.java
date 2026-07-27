package com.example.messmate.owner.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messmate.R;
import com.example.messmate.common.models.Order;
import com.example.messmate.owner.ui.adapters.OrdersAdapter;

import java.util.ArrayList;
import java.util.List;

public class OwnerOrdersFragment extends Fragment {

    private ImageButton btnNotification;
    private RecyclerView rvOrdersList;

    private OrdersAdapter ordersAdapter;
    private List<Order> orderList;

    public OwnerOrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Handle edge-to-edge system insets
        View rootOwnerOrders = view.findViewById(R.id.rootOwnerOrders);
        if (rootOwnerOrders != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootOwnerOrders, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        systemBars.bottom
                );
                return insets;
            });
        }

        // Initialize Views
        btnNotification = view.findViewById(R.id.btnNotification);
        rvOrdersList = view.findViewById(R.id.rvOrdersList);

        // Load dummy orders
        setupOrdersList();

        // Setup RecyclerView
        if (rvOrdersList != null) {
            rvOrdersList.setLayoutManager(new LinearLayoutManager(requireContext()));
            ordersAdapter = new OrdersAdapter(orderList);
            rvOrdersList.setAdapter(ordersAdapter);

            ordersAdapter.setOnOrderClickListener(order ->
                    Toast.makeText(requireContext(), "Order: " + order.getOrderId(), Toast.LENGTH_SHORT).show()
            );
        }

        // Notification button listener
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Notifications", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void setupOrdersList() {
        orderList = new ArrayList<>();
        orderList.add(new Order("#1024", "Rahul Verma", "Lunch • Extra Tiffin", "2x Roti, 1x Paneer Curry, 1x Rice", "12:45 PM • Today", "₹120", "Pending"));
        orderList.add(new Order("#1023", "Priya Kulkarni", "Lunch • Monthly Plan", "Regular Thali + Extra Sweet", "01:10 PM • Today", "₹40", "Completed"));
        orderList.add(new Order("#1022", "Amit Deshmukh", "Breakfast • Guest", "2x Poha, 1x Tea", "09:15 AM • Today", "₹60", "Completed"));
        orderList.add(new Order("#1021", "Sneha Patil", "Dinner • Guest", "1x Special Thali", "08:30 PM • Yesterday", "₹150", "Cancelled"));
    }
}
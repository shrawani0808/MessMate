package com.example.messmate.owner.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.messmate.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class OwnerDashboardFragment extends Fragment {

    private ImageButton btnNotification;
    private FloatingActionButton fabAdd;
    private TextView tvTotalMembers, tvTodaysOrdersCount, tvMonthlyRevenue, tvPendingPayments;

    public OwnerDashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_owner_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Safe WindowInsets handling using the root view ID
        View rootOwnerDashboard = view.findViewById(R.id.rootOwnerDashboard);
//        if (rootOwnerDashboard != null) {
//            ViewCompat.setOnApplyWindowInsetsListener(rootOwnerDashboard, (v, insets) -> {
//                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//                v.setPadding(
//                        systemBars.left,
//                        systemBars.top,
//                        systemBars.right,
//                        systemBars.bottom
//                );
//                return insets;
//            });
//        }

        // Initialize Views using findViewById
        btnNotification = view.findViewById(R.id.btnNotification);
        fabAdd = view.findViewById(R.id.fabAdd);
        tvTotalMembers = view.findViewById(R.id.tvTotalMembers);
        tvTodaysOrdersCount = view.findViewById(R.id.tvTodaysOrdersCount);
        tvMonthlyRevenue = view.findViewById(R.id.tvMonthlyRevenue);
        tvPendingPayments = view.findViewById(R.id.tvPendingPayments);

        // Setup Click Listeners
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Notifications Clicked", Toast.LENGTH_SHORT).show()
            );
        }

        if (fabAdd != null) {
            fabAdd.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Add Item Clicked", Toast.LENGTH_SHORT).show()
            );
        }
    }
}
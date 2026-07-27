package com.example.messmate.owner.ui;

import android.content.Intent;
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
import com.example.messmate.auth.LoginActivity;
import com.example.messmate.common.models.MoreOption;
import com.example.messmate.owner.ui.adapters.MoreAdapter;
import com.example.messmate.common.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class OwnerMoreFragment extends Fragment {

    private ImageButton btnNotification;
    private RecyclerView rvOptionsList;

    private MoreAdapter moreAdapter;
    private List<MoreOption> optionList;

    public OwnerMoreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_owner_more, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // WindowInsets handling
        View rootOwnerMore = view.findViewById(R.id.rootOwnerMore);
        if (rootOwnerMore != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootOwnerMore, (v, insets) -> {
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
        rvOptionsList = view.findViewById(R.id.rvOptionsList);
        SessionManager session = new SessionManager(requireContext());
        // Prepare option items
        setupOptionList();

        // Setup RecyclerView
        if (rvOptionsList != null) {
            rvOptionsList.setLayoutManager(new LinearLayoutManager(requireContext()));
            moreAdapter = new MoreAdapter(optionList);
            rvOptionsList.setAdapter(moreAdapter);

            moreAdapter.setOnOptionClickListener(option -> {
                if ("Logout".equalsIgnoreCase(option.getTitle())) {
                    session.logout();
                    Toast.makeText(requireContext(), "Logged Out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(requireActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(requireContext(), "Selected: " + option.getTitle(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Header click listener
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Notifications", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void setupOptionList() {
        optionList = new ArrayList<>();
        optionList.add(new MoreOption("Mess Details", "Address, contact info, and timing", android.R.drawable.ic_menu_info_details));
        optionList.add(new MoreOption("QR Code & Payment", "Manage UPI QR and payment details", android.R.drawable.ic_menu_camera));
        optionList.add(new MoreOption("Notifications Settings", "Alerts for orders and attendance", android.R.drawable.ic_popup_reminder));
        optionList.add(new MoreOption("Help & Support", "FAQs and contact support team", android.R.drawable.ic_menu_help));
        optionList.add(new MoreOption("Logout", "Sign out of your account", android.R.drawable.ic_lock_power_off));
    }
}
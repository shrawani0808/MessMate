package com.example.messmate.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.messmate.R;

public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvUserEmail, tvUserPhone;
    private TextView btnEditProfile, btnPaymentMethods, btnNotifications, btnHelpSupport, btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Bind Views
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvUserPhone = view.findViewById(R.id.tvUserPhone);

        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnPaymentMethods = view.findViewById(R.id.btnPaymentMethods);
        btnNotifications = view.findViewById(R.id.btnNotifications);
        btnHelpSupport = view.findViewById(R.id.btnHelpSupport);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Click Listeners (Placeholders for future functionality)
        btnEditProfile.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show());

        btnPaymentMethods.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Payment Options clicked", Toast.LENGTH_SHORT).show());

        btnNotifications.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Notifications clicked", Toast.LENGTH_SHORT).show());

        btnHelpSupport.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Help & Support clicked", Toast.LENGTH_SHORT).show());

        btnLogout.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Logging out...", Toast.LENGTH_SHORT).show());

        return view;
    }
}
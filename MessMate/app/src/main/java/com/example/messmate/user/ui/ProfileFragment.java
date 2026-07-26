package com.example.messmate.user.ui;

import android.content.Intent;
import android.database.Cursor;
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
import com.example.messmate.auth.LoginActivity;
import com.example.messmate.common.database.DatabaseHelper;
import com.example.messmate.common.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private TextView tvUserName, tvUserEmail, tvUserPhone, btnLogout;
    private SessionManager session;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        session = new SessionManager(requireContext());
        dbHelper = new DatabaseHelper(requireContext());

        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvUserPhone = view.findViewById(R.id.tvUserPhone);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Load DB data for active user
        loadUserData();

        // Logout execution
        btnLogout.setOnClickListener(v -> {
            session.logout();
            Toast.makeText(requireContext(), "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    private void loadUserData() {
        String email = session.getUserEmail();
        Cursor cursor = dbHelper.getUserByEmail(email);

        if (cursor != null && cursor.moveToFirst()) {
            tvUserName.setText(cursor.getString(0));
            tvUserEmail.setText(cursor.getString(1));
            tvUserPhone.setText(cursor.getString(2));
            cursor.close();
        }
    }
}
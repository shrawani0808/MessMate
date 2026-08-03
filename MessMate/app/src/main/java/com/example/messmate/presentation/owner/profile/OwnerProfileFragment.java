package com.example.messmate.presentation.owner.profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.messmate.R;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.messmate.presentation.auth.LoginActivity;
import com.example.messmate.presentation.auth.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class OwnerProfileFragment extends Fragment {

    private TextView txtProfileInitial;
    private TextView txtProfileName;
    private TextView txtProfileEmail;
    private TextView txtAccountEmail;
    private TextView txtAccountRole;

    private Chip chipRole;

    private MaterialButton btnLogout;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private SessionManager sessionManager;

    public OwnerProfileFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_owner_profile,
                container,
                false
        );

        initializeViews(view);

        initializeFirebase();

        loadProfile();

        btnLogout.setOnClickListener(v -> showLogoutDialog());

        return view;
    }

    private void initializeViews(View view) {

        txtProfileInitial = view.findViewById(R.id.txtProfileInitial);

        txtProfileName = view.findViewById(R.id.txtProfileName);

        txtProfileEmail = view.findViewById(R.id.txtProfileEmail);

        txtAccountEmail = view.findViewById(R.id.txtAccountEmail);

        txtAccountRole = view.findViewById(R.id.txtAccountRole);

        chipRole = view.findViewById(R.id.chipRole);

        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void initializeFirebase() {

        firebaseAuth = FirebaseAuth.getInstance();

        firestore = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(requireContext());
    }

    private void loadProfile() {

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {

            return;
        }

        String email = currentUser.getEmail();

        if (email != null) {

            txtProfileEmail.setText(email);

            txtAccountEmail.setText(email);
        }

        // Get saved role from SharedPreferences
        String role = sessionManager.getRole();

        if (role != null) {

            txtAccountRole.setText(
                    role.substring(0, 1).toUpperCase()
                            + role.substring(1).toLowerCase()
            );

            chipRole.setText(
                    role.toUpperCase()
            );
        }

        // Load name from Firestore
        String uid = currentUser.getUid();

        firestore
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name =
                                documentSnapshot.getString("name");

                        if (name != null && !name.isEmpty()) {

                            txtProfileName.setText(name);

                            // First letter for avatar
                            txtProfileInitial.setText(
                                    name.substring(0, 1).toUpperCase()
                            );
                        }
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            requireContext(),
                            "Unable to load profile",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void showLogoutDialog() {

        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage(
                        "Are you sure you want to logout?"
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Logout",
                        (dialog, which) -> logoutUser()
                )
                .show();
    }

    private void logoutUser() {

        // 1. Clear SharedPreferences
        sessionManager.logout();

        // 2. Sign out Firebase
        firebaseAuth.signOut();

        // 3. Open LoginActivity
        Intent intent = new Intent(
                requireActivity(),
                LoginActivity.class
        );

        // 4. Remove previous activities
        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        // 5. Close current activity
        requireActivity().finish();
    }
}
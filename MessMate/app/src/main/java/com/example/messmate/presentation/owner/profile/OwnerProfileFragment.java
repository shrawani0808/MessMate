package com.example.messmate.presentation.owner.profile;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.messmate.R;
import com.example.messmate.presentation.auth.LoginActivity;
import com.example.messmate.presentation.auth.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class OwnerProfileFragment extends Fragment {

    // ============================================================
    // UI VIEWS
    // ============================================================

    private View editProfileButton;

    private MaterialButton btnResetPassword;
    private MaterialButton btnLogout;

    private android.widget.TextView txtProfileInitial;
    private android.widget.TextView txtProfileName;
    private android.widget.TextView txtProfileEmail;

    private android.widget.TextView txtAccountEmail;
    private android.widget.TextView txtAccountRole;

    private Chip chipRole;


    // ============================================================
    // FIREBASE
    // ============================================================

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private SessionManager sessionManager;


    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public OwnerProfileFragment() {
        // Required empty constructor
    }


    // ============================================================
    // ON CREATE VIEW
    // ============================================================

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_owner_profile, container, false);

        initializeViews(view);

        initializeFirebase();

        loadProfile();

        // Edit profile
        editProfileButton.setOnClickListener(v -> showEditProfileDialog());

        // Reset password
        btnResetPassword.setOnClickListener(v -> showOldPasswordDialog());

        // Logout
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        return view;
    }


    // ============================================================
    // INITIALIZE VIEWS
    // ============================================================

    private void initializeViews(View view) {

        txtProfileInitial = view.findViewById(R.id.txtProfileInitial);

        txtProfileName = view.findViewById(R.id.txtProfileName);

        txtProfileEmail = view.findViewById(R.id.txtProfileEmail);

        txtAccountEmail = view.findViewById(R.id.txtAccountEmail);

        txtAccountRole = view.findViewById(R.id.txtAccountRole);

        chipRole = view.findViewById(R.id.chipRole);

        btnLogout = view.findViewById(R.id.btnLogout);

        // Pencil/edit button
        editProfileButton = view.findViewById(R.id.btnEditProfile);

        // Reset password button
        btnResetPassword = view.findViewById(R.id.btnResetPassword);
    }


    // ============================================================
    // INITIALIZE FIREBASE
    // ============================================================

    private void initializeFirebase() {

        firebaseAuth = FirebaseAuth.getInstance();

        firestore = FirebaseFirestore.getInstance();

        sessionManager = new SessionManager(requireContext());
    }


    // ============================================================
    // LOAD PROFILE
    // ============================================================

    private void loadProfile() {

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            return;
        }


        // --------------------------------------------------------
        // EMAIL
        // --------------------------------------------------------

        String email = currentUser.getEmail();

        if (email != null && !email.isEmpty()) {

            txtProfileEmail.setText(email);

            txtAccountEmail.setText(email);
        }


        // --------------------------------------------------------
        // ROLE
        // --------------------------------------------------------

        String role = sessionManager.getRole();

        if (role != null && !role.isEmpty()) {

            String formattedRole = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();

            txtAccountRole.setText(formattedRole);

            chipRole.setText(role.toUpperCase());
        }


        // --------------------------------------------------------
        // LOAD NAME FROM FIRESTORE
        // --------------------------------------------------------

        String uid = currentUser.getUid();

        firestore.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {

            if (documentSnapshot.exists()) {

                String name = documentSnapshot.getString("name");

                if (name != null && !name.trim().isEmpty()) {

                    name = name.trim();

                    txtProfileName.setText(name);

                    txtProfileInitial.setText(name.substring(0, 1).toUpperCase());
                }
            }
        }).addOnFailureListener(e -> {

            if (isAdded()) {

                Toast.makeText(requireContext(), "Unable to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // ============================================================
    // EDIT PROFILE DIALOG
    // ============================================================

    private void showEditProfileDialog() {

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(requireContext(), "User session expired", Toast.LENGTH_SHORT).show();

            return;
        }


        // --------------------------------------------------------
        // CREATE DIALOG LAYOUT
        // --------------------------------------------------------

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);


        TextInputLayout nameLayout = dialogView.findViewById(R.id.editNameLayout);

        TextInputLayout emailLayout = dialogView.findViewById(R.id.editEmailLayout);

        TextInputEditText etName = dialogView.findViewById(R.id.etEditName);

        TextInputEditText etEmail = dialogView.findViewById(R.id.etEditEmail);


        // --------------------------------------------------------
        // LOAD CURRENT VALUES
        // --------------------------------------------------------

        String currentEmail = currentUser.getEmail();

        if (currentEmail != null) {

            etEmail.setText(currentEmail);
        }

        etName.setText(txtProfileName.getText().toString());


        // --------------------------------------------------------
        // CREATE ALERT DIALOG
        // --------------------------------------------------------

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setTitle("Edit Profile").setView(dialogView).setNegativeButton("Cancel", null).setPositiveButton("Save", null).create();


        dialog.setOnShowListener(dialogInterface -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

                String name = etName.getText().toString().trim();

                String email = etEmail.getText().toString().trim();


                // ------------------------------------------------
                // VALIDATE NAME
                // ------------------------------------------------

                nameLayout.setError(null);
                emailLayout.setError(null);

                if (TextUtils.isEmpty(name)) {

                    nameLayout.setError("Enter your name");

                    etName.requestFocus();

                    return;
                }


                // ------------------------------------------------
                // VALIDATE EMAIL
                // ------------------------------------------------

                if (TextUtils.isEmpty(email)) {

                    emailLayout.setError("Enter your email");

                    etEmail.requestFocus();

                    return;
                }

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                    emailLayout.setError("Enter a valid email");

                    etEmail.requestFocus();

                    return;
                }


                // ------------------------------------------------
                // CHECK WHETHER EMAIL CHANGED
                // ------------------------------------------------

                String oldEmail = currentUser.getEmail();

                boolean emailChanged = oldEmail == null || !oldEmail.equalsIgnoreCase(email);


                // ------------------------------------------------
                // NAME ONLY
                // ------------------------------------------------

                if (!emailChanged) {

                    updateNameOnly(name, dialog);

                    return;
                }


                // ------------------------------------------------
                // EMAIL CHANGED
                // RE-AUTHENTICATION REQUIRED
                // ------------------------------------------------

                showCurrentPasswordForEmailChange(name, email, dialog);
            });
        });
        dialog.show();
        applyWhiteDialogTheme(dialog);

    }


    // ============================================================
    // UPDATE NAME ONLY
    // ============================================================

    private void updateNameOnly(String name, AlertDialog editDialog) {

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String uid = currentUser.getUid();


        Map<String, Object> updates = new HashMap<>();

        updates.put("name", name);


        firestore.collection("users").document(uid).update(updates).addOnSuccessListener(unused -> {

            editDialog.dismiss();

            updateProfileUI(name);

            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {

            Toast.makeText(requireContext(), "Failed to update profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }


    // ============================================================
    // ASK CURRENT PASSWORD BEFORE EMAIL CHANGE
    // ============================================================

    private void showCurrentPasswordForEmailChange(String name, String newEmail, AlertDialog editDialog) {

        View passwordView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_current_password, null);


        TextInputLayout passwordLayout = passwordView.findViewById(R.id.currentPasswordLayout);

        TextInputEditText etPassword = passwordView.findViewById(R.id.etCurrentPassword);


        AlertDialog passwordDialog = new AlertDialog.Builder(requireContext()).setTitle("Verify Your Password").setMessage("Enter your current password to change your email.").setView(passwordView).setNegativeButton("Cancel", null).setPositiveButton("Verify", null).create();


        passwordDialog.setOnShowListener(dialogInterface -> {

            passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

                passwordLayout.setError(null);

                String password = etPassword.getText().toString();


                if (TextUtils.isEmpty(password)) {

                    passwordLayout.setError("Enter your current password");

                    etPassword.requestFocus();

                    return;
                }


                reAuthenticateAndUpdateEmail(password, name, newEmail, editDialog, passwordDialog);
            });
        });
        passwordDialog.show();
        applyWhitePasswordDialogTheme(passwordDialog);
    }


    // ============================================================
    // RE-AUTHENTICATE AND UPDATE EMAIL
    // ============================================================

    private void reAuthenticateAndUpdateEmail(String password, String name, String newEmail, AlertDialog editDialog, AlertDialog passwordDialog) {

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String currentEmail = currentUser.getEmail();

        if (currentEmail == null) {

            Toast.makeText(requireContext(), "Current email not available", Toast.LENGTH_SHORT).show();

            return;
        }


        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, password);


        passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);


        currentUser.reauthenticate(credential).addOnSuccessListener(authResult -> {

            updateEmailAndName(currentUser, name, newEmail, editDialog, passwordDialog);
        }).addOnFailureListener(e -> {

            passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

            Toast.makeText(requireContext(), "Current password is incorrect", Toast.LENGTH_LONG).show();
        });
    }


    // ============================================================
    // UPDATE EMAIL + NAME
    // ============================================================

    private void updateEmailAndName(FirebaseUser currentUser, String name, String newEmail, AlertDialog editDialog, AlertDialog passwordDialog) {

        currentUser.updateEmail(newEmail).addOnSuccessListener(unused -> {

            String uid = currentUser.getUid();


            Map<String, Object> updates = new HashMap<>();

            updates.put("name", name);
            updates.put("email", newEmail);


            firestore.collection("users").document(uid).update(updates).addOnSuccessListener(firestoreUnused -> {

                passwordDialog.dismiss();
                editDialog.dismiss();

                updateProfileUI(name);

                txtProfileEmail.setText(newEmail);

                txtAccountEmail.setText(newEmail);

                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_LONG).show();
            }).addOnFailureListener(e -> {

                Toast.makeText(requireContext(), "Email changed but profile update failed", Toast.LENGTH_LONG).show();
            });
        }).addOnFailureListener(e -> {

            passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

            Toast.makeText(requireContext(), "Unable to change email: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }


    // ============================================================
    // UPDATE PROFILE UI
    // ============================================================

    private void updateProfileUI(String name) {

        txtProfileName.setText(name);

        if (!TextUtils.isEmpty(name)) {

            txtProfileInitial.setText(name.substring(0, 1).toUpperCase());
        }
    }


    // ============================================================
    // RESET PASSWORD - OLD PASSWORD
    // ============================================================

    private void showOldPasswordDialog() {

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    requireContext(),
                    "User session expired",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        View passwordView =
                LayoutInflater.from(requireContext())
                        .inflate(
                                R.layout.dialog_current_password,
                                null
                        );

        TextInputLayout passwordLayout =
                passwordView.findViewById(
                        R.id.currentPasswordLayout
                );

        TextInputEditText etPassword =
                passwordView.findViewById(
                        R.id.etCurrentPassword
                );

        AlertDialog dialog =
                new AlertDialog.Builder(requireContext())
                        .setTitle("Current Password")
                        .setMessage(
                                "Enter your current password to continue."
                        )
                        .setView(passwordView)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Continue",
                                null
                        )
                        .create();

        dialog.setOnShowListener(dialogInterface -> {

            // Apply white dialog appearance
            applyWhitePasswordDialogTheme(dialog);

            // IMPORTANT:
            // Set the Continue button listener here
            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                passwordLayout.setError(null);

                String oldPassword =
                        etPassword.getText()
                                .toString()
                                .trim();

                if (TextUtils.isEmpty(oldPassword)) {

                    passwordLayout.setError(
                            "Enter your current password"
                    );

                    etPassword.requestFocus();

                    return;
                }

                verifyOldPassword(
                        oldPassword,
                        dialog
                );
            });
        });
        dialog.show();
        applyWhitePasswordDialogTheme(dialog);
    }

    // ============================================================
    // VERIFY OLD PASSWORD
    // ============================================================

    private void verifyOldPassword(String oldPassword, AlertDialog oldPasswordDialog) {

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String email = currentUser.getEmail();

        if (email == null) {

            Toast.makeText(requireContext(), "Email authentication unavailable", Toast.LENGTH_SHORT).show();

            return;
        }


        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);


        oldPasswordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);


        currentUser.reauthenticate(credential).addOnSuccessListener(authResult -> {

            oldPasswordDialog.dismiss();

            showNewPasswordDialog();
        }).addOnFailureListener(e -> {

            oldPasswordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

            Toast.makeText(requireContext(), "Current password is incorrect", Toast.LENGTH_LONG).show();
        });
    }


    // ============================================================
    // NEW PASSWORD DIALOG
    // ============================================================

    private void showNewPasswordDialog() {

        View passwordView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_new_password, null);


        TextInputLayout newPasswordLayout = passwordView.findViewById(R.id.newPasswordLayout);

        TextInputLayout confirmPasswordLayout = passwordView.findViewById(R.id.confirmNewPasswordLayout);

        TextInputEditText etNewPassword = passwordView.findViewById(R.id.etNewPassword);

        TextInputEditText etConfirmPassword = passwordView.findViewById(R.id.etConfirmNewPassword);


        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setTitle("Create New Password").setMessage("Enter your new password.").setView(passwordView).setNegativeButton("Cancel", null).setPositiveButton("Update Password", null).create();


        dialog.setOnShowListener(dialogInterface -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

                newPasswordLayout.setError(null);
                confirmPasswordLayout.setError(null);


                String newPassword = etNewPassword.getText().toString();

                String confirmPassword = etConfirmPassword.getText().toString();


                // --------------------------------------------
                // VALIDATE NEW PASSWORD
                // --------------------------------------------

                if (TextUtils.isEmpty(newPassword)) {

                    newPasswordLayout.setError("Enter a new password");

                    etNewPassword.requestFocus();

                    return;
                }


                if (newPassword.length() < 6) {

                    newPasswordLayout.setError("Password must contain at least 6 characters");

                    etNewPassword.requestFocus();

                    return;
                }


                // --------------------------------------------
                // VALIDATE CONFIRM PASSWORD
                // --------------------------------------------

                if (TextUtils.isEmpty(confirmPassword)) {

                    confirmPasswordLayout.setError("Confirm your new password");

                    etConfirmPassword.requestFocus();

                    return;
                }


                if (!newPassword.equals(confirmPassword)) {

                    confirmPasswordLayout.setError("Passwords do not match");

                    etConfirmPassword.requestFocus();

                    return;
                }


                // --------------------------------------------
                // UPDATE PASSWORD
                // --------------------------------------------

                updateFirebasePassword(newPassword, dialog);
            });
        });
        dialog.show();
        applyWhitePasswordDialogTheme(dialog);
    }


    // ============================================================
    // UPDATE FIREBASE PASSWORD
    // ============================================================

    private void updateFirebasePassword(String newPassword, AlertDialog dialog) {

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            return;
        }


        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);


        currentUser.updatePassword(newPassword).addOnSuccessListener(unused -> {

            dialog.dismiss();

            Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_LONG).show();
        }).addOnFailureListener(e -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

            Toast.makeText(requireContext(), "Unable to update password: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }


    // ============================================================
    // LOGOUT DIALOG
    // ============================================================

    private void showLogoutDialog() {

        AlertDialog dialog =
                new AlertDialog.Builder(requireContext())
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton(
                                "Logout",
                                (dialogInterface, which) -> logoutUser()
                        )
                        .create();

        dialog.show();
        applyWhiteDialogTheme(dialog);
    }


    // ============================================================
    // LOGOUT USER
    // ============================================================

    private void logoutUser() {

        // 1. Clear SharedPreferences
        sessionManager.logout();

        // 2. Sign out Firebase
        firebaseAuth.signOut();

        // 3. Open LoginActivity
        Intent intent = new Intent(requireActivity(), LoginActivity.class);

        // 4. Remove previous activities
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);

        // 5. Close current activity
        requireActivity().finish();
    }


    private void applyWhiteDialogTheme(AlertDialog dialog) {

        if (dialog == null) {
            return;
        }

        // =====================================================
        // WHITE DIALOG BACKGROUND
        // =====================================================

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.WHITE)
            );
        }

        // =====================================================
        // TITLE
        // =====================================================

        int titleId = getResources().getIdentifier(
                "alertTitle",
                "id",
                requireContext().getPackageName()
        );

        TextView title = dialog.findViewById(titleId);

        if (title != null) {
            title.setTextColor(Color.BLACK);
        }

        // =====================================================
        // MESSAGE
        // =====================================================

        TextView message = dialog.findViewById(
                android.R.id.message
        );

        if (message != null) {
            message.setTextColor(Color.BLACK);
        }

        // =====================================================
        // EDIT TEXTS
        // =====================================================

        TextInputEditText[] editTexts = {

                dialog.findViewById(R.id.etEditName),
                dialog.findViewById(R.id.etEditEmail),

                dialog.findViewById(R.id.etCurrentPassword),

                dialog.findViewById(R.id.etNewPassword),
                dialog.findViewById(R.id.etConfirmNewPassword)
        };

        for (TextInputEditText editText : editTexts) {

            if (editText != null) {

                editText.setTextColor(Color.BLACK);

                editText.setHintTextColor(
                        Color.rgb(90, 90, 90)
                );
            }
        }

        // =====================================================
        // TEXT INPUT LAYOUTS
        // =====================================================

        TextInputLayout[] layouts = {

                dialog.findViewById(R.id.editNameLayout),
                dialog.findViewById(R.id.editEmailLayout),

                dialog.findViewById(R.id.currentPasswordLayout),

                dialog.findViewById(R.id.newPasswordLayout),
                dialog.findViewById(R.id.confirmNewPasswordLayout)
        };

        for (TextInputLayout layout : layouts) {

            if (layout != null) {

                layout.setDefaultHintTextColor(
                        android.content.res.ColorStateList.valueOf(
                                Color.rgb(70, 70, 70)
                        )
                );

                layout.setErrorTextColor(
                        android.content.res.ColorStateList.valueOf(
                                Color.RED
                        )
                );
            }
        }

        // =====================================================
        // BUTTONS
        // =====================================================

        if (dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
        ) != null) {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setTextColor(
                    Color.rgb(47, 132, 100)
            );
        }

        if (dialog.getButton(
                AlertDialog.BUTTON_NEGATIVE
        ) != null) {

            dialog.getButton(
                    AlertDialog.BUTTON_NEGATIVE
            ).setTextColor(
                    Color.rgb(47, 132, 100)
            );
        }

        if (dialog.getButton(
                AlertDialog.BUTTON_NEUTRAL
        ) != null) {

            dialog.getButton(
                    AlertDialog.BUTTON_NEUTRAL
            ).setTextColor(
                    Color.rgb(47, 132, 100)
            );
        }

        // =====================================================
        // FORCE ALL OTHER TEXT TO BLACK
        // =====================================================

        View dialogView = dialog.getWindow() != null
                ? dialog.getWindow().getDecorView()
                : null;

        if (dialogView != null) {
            setAllTextViewsBlack(dialogView);
        }
    }
    private void applyWhitePasswordDialogTheme(AlertDialog dialog) {

        if (dialog == null) {
            return;
        }

        // =====================================================
        // WHITE BACKGROUND
        // =====================================================

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.WHITE)
            );
        }

        // =====================================================
        // TITLE
        // =====================================================

        int titleId = getResources().getIdentifier(
                "alertTitle",
                "id",
                requireContext().getPackageName()
        );

        TextView title = dialog.findViewById(titleId);

        if (title != null) {
            title.setTextColor(Color.BLACK);
        }

        // =====================================================
        // MESSAGE
        // =====================================================

        TextView message = dialog.findViewById(
                android.R.id.message
        );

        if (message != null) {
            message.setTextColor(Color.BLACK);
        }

        // =====================================================
        // CURRENT PASSWORD
        // =====================================================

        TextInputEditText currentPassword =
                dialog.findViewById(
                        R.id.etCurrentPassword
                );

        if (currentPassword != null) {

            currentPassword.setTextColor(Color.BLACK);

            currentPassword.setHintTextColor(
                    Color.rgb(90, 90, 90)
            );
        }

        // =====================================================
        // NEW PASSWORD
        // =====================================================

        TextInputEditText newPassword =
                dialog.findViewById(
                        R.id.etNewPassword
                );

        if (newPassword != null) {

            newPassword.setTextColor(Color.BLACK);

            newPassword.setHintTextColor(
                    Color.rgb(90, 90, 90)
            );
        }

        // =====================================================
        // CONFIRM PASSWORD
        // =====================================================

        TextInputEditText confirmPassword =
                dialog.findViewById(
                        R.id.etConfirmNewPassword
                );

        if (confirmPassword != null) {

            confirmPassword.setTextColor(Color.BLACK);

            confirmPassword.setHintTextColor(
                    Color.rgb(90, 90, 90)
            );
        }

        // =====================================================
        // INPUT LABELS
        // =====================================================

        TextInputLayout[] layouts = {

                dialog.findViewById(
                        R.id.currentPasswordLayout
                ),

                dialog.findViewById(
                        R.id.newPasswordLayout
                ),

                dialog.findViewById(
                        R.id.confirmNewPasswordLayout
                )
        };

        for (TextInputLayout layout : layouts) {

            if (layout != null) {

                layout.setDefaultHintTextColor(
                        android.content.res.ColorStateList.valueOf(
                                Color.rgb(70, 70, 70)
                        )
                );

                layout.setErrorTextColor(
                        android.content.res.ColorStateList.valueOf(
                                Color.RED
                        )
                );
            }
        }

        // =====================================================
        // BUTTONS
        // =====================================================

        if (dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
        ) != null) {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setTextColor(
                    Color.rgb(47, 132, 100)
            );
        }

        if (dialog.getButton(
                AlertDialog.BUTTON_NEGATIVE
        ) != null) {

            dialog.getButton(
                    AlertDialog.BUTTON_NEGATIVE
            ).setTextColor(
                    Color.rgb(47, 132, 100)
            );
        }

        // =====================================================
        // FORCE TEXT BLACK
        // =====================================================

        View dialogView = dialog.getWindow() != null
                ? dialog.getWindow().getDecorView()
                : null;

        if (dialogView != null) {
            setAllTextViewsBlack(dialogView);
        }
    }
    private void setAllTextViewsBlack(View view) {

        if (view instanceof TextView
                && !(view instanceof android.widget.Button)) {

            TextView textView = (TextView) view;

            textView.setTextColor(Color.BLACK);
        }

        if (view instanceof ViewGroup) {

            ViewGroup group = (ViewGroup) view;

            for (int i = 0; i < group.getChildCount(); i++) {

                setAllTextViewsBlack(
                        group.getChildAt(i)
                );
            }
        }
    }
}
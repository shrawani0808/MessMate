package com.example.messmate.presentation.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.messmate.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmPasswordLayout;

    private RadioGroup radioRole;
    private RadioButton radioMember;
    private RadioButton radioOwner;

    private MaterialButton btnRegister;
    private ImageButton btnBack;
    private TextView txtLogin;
    private ProgressBar progressRegister;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(
                getWindow(),
                false
        );

        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registermain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        radioMember.setChecked(true);

        btnRegister.setOnClickListener(v -> registerUser());

        btnBack.setOnClickListener(v -> finish());

        txtLogin.setOnClickListener(v -> finish());
    }

    private void initializeViews() {

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout =
                findViewById(R.id.confirmPasswordLayout);

        radioRole = findViewById(R.id.radioRole);
        radioMember = findViewById(R.id.radioMember);
        radioOwner = findViewById(R.id.radioOwner);

        btnRegister = findViewById(R.id.btnRegister);
        btnBack = findViewById(R.id.btnBack);

        txtLogin = findViewById(R.id.txtLogin);

        progressRegister =
                findViewById(R.id.progressRegister);
    }

    private void registerUser() {

        clearErrors();

        String name =
                etName.getText().toString().trim();

        String email =
                etEmail.getText().toString().trim();

        String password =
                etPassword.getText().toString().trim();

        String confirmPassword =
                etConfirmPassword.getText().toString().trim();

        // Validate name

        if (TextUtils.isEmpty(name)) {

            nameLayout.setError("Enter your name");
            etName.requestFocus();
            return;
        }

        // Validate email

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

        // Validate password

        if (TextUtils.isEmpty(password)) {

            passwordLayout.setError("Enter a password");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {

            passwordLayout.setError(
                    "Password must contain at least 6 characters"
            );

            etPassword.requestFocus();
            return;
        }

        // Validate confirm password

        if (TextUtils.isEmpty(confirmPassword)) {

            confirmPasswordLayout.setError(
                    "Confirm your password"
            );

            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {

            confirmPasswordLayout.setError(
                    "Passwords do not match"
            );

            etConfirmPassword.requestFocus();
            return;
        }

        // Validate role

        int selectedRoleId =
                radioRole.getCheckedRadioButtonId();

        if (selectedRoleId == -1) {

            Toast.makeText(
                    this,
                    "Please select account type",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String role;

        if (selectedRoleId == R.id.radioOwner) {

            role = "owner";

        } else {

            role = "member";
        }

        setLoading(true);

        final String finalRole = role;
        final String finalName = name;
        final String finalEmail = email;

        // Create Firebase Authentication account

        firebaseAuth
                .createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnSuccessListener(authResult -> {

                    if (authResult.getUser() == null) {

                        setLoading(false);

                        Toast.makeText(
                                this,
                                "Registration failed",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    String uid =
                            authResult.getUser().getUid();

                    // Create Firestore profile

                    Map<String, Object> userData =
                            new HashMap<>();

                    userData.put("name", finalName);
                    userData.put("email", finalEmail);
                    userData.put("role", finalRole);

                    firestore
                            .collection("users")
                            .document(uid)
                            .set(userData)
                            .addOnSuccessListener(unused -> {

                                setLoading(false);

                                Toast.makeText(
                                        RegisterActivity.this,
                                        "Account created successfully",
                                        Toast.LENGTH_LONG
                                ).show();

                                // Sign out after registration
                                firebaseAuth.signOut();

                                // Return to Login
                                Intent intent =
                                        new Intent(
                                                RegisterActivity.this,
                                                LoginActivity.class
                                        );

                                intent.setFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK
                                                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                );

                                startActivity(intent);

                                finish();
                            })
                            .addOnFailureListener(e -> {

                                setLoading(false);

                                Toast.makeText(
                                        RegisterActivity.this,
                                        "Profile creation failed: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            });
                })
                .addOnFailureListener(e -> {

                    setLoading(false);

                    Toast.makeText(
                            RegisterActivity.this,
                            "Registration failed: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void clearErrors() {

        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);
    }

    private void setLoading(boolean loading) {

        if (loading) {

            progressRegister.setVisibility(View.VISIBLE);

            btnRegister.setEnabled(false);

            btnRegister.setText("Creating Account...");

        } else {

            progressRegister.setVisibility(View.GONE);

            btnRegister.setEnabled(true);

            btnRegister.setText("Create Account");
        }
    }
}